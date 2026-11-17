package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information to login a user.
 *
 * @author CSC 326 Course Staff
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

	/** Username or email address of user */
    private String usernameOrEmail;
    
    /** User's password */
    private String password;

}
