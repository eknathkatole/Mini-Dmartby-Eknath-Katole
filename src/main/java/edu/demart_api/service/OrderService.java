package edu.demart_api.service;

import edu.demart_api.dto.request.OrderStatusUpdateRequest;
import edu.demart_api.dto.request.PlaceOrderRequest;
import edu.demart_api.dto.response.OrderResponse;
import edu.demart_api.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    // ─── Customer ─────────────────────────────────────────────────────────────

    /** Place a new order — decrements stock atomically */
    OrderResponse placeOrder(Long userId, PlaceOrderRequest request);

    /** All orders belonging to the authenticated customer */
    List<OrderResponse> getMyOrders(Long userId);

    /** Single order — enforces customer ownership */
    OrderResponse getMyOrderById(Long orderId, Long userId);

    /** Customer-initiated cancellation (only allowed for PLACED or CONFIRMED) */
    OrderResponse cancelMyOrder(Long orderId, Long userId);

    // ─── Staff / Admin ────────────────────────────────────────────────────────

    /** Paginated order listing with optional status and fulfillment-type filters */
    PageResponse<OrderResponse> getAllOrders(String status, String fulfillmentType, Pageable pageable);

    /** Single order for staff — no ownership restriction */
    OrderResponse getOrderById(Long orderId);

    /** Transition order to the next status — validates allowed transitions */
    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);
}
