package edu.ncsu.csc326.wolfcafe.services;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;

/**
 * Interface defining the make recipe behaviors.
 *
 * @author CSC 326 Course Staff
 */
public interface MakeRecipeService {

	/**
	 * Returns true if the inventory holds enough of every ingredient in the recipe
	 * to make it.  Nothing is removed from the inventory.
	 *
	 * @param inventoryDto
	 * 			  current inventory
	 * @param recipeDto
	 *            recipe to check
	 * @return true if there are enough ingredients to make the recipe
	 */
	boolean enoughIngredients(InventoryDto inventoryDto, RecipeDto recipeDto);

	/**
     * Removes the ingredients used to make the specified recipe, if there are
     * enough of them.  If there are not, the inventory is left unchanged.
     *
     * @param inventoryDto
     * 			  current inventory
     * @param recipeDto
     *            recipe to make
     * @return true if the recipe was made and its ingredients removed
     */
	boolean makeRecipe(InventoryDto inventoryDto, RecipeDto recipeDto);

}
