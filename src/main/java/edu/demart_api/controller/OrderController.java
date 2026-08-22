package edu.demart_api.controller;

import edu.demart_api.dto.request.PlaceOrderRequest;
import edu.demart_api.dto.request.ReturnExchangeRequest;
import edu.demart_api.dto.response.ApiResponse;
import edu.demart_api.dto.response.OrderResponse;
import edu.demart_api.dto.response.ReturnExchangeResponse;
import edu.demart_api.security.SecurityUtils;
import edu.demart_api.service.OrderService;
import edu.demart_api.service.ReturnExchangeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing order and return/exchange endpoints.
 * All routes require authentication (falls under anyRequest().authenticated() in SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService          orderService;
    private final ReturnExchangeService returnExchangeService;
    private final SecurityUtils         securityUtils;

    public OrderController(OrderService orderService,
                           ReturnExchangeService returnExchangeService,
                           SecurityUtils securityUtils) {
        this.orderService          = orderService;
        this.returnExchangeService = returnExchangeService;
        this.securityUtils         = securityUtils;
    }

    // ─── Orders ───────────────────────────────────────────────────────────────

    /**
     * Place a new order.
     * Body for HOME_DELIVERY:
     *   { "fulfillmentType": "HOME_DELIVERY", "deliveryAddress": {...}, "items": [...] }
     * Body for STORE_PICKUP:
     *   { "fulfillmentType": "STORE_PICKUP", "pickupSlot": "2026-08-25T11:00:00", "items": [...] }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        OrderResponse order = orderService.placeOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Orders fetched", orderService.getMyOrders(userId)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrder(@PathVariable Long orderId) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Order fetched", orderService.getMyOrderById(orderId, userId)));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelMyOrder(@PathVariable Long orderId) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled", orderService.cancelMyOrder(orderId, userId)));
    }

    // ─── Returns & Exchanges ──────────────────────────────────────────────────

    /**
     * Submit a return or exchange request for a specific order.
     * Eligibility checks: order must be DELIVERED and within 7 days.
     */
    @PostMapping("/{orderId}/returns")
    public ResponseEntity<ApiResponse<ReturnExchangeResponse>> requestReturn(
            @PathVariable Long orderId,
            @Valid @RequestBody ReturnExchangeRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        ReturnExchangeResponse response = returnExchangeService.requestReturn(userId, orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return/exchange request submitted", response));
    }

    /** All return/exchange requests submitted by the authenticated customer */
    @GetMapping("/my-returns")
    public ResponseEntity<ApiResponse<List<ReturnExchangeResponse>>> getMyReturns() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success("Return requests fetched", returnExchangeService.getMyRequests(userId)));
    }
}
