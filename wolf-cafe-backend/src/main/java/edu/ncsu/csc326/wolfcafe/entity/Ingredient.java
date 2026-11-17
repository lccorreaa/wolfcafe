package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

/**
 * Ingredient for the coffee maker. Ingredient is a Data Access Object (DAO) tied to the database
 * using Hibernate libraries. IngredientRepository provides the methods for database CRUD operations.
 *
 * @author Shion Strickland
 */
@Entity
@Table(name = "ingredients")
public class Ingredient {

    /** Ingredient id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ingredient name */
    @Column(nullable = false)
    private String name;

    /** Initial/current inventory amount for this ingredient. */
    private Integer amount;

    /**
     * Creates a default ingredient for the coffee maker.
     */
    public Ingredient() {
        this.name = "";
        this.amount = 0;
    }

    /**
     * Creates an ingredient from all the fields.
     *
     * @param id ingredient id
     * @param name ingredient name
     */
    public Ingredient(Long id, String name) {
        this.id = id;
        this.name = name;
        this.amount = 0;
    }

    /**
     * Creates an ingredient from its name.
     *
     * @param name ingredient name
     */
    public Ingredient(String name) {
        this.name = name;
        this.amount = 0;
    }

    /**
     * Creates an ingredient with its initial inventory amount.
     *
     * @param name ingredient name
     * @param amount initial amount
     */
    public Ingredient(String name, Integer amount) {
        this.name = name;
        this.amount = amount;
    }

    /**
     * Get the ID of the Ingredient
     *
     * @return the ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID of the Ingredient (Used by Hibernate)
     *
     * @param id the ID
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Returns the name of the ingredient.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the ingredient name.
     *
     * @param name The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the amount of the ingredient.
     * @return Returns the amount.
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Sets the amount of the ingredient.
     * @param amount The amount to set.
     */
    public void setAmount(final Integer amount) {
        this.amount = amount;
    }

    /**
     * Returns the name of the ingredient.
     *
     * @return String
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        Integer result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Ingredient other = (Ingredient) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
