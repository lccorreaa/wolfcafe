package edu.ncsu.csc326.wolfcafe.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.repositories.ItemRepository;
import edu.ncsu.csc326.wolfcafe.services.ItemService;
import jakarta.persistence.EntityManager;

/**
 * Tests the InventoryController REST API through MockMvc.
 * Updated 11/3/26 to accept 'Items' rather than 'Ingredients' most refactoring was done by AI to fix failing test cases.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "staff", roles = "STAFF")
public class InventoryControllerTest {

	/** Mock MVC for testing controller */
	@Autowired
	private MockMvc mvc;

	/** Reference to ItemService, used to create test items */
	@Autowired
	private ItemService itemService;

	/** Reference to ItemRepository, used to empty the table */
	@Autowired
	private ItemRepository itemRepository;

	/** Reference to EntityManager */
	@Autowired
	private EntityManager entityManager;

	/** Id of the single item in the inventory */
	private Long itemId;

	/**
	 * Sets up one item with 5 units.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		itemRepository.deleteAll();
		itemId = itemService.addItem(new ItemDto("Coffee", "Coffee is life", 3.25, 5)).getId();

		entityManager.flush();
		entityManager.clear();
	}

	/**
	 * Builds a request body that sets the amount for the item.
	 * @param amount raw JSON value for the amount
	 * @return JSON request body
	 */
	private String addRequest(String amount) {
		return "{\"items\":[{\"id\":" + itemId + ",\"amount\":" + amount + "}]}";
	}

	/**
	 * Tests the GET /api/inventory endpoint.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testGetInventory() throws Exception {
		mvc.perform(get("/api/inventory"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(itemId))
				.andExpect(jsonPath("$.items[0].amount").value(5));
	}

	/**
	 * Tests that PUT /api/inventory adds to the current inventory.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testUpdateInventory() throws Exception {
		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(addRequest("10")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].amount").value(15));
	}

	/**
	 * Tests that a negative amount gets 400 and adds nothing.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testUpdateInventoryNegativeAmount() throws Exception {
		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(addRequest("-1")))
				.andExpect(status().isBadRequest());

		mvc.perform(get("/api/inventory"))
				.andExpect(jsonPath("$.items[0].amount").value(5));
	}

	/**
	 * Tests that an amount that is not a number gets 400 and adds nothing.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testUpdateInventoryNotAnInteger() throws Exception {
		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(addRequest("\"abc\"")))
				.andExpect(status().isBadRequest());

		mvc.perform(get("/api/inventory"))
				.andExpect(jsonPath("$.items[0].amount").value(5));
	}

	/**
	 * Tests that a customer cannot add inventory.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testUpdateInventoryAsCustomerForbidden() throws Exception {
		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(addRequest("10")))
				.andExpect(status().isForbidden());
	}
}
