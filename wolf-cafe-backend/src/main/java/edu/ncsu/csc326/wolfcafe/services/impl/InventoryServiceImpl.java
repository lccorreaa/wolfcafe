
package edu.ncsu.csc326.wolfcafe.services.impl;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.mapper.ItemMapper;
import edu.ncsu.csc326.wolfcafe.repositories.ItemRepository;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.exception.InvalidInventoryException;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.InventoryMapper;
import edu.ncsu.csc326.wolfcafe.repositories.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.services.InventoryService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the InventoryService interface.
 *
 * @author CSC 326 Course Staff
 */
@Service
public class InventoryServiceImpl implements InventoryService {
	
	/** Connection to the repository to work with the DAO + database */
	@Autowired
	private InventoryRepository inventoryRepository;

	/** Repository for items and their amounts */
	@Autowired
	private ItemRepository itemRepository;

	/**
	 * Creates the inventory.
	 * @param inventoryDto inventory to create
	 * @return updated inventory after creation
	 */
	@Override
	public InventoryDto createInventory(InventoryDto inventoryDto) {
		Inventory inventory = InventoryMapper.mapToInventory(inventoryDto);
		Inventory savedInventory = inventoryRepository.save(inventory);
		return InventoryMapper.mapToInventoryDto(savedInventory);
	}

	/**
	 * Returns the single Inventory row, if one has been created.  Inventory is
	 * treated as a singleton, so it is located by reading the only row rather than
	 * by a hard-coded id.  Assuming a particular id would tie this method's
	 * correctness to the database's AUTO_INCREMENT counter, which keeps climbing
	 * after deletes and is not reset when a transaction rolls back.
	 * @return the single Inventory, or an empty Optional if none exists yet
	 */
	private Optional<Inventory> findSingleInventory() {
		return inventoryRepository.findAll().stream().findFirst();
	}

	/**
	 * Returns the single inventory.
	 * @return the single inventory
	 */
	@Override
	public InventoryDto getInventory() {
		Optional<Inventory> inventory = findSingleInventory();
		if (inventory.isEmpty()) {
			InventoryDto newInventoryDto = new InventoryDto();

			InventoryDto savedInventoryDto = createInventory(newInventoryDto);
			return savedInventoryDto;
		}
		return InventoryMapper.mapToInventoryDto(inventory.get());
	}

	/**
	 * Updates the contents of the inventory.
	 * @param inventoryDto values to update
	 * @return updated inventory
	 * @throws ResourceNotFoundException if no inventory has been created yet
	 */
	@Override
	public InventoryDto updateInventory(InventoryDto inventoryDto) {
		Inventory inventory = findSingleInventory().orElseThrow(
				() -> new ResourceNotFoundException("Inventory does not exist.")
		);

		// pass 1: validate everything
		for (IngredientDto toAdd : inventoryDto.getIngredients()) {
			if (toAdd.getAmount() == null || toAdd.getAmount() < 0) throw new InvalidInventoryException("Units of each ingredient must be a positive integer.");
			if (inventory.getIngredientById(toAdd.getId()) == null) throw new InvalidInventoryException("Ingredient is not in the inventory with id " + toAdd.getId());
		}
		// pass 2: apply
		for (IngredientDto toAdd : inventoryDto.getIngredients()) {
			Ingredient i = inventory.getIngredientById(toAdd.getId());
			i.setAmount(i.getAmount() + toAdd.getAmount());
		}

		Inventory savedInventory = inventoryRepository.save(inventory);
		return InventoryMapper.mapToInventoryDto(savedInventory);
	}

	/**
	 * getter method for getting all the Items and their quantities in the inventory
	 * @return item inventory
	 */
	@Override
	public InventoryDto getItemInventory() {
		InventoryDto inventoryDto = new InventoryDto();
		inventoryDto.setItems(itemRepository.findAll().stream().map(ItemMapper::mapToItemDto).toList());
		return inventoryDto;
	}

	/**
	 * replaces the amounts for the items in the inventory
	 * @param inventoryDto contains the item IDs and amounts to add
	 * @return the updated item inventory
	 */
	@Override
	@Transactional
	public InventoryDto updateItemInventory(InventoryDto inventoryDto) {
		if (inventoryDto == null || inventoryDto.getItems() == null || inventoryDto.getItems().isEmpty()) {
			throw new InvalidInventoryException("At least one item must be in the inventory.");
		}

		Map<Long, Item> itemsToUpdate = new LinkedHashMap<>();
		//validate every entry before changing the amounts
		for (ItemDto requested : inventoryDto.getItems()) {
			if (requested == null || requested.getId() == null) {
				throw new InvalidInventoryException("Each item must have an ID.");
			}
			if (requested.getAmount() == null || requested.getAmount() < 0) {
				throw new InvalidInventoryException("Item amount must be zero or a positive integer.");
			}
			if (itemsToUpdate.containsKey(requested.getId())) {
				throw new InvalidInventoryException("Duplicate item ID not allowed.");
			}
			Item item = itemRepository.findById(requested.getId()).orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + requested.getId()));
			itemsToUpdate.put(item.getId(), item);
		}

		//Add the requested units to each existing item amount.
		for (ItemDto requested : inventoryDto.getItems()) {
			Item item = itemsToUpdate.get(requested.getId());
			if (requested.getAmount() > Integer.MAX_VALUE - item.getAmount()) {
				throw new InvalidInventoryException("Item amount is too large.");
			}
			item.setAmount(item.getAmount() + requested.getAmount());
		}

		itemRepository.saveAll(itemsToUpdate.values());
		return getItemInventory();
	}
}
