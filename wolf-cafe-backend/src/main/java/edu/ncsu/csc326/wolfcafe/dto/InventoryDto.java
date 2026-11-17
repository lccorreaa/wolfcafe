package edu.ncsu.csc326.wolfcafe.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * Used to transfer Inventory data between the client and server.  
 * This class will serve as the response in the REST API.
 *
 * @author CSC 326 Course Staff
 */
public class InventoryDto {
	
	/** id for inventory entry */
    private Long    id;

	/** ingredients in the inventory */
	private List<IngredientDto> ingredients;

	/** items in the inventory */
	private List<ItemDto> items = new ArrayList<>();

    /** 
     * Default InventoryDto constructor.
     */
    public InventoryDto() {
		this.ingredients = new ArrayList<>();
    }
    
    /**
     * Constructs an InventoryDto object from field values.
     * @param id inventory id
     */
	public InventoryDto(Long id) {
		super();
		this.id = id;
		this.ingredients = new ArrayList<>();
	}

	/**
	 * Adds an ingredient to the inventory.
	 * @param ingredient the ingredient to add
	 */
	public void addIngredient(IngredientDto ingredient){
		ingredients.add(ingredient);
	}

	/**
	 * Gets the ingredients in the inventory.
	 * @return the ingredients
	 */
	public List<IngredientDto> getIngredients(){
		return ingredients;
	}

	/**
	 * Sets the ingredients in the inventory.
	 * @param ingredients the ingredients to set
	 */
	public void setIngredients(List<IngredientDto> ingredients){
		this.ingredients = ingredients;
	}

	/**
	 * Gets the inventory id.
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Inventory id to set.
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * getter method for the items list
	 * @return a list of ItemDto objects
	 */
	public List<ItemDto> getItems() { return items; }

	/**
	 * setter method for the items list
	 * @param items the new List of ItemDto objects
	 */
	public void setItems(List<ItemDto> items) { this.items = items; }
}
