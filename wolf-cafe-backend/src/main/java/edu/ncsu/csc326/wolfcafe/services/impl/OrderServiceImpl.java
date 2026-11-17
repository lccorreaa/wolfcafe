package edu.ncsu.csc326.wolfcafe.services.impl;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.mapper.OrderMapper;
import edu.ncsu.csc326.wolfcafe.repositories.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repositories.OrderRepository;
import edu.ncsu.csc326.wolfcafe.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements the operations for viewing and fulfilling orders.
 *
 * @author Shreya Kamath
 */
@Service
public class OrderServiceImpl implements OrderService {

    /** repository for orders */
    @Autowired
    private OrderRepository orderRepository;

    /** repository for item */
    @Autowired
    private ItemRepository itemRepository;

    /**
     * generates the list of all orders with status PENDING
     * AI was used to make sure the syntax with stream and mapping was correct
     * @return the list of OrderDto objects that have a matching status
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING).stream().map(OrderMapper::mapToOrderDto).toList();
    }

    /**
     * checks the required inventory before fulfilling the order
     * AI was used for explanation and syntax
     * @param id the id of the order being fulfilled
     * @return the fulfilled order information
     */
    @Override
    @Transactional
    public OrderDto fulfillOrder(Long id) {
        Order order = orderRepository.findForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new WolfCafeAPIException(HttpStatus.CONFLICT, "Order is not pending status.");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new WolfCafeAPIException(HttpStatus.CONFLICT, "Order does not have any line items.");
        }
        //process IDs in a consistent order
        Map<Long, Long> reqAmts = new TreeMap<>();
        for (OrderLine line : order.getItems()) {
            if (line == null || line.getItemId() == null || line.getAmount() == null || line.getAmount() <= 0) {
                throw new WolfCafeAPIException(HttpStatus.CONFLICT, "Order contains an invalid item or amount.");
            }
            if (reqAmts.containsKey(line.getItemId())) {
                throw new WolfCafeAPIException(HttpStatus.CONFLICT, "Order cannot contain duplicate item IDs.");
            }
            reqAmts.put(line.getItemId(), line.getAmount().longValue());
        }

        Map<Long, Item> inventoryItems = new TreeMap<>();
        //lock and check every item before changing amounts
        for (Map.Entry<Long, Long> entry : reqAmts.entrySet()) {
            Item item = itemRepository.findForUpdate(entry.getKey())
                    .orElseThrow(() -> new WolfCafeAPIException
                            (HttpStatus.CONFLICT, "Ordered item is no longer available: " + entry.getKey()));
            if (item.getAmount() < entry.getValue()) {
                throw new WolfCafeAPIException(HttpStatus.CONFLICT, "Insufficient inventory for " + item.getName());
            }
            inventoryItems.put(item.getId(), item);
        }

        //remove the requested amounts
        for (Map.Entry<Long, Long> entry : reqAmts.entrySet()) {
            Item item = inventoryItems.get(entry.getKey());
            item.setAmount(item.getAmount() - entry.getValue().intValue());
        }
        itemRepository.saveAll(inventoryItems.values());
        order.setStatus(OrderStatus.FULFILLED);
        order.setFulfilledAt(Instant.now());
        Order savedOrder = orderRepository.save(order);
        return OrderMapper.mapToOrderDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        // Return error if order is null
        if (orderDto == null) {
            throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST, "Order data is required.");
        }

        // Return error if order has null/empty id or name
        if (orderDto.getCustomerName() == null
                || orderDto.getCustomerName().isBlank()) {
            throw new WolfCafeAPIException(
                    HttpStatus.BAD_REQUEST,
                    "Customer ID and name are required."
            );
        }

        // Return error if any of items in order are null or list is empty
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new WolfCafeAPIException(
                    HttpStatus.BAD_REQUEST,
                    "An order must contain at least one item."
            );
        }

        Order order = OrderMapper.mapToOrder(orderDto);
        //Always let the database assign the id of a new item
        order.setId(null);
        order.setFulfilledAt(null);
        order.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);
        return OrderMapper.mapToOrderDto(savedOrder);
    }

    @Override
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        // If Order with id is NOT found return `null`
        if (order == null) return null;
        return OrderMapper.mapToOrderDto(order);
    }

    /**
     * Update an Order
     * @param id id of order to change
     * @param orderDto What order should be changed to
     * @return the updated order
     */
    @Override
    @Transactional
    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order with given id doesn't exist."));

        Order updatedOrder = OrderMapper.mapToOrder(orderDto);
        updatedOrder.setId(order.getId());

        // Ensure Hibernate receives a mutable collection.
        updatedOrder.setItems(new ArrayList<>(updatedOrder.getItems()));

        Order savedUpdatedOrder = orderRepository.save(updatedOrder);
        return OrderMapper.mapToOrderDto(savedUpdatedOrder);
    }

    /**
     * Remove an Order from the db
     * @param id id of order to be removed
     */
    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

}
