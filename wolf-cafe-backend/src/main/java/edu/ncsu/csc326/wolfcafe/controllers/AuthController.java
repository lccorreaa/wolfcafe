package edu.ncsu.csc326.wolfcafe.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.dto.CreateStaffDto;
import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.services.AuthService;

import java.util.List;

/**
 * Controller for authentication functionality.
 *
 * @author CSC 326 Course Staff
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

	/** Link to AuthService */
    private AuthService authService;

    /** Records who called each endpoint */
    private AuditLogger auditLogger;

    /**
     * Registers a new customer user with the system.
     * @param registerDto object with registration info
     * @return response indicating success or failure
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto) {
        String response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Logs in the given user.  Bad credentials are answered with 401 Unauthorized.
     * @param loginDto user information for login
     * @return object representing the logged in user
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        JwtAuthResponse jwtAuthResponse;
        try {
            jwtAuthResponse = authService.login(loginDto);
        } catch (AuthenticationException e) {
            auditLogger.log(loginDto.getUsernameOrEmail(), "none", "failed to log in");
            throw e;
        }
        auditLogger.log(loginDto.getUsernameOrEmail(), jwtAuthResponse.getRole(), "logged in");
        return new ResponseEntity<>(jwtAuthResponse, HttpStatus.OK);
    }

    /**
     * Deletes the given user.  Requires the ADMIN role.
     * @param id id of user to delete
     * @return response indicating success or failure
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
        UserDto deletedUser = authService.deleteUserById(id);
        auditLogger.log("deleted " + displayRole(deletedUser) + " user '"
                + deletedUser.getUsername() + "' (id " + id + ")");
    	return ResponseEntity.ok("User deleted successfully.");
    }

    /**
     * Lists staff and customer accounts for administration.
     * @return password-free user details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(authService.getEditableUsers());
    }

    /**
     * Updates the editable profile information of a staff/customer account.
     * @param id account id
     * @param userDto requested name, email, and phone
     * @return updated safe user data
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("id") Long id,
            @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = authService.updateUser(id, userDto);
        auditLogger.log("edited " + displayRole(updatedUser) + " user '" + updatedUser.getUsername()
                + "'. User information now reads Name: '" + updatedUser.getName()
                + "', Email: '" + updatedUser.getEmail() + "', Phone: '" + updatedUser.getPhone() + "'");
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Creates a new staff account.
     * @param createStaffDto new staff information
     * @return created staff data without credentials
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/staff")
    public ResponseEntity<UserDto> createStaff(@Valid @RequestBody CreateStaffDto createStaffDto) {
        UserDto createdUser = authService.createStaff(createStaffDto);
        auditLogger.log("created new staff, Name: '" + createdUser.getName() + "', Username: '"
                + createdUser.getUsername() + "', Id: " + createdUser.getId());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Converts a user role to a readable audit label.
     * @param user user whose role is being formatted
     * @return role name without the {@code ROLE_} prefix
     */
    private String displayRole(UserDto user) {
        return user.getRole().replace("ROLE_", "").toLowerCase();
    }

}
