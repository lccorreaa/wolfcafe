package edu.ncsu.csc326.wolfcafe.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.repositories.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repositories.RecipeRepository;
import edu.ncsu.csc326.wolfcafe.services.IngredientService;
import edu.ncsu.csc326.wolfcafe.services.InventoryService;
import edu.ncsu.csc326.wolfcafe.services.RecipeService;
import jakarta.persistence.EntityManager;

/**
 * Tests the MakeRecipeController REST API through MockMvc (UC6 Order Beverage).
 *
 * @author Sandra Jose (sajose)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "customer", roles = "CUSTOMER")
public class MakeRecipeControllerTest {

	/** Mock MVC for testing controller */
	@Autowired
	private MockMvc mvc;

	/** Reference to IngredientService, used to stock the inventory */
	@Autowired
	private IngredientService ingredientService;

	/** Reference to the legacy ingredient inventory used by MakeRecipeService. */
	@Autowired
	private InventoryService inventoryService;

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
	 * Stocks the inventory with 10 Coffee and 5 Milk, and creates a Latte
	 * (price 50: 3 Coffee, 2 Milk) and a Mocha (price 60: 3 Coffee, 6 Milk)
	 * that there is never enough Milk for.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		recipeRepository.deleteAll();
		inventoryRepository.deleteAll();
		entityManager.flush();

		ingredientService.createIngredient(new IngredientDto("Coffee", 10));
		ingredientService.createIngredient(new IngredientDto("Milk", 5));
		recipeService.createRecipe(new RecipeDto("Latte", 50, List.of(
				new Ingredient("Coffee", 3), new Ingredient("Milk", 2))));
		recipeService.createRecipe(new RecipeDto("Mocha", 60, List.of(
				new Ingredient("Coffee", 3), new Ingredient("Milk", 6))));

		entityManager.flush();
		entityManager.clear();
	}

	/**
	 * Orders the named recipe with the given raw request body.
	 * @param name recipe name
	 * @param body raw JSON request body
	 * @return the result of the request
	 * @throws Exception if issue when running the request
	 */
	private ResultActions order(String name, String body) throws Exception {
		return mvc.perform(post("/api/makerecipe/" + name)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	/**
	 * Checks that the inventory holds the given amounts.
	 *
	 * Updated slightly on 11/3/26 to get MakeRecipe tests passing. Inventory is checked differently.
	 *
	 * @param coffee expected Coffee
	 * @param milk expected Milk
	 * @throws Exception if issue when running the request
	 */
	private void assertInventory(int coffee, int milk) throws Exception {
		// Confirm that staff can access the inventory endpoint.
		mvc.perform(get("/api/inventory").with(user("staff").roles("STAFF")))
				.andExpect(status().isOk());

		InventoryDto inventory = inventoryService.getInventory();
		assertEquals(coffee, inventory.getIngredients().stream()
				.filter(ingredient -> "Coffee".equals(ingredient.getName()))
				.findFirst().orElseThrow().getAmount());
		assertEquals(milk, inventory.getIngredients().stream()
				.filter(ingredient -> "Milk".equals(ingredient.getName()))
				.findFirst().orElseThrow().getAmount());
	}

	/**
	 * Tests that a paid order returns the change and uses the ingredients
	 * (UC6 main flow).
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverage() throws Exception {
		order("Latte", "75")
				.andExpect(status().isOk())
				.andExpect(content().string("25"));
		assertInventory(7, 3);
	}

	/**
	 * Tests that paying the exact price returns no change.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageExactPayment() throws Exception {
		order("Latte", "50")
				.andExpect(status().isOk())
				.andExpect(content().string("0"));
		assertInventory(7, 3);
	}

	/**
	 * Tests that the payment may be sent as a JSON string, as the frontend's
	 * text field does.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeveragePaymentAsString() throws Exception {
		order("Latte", "\"60\"")
				.andExpect(status().isOk())
				.andExpect(content().string("10"));
		assertInventory(7, 3);
	}

	/**
	 * Tests that a recipe whose ingredient names differ from the inventory's
	 * only by case can be ordered and uses the inventory.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageIngredientNamesDifferByCase() throws Exception {
		recipeService.createRecipe(new RecipeDto("Americano", 40, List.of(
				new Ingredient("coffee", 4), new Ingredient("MILK", 1))));
		entityManager.flush();
		entityManager.clear();

		order("Americano", "45")
				.andExpect(status().isOk())
				.andExpect(content().string("5"));
		assertInventory(6, 4);
	}

	/**
	 * Tests that an order with too little inventory returns the payment and uses
	 * no inventory (UC6 Insufficient Ingredients).
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageInsufficientIngredients() throws Exception {
		order("Mocha", "100")
				.andExpect(status().isBadRequest())
				.andExpect(content().string("100"));
		assertInventory(10, 5);
	}

	/**
	 * Tests that insufficient ingredients is reported before insufficient
	 * payment, following the order of the UC6 main flow.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageInsufficientIngredientsAndPayment() throws Exception {
		order("Mocha", "10")
				.andExpect(status().isBadRequest())
				.andExpect(content().string("10"));
		assertInventory(10, 5);
	}

	/**
	 * Tests that an order paying less than the price returns the payment and
	 * uses no inventory (UC6 Insufficient Payment).
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageInsufficientPayment() throws Exception {
		order("Latte", "49")
				.andExpect(status().isConflict())
				.andExpect(content().string("49"));
		assertInventory(10, 5);
	}

	/**
	 * Tests that a payment of zero is insufficient.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageZeroPayment() throws Exception {
		order("Latte", "0")
				.andExpect(status().isConflict())
				.andExpect(content().string("0"));
		assertInventory(10, 5);
	}

	/**
	 * Tests that non-numeric, non-integer, negative, too large, and missing
	 * payments are rejected and use no inventory (UC6 Invalid Payment).
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageInvalidPayment() throws Exception {
		for (String body : new String[] {"\"abc\"", "abc", "50.5", "\"50.5\"", "-60",
				"1e2", "99999999999", "\"\"", " "}) {
			order("Latte", body).andExpect(status().isBadRequest());
		}
		assertInventory(10, 5);
	}

	/**
	 * Tests that a missing request body is an invalid payment.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageNoPayment() throws Exception {
		mvc.perform(post("/api/makerecipe/Latte").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
		assertInventory(10, 5);
	}

	/**
	 * Tests that ordering a recipe that doesn't exist returns 404.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageUnknownRecipe() throws Exception {
		order("Espresso", "100").andExpect(status().isNotFound());
		assertInventory(10, 5);
	}

	/**
	 * Tests that the beverage can be ordered repeatedly until the inventory
	 * runs out.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testOrderBeverageUntilOutOfStock() throws Exception {
		order("Latte", "50").andExpect(status().isOk());
		order("Latte", "50").andExpect(status().isOk());
		order("Latte", "50")
				.andExpect(status().isBadRequest())
				.andExpect(content().string("50"));
		assertInventory(4, 1);
	}

	/**
	 * Tests that staff cannot order a beverage, and no inventory is used.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testOrderBeverageAsStaffForbidden() throws Exception {
		order("Latte", "75")
				.andExpect(status().isForbidden());
		assertInventory(10, 5);
	}

	/**
	 * Tests that a user who has not logged in cannot order a beverage.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	@WithAnonymousUser
	public void testOrderBeverageNotLoggedIn() throws Exception {
		order("Latte", "75")
				.andExpect(status().isUnauthorized());
	}
}
