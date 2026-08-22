package edu.demart_api.controller;

import edu.demart_api.dto.request.OrderStatusUpdateRequest;
import edu.demart_api.dto.request.ProcessReturnRequest;
import edu.demart_api.dto.response.ApiResponse;
import edu.demart_api.dto.response.OrderResponse;
import edu.demart_api.dto.response.PageResponse;
import edu.demart_api.dto.response.ReturnExchangeResponse;
import edu.demart_api.service.OrderService;
import edu.demart_api.service.ReturnExchangeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Staff and Admin operations dashboard.
 *
 * All endpoints under /api/v1/staff/** require STAFF or ADMIN role
 * (enforced at route level in SecurityConfig + @PreAuthorize here).
 *
 * ┌────────────────────────────────────────────────────┬──────────────────┐
 * │ Endpoint                                           │ Roles            │
 * ├────────────────────────────────────────────────────┼──────────────────┤
 * │ GET  /api/v1/staff/orders                         │ STAFF, ADMIN     │
 * │ GET  /api/v1/staff/orders/{id}                    │ STAFF, ADMIN     │
 * │ PATCH /api/v1/staff/orders/{id}/status            │ STAFF, ADMIN     │
 * │ GET  /api/v1/staff/returns                        │ STAFF, ADMIN     │
 * │ PATCH /api/v1/staff/returns/{id}/process          │ STAFF, ADMIN     │
 * └────────────────────────────────────────────────────┴──────────────────┘
 */
@RestController
@RequestMapping("/api/v1/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffController {

    private final OrderService          orderService;
    private final ReturnExchangeService returnExchangeService;

    public StaffController(OrderService orderService,
                           ReturnExchangeService returnExchangeService) {
        this.orderService          = orderService;
        this.returnExchangeService = returnExchangeService;
    }

    // ─── Order Management ─────────────────────────────────────────────────────

    /**
     * Paginated order list with optional filters.
     *
     * Query params:
     *   status          – PLACED | CONFIRMED | PREPARING | READY_FOR_PICKUP | OUT_FOR_DELIVERY | DELIVERED | CANCELLED
     *   fulfillmentType – STORE_PICKUP | HOME_DELIVERY
     *   page, size, sortBy, sortDir
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fulfillmentType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return ResponseEntity.ok(ApiResponse.success(
                "Orders fetched", orderService.getAllOrders(status, fulfillmentType, pageable)));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched", orderService.getOrderById(orderId)));
    }

    /**
     * Transition order status.
     * Valid transitions:
     *   PLACED      → CONFIRMED | CANCELLED
     *   CONFIRMED   → PREPARING | CANCELLED
     *   PREPARING   → READY_FOR_PICKUP (STORE_PICKUP) | OUT_FOR_DELIVERY (HOME_DELIVERY) | CANCELLED
     *   READY_FOR_PICKUP → DELIVERED
     *   OUT_FOR_DELIVERY → DELIVERED
     *
     * DELIVERED and CANCELLED are terminal — no further transitions allowed.
     */
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order status updated", orderService.updateOrderStatus(orderId, request)));
    }

    // ─── Return / Exchange Processing ─────────────────────────────────────────

    /**
     * View all return/exchange requests.
     * Query param: status = PENDING | APPROVED | REJECTED (omit for all)
     */
    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<List<ReturnExchangeResponse>>> getAllReturnRequests(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Return requests fetched", returnExchangeService.getAllRequests(status)));
    }

    /**
     * Process (approve or reject) a return/exchange request.
     *
     * Body: { "action": "APPROVE" }
     *    or { "action": "REJECT", "staffNote": "Item shows signs of use" }
     *
     * On APPROVE:
     *   RETURN  → original product stock +quantity
     *   EXCHANGE → original product stock +quantity, target product stock -quantity
     * On REJECT:
     *   No stock change; staffNote is mandatory.
     */
    @PatchMapping("/returns/{requestId}/process")
    public ResponseEntity<ApiResponse<ReturnExchangeResponse>> processReturn(
            @PathVariable Long requestId,
            @Valid @RequestBody ProcessReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Return request processed", returnExchangeService.processRequest(requestId, request)));
    }
}
