package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import edu.ncsu.csc326.wolfcafe.entity.Item;

/**
 * Tests ItemRepository.  Uses the real test database - not an embedded one.
 * {@code @DataJpaTest} starts only the JPA layer and rolls back each test's changes.
 *
 * @author CSC 326 Course Staff
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ItemRepositoryTest {

	/** Reference to item repository */
	@Autowired
	private ItemRepository itemRepository;

	/** Reference to EntityManager */
	@Autowired
	private TestEntityManager testEntityManager;

	/**
	 * Sets up the test case with two items.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		itemRepository.deleteAll();

		itemRepository.save(new Item(null, "Coffee", "Coffee is life", 3.25));
		itemRepository.save(new Item(null, "Latte", "A yummy beverage", 3.57));

		//Push the inserts to the database and empty the persistence context.  Without
		//this, a test below could be handed the same in-memory object that save()
		//just returned and would pass even if nothing reached the database.
		testEntityManager.flush();
		testEntityManager.clear();
	}

	/**
	 * Tests ItemRepository.findByName() for an item that exists.
	 */
	@Test
	public void testFindByName() {
		Item fetched = itemRepository.findByName("Latte").orElseThrow();

		assertAll("Item contents",
				() -> assertEquals("Latte", fetched.getName()),
				() -> assertEquals("A yummy beverage", fetched.getDescription()),
				() -> assertEquals(3.57, fetched.getPrice()));
	}

	/**
	 * Tests ItemRepository.findByName() for an item that does not exist.
	 */
	@Test
	public void testFindByNameNotFound() {
		assertTrue(itemRepository.findByName("Mocha").isEmpty());
	}

	/**
	 * Tests retrieving every item from the repository.
	 */
	@Test
	public void testFindAll() {
		assertEquals(2, itemRepository.findAll().size());
	}
}
