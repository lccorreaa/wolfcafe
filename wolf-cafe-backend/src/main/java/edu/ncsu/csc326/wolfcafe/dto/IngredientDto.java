package edu.ncsu.csc326.wolfcafe.dto;

/**
 * Used to transfer Ingredient data between the client and server.
 *
 * @author Sandra Jose (sajose)
 */
public class IngredientDto {
    /** primary key for the database */
    private Long id;
    /** quantity of the ingredient */
    private Integer amount;

    /** name of the ingredient */
    private String name;

    /**
     * IngredientDto constructor that takes no arguments for Hibernate to use
     */
    public IngredientDto() {

    }

    /**
     * IngredientDto constructor that takes an ingredient type and amount
     * @param name name of ingredient
     * @param amount quantity of ingredient
     */
    public IngredientDto(String name, Integer amount) {
        this.name = name;
        this.amount = amount;
    }

    // Getters

    /**
     * Gets the ingredient id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the ingredient amount.
     * @return the amount
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Gets the ingredient name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    // Setters

    /**
     * Sets the ingredient id.
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the ingredient amount.
     * @param amount the amount to set
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * Sets the ingredient name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
