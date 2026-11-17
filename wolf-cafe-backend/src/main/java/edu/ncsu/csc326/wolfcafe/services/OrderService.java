package edu.ncsu.csc326.wolfcafe.services;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;

import java.util.List;

/**
 * Operations for viewing and fulfilling orders.
 *
 * @author Shreya Kamath
 */
public interface OrderService {

    /**
     * generates a list of all orders with status PENDING
     * @return the list of OrderDto objects with status PENDING
     */
    List<OrderDto> getPendingOrders();

    /**
     * fulfills an order using the available inventory
     * @param id the id of the order being fulfilled
     * @return the fulfilled order
     */
    OrderDto fulfillOrder(Long id);

    /**
     * Create an order
     * @param orderDto Order to be created
     * @return The created order
     */
    OrderDto createOrder(OrderDto orderDto);

    /**
     * Return the order with the given id
     * @param id id to search for
     * @return Order with the given id if it exists
     */
    OrderDto getOrderById(Long id);

    /**
     * Update order information
     * @param id id of order to change
     * @param orderDto What order should be changed to
     * @return Updated order
     */
    OrderDto updateOrder(Long id, OrderDto orderDto);

    /**
     * Delete order from the db
     * @param id id of order to be removed
     */
    void deleteOrder(Long id);
}
