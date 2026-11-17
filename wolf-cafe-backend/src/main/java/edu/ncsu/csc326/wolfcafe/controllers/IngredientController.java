package edu.ncsu.csc326.wolfcafe.controllers;

import edu.ncsu.csc326.wolfcafe.dto.IngredientDto;
import edu.ncsu.csc326.wolfcafe.services.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Ingredients
 *
 * @author Sandra Jose (sajose)
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    /** Connection to IngredientService */
    @Autowired
    private IngredientService ingredientService;

    /**
     * Getter method for ingredient
     * @param id of ingredient
     * @return ingredient dto object
     */
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("{id}")
    public ResponseEntity<IngredientDto> getIngredient(@PathVariable("id") Long id) {
        IngredientDto ingredientDto = ingredientService.getIngredientById(id);
        return ResponseEntity.ok(ingredientDto);
    }

    /**
     * REST API method to create ingredient completing a POST request with the ID
     * of the ingredient as the path variable
     * @param ingredientDto the ingredient to create
     * @return the created ingredient
     */
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    public ResponseEntity<IngredientDto> createIngredient(@RequestBody IngredientDto ingredientDto) {
        IngredientDto savedIngredientDto = ingredientService.createIngredient(ingredientDto);
        return ResponseEntity.ok(savedIngredientDto);
    }
}
