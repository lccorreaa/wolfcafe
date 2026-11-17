package edu.ncsu.csc326.wolfcafe.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.mapper.InventoryMapper;
import edu.ncsu.csc326.wolfcafe.mapper.RecipeMapper;
import edu.ncsu.csc326.wolfcafe.repositories.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.services.MakeRecipeService;

/**
 * Implementation of the MakeRecipeService interface.  Recipes hold their own
 * copies of ingredient rows, so a recipe's ingredients are matched to the
 * inventory's by name (ignoring case and surrounding spaces), the same way
 * RecipeServiceImpl checks them when a recipe is created.
 *
 * @author CSC 326 Course Staff
 */
@Service
public class MakeRecipeServiceImpl implements MakeRecipeService {

	/** Connection to the repository to work with the DAO + database */
	@Autowired
	private InventoryRepository inventoryRepository;

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
	@Override
	public boolean enoughIngredients(InventoryDto inventoryDto, RecipeDto recipeDto) {
		return enoughIngredients(InventoryMapper.mapToInventory(inventoryDto),
				RecipeMapper.mapToRecipe(recipeDto));
	}

	/**
     * Removes the ingredients used to make the specified recipe, if there are
     * enough of them.  Every ingredient is checked before any is removed, so if
     * there are not enough the inventory is left unchanged.
     *
     * @param inventoryDto
     * 			  current inventory
     * @param recipeDto
     *            recipe to make
     * @return true if the recipe was made and its ingredients removed
     */
	@Override
	@Transactional
	public boolean makeRecipe(InventoryDto inventoryDto, RecipeDto recipeDto) {
		Inventory inventory = InventoryMapper.mapToInventory(inventoryDto);
		Recipe recipe = RecipeMapper.mapToRecipe(recipeDto);

		if ( enoughIngredients( inventory, recipe ) ) {
			for (Ingredient needed : recipe.getIngredients()) {
				Ingredient stock = findByName(inventory, needed.getName());
				stock.setAmount(stock.getAmount() - needed.getAmount());
			}

            inventoryRepository.save(inventory);
            return true;
		}

        return false;
	}

    /**
     * Returns true if there are enough ingredients to make the beverage.  An
     * ingredient that is no longer in the inventory counts as not enough.
     *
     * @param inventory
     * 			  coffee maker inventory
     * @param recipe
     *            recipe to check if there are enough ingredients
     * @return true if enough ingredients to make the beverage
     */
    private boolean enoughIngredients (Inventory inventory, Recipe recipe) {
        for (Ingredient needed : recipe.getIngredients()) {
            Ingredient stock = findByName(inventory, needed.getName());
            if (stock == null || stock.getAmount() == null
                    || stock.getAmount() < needed.getAmount()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the inventory ingredient with the given name, ignoring case and
     * surrounding spaces.
     *
     * @param inventory
     * 			  coffee maker inventory
     * @param name
     *            ingredient name to look for
     * @return the matching inventory ingredient, or null if there is none
     */
    private Ingredient findByName (Inventory inventory, String name) {
        if (name == null) {
            return null;
        }
        String target = name.trim();
        for (Ingredient stock : inventory.getIngredients()) {
            if (stock.getName() != null && stock.getName().trim().equalsIgnoreCase(target)) {
                return stock;
            }
        }
        return null;
    }

}
