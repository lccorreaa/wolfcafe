package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;

/**
 * Converts between InventoryDto and Inventory entity.
 *
 * @author CSC 326 Course Staff
 */
public class InventoryMapper {

	/**
	 * Converts an Inventory entity to InventoryDto
	 * @param inventory Inventory to convert
	 * @return InventoryDto object
	 */
	public static InventoryDto mapToInventoryDto(Inventory inventory) {
		InventoryDto dto = new InventoryDto(inventory.getId());
		for (Ingredient ingredient : inventory.getIngredients()) {
			dto.addIngredient(IngredientMapper.mapToIngredientDto(ingredient));
		}
		return dto;
	}
	
	/**
	 * Converts an InventoryDto to an Inventory entity
	 * @param inventoryDto InventoryDto to convert
	 * @return Inventory entity
	 */
	public static Inventory mapToInventory(InventoryDto inventoryDto) {
		Inventory inventory = new Inventory(inventoryDto.getId());
		for (IngredientDto ingredientDto : inventoryDto.getIngredients()) {
			inventory.addIngredient(IngredientMapper.mapToIngredient(ingredientDto));
		}
		return inventory;
	}
}
