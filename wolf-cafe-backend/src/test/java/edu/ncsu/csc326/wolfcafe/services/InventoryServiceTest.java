package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.InvalidInventoryException;
import edu.ncsu.csc326.wolfcafe.repositories.ItemRepository;
import jakarta.persistence.EntityManager;

/**
 * Tests InventoryServiceImpl.
 * Updated 11/3/26 to accept 'Items' rather than 'Ingredients' most refactoring was done by AI to fix failing test cases.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@Transactional
public class InventoryServiceTest {

	/** Reference to InventoryService (and InventoryServiceImpl). */
	@Autowired
	private InventoryService inventoryService;

	/** Reference to ItemService, used to create test items */
	@Autowired
	private ItemService itemService;

	/** Reference to ItemRepository, used to empty the table */
	@Autowired
	private ItemRepository itemRepository;

	/** Reference to EntityManager */
	@Autowired
	private EntityManager entityManager;

	/**
	 * Empties the inventory table before each test.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		itemRepository.deleteAll();
		entityManager.flush();
	}

	/**
	 * Builds an ItemDto with the given id and amount.
	 * @param id item id
	 * @param amount item amount
	 * @return the ItemDto
	 */
	private static ItemDto item(Long id, Integer amount) {
		return new ItemDto(id, "Coffee", "Coffee is life", 3.25, amount);
	}

	/**
	 * Creates two items and reads their inventory back from the database.
	 * @param first amount of the first ingredient
	 * @param second amount of the second ingredient
	 * @return the saved inventory
	 */
	private InventoryDto createInventoryWith(int first, int second) {
		itemService.addItem(new ItemDto("Coffee", "Coffee is life", 3.25, first));
		itemService.addItem(new ItemDto("Latte", "A yummy beverage", 3.57, second));

		entityManager.flush();
		entityManager.clear();
		return inventoryService.getItemInventory();
	}

	/**
	 * Tests that getInventory() creates an empty inventory when none exists.
	 */
	@Test
	public void testGetInventoryWhenNoneExists() {
		assertEquals(0, inventoryService.getItemInventory().getItems().size());
	}

	/**
	 * Tests InventoryService.createInventory().
	 */
	@Test
	public void testCreateInventory() {
		InventoryDto created = createInventoryWith(5, 9);
		assertAll("InventoryDto contents",
				() -> assertEquals(2, created.getItems().size()),
				() -> assertEquals(5, created.getItems().get(0).getAmount()),
				() -> assertEquals(9, created.getItems().get(1).getAmount()));
	}

	/**
	 * Tests that updateInventory() adds to the current amounts (UC11 main flow).
	 */
	@Test
	public void testUpdateInventoryAddsAmounts() {
		InventoryDto current = createInventoryWith(5, 9);

		InventoryDto toUpdate = new InventoryDto();
		toUpdate.setItems(java.util.List.of(
				item(current.getItems().get(0).getId(), 10),
				item(current.getItems().get(1).getId(), 9)));
		inventoryService.updateItemInventory(toUpdate);

		entityManager.flush();
		entityManager.clear();

		InventoryDto updated = inventoryService.getItemInventory();
		assertAll("InventoryDto contents",
				() -> assertEquals(15, updated.getItems().get(0).getAmount()),
				() -> assertEquals(18, updated.getItems().get(1).getAmount()));
	}

	/**
	 * Tests that a negative amount is rejected and nothing is added, even for the
	 * valid ingredient in the same request (UC5 Invalid Unit).
	 */
	@Test
	public void testUpdateInventoryNegativeAmount() {
		InventoryDto current = createInventoryWith(5, 9);

		InventoryDto toUpdate = new InventoryDto();
		toUpdate.setItems(java.util.List.of(
				item(current.getItems().get(0).getId(), 10),
				item(current.getItems().get(1).getId(), -1)));
		assertThrows(InvalidInventoryException.class, () -> inventoryService.updateItemInventory(toUpdate));

		entityManager.flush();
		entityManager.clear();

		InventoryDto unchanged = inventoryService.getItemInventory();
		assertAll("InventoryDto contents",
				() -> assertEquals(5, unchanged.getItems().get(0).getAmount()),
				() -> assertEquals(9, unchanged.getItems().get(1).getAmount()));
	}

	/**
	 * Tests that a missing amount is rejected.
	 */
	@Test
	public void testUpdateInventoryMissingAmount() {
		InventoryDto current = createInventoryWith(5, 9);

		InventoryDto toUpdate = new InventoryDto();
		toUpdate.setItems(java.util.List.of(item(current.getItems().get(0).getId(), null)));
		assertThrows(InvalidInventoryException.class, () -> inventoryService.updateItemInventory(toUpdate));
	}

	/**
	 * Tests that an item not in the inventory is rejected.
	 */
	@Test
	public void testUpdateInventoryUnknownIngredient() {
		createInventoryWith(5, 9);

		InventoryDto toUpdate = new InventoryDto();
		toUpdate.setItems(java.util.List.of(item(-1L, 3)));
		assertThrows(Exception.class, () -> inventoryService.updateItemInventory(toUpdate));
	}
}