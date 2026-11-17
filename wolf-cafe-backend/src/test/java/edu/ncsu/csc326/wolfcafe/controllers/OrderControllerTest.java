package edu.ncsu.csc326.wolfcafe.controllers;

import static org.junit.jupiter.api.Assertions.*;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.services.InventoryService;
import edu.ncsu.csc326.wolfcafe.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.services.ItemService;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the ItemController REST API through MockMvc, with the whole application
 * running against the test database.  ItemControllerWebMvcTest tests the same
 * controller with a mocked service; see that class for when to use each.
 *
 * {@code @WithMockUser} runs a test as a logged in user with the given role, without
 * needing a real account or login token.  A test with no @WithMockUser runs as a
 * user who has not logged in.  AuthControllerTest shows a real login token in use.
 *
 * @author CSC 326 Course Staff
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerTest {

    /** API path */
    private static final String API_PATH = "/api/orders";

    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc mvc;

    /** Used to create an item for the tests to work with */
    @Autowired
    private OrderService orderService;

    /** Used to create items for the tests to work with */
    @Autowired
    private ItemService itemService;

    /** Reference to EntityManager */
    @Autowired
    private EntityManager entityManager;

    /** Item created for each test */
    private ItemDto coffee;

    /** Used to set up inventory for the tests */
    @Autowired
    private InventoryService inventoryService;

    /**
     * Sets up the test case with a single item.
     * @throws java.lang.Exception if error
     */
    @BeforeEach
    public void setUp() throws Exception {
        coffee = itemService.addItem(new ItemDto(null, "Coffee", "Coffee is life", 3.25));

        assertNotNull(coffee.getId(), "setUp must create Coffee with an ID");
    }

    /**
     * Test creating an order
     */
    @WithMockUser(username = "staff", roles = "STAFF")
    @Test
    void createOrderTest() {
        List<OrderLine> items = List.of(
                new OrderLine(coffee.getId(), coffee.getName(), 1)
        );

        OrderDto request = new OrderDto("Jensen Huang", items);
        request.setId(999L); // createOrder should ignore client-supplied IDs
        request.setStatus(OrderStatus.FULFILLED);
        request.setFulfilledAt(Instant.now());

        OrderDto created = orderService.createOrder(request);

        assertNotNull(created.getId());
        assertEquals("Jensen Huang", created.getCustomerName());
        assertEquals(OrderStatus.PENDING, created.getStatus());
        assertNull(created.getFulfilledAt());
        assertEquals(1, created.getItems().size());
        assertEquals(coffee.getId(), created.getItems().get(0).getItemId());
        assertEquals(1, created.getItems().get(0).getAmount());
    }

    /**
     * Test updating an order in the db
     */
    @WithMockUser(username = "staff", roles = "STAFF")
    @Test
    void updateOrderTest() {
        OrderDto originalOrder = orderService.createOrder(
                new OrderDto(
                        "Jensen Huang",
                        new ArrayList<>(List.of(
                                new OrderLine(coffee.getId(), coffee.getName(), 1)
                        ))
                )
        );

        OrderDto updateRequest = new OrderDto(
                "Jordan Lee",
                new ArrayList<>(List.of(
                        new OrderLine(coffee.getId(), coffee.getName(), 2)
                ))
        );

        OrderDto updatedOrder =
                orderService.updateOrder(originalOrder.getId(), updateRequest);

        assertNotNull(updatedOrder);
        assertEquals(originalOrder.getId(), updatedOrder.getId());
        assertEquals("Jordan Lee", updatedOrder.getCustomerName());
        assertEquals(1, updatedOrder.getItems().size());
        assertEquals(coffee.getId(), updatedOrder.getItems().get(0).getItemId());
        assertEquals(2, updatedOrder.getItems().get(0).getAmount());
    }

    /**
     * Test deleting an order from the db
     */
    @Test
    void deleteOrderTest() {
        OrderDto createdOrder = orderService.createOrder(
                new OrderDto(
                        "Jensen Huang",
                        List.of(new OrderLine(coffee.getId(), coffee.getName(), 1))
                )
        );

        Long orderId = createdOrder.getId();
        assertNotNull(orderService.getOrderById(orderId));

        orderService.deleteOrder(orderId);

        assertNull(orderService.getOrderById(orderId));
    }

}
