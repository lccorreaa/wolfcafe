package edu.ncsu.csc326.wolfcafe.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.RecipeMapper;
import edu.ncsu.csc326.wolfcafe.repositories.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.services.RecipeService;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.services.InventoryService;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;

/**
 * Implementation of the RecipeService interface.
 *
 * @author CSC 326 Course Staff
 */
@Service
public class RecipeServiceImpl implements RecipeService {

	/** Connection to the repository to work with the DAO + database */
	@Autowired
	private RecipeRepository recipeRepository;

	/** Connection to the inventory, the master list of known ingredients */
	@Autowired
	private InventoryService inventoryService;

	/**
	 * Creates a recipe with the given information.
	 * @param recipeDto recipe to create
	 * @return created recipe
	 */
	@Override
	public RecipeDto createRecipe(RecipeDto recipeDto) {
		validateRecipe(recipeDto);
		recipeDto.setIngredients(resolveIngredients(recipeDto.getIngredients()));

		Recipe recipe = RecipeMapper.mapToRecipe(recipeDto);
		Recipe savedRecipe = recipeRepository.save(recipe);
		return RecipeMapper.mapToRecipeDto(savedRecipe);
	}

	/**
	 * Returns the recipe with the given id.
	 * @param recipeId recipe's id
	 * @return the recipe with the given id
	 * @throws ResourceNotFoundException if the recipe doesn't exist
	 */
	@Override
	public RecipeDto getRecipeById(Long recipeId) {
		Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
				() -> new ResourceNotFoundException("Recipe does not exist with id " + recipeId)
		);
		return RecipeMapper.mapToRecipeDto(recipe);
	}

	/**
	 * Returns the recipe with the given name
	 * @param recipeName recipe's name
	 * @return the recipe with the given name.
	 * @throws ResourceNotFoundException if the recipe doesn't exist
	 */
	@Override
	public RecipeDto getRecipeByName(String recipeName) {
		Recipe recipe = recipeRepository.findByName(recipeName).orElseThrow(
				() -> new ResourceNotFoundException("Recipe does not exist with name " + recipeName)
		);
		return RecipeMapper.mapToRecipeDto(recipe);
	}

	/**
	 * Returns true if the recipe already exists in the database.
	 * @param recipeName recipe's name to check
	 * @return true if already in the database
	 */
	@Override
	public boolean isDuplicateName(String recipeName) {
		try {
			getRecipeByName(recipeName);
			return true;
		} catch (ResourceNotFoundException e) {
			return false;
		}
	}

	/**
	 * Returns a list of all the recipes
	 * @return all the recipes
	 */
	@Override
	public List<RecipeDto> getAllRecipes() {
		List<Recipe> recipes = recipeRepository.findAll();
		return recipes.stream().map((recipe) -> RecipeMapper.mapToRecipeDto(recipe)).collect(Collectors.toList());
	}

	/**
	 * Updates the recipe with the given id with the recipe information
	 * @param recipeId id of recipe to update
	 * @param recipeDto values to update
	 * @return updated recipe
	 * @throws ResourceNotFoundException if the recipe doesn't exist
	 */
	@Override
	public RecipeDto updateRecipe(Long recipeId, RecipeDto recipeDto) {
		validateRecipe(recipeDto);

		Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
				() -> new ResourceNotFoundException("Recipe does not exist with id " + recipeId)
		);

		recipe.setName(recipeDto.getName());
		recipe.setPrice(recipeDto.getPrice());
		recipe.setIngredients(resolveIngredients(recipeDto.getIngredients()));

		Recipe savedRecipe = recipeRepository.save(recipe);

		recipe.getIngredients().clear();
		recipe.getIngredients().addAll(resolveIngredients(recipeDto.getIngredients()));

		return RecipeMapper.mapToRecipeDto(savedRecipe);
	}

	/**
	 * Validates a recipe's price and ingredients before it is created or updated.
	 *
	 * @param recipeDto recipe to validate
	 * @throws IllegalArgumentException if the price is invalid, there are no
	 *         ingredients, or any ingredient amount is not a positive integer
	 */
	private void validateRecipe(RecipeDto recipeDto) {
		if (recipeDto.getPrice() == null || recipeDto.getPrice() <= 0) {
			throw new IllegalArgumentException("Recipe price must be a positive integer");
		}

		if (recipeDto.getIngredients() == null || recipeDto.getIngredients().isEmpty()) {
			throw new IllegalArgumentException("Recipe must have at least one ingredient");
		}

		for (Ingredient ingredient : recipeDto.getIngredients()) {
			Integer amount = ingredient.getAmount();
			if (amount == null || amount <= 0) {
				throw new IllegalArgumentException("Ingredient amounts must be positive integers");
			}
		}
	}

	/**
	 * Builds the recipe's own ingredient rows from the incoming list.  Each name must
	 * match an ingredient in the inventory; the recipe gets a new row holding the
	 * units it needs, so the inventory's rows (and their stock amounts) are never
	 * touched.
	 *
	 * @param incoming ingredients from the incoming RecipeDto
	 * @return new, unsaved Ingredient rows for the recipe
	 * @throws IllegalArgumentException if an ingredient is not in the inventory
	 */
	private List<Ingredient> resolveIngredients(List<Ingredient> incoming) {
		Set<String> known = new HashSet<>();
		for (IngredientDto ingredient : inventoryService.getInventory().getIngredients()) {
			known.add(ingredient.getName().trim().toLowerCase());
		}

		List<Ingredient> resolved = new ArrayList<>();
		for (Ingredient ingredient : incoming) {
			String name = ingredient.getName() == null ? "" : ingredient.getName().trim();
			if (!known.contains(name.toLowerCase())) {
				throw new IllegalArgumentException("Ingredient is not in the inventory: " + name);
			}
			resolved.add(new Ingredient(name, ingredient.getAmount()));
		}
		return resolved;
	}

	/**
	 * Deletes the recipe with the given id
	 * @param recipeId recipe's id
	 * @throws ResourceNotFoundException if the recipe doesn't exist
	 */
	@Override
	public void deleteRecipe(Long recipeId) {
		Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
				() -> new ResourceNotFoundException("Recipe does not exist with id " + recipeId)
		);

		recipeRepository.delete(recipe);
	}

}