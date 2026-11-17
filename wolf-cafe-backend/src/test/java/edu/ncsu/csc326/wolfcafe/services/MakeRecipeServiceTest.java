package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.repositories.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repositories.RecipeRepository;
import jakarta.persistence.EntityManager;

/**
 * Tests MakeRecipeServiceImpl (UC6).
 *
 * @author Sandra Jose (sajose)
 */
@SpringBootTest
@Transactional
public class MakeRecipeServiceTest {

	/** Reference to MakeRecipeService (and MakeRecipeServiceImpl). */
	@Autowired
	private MakeRecipeService makeRecipeService;

	/** Reference to InventoryService, used to read the inventory */
	@Autowired
	private InventoryService inventoryService;

	/** Reference to IngredientService, used to stock the inventory */
	@Autowired
	private IngredientService ingredientService;

	/** Reference to RecipeService, used to create recipes */
	@Autowired
	private RecipeService recipeService;

	/** Reference to InventoryRepository, used to empty the table */
	@Autowired
	private InventoryRepository inventoryRepository;

	/** Reference to RecipeRepository, used to empty the table */
	@Autowired
	private RecipeRepository recipeRepository;

	/** Reference to EntityManager */
	@Autowired
	private EntityManager entityManager;

	/**
	 * Stocks the inventory with 10 Coffee and 5 Milk.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		recipeRepository.deleteAll();
		inventoryRepository.deleteAll();
		entityManager.flush();

		ingredientService.createIngredient(new IngredientDto("Coffee", 10));
		ingredientService.createIngredient(new IngredientDto("Milk", 5));
		entityManager.flush();
		entityManager.clear();
	}

	/**
	 * Creates a recipe needing the given units of Coffee and Milk.
	 * @param coffee units of Coffee
	 * @param milk units of Milk
	 * @return the created recipe
	 */
	private RecipeDto createLatte(int coffee, int milk) {
		RecipeDto recipe = new RecipeDto("Latte", 50, List.of(
				new Ingredient("Coffee", coffee), new Ingredient("Milk", milk)));
		RecipeDto created = recipeService.createRecipe(recipe);
		entityManager.flush();
		entityManager.clear();
		return created;
	}

	/**
	 * Reads the inventory back from the database.
	 * @return the current inventory
	 */
	private InventoryDto freshInventory() {
		entityManager.flush();
		entityManager.clear();
		return inventoryService.getInventory();
	}

	/**
	 * Returns the amount of the named ingredient in the inventory.
	 * @param inventory inventory to search
	 * @param name ingredient name
	 * @return the amount
	 */
	private static int amountOf(InventoryDto inventory, String name) {
		return inventory.getIngredients().stream()
				.filter(i -> name.equals(i.getName()))
				.findFirst().orElseThrow().getAmount();
	}

	/**
	 * Tests that making a recipe removes its ingredients from the inventory
	 * (UC6 main flow).
	 */
	@Test
	public void testMakeRecipe() {
		RecipeDto latte = createLatte(3, 2);

		assertTrue(makeRecipeService.makeRecipe(inventoryService.getInventory(), latte));

		InventoryDto after = freshInventory();
		assertAll("Inventory after making recipe",
				() -> assertEquals(7, amountOf(after, "Coffee")),
				() -> assertEquals(3, amountOf(after, "Milk")));
	}

	/**
	 * Tests that a recipe using exactly the amounts in stock can be made and
	 * empties the inventory.
	 */
	@Test
	public void testMakeRecipeUsesAllInventory() {
		RecipeDto latte = createLatte(10, 5);

		assertTrue(makeRecipeService.makeRecipe(inventoryService.getInventory(), latte));

		InventoryDto after = freshInventory();
		assertAll("Inventory after making recipe",
				() -> assertEquals(0, amountOf(after, "Coffee")),
				() -> assertEquals(0, amountOf(after, "Milk")));
	}

	/**
	 * Tests that a recipe's ingredients are matched to the inventory's by name
	 * ignoring case, the same way RecipeServiceImpl accepts them when the recipe
	 * is created.
	 */
	@Test
	public void testMakeRecipeIngredientNamesDifferByCase() {
		RecipeDto latte = recipeService.createRecipe(new RecipeDto("Latte", 50, List.of(
				new Ingredient("coffee", 3), new Ingredient("MILK", 2))));
		entityManager.flush();
		entityManager.clear();

		assertTrue(makeRecipeService.enoughIngredients(inventoryService.getInventory(), latte));
		assertTrue(makeRecipeService.makeRecipe(inventoryService.getInventory(), latte));

		InventoryDto after = freshInventory();
		assertAll("Inventory after making recipe",
				() -> assertEquals(7, amountOf(after, "Coffee")),
				() -> assertEquals(3, amountOf(after, "Milk")));
	}

	/**
	 * Tests that the recipe can be made repeatedly until the inventory runs out.
	 */
	@Test
	public void testMakeRecipeUntilOutOfStock() {
		RecipeDto latte = createLatte(3, 2);

		assertTrue(makeRecipeService.makeRecipe(inventoryService.getInventory(), latte));
		assertTrue(makeRecipeService.makeRecipe(freshInventory(), latte));
		// Only 1 Milk left
		assertFalse(makeRecipeService.makeRecipe(freshInventory(), latte));

		InventoryDto after = freshInventory();
		assertAll("Inventory after running out",
				() -> assertEquals(4, amountOf(after, "Coffee")),
				() -> assertEquals(1, amountOf(after, "Milk")));
	}

	/**
	 * Tests that no inventory is used when one ingredient is short, even the
	 * ingredients that were in stock (UC6 Insufficient Ingredients).
	 */
	@Test
	public void testMakeRecipeInsufficientIngredients() {
		RecipeDto latte = createLatte(3, 6);

		assertFalse(makeRecipeService.enoughIngredients(inventoryService.getInventory(), latte));
		assertFalse(makeRecipeService.makeRecipe(inventoryService.getInventory(), latte));

		InventoryDto after = freshInventory();
		assertAll("Inventory unchanged",
				() -> assertEquals(10, amountOf(after, "Coffee")),
				() -> assertEquals(5, amountOf(after, "Milk")));
	}

	/**
	 * Tests that a recipe whose ingredient was deleted from the inventory after
	 * the recipe was created cannot be made.
	 */
	@Test
	public void testMakeRecipeIngredientRemovedFromInventory() {
		RecipeDto latte = createLatte(3, 2);
		Long milkId = inventoryService.getInventory().getIngredients().stream()
				.filter(i -> "Milk".equals(i.getName())).findFirst().orElseThrow().getId();
		ingredientService.deleteIngredient(milkId);

		assertFalse(makeRecipeService.makeRecipe(freshInventory(), latte));
		assertEquals(10, amountOf(freshInventory(), "Coffee"));
	}

	/**
	 * Tests that enoughIngredients() does not change the inventory.
	 */
	@Test
	public void testEnoughIngredientsDoesNotUseInventory() {
		RecipeDto latte = createLatte(3, 2);

		assertTrue(makeRecipeService.enoughIngredients(inventoryService.getInventory(), latte));

		InventoryDto after = freshInventory();
		assertAll("Inventory unchanged",
				() -> assertEquals(10, amountOf(after, "Coffee")),
				() -> assertEquals(5, amountOf(after, "Milk")));
	}
}
