package edu.ncsu.csc326.wolfcafe.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.config.SpringSecurityConfig;
import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.security.JwtAuthenticationEntryPoint;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;
import edu.ncsu.csc326.wolfcafe.services.ItemService;

/**
 * Tests ItemController on its own, without the rest of the application.
 *
 * ItemControllerTest starts the whole application with @SpringBootTest and talks to
 * the real test database, so it proves that the controller, the service, the
 * repository, and the database all work together.  This class starts only Spring's
 * web layer with @WebMvcTest and hands the controller a Mockito mock in place of the
 * real ItemService, using @MockitoBean.  Nothing here touches a database.  In
 * exchange it can:
 *
 *   1. decide exactly what the service returns;
 *   2. inspect what the controller handed down to the service, which is the only
 *      way to prove the JSON request body was bound to an ItemDto correctly;
 *   3. make the service fail on command, so error handling can be tested.
 *
 * Security still applies.  @WebMvcTest leaves out the application's configuration
 * and most of its beans, so @Import brings back the security rules (and the
 * AuditLogger the controller logs through), and the two beans the login token
 * filter depends on are mocked.  @WithMockUser then supplies the logged in user.
 *
 * @author CSC 326 Course Staff
 */
@WebMvcTest(ItemController.class)
@Import({ SpringSecurityConfig.class, JwtAuthenticationEntryPoint.class, AuditLogger.class })
@ExtendWith(OutputCaptureExtension.class)
public class ItemControllerWebMvcTest {

	/** API path */
	private static final String API_PATH = "/api/items";

	/** Mock MVC for testing the controller without a running server. */
	@Autowired
	private MockMvc mvc;

	/** Stands in for the real ItemService. */
	@MockitoBean
	private ItemService itemService;

	/** Needed by JwtAuthenticationFilter; unused because these tests send no token. */
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	/** Needed by JwtAuthenticationFilter; unused because these tests send no token. */
	@MockitoBean
	private UserDetailsService userDetailsService;

	/**
	 * Tests that POST /api/items turns the JSON request body into an ItemDto and
	 * passes it to the service.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testAddItemPassesRequestBodyToService() throws Exception {
		when(itemService.addItem(any(ItemDto.class)))
				.thenReturn(new ItemDto(27L, "Coffee", "Coffee is life", 3.25));

		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Coffee", "Coffee is life", 3.25)))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(27));

		ArgumentCaptor<ItemDto> sentToService = ArgumentCaptor.forClass(ItemDto.class);
		verify(itemService).addItem(sentToService.capture());

		ItemDto captured = sentToService.getValue();
		assertAll("ItemDto the controller built from the request body",
				() -> assertEquals("Coffee", captured.getName()),
				() -> assertEquals("Coffee is life", captured.getDescription()),
				() -> assertEquals(3.25, captured.getPrice()));
	}

	/**
	 * Tests that a successful request is recorded in the audit log with the user,
	 * their role, and the action.  OutputCaptureExtension collects what the
	 * application wrote to the console during the test.
	 * @param output console output captured during the test
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testAddItemIsAuditLogged(CapturedOutput output) throws Exception {
		when(itemService.addItem(any(ItemDto.class)))
				.thenReturn(new ItemDto(27L, "Coffee", "Coffee is life", 3.25));

		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Coffee", "Coffee is life", 3.25))))
				.andExpect(status().isCreated());

		assertTrue(output.getOut().contains("user=staff role=ROLE_STAFF action=created item 27 (Coffee)"));
	}

	/**
	 * Tests that an item with no name is rejected before it reaches the service.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testAddItemWithoutName() throws Exception {
		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, " ", "No name", 3.25))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Item name is required."));

		verifyNoInteractions(itemService);
	}

	/**
	 * Tests that an item with a negative price is rejected before it reaches the service.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testAddItemNegativePrice() throws Exception {
		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Coffee", "Free money", -1.00))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Item price must be positive."));

		verifyNoInteractions(itemService);
	}

	/**
	 * Tests that a WolfCafeAPIException from the service is answered with the
	 * status it carries.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "staff", roles = "STAFF")
	public void testAddItemConflict() throws Exception {
		when(itemService.addItem(any(ItemDto.class)))
				.thenThrow(new WolfCafeAPIException(HttpStatus.CONFLICT, "Item already exists with name Coffee"));

		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Coffee", "Coffee is life", 3.25))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Item already exists with name Coffee"));
	}

	/**
	 * Tests that a customer cannot create an item, and the service is never called.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testAddItemAsCustomer() throws Exception {
		mvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Coffee", "Coffee is life", 3.25))))
				.andExpect(status().isForbidden());

		verifyNoInteractions(itemService);
	}

	/**
	 * Tests that a user who has not logged in cannot see an item.
	 * @throws Exception if error
	 */
	@Test
	public void testGetItemNotLoggedIn() throws Exception {
		mvc.perform(get(API_PATH + "/27"))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(itemService);
	}

	/**
	 * Tests that GET /api/items/{id} returns what the service reports.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testGetItem() throws Exception {
		when(itemService.getItem(27L)).thenReturn(new ItemDto(27L, "Coffee", "Coffee is life", 3.25));

		mvc.perform(get(API_PATH + "/27"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(27))
				.andExpect(jsonPath("$.name").value("Coffee"))
				.andExpect(jsonPath("$.description").value("Coffee is life"))
				.andExpect(jsonPath("$.price").value(3.25));
	}

	/**
	 * Tests that updating an item the service cannot find answers 404.  404 rather
	 * than 500 because ResourceNotFoundException carries @ResponseStatus(NOT_FOUND).
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testUpdateItemNotFound() throws Exception {
		when(itemService.updateItem(eq(27L), any(ItemDto.class)))
				.thenThrow(new ResourceNotFoundException("Item not found with id 27"));

		mvc.perform(put(API_PATH + "/27")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new ItemDto(null, "Coffee", "Coffee is life", 3.25))))
				.andExpect(status().isNotFound());
	}
}
