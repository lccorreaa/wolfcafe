package edu.ncsu.csc326.wolfcafe.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests the Order class constructors and generated accessors.
 *
 * @author Lucas Correa (lccorrea)
 */
class OrderTest {

    /**
     * Test empty constructor of Order used by mapper classes
     */
    @Test
    void testNoArgumentConstructorAndSetters() {
        Order order = new Order();
        List<OrderLine> items = new ArrayList<>();
        items.add(new OrderLine(7L, "Latte", 2));
        Instant fulfilledAt = Instant.parse("2026-01-01T12:00:00Z");

        order.setId(15L);
        order.setCustomerId(42L);
        order.setCustomerName("Jensen Huang");
        order.setStatus(OrderStatus.FULFILLED);
        order.setFulfilledAt(fulfilledAt);
        order.setItems(items);

        assertAll("order properties",
                () -> assertEquals(15L, order.getId()),
                () -> assertEquals(42L, order.getCustomerId()),
                () -> assertEquals("Jensen Huang", order.getCustomerName()),
                () -> assertEquals(OrderStatus.FULFILLED, order.getStatus()),
                () -> assertEquals(fulfilledAt, order.getFulfilledAt()),
                () -> assertEquals(items, order.getItems()));
    }

    /**
     * Test constructors of Order entity
     */
    @Test
    void testConstructors() {
        List<OrderLine> items = new ArrayList<>(
                List.of(new OrderLine(5L, "Americano", 1))
        );

        Order order = new Order("Jensen Huang", items);

        assertAll("constructor values",
                () -> assertEquals("Jensen Huang", order.getCustomerName()),
                () -> assertEquals(OrderStatus.PENDING, order.getStatus()),
                () -> assertNull(order.getFulfilledAt()),
                () -> assertEquals(items, order.getItems()));
    }
}