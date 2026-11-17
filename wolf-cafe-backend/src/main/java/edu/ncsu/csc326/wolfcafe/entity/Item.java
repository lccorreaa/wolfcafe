package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an item for sale in the WolfCafe.
 *
 * @author CSC 326 Course Staff
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

	/** Item id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Item name */
    @Column(nullable = false, unique = true)
    private String name;
    
    /** Item description */
    private String description;
    
    /** Item price */
    @Column(nullable = false)
    private double price;

    /** Item amount */
    private int amount;

    /**
     * Creates an item without an initial inventory amount. (Constructor)
     *
     * @param id item id
     * @param name item name
     * @param description item description
     * @param price item price
     */
    public Item(Long id, String name, String description, double price) {
        this(id, name, description, price, 1);
    }

    /**
     * Creates an item without an id. The database assigns the id when the item
     * is persisted.
     *
     * @param name item name
     * @param description item description
     * @param price item price
     * @param amount initial inventory amount
     */
    public Item(String name, String description, double price, int amount) {
        this(null, name, description, price, amount);
    }

}
