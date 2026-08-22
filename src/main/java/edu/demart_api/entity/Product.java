package edu.demart_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    // ─── Category ────────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ─── Unit & Pricing ──────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    /**
     * Maximum Retail Price — the printed price on the product.
     * Used to show the original/strikethrough price on the UI.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal mrpPrice;

    /**
     * Actual price the customer pays — always <= mrpPrice.
     * Discount % is derived: ((mrp - selling) / mrp) * 100
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    // ─── Inventory / Stock ───────────────────────────────────────────────────

    /**
     * Current available stock (in terms of the product's unit).
     * Decremented on order placement, incremented on returns/cancellations.
     */
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    /**
     * Threshold below which the product is flagged as "low stock" for staff.
     * Default is 10 units.
     */
    @Column(nullable = false)
    private Integer minStockAlert = 10;

    // ─── Metadata ────────────────────────────────────────────────────────────

    private String imageUrl;

    /** Soft-delete: deactivated products are hidden from public but kept in DB */
    @Column(nullable = false)
    private boolean active = true;

    // ─── Derived helpers (not persisted) ─────────────────────────────────────

    /** True if stock > 0 */
    @Transient
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /** True if stock is at or below the minStockAlert threshold */
    @Transient
    public boolean isLowStock() {
        return stockQuantity != null && minStockAlert != null
                && stockQuantity > 0
                && stockQuantity <= minStockAlert;
    }
}
