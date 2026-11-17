package edu.ncsu.csc326.wolfcafe.mapper;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderLine;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Converts between OrderDto and Order entity.
 *
 * @author Shreya Kamath
 */
public class OrderMapper {
    /**
     * converts an Order into an OrderDto
     * @param order the Order to convert
     * @return OrderDto information
     */
    public static OrderDto mapToOrderDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setCustomerId(order.getCustomerId());
        orderDto.setCustomerName(order.getCustomerName());
        orderDto.setStatus(order.getStatus());
        orderDto.setFulfilledAt(order.getFulfilledAt());

        //AI-generated to ensure syntax is correct
        orderDto.setItems(order.getItems().stream()
                .map(line -> new OrderLine(
                        line.getItemId(),
                        line.getItemName(),
                        line.getAmount()))
                .toList());

        return orderDto;
    }

    /**
     * Converts an OrderDto into an Order
     * @param orderDto the OrderDto to convert
     * @return Order information
     */
    public static Order mapToOrder(OrderDto orderDto) {
        Order order = new Order();
        order.setId(orderDto.getId());
        order.setCustomerId(orderDto.getCustomerId());
        order.setCustomerName(orderDto.getCustomerName());
        order.setStatus(orderDto.getStatus());
        order.setFulfilledAt(orderDto.getFulfilledAt());

        order.setItems(orderDto.getItems().stream()
                .map(line -> new OrderLine(
                        line.getItemId(),
                        line.getItemName(),
                        line.getAmount()))
                .collect(Collectors.toCollection(ArrayList::new)));

        return order;
    }
}
