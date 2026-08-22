package edu.demart_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;

    // ─── Category ────────────────────────────────────────────────────────────
    private Long categoryId;
    private String categoryName;

    // ─── Unit & Pricing ──────────────────────────────────────────────────────
    private String unit;

    /** MRP — shown as strikethrough price */
    private BigDecimal mrpPrice;

    /** Actual selling price */
    private BigDecimal sellingPrice;

    /**
     * Calculated discount percentage.
     * = ((mrp - selling) / mrp) * 100, rounded to 1 decimal.
     * e.g. 25.0 means 25% off
     */
    private double discountPercent;

    // ─── Stock ───────────────────────────────────────────────────────────────
    private Integer stockQuantity;
    private Integer minStockAlert;

    /** true if stockQuantity > 0 */
    private boolean inStock;

    /**
     * true if stock is between 1 and minStockAlert (inclusive).
     * Visible in admin/staff responses to flag replenishment needs.
     */
    private boolean lowStock;

    // ─── Metadata ────────────────────────────────────────────────────────────
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
