package edu.ncsu.csc326.wolfcafe.controllers;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests IngredientController.
 *
 * @author Sandra Jose (sajose)
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "staff", roles = "STAFF")
public class IngredientControllerTest {

    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc mvc;

    @Test
    @Transactional
    void testCreateIngredient() throws Exception {
        IngredientDto ingredient1 = new IngredientDto("Coffee", 5);

        mvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(ingredient1))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("5"));
    }

    @Test
    @Transactional
    void testAddIngredientWithName() throws Exception {
        mvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Vanilla\",\"amount\":7}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vanilla"))
                .andExpect(jsonPath("$.amount").value(7));
    }

    @Test
    @Transactional
    void testRejectsInvalidUnit() throws Exception {
        mvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Vanilla\",\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void testRejectsDuplicateIngredient() throws Exception {
        String ingredient = "{\"name\":\"Vanilla\",\"amount\":7}";

        mvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredient))
                .andExpect(status().isOk());

        mvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ingredient))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests that a customer cannot create an ingredient.
     * @throws Exception if issue when running the test.
     */
    @Test
    @Transactional
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void testCreateIngredientAsCustomerForbidden() throws Exception {
        mvc.perform(post("/api/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(new IngredientDto("Coffee", 5)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
