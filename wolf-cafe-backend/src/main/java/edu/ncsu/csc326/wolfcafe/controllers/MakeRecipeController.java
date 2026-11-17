package edu.ncsu.csc326.wolfcafe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.dto.RecipeDto;
import edu.ncsu.csc326.wolfcafe.services.InventoryService;
import edu.ncsu.csc326.wolfcafe.services.MakeRecipeService;
import edu.ncsu.csc326.wolfcafe.services.RecipeService;

/**
 * MakeRecipeController provides the endpoint for making a recipe (UC6).
 *
 * @author CSC 326 Course Staff
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/makerecipe")
public class MakeRecipeController {

	/** Connection to InventoryService */
	@Autowired
	private InventoryService inventoryService;

	/** Connection to RecipeService */
	@Autowired
	private RecipeService recipeService;

	/** Connection to MakeRecipeService */
	@Autowired
	private MakeRecipeService makeRecipeService;


    /**
     * REST API method to make coffee by completing a POST request with the name
     * of the recipe as the path variable and the amount that has been paid as
     * the body of the request.
     *
     * The payment is read as raw text rather than bound to an Integer, because
     * Jackson would silently truncate a payment such as 3.5 to 3.  Following
     * UC6, the ingredients are checked before the payment, and no inventory is
     * used unless both checks pass.
     *
     * @param recipeName
     *            recipe name
     * @param amtPaid
     *            amount paid
     * @return 200 with the change if successful; 400 with the payment returned
     *         if there are not enough ingredients; 409 with the payment returned
     *         if the payment is less than the price; 400 with no body if the
     *         payment is not a non-negative integer; 404 if there is no recipe
     *         with the given name
     */
	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping("{name}")
	public ResponseEntity<Integer> makeRecipe(@PathVariable("name") String recipeName,
			@RequestBody(required = false) String amtPaid) {
		Integer payment = parsePayment(amtPaid);
		if (payment == null) {
			// [Invalid Payment]
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		RecipeDto recipeDto = recipeService.getRecipeByName(recipeName);
		InventoryDto inventoryDto = inventoryService.getInventory();

		if (!makeRecipeService.enoughIngredients(inventoryDto, recipeDto)) {
			// [Insufficient Ingredients]
			return new ResponseEntity<>(payment, HttpStatus.BAD_REQUEST);
		}
		if (payment < recipeDto.getPrice()) {
			// [Insufficient Payment]
			return new ResponseEntity<>(payment, HttpStatus.CONFLICT);
		}
		if (!makeRecipeService.makeRecipe(inventoryDto, recipeDto)) {
			// Inventory changed between the check and the purchase
			return new ResponseEntity<>(payment, HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(payment - recipeDto.getPrice());
	}

	/**
	 * Parses the request body as a payment.  Accepts a bare JSON number (5) or a
	 * JSON string holding one ("5"), since the frontend sends the text field's
	 * value as-is.
	 *
	 * @param body raw request body
	 * @return the payment, or null if it is missing, not numeric, not an
	 *         integer, negative, or too large
	 */
	private static Integer parsePayment(String body) {
		if (body == null) {
			return null;
		}
		String text = body.trim();
		if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
			text = text.substring(1, text.length() - 1).trim();
		}
		if (!text.matches("\\d+")) {
			return null;
		}
		try {
			return Integer.valueOf(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
