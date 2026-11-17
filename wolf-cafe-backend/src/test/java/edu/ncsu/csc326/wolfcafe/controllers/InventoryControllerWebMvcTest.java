package edu.ncsu.csc326.wolfcafe.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import edu.ncsu.csc326.wolfcafe.config.SpringSecurityConfig;
import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.security.JwtAuthenticationEntryPoint;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.InvalidInventoryException;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.services.InventoryService;

/**
 * Tests InventoryController on its own, without the rest of the application.
 *
 * This class and InventoryControllerTest test the same two endpoints on purpose.
 * InventoryControllerTest starts the whole application with @SpringBootTest and
 * talks to a real MySQL database, so it proves that the controller, the service,
 * the repository, and the database all work together.  That takes several seconds
 * and needs MySQL running.
 *
 * This class starts only Spring's web layer with @WebMvcTest and hands the
 * controller a Mockito mock in place of the real InventoryService, using an
 * annotation called @MockitoBean.  Nothing here touches a database.  It tests
 * less of the system, and in exchange it can do three things the other test
 * cannot:
 *
 *   1. decide exactly what the service returns, so the controller can be checked
 *      against values that would be awkward to arrange in a real database;
 *   2. inspect what the controller handed down to the service, which is the only
 *      way to prove the JSON request body was bound to an InventoryDto correctly;
 *   3. make the service fail on command, so error handling can be tested at all.
 *
 * Neither style replaces the other.  Reach for this one while working on a
 * controller: it starts its Spring context roughly ten times faster than
 * InventoryControllerTest does, and it passes with no MySQL running at all, so it
 * still works before the database is set up.  Keep an integration test around too,
 * to catch the wiring mistakes that a mock will happily hide.
 *
 * Note @MockitoBean and not @MockBean.  They do the same job, but @MockBean was
 * deprecated in Spring Boot 3.4, so it is what older tutorials will show you.
 *
 * The test was updated on 11/3/26 to use items rather than ingredients after
 * the inventory API was refactored; its test purposes are unchanged.
 *
 * @author CSC 326 Course Staff
 */
@WebMvcTest(InventoryController.class)
@Import({ SpringSecurityConfig.class, JwtAuthenticationEntryPoint.class, AuditLogger.class })
@WithMockUser(username = "staff", roles = "STAFF")
public class InventoryControllerWebMvcTest {

	/** Mock MVC for testing the controller without a running server. */
	@Autowired
	private MockMvc mvc;

	/**
	 * Stands in for the real InventoryService.  @WebMvcTest does not create the
	 * application's @Service beans, so without this the controller would have
	 * nothing to call and the test context would fail to start.
	 */
	@MockitoBean
	private InventoryService inventoryService;

	/** Needed by JwtAuthenticationFilter; unused because these tests send no token. */
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	/** Needed by JwtAuthenticationFilter; unused because these tests send no token. */
	@MockitoBean
	private UserDetailsService userDetailsService;

	/**
	 * Builds an InventoryDto holding one ingredient with the given id and amount.
	 * @param ingredientId ingredient id
	 * @param amount ingredient amount
	 * @return the InventoryDto
	 */
	private static InventoryDto inventoryWith(Long ingredientId, Integer amount) {
		IngredientDto ingredientDto = new IngredientDto();
		ingredientDto.setId(ingredientId);
		ingredientDto.setAmount(amount);
		InventoryDto inventoryDto = new InventoryDto(1L);
		inventoryDto.addIngredient(ingredientDto);
		return inventoryDto;
	}

	/**
	 * Builds an InventoryDto holding one item with the given id and amount.
	 *
	 * Implemented by AI to fix Inventory Test errors
	 *
	 * @param itemId item id
	 * @param amount item amount
	 * @return the InventoryDto
	 */
	private static InventoryDto itemInventoryWith(Long itemId, Integer amount) {
		InventoryDto inventoryDto = new InventoryDto(1L);
		inventoryDto.setItems(java.util.List.of(
				new ItemDto(itemId, "Coffee", "Coffee is life", 3.25, amount)));
		return inventoryDto;
	}

	/**
	 * Tests that GET /api/inventory returns whatever the service reports, as JSON.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testGetInventory() throws Exception {
		when(inventoryService.getItemInventory()).thenReturn(itemInventoryWith(7L, 5));

		mvc.perform(get("/api/inventory"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(7))
				.andExpect(jsonPath("$.items[0].amount").value(5));

		//The stub above would still be in place if the controller never called the
		//service, so check that it actually did.
		verify(inventoryService).getItemInventory();
	}

	/**
	 * Tests that PUT /api/inventory turns the JSON request body into an InventoryDto
	 * and passes it to the service.  Capturing the argument is the only way to see
	 * what the controller sent onward; the response alone cannot tell us, because
	 * the mock decides what comes back regardless of what it was given.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testUpdateInventoryPassesRequestBodyToService() throws Exception {
		when(inventoryService.updateItemInventory(any(InventoryDto.class)))
				.thenReturn(itemInventoryWith(7L, 15));

		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TestUtils.asJsonString(itemInventoryWith(7L, 10)))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].amount").value(15));

		ArgumentCaptor<InventoryDto> sentToService = ArgumentCaptor.forClass(InventoryDto.class);
		verify(inventoryService).updateItemInventory(sentToService.capture());

		InventoryDto captured = sentToService.getValue();
		assertAll("InventoryDto the controller built from the request body",
				() -> assertEquals(1, captured.getItems().size()),
				() -> assertEquals(7L, captured.getItems().get(0).getId()),
				() -> assertEquals(10, captured.getItems().get(0).getAmount()));
	}

	/**
	 * Tests that PUT /api/inventory answers 404 when the service reports that there
	 * is no inventory to update.  Arranging that against a real database would mean
	 * emptying the table first; here the mock simply throws.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testUpdateInventoryWhenInventoryIsMissing() throws Exception {
		when(inventoryService.updateItemInventory(any(InventoryDto.class)))
				.thenThrow(new ResourceNotFoundException("Inventory does not exist."));

		//404 rather than 500 because ResourceNotFoundException carries
		//@ResponseStatus(HttpStatus.NOT_FOUND).
		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TestUtils.asJsonString(itemInventoryWith(7L, 10)))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	/**
	 * Tests that PUT /api/inventory answers 400 when the service rejects an amount
	 * (UC5 Invalid Unit).  400 because InvalidInventoryException carries
	 * @ResponseStatus(HttpStatus.BAD_REQUEST).
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testUpdateInventoryWithInvalidUnit() throws Exception {
		when(inventoryService.updateItemInventory(any(InventoryDto.class)))
				.thenThrow(new InvalidInventoryException("Units of each ingredient must be a positive integer."));

		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content(TestUtils.asJsonString(itemInventoryWith(7L, -1)))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	/**
	 * Tests that PUT /api/inventory answers 400 for an amount that is not a
	 * number, without ever calling the service.  Jackson rejects the body before
	 * the controller runs.
	 * @throws Exception if issue when running the test.
	 */
	@Test
	public void testUpdateInventoryWithNonIntegerUnit() throws Exception {
		mvc.perform(put("/api/inventory")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"items\":[{\"id\":7,\"amount\":\"abc\"}]}")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		verify(inventoryService, org.mockito.Mockito.never()).updateItemInventory(any(InventoryDto.class));
	}
}