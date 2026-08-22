package edu.demart_api.repository;

import edu.demart_api.entity.Order;
import edu.demart_api.entity.OrderStatus;
import edu.demart_api.entity.FulfillmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Customer's own orders — JOIN FETCH items to avoid N+1 on the list view.
     * Ordered newest first.
     */
    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.product
            WHERE o.user.id = :userId
            ORDER BY o.createdAt DESC
            """)
    java.util.List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * Single order with items — owned by the given user (customer ownership check).
     */
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.product
            WHERE o.id = :orderId AND o.user.id = :userId
            """)
    Optional<Order> findByIdAndUserId(@Param("orderId") Long orderId,
                                      @Param("userId") Long userId);

    /**
     * Single order with items — no ownership check (for staff/admin).
     */
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.product
            WHERE o.id = :orderId
            """)
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    /**
     * Staff view: all orders with optional filters.
     * Uses a count query to support pagination correctly with DISTINCT.
     */
    @Query(value = """
            SELECT o FROM Order o
            LEFT JOIN FETCH o.user
            WHERE (:status IS NULL OR o.status = :status)
              AND (:fulfillmentType IS NULL OR o.fulfillmentType = :fulfillmentType)
            ORDER BY o.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(o) FROM Order o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:fulfillmentType IS NULL OR o.fulfillmentType = :fulfillmentType)
            """)
    Page<Order> findAllWithFilters(@Param("status") OrderStatus status,
                                   @Param("fulfillmentType") FulfillmentType fulfillmentType,
                                   Pageable pageable);
}
