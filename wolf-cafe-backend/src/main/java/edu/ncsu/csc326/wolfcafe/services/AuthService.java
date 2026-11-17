package edu.ncsu.csc326.wolfcafe.services;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.CreateStaffDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;

import java.util.List;

/**
 * Authorization service
 *
 * @author CSC 326 Course Staff
 */
public interface AuthService {
	/**
	 * Registers the given user
	 * @param registerDto new user information
	 * @return message for success or failure
	 */
    String register(RegisterDto registerDto);

    /**
     * Logs in the given user
     * @param loginDto username/email and password
     * @return response with authenticated user
     */
    JwtAuthResponse login(LoginDto loginDto);
    
    /**
     * Deletes the given user by id
     * @param id id of user to delete
     * @return safe information about the deleted account
     */
    UserDto deleteUserById(Long id);

    /**
     * Returns staff and customer accounts available for administration.
     * @return password-free user information
     */
    List<UserDto> getEditableUsers();

    /**
     * Updates the editable profile fields of a staff or customer account.
     * @param id account id
     * @param userDto requested name, email, and phone
     * @return updated safe user information
     */
    UserDto updateUser(Long id, UserDto userDto);

    /**
     * Creates a staff account with an encoded password.
     * @param createStaffDto new staff information
     * @return safe information about the created staff account
     */
    UserDto createStaff(CreateStaffDto createStaffDto);
}
