package edu.ncsu.csc326.wolfcafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.Inventory;

/**
 * InventoryRepository for working with the DB through the 
 * JpaRepository.
 *
 * @author CSC 326 Course Staff
 */
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

}
