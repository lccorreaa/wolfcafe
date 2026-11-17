package edu.ncsu.csc326.wolfcafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.Role;

/**
 * Repository interface for Roles.
 *
 * @author CSC 326 Course Staff
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
	
	/**
	 * Finds the role by name.
	 * @param name role's name
	 * @return Role object
	 */
    Role findByName(String name);
}
