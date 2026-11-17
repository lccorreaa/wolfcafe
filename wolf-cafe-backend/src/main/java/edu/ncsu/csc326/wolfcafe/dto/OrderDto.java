package edu.ncsu.csc326.wolfcafe.dto;

import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * the information that can be returned from the API for Order
 *
 * @author Shreya Kamath
 */
public class OrderDto {

    /** order id */
    @Getter
    @Setter
    private Long id;

    /** id of the customer who placed the order */
    @Getter
    @Setter
    private Long customerId;

    /** name of the customer who placed the order */
    @Getter
    @Setter
    private String customerName;

    /** list of line items in the order */
    @Getter
    @Setter
    private List<OrderLine> items = new ArrayList<>();

    /** status of the order */
    @Getter
    @Setter
    private OrderStatus status;

    /** time the order was fulfilled */
    @Getter
    @Setter
    private Instant fulfilledAt;

    /**
     * Empty Constructor for DTO-Entity mapping
     */
    public OrderDto() {

    }

    /**
     * Create a new OrderDto in Testing
     * @param customerName Name of customer
     * @param orders list of orders
     */
    public OrderDto(String customerName, List<OrderLine> orders) {
        this.items = orders;
        this.customerName = customerName;
    }
}
