package edu.ncsu.csc326.wolfcafe.config;

/**
 * Defines user roles for WolfCafe
 *
 * @author CSC 326 Course Staff
 */
public class Roles {
	
	/** Admin role name */
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	
	/**
	 * Defines all roles in the system, EXCEPT for the Admin role.
	 */
	public enum UserRoles {
		
		/** Staff for WolfCafe */
		ROLE_STAFF,
		/** Customer for WolfCafe */
		ROLE_CUSTOMER

	}
	
}
