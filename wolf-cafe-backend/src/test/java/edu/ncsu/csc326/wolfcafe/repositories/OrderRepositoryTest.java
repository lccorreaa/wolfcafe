package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Tests OrderRepository against the test database.
 * @author Lucas Correa (lccorrea)
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderRepositoryTest {

    /** Reference to order repository */
    @Autowired
    private OrderRepository orderRepository;

    /** Entity manager for setting up test data */
    @Autowired
    private TestEntityManager testEntityManager;

    /** Order with status PENDING */
    private Order pendingOrder;
    /** Order with status FULFILLED */
    private Order fulfilledOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        pendingOrder = new Order();
        pendingOrder.setCustomerId(42L);
        pendingOrder.setCustomerName("Jensen Huang");
        pendingOrder.setStatus(OrderStatus.PENDING);
        pendingOrder.setItems(new ArrayList<>(List.of(
                new OrderLine(7L, "Coffee", 2)
        )));
        pendingOrder = orderRepository.save(pendingOrder);

        fulfilledOrder = new Order();
        fulfilledOrder.setCustomerId(43L);
        fulfilledOrder.setCustomerName("Jordan Lee");
        fulfilledOrder.setStatus(OrderStatus.FULFILLED);
        fulfilledOrder.setItems(new ArrayList<>(List.of(
                new OrderLine(8L, "Latte", 1)
        )));
        fulfilledOrder = orderRepository.save(fulfilledOrder);

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    void testFindByStatus() {
        List<Order> fetched = orderRepository.findByStatus(OrderStatus.PENDING);

        assertEquals(1, fetched.size());
        assertAll("Pending order",
                () -> assertEquals("Jensen Huang", fetched.get(0).getCustomerName()),
                () -> assertEquals(OrderStatus.PENDING, fetched.get(0).getStatus()),
                () -> assertEquals(7L, fetched.get(0).getItems().get(0).getItemId()),
                () -> assertEquals(2, fetched.get(0).getItems().get(0).getAmount()));
    }

    @Test
    void testFindByStatusWithNoMatches() {
        assertTrue(orderRepository.findByStatus(null).isEmpty());
    }

    @Test
    void testFindById() {
        Order fetched = orderRepository.findById(pendingOrder.getId()).orElseThrow();

        assertAll("Order contents",
                () -> assertEquals("Jensen Huang", fetched.getCustomerName()),
                () -> assertEquals(42L, fetched.getCustomerId()),
                () -> assertEquals(OrderStatus.PENDING, fetched.getStatus()),
                () -> assertEquals("Coffee", fetched.getItems().get(0).getItemName()));
    }

    @Test
    void testFindAll() {
        assertEquals(2, orderRepository.findAll().size());
    }
}