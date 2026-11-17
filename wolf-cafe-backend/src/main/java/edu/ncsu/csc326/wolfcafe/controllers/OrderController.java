package edu.ncsu.csc326.wolfcafe.controllers;

import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * endpoints for viewing pending orders and fulfilling them
 *
 * @author Shreya Kamath
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /** service for order operations */
    @Autowired
    private OrderService orderService;

    /** audit logger for successful order operations */
    @Autowired
    private AuditLogger auditLogger;

    /**
     * generates the list of orders with status PENDING
     * @return the list of OrderDto objects
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrderDto>> getPendingOrders() {
        List<OrderDto> orders = orderService.getPendingOrders();
        auditLogger.log("viewed pending orders");
        return ResponseEntity.ok(orders);
    }

    /**
     * fulfills the specified order
     * AI was used for explanation and syntax
     * @param id the order id
     * @return the fulfilled order
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PostMapping("/{id}/fulfill")
    public ResponseEntity<OrderDto> fulfillOrder(@PathVariable("id") Long id) {
        OrderDto fulfilledOrder = orderService.fulfillOrder(id);
        String items = fulfilledOrder.getItems().stream()
                .map(line -> line.getAmount() + "x " + line.getItemName())
                .collect(Collectors.joining(", "));
        auditLogger.log("fulfilled order for customer " + fulfilledOrder.getCustomerName() +
                " with items: " + items + " and id: " + fulfilledOrder.getId());
        return ResponseEntity.ok(fulfilledOrder);
    }

    /**
     * creates a new order
     * @param orderDto the order to create
     * @return the created order
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'CUSTOMER')")
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        orderService.createOrder(orderDto);
        return ResponseEntity.ok(orderDto);
    }

    /**
     * updates the specified order
     * @param id the order id
     * @param orderDto the updated order information
     * @return the updated order
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'CUSTOMER')")
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(@RequestBody Long id, @RequestBody OrderDto orderDto) {
        orderService.updateOrder(id, orderDto);
        return ResponseEntity.ok(orderDto);
    }

    /**
     * deletes the specified order
     * @param id the order id
     * @return an empty response if the order was deleted
     */
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(@RequestBody Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }
}
