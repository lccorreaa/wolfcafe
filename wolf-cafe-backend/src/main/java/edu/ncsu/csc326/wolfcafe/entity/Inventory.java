package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
/**
 * Inventory for the coffee maker. Inventory is a Data Access Object (DAO) is tied to the database using
 * Hibernate libraries. InventoryRepository provides the methods for database CRUD operations.
 *
 * @author CSC 326 Course Staff
 * @author Sandra Jose (sajose)
 */
@Entity
public class Inventory {
	
	/** id for inventory entry */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long    id;

    /** Ingredients used in this recipe */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Ingredient> ingredients;
    
    /**
     * Empty constructor for Hibernate
     */
    public Inventory () {
        // Intentionally empty so that Hibernate can instantiate
        // Inventory object.
        this.ingredients = new ArrayList<>();
    }
    
    /**
     * Creates an Inventory with all fields
     * @param id inventory's id
     */
    public Inventory(Long id) {
		super();
		this.id = id;
        this.ingredients = new ArrayList<>();
	}

    /**
     * Add an ingredient to the inventory list
     * @param ingredient to add
     */
    public void addIngredient(Ingredient ingredient){
        ingredients.add(ingredient);
    }

    /**
     * Getter method for the inventory list
     * @return list of ingredients that are the inventory
     */
    public List<Ingredient> getIngredients(){
        return ingredients;
    }

    /**
     * Getter method for ingredient to search by id
     * @param id to search for in the inventory list
     * @return ingredient found from list
     */
    public Ingredient getIngredientById(Long id){
        for(Ingredient i : ingredients){
            if (id != null && id.equals(i.getId())) {
                return i;
            }
        }
        return null;
    }

    /**
     * Returns the ID of the entry in the DB
     *
     * @return long
     */
    public Long getId () {
        return id;
    }

    /**
     * Set the ID of the Inventory (Used by Hibernate)
     *
     * @param id
     *            the ID
     */
    public void setId ( final Long id ) {
        this.id = id;
    }

}
