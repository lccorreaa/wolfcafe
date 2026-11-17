package edu.ncsu.csc326.wolfcafe.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.config.AuditLogger;
import edu.ncsu.csc326.wolfcafe.config.SpringSecurityConfig;
import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.security.JwtAuthenticationEntryPoint;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;
import edu.ncsu.csc326.wolfcafe.services.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Class for testing the orders API endpoints
 *
 * @author Lucas Correa (lccorrea)
 */
@WebMvcTest(OrderController.class)
@Import({ SpringSecurityConfig.class, JwtAuthenticationEntryPoint.class, AuditLogger.class })
@ExtendWith(OutputCaptureExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class OrderControllerWebMvcTest {

    /** Path to the orders API */
    private static final String API_PATH = "/api/orders";

    /** Mock MVC for testing controller */
    @Autowired
    private MockMvc mvc;

    /** Mocked order service */
    @MockitoBean
    private OrderService orderService;

    /** Needed by JwtAuthenticationFilter; unused because tests send no token. */
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    /** Needed by JwtAuthenticationFilter; unused because tests send no token. */
    @MockitoBean
    private UserDetailsService userDetailsService;

    /**
     * Tests that a customer can create an order.
     * @throws Exception if error
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testCreateOrder() throws Exception {
        OrderDto request = new OrderDto(
                "Jensen Huang",
                List.of(new OrderLine(7L, "Coffee", 2))
        );

        when(orderService.createOrder(any(OrderDto.class))).thenReturn(request);

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Jensen Huang"))
                .andExpect(jsonPath("$.items[0].itemId").value(7))
                .andExpect(jsonPath("$.items[0].itemName").value("Coffee"))
                .andExpect(jsonPath("$.items[0].amount").value(2));

        verify(orderService).createOrder(any(OrderDto.class));
    }

    /**
     * Tests that creating an order as a customer is not audit logged.
     * @throws Exception if error
     * @param output output
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testCreateOrderAsCustomerIsNotAuditLogged(CapturedOutput output) throws Exception {
        OrderDto request = new OrderDto(
                "Jensen Huang",
                List.of(new OrderLine(7L, "Coffee", 2))
        );

        when(orderService.createOrder(any(OrderDto.class))).thenReturn(request);

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                .andExpect(status().isOk());

        // createOrder currently does not write to AuditLogger.
        verify(orderService).createOrder(any(OrderDto.class));
    }

    /**
     * Tests that a user who is not logged in cannot create an order.
     * @throws Exception if error
     */
    @Test
    public void testCreateOrderNotLoggedIn() throws Exception {
        OrderDto request = new OrderDto(
                "Jensen Huang",
                List.of(new OrderLine(7L, "Coffee", 2))
        );

        mvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    /**
     * Tests that staff can get the pending orders.
     * @throws Exception if error
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testGetPendingOrders() throws Exception {
        OrderDto pendingOrder = new OrderDto(
                "Jensen Huang",
                List.of(new OrderLine(7L, "Coffee", 2))
        );
        pendingOrder.setId(15L);
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(orderService.getPendingOrders()).thenReturn(List.of(pendingOrder));

        mvc.perform(get(API_PATH).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15))
                .andExpect(jsonPath("$[0].customerName").value("Jensen Huang"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].items[0].itemName").value("Coffee"))
                .andExpect(jsonPath("$[0].items[0].amount").value(2));

        verify(orderService).getPendingOrders();
    }

    /**
     * Tests that a customer cannot get the pending orders.
     * @throws Exception if error
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testCustomerCannotGetPendingOrders() throws Exception {
        mvc.perform(get(API_PATH))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    /**
     * Tests that staff can fulfill an order.
     * @throws Exception if error
     */
    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    public void testFulfillOrder() throws Exception {
        OrderDto fulfilledOrder = new OrderDto(
                "Jensen Huang",
                List.of(new OrderLine(7L, "Coffee", 2))
        );
        fulfilledOrder.setId(15L);
        fulfilledOrder.setStatus(OrderStatus.FULFILLED);
        fulfilledOrder.setFulfilledAt(Instant.parse("2026-01-01T12:00:00Z"));

        when(orderService.fulfillOrder(15L)).thenReturn(fulfilledOrder);

        mvc.perform(post(API_PATH + "/15/fulfill")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.customerName").value("Jensen Huang"))
                .andExpect(jsonPath("$.status").value("FULFILLED"))
                .andExpect(jsonPath("$.items[0].itemName").value("Coffee"));

        verify(orderService).fulfillOrder(15L);
    }

    /**
     * Tests that a customer cannot fulfill an order.
     * @throws Exception if error
     */
    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    public void testCustomerCannotFulfillOrder() throws Exception {
        mvc.perform(post(API_PATH + "/15/fulfill"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }
}