package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information needed to register a new customer.
 *
 * @author CSC 326 Course Staff
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {
	
	/** User's name */
    @NotBlank(message = "Name is required.")
    private String name;
    
    /** User's username */
    @NotBlank(message = "Username is required.")
    private String username;
    
    /** User's email */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    private String email;
    
    /** User's password */
    @NotBlank(message = "Password is required.")
    private String password;
}
