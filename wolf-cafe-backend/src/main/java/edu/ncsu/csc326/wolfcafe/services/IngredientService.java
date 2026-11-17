package edu.ncsu.csc326.wolfcafe.services;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;

import java.util.List;

/**
 * Interface defining the ingredient behaviors.
 *
 * @author Sandra Jose (sajose)
 */
public interface IngredientService {

    /**
     * Creates the ingredient and adds it to the inventory.
     * @param ingredientDto ingredient to create
     * @return the created ingredient
     */
    IngredientDto createIngredient(IngredientDto ingredientDto);

    /**
     * Returns the ingredient with the given id.
     * @param ingredientId ingredient id
     * @return the ingredient with the given id
     */
    IngredientDto getIngredientById(Long ingredientId);

    /**
     * Returns all ingredients.
     * @return all ingredients
     */
    List<IngredientDto> getAllIngredients();

    /**
     * Deletes the ingredient with the given id.
     * @param ingredientId ingredient id
     */
    void deleteIngredient(Long ingredientId);

    /**
     * Deletes all ingredients.
     */
    void deleteAllIngredients();

}
