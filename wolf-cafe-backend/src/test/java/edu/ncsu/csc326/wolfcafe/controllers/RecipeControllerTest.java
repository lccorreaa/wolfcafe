package edu.ncsu.csc326.wolfcafe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;
import edu.ncsu.csc326.wolfcafe.repositories.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.services.IngredientService;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests RecipeController.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "staff", roles = "STAFF")
public class RecipeControllerTest {
    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc mvc;

    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;

    /** ingredient service */
    @Autowired
    private IngredientService ingredientService;

    /** inventory repository */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Sets up the test case.
     * @throws java.lang.Exception if error
     */
    @BeforeEach
    public void setUp() throws Exception {
        entityManager.createQuery("DELETE FROM Recipe").executeUpdate();
        entityManager.createQuery("DELETE FROM Ingredient").executeUpdate();

        inventoryRepository.deleteAll();
        for (String name : new String[] {"Coffee", "Milk", "Chocolate"}) {
            ingredientService.createIngredient(new IngredientDto(name, 50));
        }
    }

    /**
     * Builds a one-entry ingredients list for a RecipeDto.
     *
     * @param name ingredient name
     * @param amount ingredient amount
     * @return the ingredients list
     */
    private List<Ingredient> ingredientsOf(String name, Integer amount) {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(name, amount));
        return ingredients;
    }

    /**
     * Builds a two-entry ingredients list for a RecipeDto.
     *
     * @param name1 first ingredient name
     * @param amount1 first ingredient amount
     * @param name2 second ingredient name
     * @param amount2 second ingredient amount
     * @return the ingredients list
     */
    private List<Ingredient> ingredientsOf(String name1, Integer amount1, String name2, Integer amount2) {
        List<Ingredient> ingredients = ingredientsOf(name1, amount1);
        ingredients.add(new Ingredient(name2, amount2));
        return ingredients;
    }

    /**
     * Tests retrieving all recipes.
     */
    @Test
    public void testGetRecipes() throws Exception {
        RecipeDto recipe1 = new RecipeDto("Mocha", 200, ingredientsOf("Coffee", 2, "Chocolate", 3));
        RecipeDto recipe2 = new RecipeDto("Latte", 100, ingredientsOf("Coffee", 3, "Milk", 2));

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipe1))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipe2))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String recipes = mvc.perform(get("/api/recipes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(recipes.contains("Mocha"));
        assertTrue(recipes.contains("Latte"));
        assertFalse(recipes.contains("NotARealRecipe"));
    }

    /**
     * Tests retrieving a single recipe by name.
     */
    @Test
    public void testGetRecipe() throws Exception {
        RecipeDto recipeDto = new RecipeDto("Latte", 100, ingredientsOf("Coffee", 3, "Milk", 2));

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/recipes/{name}", "Latte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Latte"))
                .andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.ingredients[?(@.name=='Coffee')].amount").value(contains(3)))
                .andExpect(jsonPath("$.ingredients[?(@.name=='Milk')].amount").value(contains(2)));
    }

    /**
     * Tests retrieving a recipe that doesn't exist.
     */
    @Test
    public void testGetRecipeNotFound() throws Exception {
        mvc.perform(get("/api/recipes/{name}", "NotARealRecipe"))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests creating a recipe.
     */
    @Test
    public void testCreateRecipe() throws Exception {
        RecipeDto recipeDto = new RecipeDto("Mocha", 200, ingredientsOf("Coffee", 2, "Chocolate", 3));

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mocha"))
                .andExpect(jsonPath("$.price").value(200))
                .andExpect(jsonPath("$.ingredients[?(@.name=='Coffee')].amount").value(contains(2)))
                .andExpect(jsonPath("$.ingredients[?(@.name=='Chocolate')].amount").value(contains(3)));
    }

    /**
     * Tests creating a recipe with an invalid (non-positive) price.
     */
    @Test
    public void testCreateRecipeInvalidPrice() throws Exception {
        RecipeDto recipeDto = new RecipeDto("Mocha", 0, ingredientsOf("Coffee", 2));

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests creating a recipe with no ingredients.
     */
    @Test
    public void testCreateRecipeNoIngredients() throws Exception {
        RecipeDto recipeDto = new RecipeDto("Mocha", 200, new ArrayList<>());

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests creating a recipe with a name that already exists.
     */
    @Test
    public void testCreateRecipeDuplicateName() throws Exception {
        RecipeDto recipeDto = new RecipeDto("Mocha", 200, ingredientsOf("Coffee", 2, "Chocolate", 3));

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    /**
     * Tests creating a recipe when the maximum number of recipes already exists.
     */
    @Test
    public void testCreateRecipeTooMany() throws Exception {
        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(new RecipeDto("Recipe1", 100, ingredientsOf("Coffee", 1))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(new RecipeDto("Recipe2", 100, ingredientsOf("Coffee", 1))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(new RecipeDto("Recipe3", 100, ingredientsOf("Coffee", 1))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(new RecipeDto("Recipe4", 100, ingredientsOf("Coffee", 1))))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests updating an existing recipe.
     */
    @Test
    public void testUpdateRecipe() throws Exception {
        RecipeDto recipeDto = new RecipeDto("Mocha", 200, ingredientsOf("Coffee", 2, "Chocolate", 3));

        String response = mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        RecipeDto createdRecipe = mapper.readValue(response, RecipeDto.class);

        RecipeDto updateDto = new RecipeDto("Mocha Deluxe", 250, ingredientsOf("Coffee", 4, "Milk", 1));

        mvc.perform(put("/api/recipes/{id}", createdRecipe.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(updateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mocha Deluxe"))
                .andExpect(jsonPath("$.price").value(250))
                .andExpect(jsonPath("$.ingredients[?(@.name=='Coffee')].amount").value(contains(4)))
                .andExpect(jsonPath("$.ingredients[?(@.name=='Milk')].amount").value(contains(1)));
    }

    /**
     * Tests updating a recipe that doesn't exist.
     */
    @Test
    public void testUpdateRecipeNotFound() throws Exception {
        RecipeDto updateDto = new RecipeDto("Mocha", 200, ingredientsOf("Coffee", 2));

        mvc.perform(put("/api/recipes/{id}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(updateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests deleting a recipe.
     */
    @Test
    public void testDeleteRecipe() throws Exception {
        RecipeDto recipeDto = new RecipeDto("Mocha", 200, ingredientsOf("Coffee", 2, "Chocolate", 3));

        String response = mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        RecipeDto createdRecipe = mapper.readValue(response, RecipeDto.class);
        Long recipeId = createdRecipe.getId();

        mvc.perform(delete("/api/recipes/{id}", recipeId))
                .andExpect(status().isOk());

        mvc.perform(get("/api/recipes/{name}", "Mocha"))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests that a customer can view recipes but cannot create one.
     * @throws Exception if issue when running the test.
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testCreateRecipeAsCustomerForbidden() throws Exception {
        mvc.perform(get("/api/recipes"))
                .andExpect(status().isOk());

        RecipeDto recipeDto = new RecipeDto("Mocha", 200, ingredientsOf("Coffee", 2, "Chocolate", 3));
        mvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(recipeDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
