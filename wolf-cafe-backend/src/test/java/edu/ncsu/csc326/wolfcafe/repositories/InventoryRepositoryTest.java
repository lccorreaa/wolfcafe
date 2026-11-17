package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;

/**
 * Tests InventoryRepository.  Uses the real database - not an embedded one.
 *
 * @author CSC 326 Course Staff
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class InventoryRepositoryTest {

	/** Reference to inventory repository */
	@Autowired
	private InventoryRepository inventoryRepository;

	/** Reference to EntityManager */
	@Autowired
	private TestEntityManager testEntityManager;

	/** Reference to inventory */
	private Inventory inventory;

	/**
	 * Builds an Ingredient with the given amount.  Only the amount is set so the
	 * test does not depend on how ingredients are named.
	 * @param amount ingredient amount
	 * @return the Ingredient
	 */
	private static Ingredient ingredient(Integer amount) {
		Ingredient ingredient = new Ingredient();
		ingredient.setAmount(amount);
		return ingredient;
	}

	/**
	 * Sets up the test case with an inventory of two ingredients.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		inventoryRepository.deleteAll();

		inventory = new Inventory();
		inventory.addIngredient(ingredient(20));
		inventory.addIngredient(ingredient(14));
		inventoryRepository.save(inventory);

		testEntityManager.flush();
		testEntityManager.clear();
	}

	/**
	 * Test saving the inventory and retrieving it with its ingredients.
	 */
	@Test
	public void testSaveAndGetInventory() {
		Inventory fetched = inventoryRepository.findById(inventory.getId()).orElseThrow();
		assertAll("Inventory contents",
				() -> assertEquals(2, fetched.getIngredients().size()),
				() -> assertEquals(20, fetched.getIngredients().get(0).getAmount()),
				() -> assertEquals(14, fetched.getIngredients().get(1).getAmount()));
	}

	/**
	 * Tests changing an ingredient's amount and adding a new ingredient.
	 */
	@Test
	public void testUpdateInventory() {
		Inventory fetched = inventoryRepository.findById(inventory.getId()).orElseThrow();
		fetched.getIngredients().get(0).setAmount(13);
		fetched.addIngredient(ingredient(7));
		inventoryRepository.save(fetched);

		testEntityManager.flush();
		testEntityManager.clear();

		Inventory updated = inventoryRepository.findById(inventory.getId()).orElseThrow();
		assertAll("Inventory contents",
				() -> assertEquals(3, updated.getIngredients().size()),
				() -> assertEquals(13, updated.getIngredients().get(0).getAmount()),
				() -> assertEquals(14, updated.getIngredients().get(1).getAmount()),
				() -> assertEquals(7, updated.getIngredients().get(2).getAmount()));
	}
}