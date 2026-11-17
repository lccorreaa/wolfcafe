package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;

/**
 * Converts between Ingredient entities and IngredientDtos.
 *
 * @author Sandra Jose (sajose)
 */
public class IngredientMapper {

    /**
     * Converts an Ingredient entity to an IngredientDto.
     * @param ingredient Ingredient to convert
     * @return IngredientDto object
     */
    public static IngredientDto mapToIngredientDto(Ingredient ingredient) {
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setId(ingredient.getId());
        ingredientDto.setAmount(ingredient.getAmount());
        ingredientDto.setName(ingredient.getName());
        return ingredientDto;
    }

    /**
     * Converts an IngredientDto to an Ingredient entity.
     * @param ingredientDto IngredientDto to convert
     * @return Ingredient entity
     */
    public static Ingredient mapToIngredient(IngredientDto ingredientDto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientDto.getId());
        ingredient.setAmount(ingredientDto.getAmount());
        if (ingredientDto.getName() != null) {
            ingredient.setName(ingredientDto.getName());
        }
        return ingredient;
    }

}
