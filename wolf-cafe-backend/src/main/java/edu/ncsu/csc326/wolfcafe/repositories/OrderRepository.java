package edu.ncsu.csc326.wolfcafe.repositories;

import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Operations for the Order entity.
 *
 * @author Shreya Kamath
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * finds any orders with the specified status
     * @param status the status for all resulting orders
     * @return the List of Order objects that have a matching status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * AI suggested: locks an order while it is being fulfilled so there's no a chance of duplicate fulfillment
     * @param id the order ID
     * @return the matching order if it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findForUpdate(@Param("id") Long id);

    /**
     * finds any orders placed by the specified customer
     * @param customerName the name of the customer
     * @return the List of Order objects placed by the customer
     */
    @Query("SELECT o FROM Order o WHERE o.customerName = :customerName")
    List<Order> findByCustomerName(String customerName);
}
