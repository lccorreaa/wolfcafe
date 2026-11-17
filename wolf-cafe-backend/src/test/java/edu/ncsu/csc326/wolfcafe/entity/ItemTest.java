package edu.ncsu.csc326.wolfcafe.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Tests the Item entity constructors and Lombok-generated accessors.
 * Made 11/04/2026 with Copilot to improve test coverage for the Item entity class.
 *
 * @author Raina Yowell (rwyowell)
 */
class ItemTest {

	/**
	 * Tests the no-argument constructor and all generated setters.
	 */
	@Test
	void testNoArgumentConstructorAndSetters() {
		Item item = new Item();

		item.setId(7L);
		item.setName("Latte");
		item.setDescription("Steamed milk and espresso");
		item.setPrice(3.50);
		item.setAmount(10);

		assertAll("item properties",
				() -> assertEquals(7L, item.getId()),
				() -> assertEquals("Latte", item.getName()),
				() -> assertEquals("Steamed milk and espresso", item.getDescription()),
				() -> assertEquals(3.50, item.getPrice()),
				() -> assertEquals(10, item.getAmount()));
	}

	/**
	 * Tests the generated all-argument constructor and the constructor that
	 * supplies the default initial amount.
	 */
	@Test
	void testConstructors() {
		Item allArguments = new Item(4L, "Mocha", "Chocolate espresso", 4.25, 5);
		Item defaultAmount = new Item(5L, "Americano", "Espresso and water", 2.75);
		Item withoutId = new Item("Tea", "Black tea", 2.00, 8);

		assertAll("constructor values",
				() -> assertEquals(4L, allArguments.getId()),
				() -> assertEquals(5, allArguments.getAmount()),
				() -> assertEquals(1, defaultAmount.getAmount()),
				() -> assertNull(withoutId.getId()),
				() -> assertEquals("Tea", withoutId.getName()),
				() -> assertEquals(8, withoutId.getAmount()));
	}
}
