package edu.ncsu.csc326.wolfcafe.services.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ncsu.csc326.wolfcafe.config.Roles;
import edu.ncsu.csc326.wolfcafe.dto.CreateStaffDto;
import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.mapper.UserMapper;
import edu.ncsu.csc326.wolfcafe.repositories.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repositories.UserRepository;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;
import edu.ncsu.csc326.wolfcafe.services.AuthService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implemented AuthService
 *
 * @author CSC 326 Course Staff
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** Email format required when editing an account. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** UC5 phone format. */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{3}-\\d{3}-\\d{4}$");

	/** User repository */
    private UserRepository userRepository;
    /** Role repository */
    private RoleRepository roleRepository;
    /** Password encoder object */
    private PasswordEncoder passwordEncoder;
    /** Authentication manager */
    private AuthenticationManager authenticationManager;
    /** JWT Token provider for working with user tokens */
    private JwtTokenProvider jwtTokenProvider;

    /** Records successful customer account creation */
    private static final Logger REGISTRATION_LOG = LoggerFactory.getLogger("wolfcafe.audit");

    /**
	 * Registers the given user
	 * @param registerDto new user information
	 * @return message for success or failure
	 */
    @Override
    public String register(RegisterDto registerDto) {
        if (registerDto == null || isBlank(registerDto.getName()) || isBlank(registerDto.getUsername()) ||
                isBlank(registerDto.getEmail()) || isBlank(registerDto.getPassword())) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Name, username, email, and password are required.");
        }

        String name = registerDto.getName().trim();
        String username = registerDto.getUsername().trim();
        String email = registerDto.getEmail().trim();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "A valid email is required.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        Role customerRole = roleRepository.findByName(
                Roles.UserRoles.ROLE_CUSTOMER.name()
        );

        if (customerRole == null) {
            throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Customer registration is currently unavailable.");
        }

        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        REGISTRATION_LOG.info("user={} role={} action=registered accountId={}",
                savedUser.getUsername(), Roles.UserRoles.ROLE_CUSTOMER.name(), savedUser.getId());

        return "User registered successfully.";
    }

    /**
     * Logs in the given user
     * @param loginDto username/email and password
     * @return response with authenticated user
     */
    @Override
    public JwtAuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(),
                loginDto.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        Optional<User> userOptional = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail());

        String role = null;
        if (userOptional.isPresent()) {
            User loggedInUser = userOptional.get();
            Optional<Role> optionalRole = loggedInUser.getRoles().stream().findFirst();

            if (optionalRole.isPresent()) {
                Role userRole = optionalRole.get();
                role = userRole.getName();
            }
        }

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setRole(role);
        jwtAuthResponse.setAccessToken(token);

        return jwtAuthResponse;
    }

    /**
     * Deletes the given user by id
     * @param id id of user to delete
     * @return safe information about the deleted account
     */
    @Override
    @Transactional
    public UserDto deleteUserById(Long id) {
        User user = findEditableUser(id);
        UserDto deletedUser = UserMapper.mapToUserDto(user);
        userRepository.delete(user);
        return deletedUser;
    }

    /**
     * Lists only staff and customer accounts.
     * @return password-free staff/customer information
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getEditableUsers() {
        return userRepository.findAll().stream()
                .filter(this::isEditableUser)
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates the editable profile fields of an existing staff/customer account.
     * @param id account id
     * @param userDto requested profile values
     * @return updated user data
     */
    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = findEditableUser(id);
        if (userDto == null || isBlank(userDto.getName())
                || isBlank(userDto.getEmail()) || !EMAIL_PATTERN.matcher(userDto.getEmail()).matches()
                || isBlank(userDto.getPhone()) || !PHONE_PATTERN.matcher(userDto.getPhone()).matches()) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST,
                    "Name, valid email, and phone in 000-000-0000 format are required.");
        }
        if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    /**
     * Creates a staff account with the required username/password rules.
     * @param createStaffDto name, username, and initial password
     * @return safe information about the created staff member
     */
    @Override
    @Transactional
    public UserDto createStaff(CreateStaffDto createStaffDto) {
        if (createStaffDto == null || isBlank(createStaffDto.getName())
                || isBlank(createStaffDto.getUsername()) || createStaffDto.getUsername().length() < 4
                || isBlank(createStaffDto.getPassword()) || createStaffDto.getPassword().length() < 8
                || !createStaffDto.getPassword().matches(".*\\d.*")) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Staff information is invalid.");
        }
        if (userRepository.existsByUsername(createStaffDto.getUsername())) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }
        Role staffRole = roleRepository.findByName(Roles.UserRoles.ROLE_STAFF.name());
        if (staffRole == null) {
            throw new ResourceNotFoundException("Staff role not found.");
        }

        User user = new User();
        user.setName(createStaffDto.getName());
        user.setUsername(createStaffDto.getUsername());
        user.setPassword(passwordEncoder.encode(createStaffDto.getPassword()));
        user.setRoles(Set.of(staffRole));
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    /**
     * Finds an existing staff/customer account.
     * @param id account id
     * @return eligible account
     */
    private User findEditableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        if (!isEditableUser(user)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        return user;
    }

    /**
     * Checks whether a user has a staff or customer role.
     * @param user account to inspect
     * @return true for staff/customer accounts
     */
    private boolean isEditableUser(User user) {
        return user.getRoles().stream().map(Role::getName)
                .anyMatch(role -> Objects.equals(role, Roles.UserRoles.ROLE_STAFF.name())
                        || Objects.equals(role, Roles.UserRoles.ROLE_CUSTOMER.name()));
    }

    /**
     * Checks for null, empty, or whitespace-only text.
     * @param value text to check
     * @return true if the text is blank
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
