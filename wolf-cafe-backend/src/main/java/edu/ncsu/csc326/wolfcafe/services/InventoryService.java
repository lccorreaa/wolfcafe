package edu.ncsu.csc326.wolfcafe.services;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;

/**
 * Interface defining the inventory behaviors.
 *
 * @author CSC 326 Course Staff
 */
public interface InventoryService {
	
	/**
	 * Creates the inventory.
	 * @param inventoryDto inventory to create
	 * @return updated inventory after creation
	 */
	InventoryDto createInventory(InventoryDto inventoryDto);
	
	/**
	 * Returns the single inventory.
	 * @return the single inventory
	 */
	InventoryDto getInventory();
	
	/**
	 * Updates the contents of the inventory.
	 * @param inventoryDto values to update
	 * @return updated inventory
	 */
	InventoryDto updateInventory(InventoryDto inventoryDto);

	/**
	 * gets the item inventory
	 * @return the InventoryDto containing the item inventory
	 */
	InventoryDto getItemInventory();

	/**
	 * updates the current InventoryDto with a new InventoryDto for Item
	 * @param inventoryDto the amounts to add to the current item quantities
	 * @return the new InventoryDto
	 */
	InventoryDto updateItemInventory(InventoryDto inventoryDto);

}
