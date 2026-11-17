package edu.ncsu.csc326.wolfcafe.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.exception.InvalidIngredientException;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.IngredientMapper;
import edu.ncsu.csc326.wolfcafe.repositories.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.services.IngredientService;

/**
 * Implementation of the IngredientService interface.  The inventory is the master
 * list of ingredients: creating an ingredient adds it to the inventory with its
 * initial amount, and every lookup reads from the inventory.  Recipes hold their
 * own copies of ingredient rows, so the ingredients table as a whole is never
 * treated as the list of known ingredients.
 *
 * @author CSC 326 Course Staff
 */
@Service
public class IngredientServiceImpl implements IngredientService {

    /** Connection to the inventory, which owns every ingredient added here */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Returns the single Inventory row, creating an empty one if none exists yet.
     * Inventory is treated as a singleton, so it is located by reading the only
     * row rather than by a hard-coded id.
     * @return the single Inventory
     */
    private Inventory findOrCreateInventory() {
        return inventoryRepository.findAll().stream().findFirst()
                .orElseGet(() -> inventoryRepository.save(new Inventory()));
    }

    /**
     * Creates an ingredient and adds it to the inventory with its initial amount
     * (UC7).  Validation happens before anything is saved, and the method runs in
     * a single transaction, so an invalid ingredient adds nothing to the inventory.
     * @param ingredientDto ingredient to create
     * @return created ingredient
     * @throws InvalidIngredientException if the name is blank or already in the
     *         inventory, or the amount is not a positive integer
     */
    @Override
    @Transactional
    public IngredientDto createIngredient(IngredientDto ingredientDto) {
        Inventory inventory = findOrCreateInventory();
        validateIngredient(ingredientDto, inventory);

        Ingredient ingredient = new Ingredient(ingredientDto.getName().trim(), ingredientDto.getAmount());
        inventory.addIngredient(ingredient);
        Inventory savedInventory = inventoryRepository.saveAndFlush(inventory);

        //save() merges the inventory, so the persisted ingredient is a copy of the one
        //added above.  The new ingredient is the last one in the saved list.
        List<Ingredient> saved = savedInventory.getIngredients();
        return IngredientMapper.mapToIngredientDto(saved.get(saved.size() - 1));
    }

    /**
     * Checks that a new ingredient has a name, a positive amount, and a name that
     * is not already in the inventory (ignoring case and surrounding spaces).
     * @param ingredientDto ingredient to validate
     * @param inventory current inventory to check for duplicates
     * @throws InvalidIngredientException if any check fails
     */
    private void validateIngredient(IngredientDto ingredientDto, Inventory inventory) {
        if (ingredientDto == null || ingredientDto.getName() == null
                || ingredientDto.getName().isBlank()) {
            throw new InvalidIngredientException("Ingredient name is required.");
        }
        if (ingredientDto.getAmount() == null || ingredientDto.getAmount() <= 0) {
            throw new InvalidIngredientException(
                    "Units of each ingredient must be a positive integer.");
        }
        String name = ingredientDto.getName().trim();
        for (Ingredient existing : inventory.getIngredients()) {
            if (existing.getName() != null && existing.getName().equalsIgnoreCase(name)) {
                throw new InvalidIngredientException(
                        "An ingredient with this name already exists.");
            }
        }
    }

    /**
     * Returns the inventory ingredient with the given id
     * @param ingredientId ingredient's id
     * @return the ingredient with the given id
     * @throws ResourceNotFoundException if the inventory has no such ingredient
     */
    @Override
    public IngredientDto getIngredientById(Long ingredientId) {
        Ingredient ingredient = findOrCreateInventory().getIngredientById(ingredientId);
        if (ingredient == null) {
            throw new ResourceNotFoundException("Ingredient does not exist with id " + ingredientId);
        }
        return IngredientMapper.mapToIngredientDto(ingredient);
    }

    /**
     * Returns all the ingredients in the inventory
     * @return all the ingredients in the inventory
     */
    @Override
    public List<IngredientDto> getAllIngredients() {
        return findOrCreateInventory().getIngredients().stream()
                .map((Ingredient ingredient) -> IngredientMapper.mapToIngredientDto(ingredient))
                .collect(Collectors.toList());
    }

    /**
     * Removes the ingredient with the given id from the inventory.  Recipes keep
     * their own copies, so existing recipes are not changed.
     * @param ingredientId ingredient's id
     * @throws ResourceNotFoundException if the inventory has no such ingredient
     */
    @Override
    @Transactional
    public void deleteIngredient(Long ingredientId) {
        Inventory inventory = findOrCreateInventory();
        Ingredient ingredient = inventory.getIngredientById(ingredientId);
        if (ingredient == null) {
            throw new ResourceNotFoundException("Ingredient does not exist with id " + ingredientId);
        }
        //Remove by id rather than with remove(ingredient): Ingredient.equals()
        //compares names, not rows.  orphanRemoval deletes the row itself.
        inventory.getIngredients().removeIf(i -> ingredientId.equals(i.getId()));
        inventoryRepository.save(inventory);
    }

    /**
     * Removes every ingredient from the inventory.  orphanRemoval deletes the rows;
     * recipes' own ingredient rows are left alone.
     */
    @Override
    @Transactional
    public void deleteAllIngredients() {
        Inventory inventory = findOrCreateInventory();
        inventory.getIngredients().clear();
        inventoryRepository.save(inventory);
    }
}