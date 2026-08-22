package edu.demart_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Unit is required (KG, GRAMS, LITERS, ML, PIECES, DOZEN, PACK)")
    private String unit;

    @NotNull(message = "MRP price is required")
    @DecimalMin(value = "0.01", message = "MRP price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "MRP price is invalid")
    private BigDecimal mrpPrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.01", message = "Selling price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Selling price is invalid")
    private BigDecimal sellingPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Min(value = 1, message = "Minimum stock alert must be at least 1")
    private Integer minStockAlert = 10;

    private String imageUrl;
}
