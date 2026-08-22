package edu.demart_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // ─── Fulfillment ──────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FulfillmentType fulfillmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    /**
     * Embedded delivery address columns — populated only for HOME_DELIVERY.
     * All columns are nullable so STORE_PICKUP rows don't fail insertion.
     */
    @Embedded
    private DeliveryAddress deliveryAddress;

    /** Requested pickup time — populated only for STORE_PICKUP */
    private LocalDateTime pickupSlot;

    // ─── Pricing ─────────────────────────────────────────────────────────────

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Delivery charge: ₹0 for STORE_PICKUP, ₹0 for HOME_DELIVERY >= ₹500,
     * otherwise ₹50.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // ─── Metadata ─────────────────────────────────────────────────────────────

    private String notes;

    /**
     * Set when status transitions to DELIVERED.
     * Return/exchange eligibility = deliveredAt + 7 days.
     */
    private LocalDateTime deliveredAt;
}
