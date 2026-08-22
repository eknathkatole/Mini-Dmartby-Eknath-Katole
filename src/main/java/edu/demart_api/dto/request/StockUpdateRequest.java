package edu.demart_api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for updating product stock.
 *
 * Operations:
 *  - ADD      → stockQuantity += quantity   (receiving new stock)
 *  - SUBTRACT → stockQuantity -= quantity   (manual removal, damage write-off)
 *  - SET      → stockQuantity  = quantity   (full inventory reconciliation)
 */
@Getter
@Setter
public class StockUpdateRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotBlank(message = "Operation is required: ADD, SUBTRACT, or SET")
    private String operation; // ADD | SUBTRACT | SET

    /** Optional note explaining the reason (e.g. "Received from supplier", "Damaged goods") */
    private String reason;
}
