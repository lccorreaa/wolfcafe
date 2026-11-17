package edu.ncsu.csc326.wolfcafe.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import edu.ncsu.csc326.wolfcafe.repositories.TaxRateRepository;
import jakarta.persistence.EntityManager;

/**
 * Tests the TaxRateController REST API through MockMvc.
 *
 * @author Sandra Jose (sajose)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaxRateControllerTest {

    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc mvc;

    /** Reference to TaxRateRepository, used to empty the table */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;

    /**
     * Empties the tax rate table before each test.
     */
    @BeforeEach
    public void setUp() {
        taxRateRepository.deleteAll();
        entityManager.flush();
    }

    /**
     * Builds a PUT request body for the given rate.
     * @param rate raw JSON value for the rate
     * @return JSON request body
     */
    private String body(String rate) {
        return "{\"rate\":" + rate + "}";
    }

    /**
     * Tests that a customer can view the default tax rate.
     * @throws Exception if issue when running the test
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testGetTaxRateCustomer() throws Exception {
        mvc.perform(get("/api/tax"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(6.25));
    }

    /**
     * Tests that staff can view the tax rate.
     * @throws Exception if issue when running the test
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testGetTaxRateStaff() throws Exception {
        mvc.perform(get("/api/tax")).andExpect(status().isOk());
    }

    /**
     * Tests that an anonymous user cannot view the tax rate.
     * @throws Exception if issue when running the test
     */
    @Test
    public void testGetTaxRateUnauthenticated() throws Exception {
        mvc.perform(get("/api/tax")).andExpect(status().isUnauthorized());
    }

    /**
     * Tests that the admin can update the tax rate and the new value is returned.
     * @throws Exception if issue when running the test
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testUpdateTaxRateAdmin() throws Exception {
        mvc.perform(put("/api/tax").contentType(MediaType.APPLICATION_JSON).content(body("8.0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(8.0));

        mvc.perform(get("/api/tax"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(8.0));
    }

    /**
     * Tests that invalid rates are rejected with 400.
     * @throws Exception if issue when running the test
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testUpdateTaxRateInvalid() throws Exception {
        for (String bad : new String[] { "0", "-5", "100.5", "null" }) {
            mvc.perform(put("/api/tax").contentType(MediaType.APPLICATION_JSON).content(body(bad)))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * Tests that staff cannot update the tax rate.
     * @throws Exception if issue when running the test
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testUpdateTaxRateStaffForbidden() throws Exception {
        mvc.perform(put("/api/tax").contentType(MediaType.APPLICATION_JSON).content(body("8.0")))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that a customer cannot update the tax rate.
     * @throws Exception if issue when running the test
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testUpdateTaxRateCustomerForbidden() throws Exception {
        mvc.perform(put("/api/tax").contentType(MediaType.APPLICATION_JSON).content(body("8.0")))
                .andExpect(status().isForbidden());
    }
}
