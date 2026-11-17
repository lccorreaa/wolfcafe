package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.ncsu.csc326.wolfcafe.dto.CreateStaffDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repositories.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repositories.UserRepository;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;
import edu.ncsu.csc326.wolfcafe.services.impl.AuthServiceImpl;

/**
 * Tests service behavior required by UC5 and UC7.
 *
 * @author Lylan Bui (labui)
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    /** User persistence mock */
    @Mock
    private UserRepository userRepository;

    /** Role persistence mock */
    @Mock
    private RoleRepository roleRepository;

    /** Password hashing mock */
    @Mock
    private PasswordEncoder passwordEncoder;

    /** Authentication manager unused by these use cases */
    @Mock
    private AuthenticationManager authenticationManager;

    /** Token provider unused by these use cases */
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    /** Service under test */
    private AuthServiceImpl authService;

    /** Staff role fixture */
    private Role staffRole;

    /** Customer role fixture */
    private Role customerRole;

    /**
     * Initializes the service and roles required by the tests.
     */
    @BeforeEach
    public void setUp() {
        authService = new AuthServiceImpl(userRepository, roleRepository, passwordEncoder,
                authenticationManager, jwtTokenProvider);
        staffRole = new Role(1L, "ROLE_STAFF");
        customerRole = new Role(2L, "ROLE_CUSTOMER");
    }

    /**
     * Creates a user fixture for the supplied account type.
     * @param username account username
     * @param role account role
     * @return test user
     */
    private User user(String username, Role role) {
        User user = new User();
        user.setId(5L);
        user.setName("Test User");
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded-password");
        user.setRoles(Set.of(role));
        return user;
    }

    /**
     * UC5: lists customers and staff without including administrators or passwords.
     */
    @Test
    public void testGetEditableUsersListsCustomersAndStaffOnly() {
        User customer = user("customer", customerRole);
        User staff = user("staff", staffRole);
        User admin = user("admin", new Role(3L, "ROLE_ADMIN"));
        when(userRepository.findAll()).thenReturn(List.of(customer, staff, admin));

        List<UserDto> results = authService.getEditableUsers();

        assertEquals(2, results.size());
        assertEquals(Set.of("customer", "staff"),
                results.stream().map(UserDto::getUsername).collect(java.util.stream.Collectors.toSet()));
        assertFalse(java.util.Arrays.stream(UserDto.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("password")));
    }

    /**
     * UC5: updates editable profile fields without changing username or role.
     */
    @Test
    public void testUpdateUserChangesProfileFields() {
        User customer = user("customer", customerRole);
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(userRepository.existsByEmailAndIdNot("new@example.com", 5L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updated = authService.updateUser(5L,
                new UserDto(null, "New Name", "ignored-username", "new@example.com",
                        "828-888-8282", "ROLE_ADMIN"));

        assertEquals("New Name", customer.getName());
        assertEquals("new@example.com", customer.getEmail());
        assertEquals("828-888-8282", customer.getPhone());
        assertEquals("customer", updated.getUsername());
        assertEquals("ROLE_CUSTOMER", updated.getRole());
    }

    /**
     * UC5 [Invalid Input]: rejects malformed profile fields and duplicate email.
     */
    @Test
    public void testUpdateUserRejectsInvalidInputAndDuplicateEmail() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user("customer", customerRole)));

        assertThrows(WolfCafeAPIException.class, () -> authService.updateUser(5L,
                new UserDto(null, " ", "customer", "invalid-email", "123", null)));

        when(userRepository.existsByEmailAndIdNot("used@example.com", 5L)).thenReturn(true);
        WolfCafeAPIException duplicate = assertThrows(WolfCafeAPIException.class,
                () -> authService.updateUser(5L, new UserDto(null, "Name", null,
                        "used@example.com", "828-888-8282", null)));
        assertEquals(HttpStatus.BAD_REQUEST, duplicate.getStatus());
        assertEquals("Email already exists.", duplicate.getMessage());
    }

    /**
     * UC5 [Cannot Edit]: reports missing and non-editable accounts as not found.
     */
    @Test
    public void testUpdateAndDeleteRejectMissingOrNonEditableUsers() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(userRepository.findById(10L)).thenReturn(Optional.of(user("admin",
                new Role(3L, "ROLE_ADMIN"))));

        assertThrows(ResourceNotFoundException.class, () -> authService.updateUser(99L,
                new UserDto(null, "Name", null, "name@example.com", "828-888-8282", null)));
        assertThrows(ResourceNotFoundException.class, () -> authService.deleteUserById(10L));
    }

    /**
     * UC5 [Delete User]: removes an eligible account and returns its display details.
     */
    @Test
    public void testDeleteUserRemovesStaffOrCustomerAccount() {
        User staff = user("staff", staffRole);
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));

        UserDto deleted = authService.deleteUserById(5L);

        assertEquals("staff", deleted.getUsername());
        assertEquals("ROLE_STAFF", deleted.getRole());
        verify(userRepository).delete(staff);
    }

    /**
     * UC7 main flow: creates staff with a hashed password and staff role.
     */
    @Test
    public void testCreateStaffAssignsRoleAndEncodesPassword() {
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STAFF")).thenReturn(staffRole);
        when(passwordEncoder.encode("JaneDoe8")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        UserDto created = authService.createStaff(new CreateStaffDto("Jane Doe", "janedoe", "JaneDoe8"));

        assertEquals(9L, created.getId());
        assertEquals("ROLE_STAFF", created.getRole());
        assertEquals("janedoe", created.getUsername());
        assertNull(created.getEmail());
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals("bcrypt-hash", saved.getValue().getPassword());
        assertEquals(Set.of(staffRole), saved.getValue().getRoles());
    }

    /**
     * UC7 [Invalid Username] and [Invalid Password]: rejects values outside the rules.
     */
    @Test
    public void testCreateStaffRejectsInvalidUsernameAndPassword() {
        WolfCafeAPIException shortUsername = assertThrows(WolfCafeAPIException.class,
                () -> authService.createStaff(new CreateStaffDto("Jane Doe", "abc", "JaneDoe8")));
        assertEquals(HttpStatus.BAD_REQUEST, shortUsername.getStatus());

        WolfCafeAPIException weakPassword = assertThrows(WolfCafeAPIException.class,
                () -> authService.createStaff(new CreateStaffDto("Jane Doe", "janedoe", "password")));
        assertEquals(HttpStatus.BAD_REQUEST, weakPassword.getStatus());
    }

    /**
     * UC7 [Duplicate]: rejects a username already assigned to an existing account.
     */
    @Test
    public void testCreateStaffRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("janedoe")).thenReturn(true);

        WolfCafeAPIException duplicate = assertThrows(WolfCafeAPIException.class,
                () -> authService.createStaff(new CreateStaffDto("Jane Doe", "janedoe", "JaneDoe8")));

        assertEquals(HttpStatus.BAD_REQUEST, duplicate.getStatus());
        assertEquals("Username already exists.", duplicate.getMessage());
    }
}
