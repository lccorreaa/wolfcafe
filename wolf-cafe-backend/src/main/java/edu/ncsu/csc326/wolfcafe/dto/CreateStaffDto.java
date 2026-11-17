package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request data for creating a staff account.
 *
 * @author Lylan Bui (labui)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffDto {

    /** Staff member's name */
    @NotBlank(message = "Name is required.")
    private String name;

    /** Staff member's username */
    @NotBlank(message = "Username is required.")
    @Size(min = 4, message = "Username must be more than 3 characters.")
    private String username;

    /** Initial password, never returned or written to audit logs */
    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one number.")
    private String password;
}
