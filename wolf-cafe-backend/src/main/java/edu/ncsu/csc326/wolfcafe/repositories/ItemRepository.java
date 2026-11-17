package edu.ncsu.csc326.wolfcafe.repositories;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.Item;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for Items.
 *
 * @author CSC 326 Course Staff
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

	/**
	 * Finds the item with the given name.  Spring Data JPA writes the query from
	 * the method name, so there is no implementation to write.
	 * @param name item's name
	 * @return the item, or an empty Optional if no item has that name
	 */
	Optional<Item> findByName(String name);

	/**
	 * AI suggested: locks an item while its amount is being changed
	 * @param id item ID
	 * @return matching item, if present
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT i FROM Item i WHERE i.id = :id")
	Optional<Item> findForUpdate(@Param("id") Long id);


}
