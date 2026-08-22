package edu.demart_api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;

    // ─── Customer info ────────────────────────────────────────────────────────
    private Long userId;
    private String customerName;
    private String customerEmail;

    // ─── Fulfillment ──────────────────────────────────────────────────────────
    private String fulfillmentType;
    private String status;
    private LocalDateTime pickupSlot;

    // ─── Delivery address (null for STORE_PICKUP) ─────────────────────────────
    private String deliveryStreet;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryPincode;
    private String deliveryLandmark;

    // ─── Pricing ─────────────────────────────────────────────────────────────
    private BigDecimal subtotal;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;

    // ─── Items ───────────────────────────────────────────────────────────────
    private List<OrderItemResponse> items;
    private int itemCount;

    // ─── Metadata ─────────────────────────────────────────────────────────────
    private String notes;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
