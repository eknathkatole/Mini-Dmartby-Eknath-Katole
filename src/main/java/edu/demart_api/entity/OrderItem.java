package edu.demart_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** FK to the product — kept for admin traceability even after deactivation */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ─── Snapshots captured at order time ─────────────────────────────────────
    // (product name, unit, and price can change after order is placed;
    //  these fields preserve the customer's order history accurately)

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productUnit;

    /** sellingPrice at the moment this order was placed */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // unitPrice × quantity

    /**
     * Flipped to true when a return/exchange for this item is APPROVED.
     * Prevents duplicate return requests on the same line item.
     */
    @Column(nullable = false)
    private boolean returned = false;
}
