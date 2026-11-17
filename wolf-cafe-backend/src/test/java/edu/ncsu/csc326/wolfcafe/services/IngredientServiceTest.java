package edu.ncsu.csc326.wolfcafe.services;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import edu.ncsu.csc326.wolfcafe.exception.InvalidIngredientException;

/**
 * Tests IngredientServiceImpl.
 *
 * @author Sandra Jose (sajose)
 */
@SpringBootTest
public class IngredientServiceTest {

    /** Reference to ingredient service */
    @Autowired
    private IngredientService ingredientService;

    /**
     * Removes all ingredients before each test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        ingredientService.deleteAllIngredients();
    }

    /**
     * Tests creating ingredients.
     */
    @Test
    @Transactional
    public void testCreateIngredient() {

        IngredientDto ingredient1 = new IngredientDto("Coffee", 5);

        IngredientDto createdIngredient1 = ingredientService.createIngredient(ingredient1);
        assertAll("Ingredient contents",
                () -> assertEquals(ingredient1.getName(), createdIngredient1.getName()),
                () -> assertEquals(5, createdIngredient1.getAmount()));

        IngredientDto ingredient2 = new IngredientDto("Pumpkin Spice", 10);
        IngredientDto createdIngredient2 = ingredientService.createIngredient(ingredient2);
        assertAll("Ingredient contents",
                () -> assertEquals(ingredient2.getName(), createdIngredient2.getName()),
                () -> assertEquals(10, createdIngredient2.getAmount()));
    }

    /**
     * Tests getting ingredients by id.
     */
    @Test
    @Transactional
    public void testGetIngredientById() {

        IngredientDto ingredient1 = new IngredientDto("Coffee", 5);

        IngredientDto createdIngredient1 = ingredientService.createIngredient(ingredient1);
        IngredientDto fetchedIngredient1 = ingredientService.getIngredientById(createdIngredient1.getId());
        assertAll("Ingredient contents",
                () -> assertEquals(ingredient1.getName(), fetchedIngredient1.getName()),
                () -> assertEquals(5, fetchedIngredient1.getAmount()));

        IngredientDto ingredient2 = new IngredientDto("Pumpkin Spice", 10);
        IngredientDto createdIngredient2 = ingredientService.createIngredient(ingredient2);
        IngredientDto fetchedIngredient2 = ingredientService.getIngredientById(createdIngredient2.getId());
        assertAll("Ingredient contents",
                () -> assertEquals(ingredient2.getName(), fetchedIngredient2.getName()),
                () -> assertEquals(10, fetchedIngredient2.getAmount()));
    }

    /**
     * Tests adding an ingredient by name and amount.
     */
    @Test
    @Transactional
    public void testAddIngredientByName() {
        IngredientDto created = ingredientService.createIngredient(
                new IngredientDto("Vanilla", 7));

        assertAll("Ingredient contents",
                () -> assertEquals("Vanilla", created.getName()),
                () -> assertEquals(7, created.getAmount()));
    }

    /**
     * Tests that a non-positive amount is rejected and nothing is added.
     */
    @Test
    @Transactional
    public void testRejectsNonPositiveAmount() {
        InvalidIngredientException exception = assertThrows(InvalidIngredientException.class,
                () -> ingredientService.createIngredient(new IngredientDto("Vanilla", 0)));

        assertTrue(exception.getMessage().contains("positive integer"));
        assertEquals(0, ingredientService.getAllIngredients().size());
    }

    /**
     * Tests that a duplicate name is rejected and the existing amount is unchanged.
     */
    @Test
    @Transactional
    public void testRejectsDuplicateNameWithoutAddingIngredient() {
        ingredientService.createIngredient(new IngredientDto("Vanilla", 7));

        InvalidIngredientException exception = assertThrows(InvalidIngredientException.class,
                () -> ingredientService.createIngredient(new IngredientDto("Vanilla", 3)));

        assertTrue(exception.getMessage().contains("already exists"));
        assertEquals(1, ingredientService.getAllIngredients().size());
        assertEquals(7, ingredientService.getAllIngredients().get(0).getAmount());
    }

}
