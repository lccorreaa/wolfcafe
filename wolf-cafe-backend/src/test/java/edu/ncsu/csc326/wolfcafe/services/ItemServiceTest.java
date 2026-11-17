package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import jakarta.persistence.EntityManager;

/**
 * Tests ItemServiceImpl against the test database.
 *
 * The class is @Transactional, so each test runs in a transaction that is rolled
 * back when the test ends and nothing it saves is left behind for the next one.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@Transactional
public class ItemServiceTest {

	/** Reference to ItemService (and ItemServiceImpl) */
	@Autowired
	private ItemService itemService;

	/** Reference to EntityManager */
	@Autowired
	private EntityManager entityManager;

	/** Item name */
	private static final String ITEM_NAME = "Coffee";
	/** Item description */
	private static final String ITEM_DESCRIPTION = "Coffee is life";
	/** Item price */
	private static final double ITEM_PRICE = 3.25;

	/**
	 * Sets up the test case by emptying the items table.  This is a JPQL delete
	 * rather than a SQL TRUNCATE: MySQL commits a TRUNCATE immediately, which would
	 * escape the test's transaction.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		entityManager.createQuery("DELETE FROM Item").executeUpdate();
	}

	/**
	 * Creates an item through the service and pushes it to the database.
	 * @return the created item
	 */
	private ItemDto createCoffee() {
		ItemDto created = itemService.addItem(new ItemDto(null, ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE));
		//Push the insert to the database and empty the persistence context, so a
		//later read has to come back from the database rather than from memory.
		entityManager.flush();
		entityManager.clear();
		return created;
	}

	/**
	 * Tests creating an item.  The item is read back through the service rather than
	 * checked on the object addItem() returned, so the assertions test what was saved.
	 */
	@Test
	public void testAddItem() {
		ItemDto created = createCoffee();

		ItemDto retrieved = itemService.getItem(created.getId());
		assertAll("ItemDto contents",
				() -> assertEquals(ITEM_NAME, retrieved.getName()),
				() -> assertEquals(ITEM_DESCRIPTION, retrieved.getDescription()),
				() -> assertEquals(ITEM_PRICE, retrieved.getPrice()));
	}

	/**
	 * Tests that adding an item ignores any id sent by the client.
	 */
	@Test
	public void testAddItemIgnoresId() {
		ItemDto created = itemService.addItem(new ItemDto(9999L, ITEM_NAME, ITEM_DESCRIPTION, ITEM_PRICE));
		entityManager.flush();

		assertEquals(1, itemService.getAllItems().size());
		assertEquals(ITEM_NAME, itemService.getItem(created.getId()).getName());
	}

	/**
	 * Tests that a second item with the same name is rejected with 409 Conflict.
	 */
	@Test
	public void testAddItemDuplicateName() {
		createCoffee();

		WolfCafeAPIException e = assertThrows(WolfCafeAPIException.class,
				() -> itemService.addItem(new ItemDto(null, ITEM_NAME, "Another", 1.00)));
		assertEquals(HttpStatus.CONFLICT, e.getStatus());
	}

	/**
	 * Tests getting an item that does not exist.
	 */
	@Test
	public void testGetItemNotFound() {
		ItemDto created = createCoffee();

		assertThrows(ResourceNotFoundException.class, () -> itemService.getItem(created.getId() + 1));
	}

	/**
	 * Tests getting all items.
	 */
	@Test
	public void testGetAllItems() {
		assertEquals(0, itemService.getAllItems().size());

		createCoffee();
		itemService.addItem(new ItemDto(null, "Latte", "A yummy beverage", 3.57));
		entityManager.flush();
		entityManager.clear();

		List<ItemDto> items = itemService.getAllItems();
		assertEquals(2, items.size());
		assertEquals(List.of(ITEM_NAME, "Latte"), items.stream().map(ItemDto::getName).sorted().toList());
	}

	/**
	 * Tests updating an item.
	 */
	@Test
	public void testUpdateItem() {
		ItemDto created = createCoffee();

		itemService.updateItem(created.getId(), new ItemDto(null, "Latte", "A yummy beverage", 3.57));
		entityManager.flush();
		entityManager.clear();

		ItemDto updated = itemService.getItem(created.getId());
		assertAll("ItemDto contents",
				() -> assertEquals("Latte", updated.getName()),
				() -> assertEquals("A yummy beverage", updated.getDescription()),
				() -> assertEquals(3.57, updated.getPrice()));
	}

	/**
	 * Tests that an item can be updated without changing its name.
	 */
	@Test
	public void testUpdateItemKeepName() {
		ItemDto created = createCoffee();

		ItemDto updated = itemService.updateItem(created.getId(), new ItemDto(null, ITEM_NAME, "Still life", 4.00));

		assertEquals(4.00, updated.getPrice());
	}

	/**
	 * Tests that an item cannot be renamed to the name of another item.
	 */
	@Test
	public void testUpdateItemDuplicateName() {
		createCoffee();
		ItemDto latte = itemService.addItem(new ItemDto(null, "Latte", "A yummy beverage", 3.57));

		WolfCafeAPIException e = assertThrows(WolfCafeAPIException.class,
				() -> itemService.updateItem(latte.getId(), new ItemDto(null, ITEM_NAME, "", 1.00)));
		assertEquals(HttpStatus.CONFLICT, e.getStatus());
	}

	/**
	 * Tests updating an item that does not exist.
	 */
	@Test
	public void testUpdateItemNotFound() {
		ItemDto created = createCoffee();

		assertThrows(ResourceNotFoundException.class,
				() -> itemService.updateItem(created.getId() + 1, new ItemDto(null, "Latte", "", 3.57)));
	}

	/**
	 * Tests deleting an item.
	 */
	@Test
	public void testDeleteItem() {
		ItemDto created = createCoffee();

		itemService.deleteItem(created.getId());

		assertThrows(ResourceNotFoundException.class, () -> itemService.getItem(created.getId()));
	}

	/**
	 * Tests deleting an item that does not exist.
	 */
	@Test
	public void testDeleteItemNotFound() {
		ItemDto created = createCoffee();

		assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItem(created.getId() + 1));
	}
}
