package edu.demart_api.repository;

import edu.demart_api.entity.ReturnExchange;
import edu.demart_api.entity.ReturnExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnExchangeRepository extends JpaRepository<ReturnExchange, Long> {

    /**
     * Customer's own return/exchange requests across all their orders.
     */
    @Query("""
            SELECT r FROM ReturnExchange r
            LEFT JOIN FETCH r.order o
            LEFT JOIN FETCH r.orderItem oi
            LEFT JOIN FETCH r.targetProduct
            WHERE o.user.id = :userId
            ORDER BY r.createdAt DESC
            """)
    List<ReturnExchange> findByUserId(@Param("userId") Long userId);

    /**
     * Staff view: all requests, optionally filtered by status (PENDING / APPROVED / REJECTED).
     */
    @Query("""
            SELECT r FROM ReturnExchange r
            LEFT JOIN FETCH r.order o
            LEFT JOIN FETCH o.user
            LEFT JOIN FETCH r.orderItem oi
            LEFT JOIN FETCH r.targetProduct
            WHERE (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    List<ReturnExchange> findAllWithFilters(@Param("status") ReturnExchangeStatus status);

    /**
     * Check if a return/exchange already exists for an order item.
     * Prevents duplicate requests on the same line item.
     */
    boolean existsByOrderItemIdAndStatusIn(Long orderItemId, List<ReturnExchangeStatus> statuses);
}
