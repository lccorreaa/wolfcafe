package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;

/**
 * Converts between RecipeDto and Recipe entity
 *
 * @author CSC 326 Course Staff
 */
public class RecipeMapper {

	/**
	 * Converts a Recipe entity to RecipeDto
	 * @param recipe Recipe to convert
	 * @return RecipeDto object
	 */
	public static RecipeDto mapToRecipeDto(Recipe recipe) {
		return new RecipeDto(
				recipe.getId(),
				recipe.getName(),
				recipe.getPrice(),
				recipe.getIngredients()
		);
	}

	/**
	 * Converts a RecipeDto object to a Recipe entity.
	 * @param recipeDto RecipeDto to convert
	 * @return Recipe entity
	 */
	public static Recipe mapToRecipe(RecipeDto recipeDto) {
		return new Recipe(
				recipeDto.getId(),
				recipeDto.getName(),
				recipeDto.getPrice(),
				recipeDto.getIngredients()
		);
	}

}