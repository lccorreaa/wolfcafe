package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests OrderServiceImpl against the test database.
 * Each test runs in a transaction that is rolled back afterward.
 * (This class was created with the help of an AI agent)
 *
 * @author Lucas Correa (lccorrea)
 */
@SpringBootTest
@Transactional
class OrderServiceTest {

    /** Reference to order service */
    @Autowired
    private OrderService orderService;

    /** Reference to entity manager */
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
    }

    private OrderDto createOrder(String customerName, int amount) {
        OrderDto request = new OrderDto(
                customerName,
                new ArrayList<>(List.of(
                        new OrderLine(7L, "Coffee", amount)
                ))
        );

        return orderService.createOrder(request);
    }

    @Test
    void createOrderTest() {
        OrderDto created = createOrder("Jensen Huang", 2);

        assertNotNull(created.getId());
        assertEquals("Jensen Huang", created.getCustomerName());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertNull(created.getFulfilledAt());
        assertEquals(1, created.getItems().size());
        assertEquals(7L, created.getItems().get(0).getItemId());
        assertEquals("Coffee", created.getItems().get(0).getItemName());
        assertEquals(2, created.getItems().get(0).getAmount());

        entityManager.flush();
        entityManager.clear();

        OrderDto retrieved = orderService.getOrderById(created.getId());
        assertNotNull(retrieved);
        assertEquals("Jensen Huang", retrieved.getCustomerName());
        assertEquals(OrderStatus.PENDING, retrieved.getStatus());
    }

    @Test
    void getPendingOrdersTest() {
        OrderDto created = createOrder("Jensen Huang", 1);

        List<OrderDto> pendingOrders = orderService.getPendingOrders();

        assertEquals(1, pendingOrders.size());
        assertEquals(created.getId(), pendingOrders.get(0).getId());
        assertEquals("Jensen Huang", pendingOrders.get(0).getCustomerName());
        assertEquals(OrderStatus.PENDING, pendingOrders.get(0).getStatus());
    }

    @Test
    void updateOrderTest() {
        OrderDto original = createOrder("Jensen Huang", 1);

        OrderDto updateRequest = new OrderDto(
                "Jordan Lee",
                new ArrayList<>(List.of(
                        new OrderLine(7L, "Coffee", 2)
                ))
        );

        OrderDto updated = orderService.updateOrder(original.getId(), updateRequest);

        assertEquals(original.getId(), updated.getId());
        assertEquals("Jordan Lee", updated.getCustomerName());
        assertEquals(1, updated.getItems().size());
        assertEquals(7L, updated.getItems().get(0).getItemId());
        assertEquals(2, updated.getItems().get(0).getAmount());
    }

    @Test
    void deleteOrderTest() {
        OrderDto created = createOrder("Jensen Huang", 1);

        orderService.deleteOrder(created.getId());

        assertNull(orderService.getOrderById(created.getId()));
    }
}
