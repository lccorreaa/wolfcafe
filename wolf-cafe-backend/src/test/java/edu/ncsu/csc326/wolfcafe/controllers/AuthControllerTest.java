package edu.ncsu.csc326.wolfcafe.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.repositories.UserRepository;


/**
 * Tests the authentication controller against the test database.  The default
 * admin user is created by SetupDataLoader when the application starts.
 *
 * Each test is @Transactional, so users it registers are rolled back when it ends.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ExtendWith(OutputCaptureExtension.class)
public class AuthControllerTest {

	/** Customer used by the tests */
	private static final RegisterDto CUSTOMER =
			new RegisterDto("Jordan Estes", "jestes", "vitae.erat@yahoo.edu", "JXB16TBD4LC");

	/** Admin password from application.properties */
	@Value("${app.admin-user-password}")
	private String adminUserPassword;

	/** Mocked MVC for testing */
	@Autowired
	private MockMvc mvc;

	/** Used to look up the id of a registered user */
	@Autowired
	private UserRepository userRepository;

	/**
	 * Registers the given user.
	 * @param registerDto user to register
	 * @return the result of the request, for further checks
	 * @throws Exception if error
	 */
	private ResultActions register(RegisterDto registerDto) throws Exception {
		return mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(registerDto))
				.accept(MediaType.APPLICATION_JSON));
	}

	/**
	 * Logs in the given user.
	 * @param usernameOrEmail user's username or email
	 * @param password user's password
	 * @return the result of the request, for further checks
	 * @throws Exception if error
	 */
	private ResultActions login(String usernameOrEmail, String password) throws Exception {
		return mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(TestUtils.asJsonString(new LoginDto(usernameOrEmail, password)))
				.accept(MediaType.APPLICATION_JSON));
	}

	/**
	 * Tests logging in as an admin user.
	 * @throws Exception if error
	 */
	@Test
	public void testLoginAdmin() throws Exception {
		login("admin", adminUserPassword)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
	}

	/**
	 * Tests creating a customer user and logging in by username and by email.
	 * @throws Exception if error
	 */
	@Test
	public void testCreateCustomerAndLogin() throws Exception {
		register(CUSTOMER)
				.andExpect(status().isCreated())
				.andExpect(content().string("User registered successfully."));

		login("jestes", "JXB16TBD4LC")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));

		login("vitae.erat@yahoo.edu", "JXB16TBD4LC")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));
	}

	/**
	 * Tests that a username or email can only be registered once.
	 * @throws Exception if error
	 */
	@Test
	public void testRegisterDuplicate() throws Exception {
		register(CUSTOMER).andExpect(status().isCreated());

		register(new RegisterDto("Other Person", "jestes", "other@ncsu.edu", "pw"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Username already exists."));

		register(new RegisterDto("Other Person", "other", "vitae.erat@yahoo.edu", "pw"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Email already exists."));
	}

	/**
	 * Tests that a wrong password is answered with 401 Unauthorized, and that the
	 * failed attempt is logged without the password.
	 * @param output console output captured during the test
	 * @throws Exception if error
	 */
	@Test
	public void testLoginBadPassword(CapturedOutput output) throws Exception {
		register(CUSTOMER).andExpect(status().isCreated());

		login("jestes", "wrong-password")
				.andExpect(status().isUnauthorized());

		assertTrue(output.getOut().contains("user=jestes role=none action=failed to log in"));
		assertTrue(!output.getOut().contains("wrong-password"));
	}

	/**
	 * Tests that the token returned at login is accepted by a secured endpoint, which
	 * is how the frontend calls the API: it sends "Bearer " and the token in the
	 * Authorization header of every request.
	 * @throws Exception if error
	 */
	@Test
	public void testLoginTokenAuthorizesRequests() throws Exception {
		register(CUSTOMER).andExpect(status().isCreated());
		String response = login("jestes", "JXB16TBD4LC")
				.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(response, "$.accessToken");

		mvc.perform(get("/api/items").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	/**
	 * Tests that a token that was not issued by this application is rejected.
	 * @throws Exception if error
	 */
	@Test
	public void testInvalidToken() throws Exception {
		mvc.perform(get("/api/items").header("Authorization", "Bearer not-a-real-token"))
				.andExpect(status().isUnauthorized());
	}

	/**
	 * Tests that an admin can delete a user.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testDeleteUserAsAdmin() throws Exception {
		register(CUSTOMER).andExpect(status().isCreated());
		Long id = userRepository.findByUsername("jestes").orElseThrow().getId();

		mvc.perform(delete("/api/auth/user/" + id))
				.andExpect(status().isOk());

		assertTrue(userRepository.findByUsername("jestes").isEmpty());

		mvc.perform(delete("/api/auth/user/" + id))
				.andExpect(status().isNotFound());

		Long adminId = userRepository.findByUsername("admin").orElseThrow().getId();
		mvc.perform(delete("/api/auth/user/" + adminId))
				.andExpect(status().isNotFound());
		assertTrue(userRepository.findByUsername("admin").isPresent());
	}

	/**
	 * Tests that a customer cannot delete a user.
	 * @throws Exception if error
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testDeleteUserAsCustomer() throws Exception {
		register(CUSTOMER).andExpect(status().isCreated());
		Long id = userRepository.findByUsername("jestes").orElseThrow().getId();

		mvc.perform(delete("/api/auth/user/" + id))
				.andExpect(status().isForbidden());
	}

	/**
	 * UC5: verifies the admin can list and update customer data without receiving credentials.
	 * @throws Exception if the request fails
	 */
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testAdminListsAndUpdatesUsers() throws Exception {
	    register(CUSTOMER).andExpect(status().isCreated());
	    Long id = userRepository.findByUsername("jestes").orElseThrow().getId();

	    String response = mvc.perform(get("/api/auth/users"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
	    assertTrue(response.contains("\"username\":\"jestes\""));
	    assertFalse(response.contains("\"username\":\"admin\""));
	    assertFalse(response.contains("\"password\""));

	    mvc.perform(put("/api/auth/user/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Jane Doe\",\"email\":\"jdoe@example.com\","
				        + "\"phone\":\"828-888-8282\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.name").value("Jane Doe"))
				.andExpect(jsonPath("$.email").value("jdoe@example.com"))
				.andExpect(jsonPath("$.phone").value("828-888-8282"))
				.andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	/**
	 * UC5 [Cannot Edit] and [Invalid Input]: verifies deleted users and invalid profiles fail.
	 * @throws Exception if the request fails
	 */
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testAdminUpdateRejectsMissingAndInvalidUsers() throws Exception {
	    register(CUSTOMER).andExpect(status().isCreated());
	    Long id = userRepository.findByUsername("jestes").orElseThrow().getId();

	    mvc.perform(put("/api/auth/user/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Jane Doe\",\"email\":\"invalid-email\","
				        + "\"phone\":\"8288888282\"}"))
				.andExpect(status().isBadRequest());

	    mvc.perform(put("/api/auth/user/999999")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Jane Doe\",\"email\":\"jdoe@example.com\","
				        + "\"phone\":\"828-888-8282\"}"))
				.andExpect(status().isNotFound());
	}

	/**
	 * UC7 main/alternative flows: creates staff and rejects invalid or duplicate data.
	 * @param output captured audit log output
	 * @throws Exception if the request fails
	 */
	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testAdminCreatesStaff(CapturedOutput output) throws Exception {
	    register(CUSTOMER).andExpect(status().isCreated());

	    mvc.perform(post("/api/auth/staff")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Jane Doe\",\"username\":\"janedoe\","
				        + "\"password\":\"JaneDoe8\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Jane Doe"))
				.andExpect(jsonPath("$.username").value("janedoe"))
				.andExpect(jsonPath("$.role").value("ROLE_STAFF"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.email").value(nullValue()));
	    assertFalse(output.getOut().contains("JaneDoe8"));

	    mvc.perform(post("/api/auth/staff")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Invalid Username\",\"username\":\"abc\","
				        + "\"password\":\"JaneDoe8\"}"))
				.andExpect(status().isBadRequest());

	    mvc.perform(post("/api/auth/staff")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Invalid Password\",\"username\":\"validuser\","
				        + "\"password\":\"password\"}"))
				.andExpect(status().isBadRequest());

	    mvc.perform(post("/api/auth/staff")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Duplicate\",\"username\":\"jestes\","
				        + "\"password\":\"JaneDoe8\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Username already exists."));
	}

	/**
	 * UC5/UC7 precondition: user-management endpoints require an admin.
	 * @throws Exception if the request fails
	 */
	@Test
	@WithMockUser(username = "customer", roles = "CUSTOMER")
	public void testUserManagementRequiresAdmin() throws Exception {
	    mvc.perform(get("/api/auth/users"))
				.andExpect(status().isForbidden());
	    mvc.perform(post("/api/auth/staff")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Jane Doe\",\"username\":\"janedoe\","
				        + "\"password\":\"JaneDoe8\"}"))
				.andExpect(status().isForbidden());
	}
}
