package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Line item for an order containing many different items.
 *
 * @author Shreya Kamath
 */
@Embeddable
public class OrderLine {
    /** the id for the specific item */
    @Getter
    @Setter
    private Long itemId;
    /** the name for the specific item */
    @Getter
    @Setter
    private String itemName;
    /** the amount of the item in the order (not in the inventory) */
    @Getter
    @Setter
    private Integer amount;

    /** empty constructor */
    public OrderLine() {

    }

    /**
     * Constructs a line item with all three fields.
     * @param itemId the item id
     * @param itemName the item name
     * @param amount the amount ordered
     */
    public OrderLine(Long itemId, String itemName, Integer amount) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.amount = amount;
    }
}
