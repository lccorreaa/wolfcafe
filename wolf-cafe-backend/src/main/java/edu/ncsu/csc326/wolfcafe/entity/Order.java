package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order placed by a customer in the WolfCafe.
 * @author Shreya Kamath
 */
@Entity
@Table(name = "orders")
public class Order {

    /** id for the order; GeneratedValue creates a unique ID when saved */
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** id for the customer who placed the order */
    @Getter
    @Setter
    private Long customerId;

    /** name of the customer who placed the order */
    @Getter
    @Setter
    private String customerName;

    /** the list of all order line items in the order */
    @ElementCollection
    @Getter
    @Setter
    private List<OrderLine> items = new ArrayList<>();

    /** set the status of the order to PENDING */
    @Getter
    @Setter
    private OrderStatus status = OrderStatus.PENDING;

    /** the time the order is fulfilled */
    @Getter
    @Setter
    private Instant fulfilledAt;

    /** empty constructor */
    public Order() {

    }

    /**
     * creates an order for a customer with the given items
     * @param customerName name of the customer placing the order
     * @param orders the items in the order
     */
    public Order(String customerName, List<OrderLine> orders) {
        this.items = orders;
        this.customerName = customerName;
    }

}
