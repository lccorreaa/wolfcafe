package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.entity.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests Recipe repository.  Uses the real database - not an embedded one.
 *
 * @author CSC 326 Course Staff
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RecipeRepositoryTest {

	/** Reference to recipe repository */
	@Autowired
	private RecipeRepository recipeRepository;

	/** Reference to EntityManager */
	@Autowired
	private TestEntityManager testEntityManager;

	/** Coffee recipe */
	private Recipe recipe1;
	/** Latte recipe */
	private Recipe recipe2;

	/**
	 * Finds an ingredient by name within a list of ingredients.
	 * @param ingredients ingredients to search
	 * @param name name to look for
	 * @return the matching ingredient
	 */
	private Ingredient findByName(List<Ingredient> ingredients, String name) {
		return ingredients.stream()
				.filter(i -> i.getName().equals(name))
				.findFirst()
				.orElseThrow();
	}

	/**
	 * Sets up the test case.
	 * @throws java.lang.Exception if error
	 */
	@BeforeEach
	public void setUp() throws Exception {
		recipeRepository.deleteAll();

		recipeRepository.deleteAll();

		//Each recipe gets its own Ingredient row: with @OneToMany a row belongs to
		//exactly one recipe, so the same Ingredient object cannot be shared.
		List<Ingredient> ingredients1 = new ArrayList<>();
		ingredients1.add(new Ingredient("Coffee", 3));
		List<Ingredient> ingredients2 = new ArrayList<>();
		ingredients2.add(new Ingredient("Coffee", 1));

		recipe1 = new Recipe("Coffee", 50, ingredients1);
		recipe2 = new Recipe("Latte", 100, ingredients2);

		recipeRepository.save(recipe1);
		recipeRepository.save(recipe2);

		//Push the inserts to the database and empty the persistence context.  Without
		//this, a test below could be handed the same in-memory object that save()
		//just returned and would pass even if nothing reached the database.
		testEntityManager.flush();
		testEntityManager.clear();
	}

	/**
	 * Tests RecipeRepository.findByName().  Use this test as the model for the
	 * repository tests you write for the remaining operations.
	 */
	@Test
	public void testFindRecipeByName() {
		Recipe fetchedRecipe = recipeRepository.findByName("Latte").orElseThrow();

		assertAll("Recipe contents",
				() -> assertEquals("Latte", fetchedRecipe.getName()),
				() -> assertEquals(100, fetchedRecipe.getPrice()));
	}

	/**
	 * Tests retrieving every recipe from the repository.
	 */
	@Test
	public void testGetAllRecipes() {
		List<Recipe> recipeList = recipeRepository.findAll();
		assertEquals(2, recipeList.size());

		assertAll("Recipe list contents",
				() -> assertEquals("Coffee", recipeList.get(0).getName()),
				() -> assertEquals(50, recipeList.get(0).getPrice()),

				() -> assertEquals("Latte", recipeList.get(1).getName()),
				() -> assertEquals(100, recipeList.get(1).getPrice()));
	}

	/**
	 * Tests updating a recipe already in the repository.
	 */
	@Test
	public void testUpdateRecipe() {
		assertEquals(50, recipe1.getPrice());

		recipe1.setPrice(75);
		recipeRepository.save(recipe1);

		testEntityManager.flush();
		testEntityManager.clear();

		Recipe updatedRecipe = recipeRepository.findByName("Coffee").orElseThrow();
		assertEquals(75, updatedRecipe.getPrice());
	}

	/**
	 * Tests deleting a recipe from the repository.
	 */
	@Test
	public void testDeleteRecipe() {
		recipeRepository.delete(recipe2);

		testEntityManager.flush();
		testEntityManager.clear();

		assertTrue(recipeRepository.findByName("Latte").isEmpty());
		assertTrue(recipeRepository.findByName("Coffee").isPresent());
	}

	/**
	 * Tests saving and retrieving a recipe with ingredients.
	 */
	@Test
	public void testRecipeWithIngredients() {
		Ingredient coffeeIngredient = new Ingredient("Espresso Shot", 5);
		Ingredient milkIngredient = new Ingredient("Milk", 2);
		testEntityManager.persist(coffeeIngredient);
		testEntityManager.persist(milkIngredient);

		List<Ingredient> ingredients = new ArrayList<>();
		ingredients.add(coffeeIngredient);
		ingredients.add(milkIngredient);

		Recipe mocha = new Recipe("Mocha", 150, ingredients);
		recipeRepository.save(mocha);

		testEntityManager.flush();
		testEntityManager.clear();

		Recipe fetchedMocha = recipeRepository.findByName("Mocha").orElseThrow();
		assertAll("Recipe ingredients",
				() -> assertEquals(2, fetchedMocha.getIngredients().size()),
				() -> assertEquals(5, findByName(fetchedMocha.getIngredients(), "Espresso Shot").getAmount()),
				() -> assertEquals(2, findByName(fetchedMocha.getIngredients(), "Milk").getAmount()));
	}

	/**
	 * Tests Recipe's equals() and hashCode() implementations.
	 */
	@Test
	public void testRecipeEqualsAndHashCode() {
		assertTrue(recipe1.equals(recipe1));           // self-reference branch
		assertFalse(recipe1.equals(null));              // null branch
		assertFalse(recipe1.equals("Coffee"));           // class-mismatch branch
		assertFalse(recipe1.equals(recipe2));            // different name, both non-null

		Recipe sameNameRecipe = new Recipe("Coffee", 999, new ArrayList<>());
		assertTrue(recipe1.equals(sameNameRecipe));      // same name -> equal
		assertEquals(recipe1.hashCode(), sameNameRecipe.hashCode());

		Recipe nullName1 = new Recipe();
		nullName1.setName(null);
		Recipe nullName2 = new Recipe();
		nullName2.setName(null);

		assertTrue(nullName1.equals(nullName2));         // name==null, other.name==null
		assertFalse(nullName1.equals(recipe1));          // name==null, other.name!=null
	}

	/**
	 * Tests Recipe's toString(), addIngredient(), and the all-args constructor.
	 */
	@Test
	public void testRecipeToStringAndAddIngredient() {
		assertEquals("Coffee", recipe1.toString());

		Ingredient sugar = new Ingredient("Sugar", 3);
		testEntityManager.persist(sugar);
		recipe1.addIngredient(sugar);
		assertEquals(3, findByName(recipe1.getIngredients(), "Sugar").getAmount());

		Recipe fullRecipe = new Recipe(5L, "Espresso", 200, new ArrayList<>());
		assertAll("All-args constructor",
				() -> assertEquals(5L, fullRecipe.getId()),
				() -> assertEquals("Espresso", fullRecipe.getName()),
				() -> assertEquals(200, fullRecipe.getPrice()));
	}

}