package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repositories.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

/**
 * Tests RecipeService (and RecipeServiceImpl).
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@Transactional
class RecipeServiceTest {

    /** Reference to RecipeService (and RecipeServiceImpl). */
    @Autowired
    private RecipeService recipeService;

    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;

    /** Coffee recipe */
    private RecipeDto recipe1;
    /** Latte recipe */
    private RecipeDto recipe2;

    /** Coffee ingredient, persisted before use */
    private Ingredient coffeeIngredient;
    /** Milk ingredient, persisted before use */
    private Ingredient milkIngredient;

    /** ingredient service */
    @Autowired
    private IngredientService ingredientService;

    /** inventory repository */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Sets up the test case.
     * @throws java.lang.Exception if error
     */
    @BeforeEach
    public void setUp() throws Exception {
        entityManager.createQuery("DELETE FROM Recipe").executeUpdate();
        entityManager.createQuery("DELETE FROM Ingredient").executeUpdate();

        coffeeIngredient = new Ingredient("Coffee");
        milkIngredient = new Ingredient("Milk");
        entityManager.persist(coffeeIngredient);
        entityManager.persist(milkIngredient);

        inventoryRepository.deleteAll();
        for (String name : new String[] {"Coffee", "Milk", "Chocolate"}) {
            ingredientService.createIngredient(new IngredientDto(name, 50));
        }
    }

    /**
     * Builds a one-entry ingredients list.
     *
     * @param ingredient1 the ingredient
     * @param amount1 its amount
     * @return a list containing that ingredient with the given amount
     */
    private List<Ingredient> ingredientsOf(Ingredient ingredient1, Integer amount1) {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(ingredient1.getName(), amount1));
        return ingredients;
    }

    /**
     * Builds a two-entry ingredients list.
     *
     * @param ingredient1 first ingredient
     * @param amount1 first ingredient's amount
     * @param ingredient2 second ingredient
     * @param amount2 second ingredient's amount
     * @return a list containing both ingredients
     */
    private List<Ingredient> ingredientsOf(Ingredient ingredient1, Integer amount1,
                                           Ingredient ingredient2, Integer amount2) {
        List<Ingredient> ingredients = ingredientsOf(ingredient1, amount1);
        ingredients.add(new Ingredient(ingredient2.getName(), amount2));
        return ingredients;
    }

    /**
     * Finds the amount of the given ingredient (matched by name) within a RecipeDto.
     *
     * @param recipeDto recipe to search
     * @param ingredient ingredient to look for
     * @return the amount recorded for that ingredient in the recipe
     */
    private Integer amountOf(RecipeDto recipeDto, Ingredient ingredient) {
        return recipeDto.getIngredients().stream()
                .filter(i -> i.getName().equals(ingredient.getName()))
                .findFirst()
                .map(Ingredient::getAmount)
                .orElseThrow();
    }

    @Test
    public void testCreateRecipe() {
        RecipeDto recipeDto = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 2, milkIngredient, 1));

        recipeService.createRecipe(recipeDto);

        entityManager.flush();
        entityManager.clear();

        RecipeDto createdRecipe = recipeService.getRecipeByName("Coffee");
        assertAll("Recipe contents",
                () -> assertEquals("Coffee", createdRecipe.getName()),
                () -> assertEquals(50, createdRecipe.getPrice()),
                () -> assertEquals(2, amountOf(createdRecipe, coffeeIngredient)),
                () -> assertEquals(1, amountOf(createdRecipe, milkIngredient)));
    }

    @Test
    void testCreateRecipeInvalidPrice() {
        RecipeDto recipeDto = new RecipeDto("Coffee", 0, ingredientsOf(coffeeIngredient, 2));
        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(recipeDto));
    }

    @Test
    void testCreateRecipeNoIngredients() {
        RecipeDto recipeDto = new RecipeDto("Coffee", 50, new ArrayList<>());
        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(recipeDto));
    }

    @Test
    void testCreateRecipeInvalidUnit() {
        RecipeDto recipeDto = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 0));
        assertThrows(IllegalArgumentException.class, () -> recipeService.createRecipe(recipeDto));
    }

    @Test
    void testIsDuplicateName() {
        recipe1 = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 3));
        recipeService.createRecipe(recipe1);

        entityManager.flush();
        entityManager.clear();

        assertTrue(recipeService.isDuplicateName("Coffee"));
        assertFalse(recipeService.isDuplicateName("Tea"));

        RecipeDto foundByName = recipeService.getRecipeByName("Coffee");
        assertEquals("Coffee", foundByName.getName());

        RecipeDto foundById = recipeService.getRecipeById(foundByName.getId());
        assertAll("Recipe lookup by id and name",
                () -> assertEquals(foundByName.getId(), foundById.getId()),
                () -> assertEquals(foundByName.getName(), foundById.getName()),
                () -> assertEquals(foundByName.getPrice(), foundById.getPrice()));
    }

    @Test
    void testGetAllRecipes() {
        recipe1 = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 3));
        recipe2 = new RecipeDto("Latte", 100, ingredientsOf(coffeeIngredient, 3, milkIngredient, 2));

        recipeService.createRecipe(recipe1);
        recipeService.createRecipe(recipe2);

        RecipeDto created1 = recipeService.getAllRecipes().getFirst();
        RecipeDto created2 = recipeService.getAllRecipes().getLast();
        assertAll("Comparing Recipe 1 and Created1",
                () -> assertEquals(recipe1.getName(), created1.getName()),
                () -> assertEquals(recipe1.getPrice(), created1.getPrice()),
                () -> assertEquals(amountOf(recipe1, coffeeIngredient), amountOf(created1, coffeeIngredient)));

        assertAll("Comparing Recipe 2 and Created2",
                () -> assertEquals(recipe2.getName(), created2.getName()),
                () -> assertEquals(recipe2.getPrice(), created2.getPrice()),
                () -> assertEquals(amountOf(recipe2, coffeeIngredient), amountOf(created2, coffeeIngredient)),
                () -> assertEquals(amountOf(recipe2, milkIngredient), amountOf(created2, milkIngredient)));
    }

    @Test
    void testUpdateRecipe() {
        recipe1 = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 3));
        recipe1 = recipeService.createRecipe(recipe1);

        assertAll("Recipe contents",
                () -> assertEquals("Coffee", recipe1.getName()),
                () -> assertEquals(50, recipe1.getPrice()),
                () -> assertEquals(3, amountOf(recipe1, coffeeIngredient)));

        RecipeDto newRecipe = new RecipeDto("New Name", 25, ingredientsOf(coffeeIngredient, 2, milkIngredient, 1));
        recipe1 = recipeService.updateRecipe(recipe1.getId(), newRecipe);

        entityManager.flush();
        entityManager.clear();

        assertAll("New Recipe contents",
                () -> assertEquals("New Name", recipe1.getName()),
                () -> assertEquals(25, recipe1.getPrice()),
                () -> assertEquals(2, amountOf(recipe1, coffeeIngredient)),
                () -> assertEquals(1, amountOf(recipe1, milkIngredient)));
    }

    @Test
    void testUpdateRecipeInvalidPrice() {
        recipe1 = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 3));
        recipe1 = recipeService.createRecipe(recipe1);

        RecipeDto invalidUpdate = new RecipeDto("Coffee", 0, ingredientsOf(coffeeIngredient, 3));
        Long id = recipe1.getId();
        assertThrows(IllegalArgumentException.class, () -> recipeService.updateRecipe(id, invalidUpdate));
    }

    @Test
    void testUpdateRecipeNotFound() {
        RecipeDto update = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 3));
        assertThrows(ResourceNotFoundException.class, () -> recipeService.updateRecipe(-1L, update));
    }

    @Test
    void testDeleteRecipe() {
        recipe1 = new RecipeDto("Coffee", 50, ingredientsOf(coffeeIngredient, 3));
        RecipeDto coffee = recipeService.createRecipe(recipe1);

        RecipeDto fetchedByName = recipeService.getRecipeByName("Coffee");
        RecipeDto fetchedById = recipeService.getRecipeById(coffee.getId());

        assertAll("Recipe exists before delete",
                () -> assertEquals(coffee.getId(), fetchedByName.getId()),
                () -> assertEquals(coffee.getId(), fetchedById.getId()),
                () -> assertEquals("Coffee", fetchedByName.getName()),
                () -> assertEquals("Coffee", fetchedById.getName()));

        recipeService.deleteRecipe(coffee.getId());

        entityManager.flush();
        entityManager.clear();

        assertThrows(ResourceNotFoundException.class, () -> recipeService.getRecipeById(coffee.getId()));
        assertThrows(ResourceNotFoundException.class, () -> recipeService.getRecipeByName("Coffee"));
        assertFalse(recipeService.isDuplicateName("Coffee"));
    }
}