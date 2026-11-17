package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Safe user information for the admin user-management API.
 *
 * <p>Credentials are not included. Name, email, and phone are editable;
 * username and role are display-only.</p>
 *
 * @author Lylan Bui (labui)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /** User id */
    private Long id;

    /** User's name */
    @NotBlank(message = "Name is required.")
    private String name;

    /** User's username */
    private String username;

    /** User's email address */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    private String email;

    /** User's phone number */
    @NotBlank(message = "Phone is required.")
    @Pattern(regexp = "^\\d{3}-\\d{3}-\\d{4}$",
            message = "Phone must use the format 000-000-0000.")
    private String phone;

    /** User's role name */
    private String role;
}
