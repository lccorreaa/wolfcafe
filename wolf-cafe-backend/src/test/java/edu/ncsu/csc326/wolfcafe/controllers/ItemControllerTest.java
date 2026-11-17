package edu.ncsu.csc326.wolfcafe.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.services.ItemService;
import jakarta.persistence.EntityManager;

/**
 * Tests the ItemController REST API through MockMvc, with the whole application
 * running against the test database.  ItemControllerWebMvcTest tests the same
 * controller with a mocked service; see that class for when to use each.
 *
 * {@code @WithMockUser} runs a test as a logged in user with the given role, without
 * needing a real account or login token.  A test with no @WithMockUser runs as a
 * user who has not logged in.  AuthControllerTest shows a real login token in use.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ItemControllerTest {

	/** API path */
	private static final String API_PATH = "/api/items";

	/** Mock MVC for testing controller */
	@Autowired
	private MockMvc mvc;

	/** Used to create an item for the tests to work with */
	@Autowired
	private ItemService itemService;

	/** Reference to EntityManager */
	@Autowired
	private EntityManager entityManager;

	/** Item created for each test */
	private ItemDto coffee;

	/**
	 * Sets up the test case with a single item.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		entityManager.createQuery("DELETE FROM Item").executeUpdate();
		coffee = itemService.addItem(new ItemDto(null, "Coffee", "Coffee is life", 3.25));
	}

	/**
	 * Tests that staff can create an item.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testAddItemAsStaff() throws Exception {
		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Latte", "A yummy beverage", 3.57)))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Latte"))
				.andExpect(jsonPath("$.description").value("A yummy beverage"))
				.andExpect(jsonPath("$.price").value(3.57));
	}

	/**
	 * Tests that a customer cannot create an item.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testAddItemAsCustomer() throws Exception {
		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Latte", "A yummy beverage", 3.57))))
				.andExpect(status().isForbidden());
	}

	/**
	 * Tests that a user who has not logged in cannot create an item.
	 * @throws Exception if error
	 */
	@Test
	public void testAddItemNotLoggedIn() throws Exception {
		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Latte", "A yummy beverage", 3.57))))
				.andExpect(status().isUnauthorized());
	}

	/**
	 * Tests that creating a second item with an existing name is a conflict.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testAddItemDuplicateName() throws Exception {
		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Coffee", "Again", 1.00))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Item already exists with name Coffee"));
	}

	/**
	 * Tests that a customer can get a single item and the list of items.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testGetItemsAsCustomer() throws Exception {
		mvc.perform(get(API_PATH + "/" + coffee.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Coffee"))
				.andExpect(jsonPath("$.price").value(3.25));

		mvc.perform(get(API_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Coffee"));
	}

	/**
	 * Tests getting an item that does not exist.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testGetItemNotFound() throws Exception {
		mvc.perform(get(API_PATH + "/" + (coffee.getId() + 1)))
				.andExpect(status().isNotFound());
	}

	/**
	 * Tests that a user who has not logged in cannot see the items.
	 * @throws Exception if error
	 */
	@Test
	public void testGetItemsNotLoggedIn() throws Exception {
		mvc.perform(get(API_PATH))
				.andExpect(status().isUnauthorized());
	}

	/**
	 * Tests that an admin can update an item.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testUpdateItemAsAdmin() throws Exception {
		mvc.perform(put(API_PATH + "/" + coffee.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Dark Roast", "Bold", 2.75))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(coffee.getId()))
				.andExpect(jsonPath("$.name").value("Dark Roast"))
				.andExpect(jsonPath("$.price").value(2.75));
	}

	/**
	 * Tests that staff cannot update an item.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testUpdateItemAsStaff() throws Exception {
		mvc.perform(put(API_PATH + "/" + coffee.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Dark Roast", "Bold", 2.75))))
				.andExpect(status().isForbidden());
	}

	/**
	 * Tests that an admin can delete an item, after which it is gone.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testDeleteItemAsAdmin() throws Exception {
		mvc.perform(delete(API_PATH + "/" + coffee.getId()))
				.andExpect(status().isOk());

		mvc.perform(get(API_PATH + "/" + coffee.getId()))
				.andExpect(status().isNotFound());
	}

	/**
	 * Tests that a customer cannot delete an item.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testDeleteItemAsCustomer() throws Exception {
		mvc.perform(delete(API_PATH + "/" + coffee.getId()))
				.andExpect(status().isForbidden());
	}
}
