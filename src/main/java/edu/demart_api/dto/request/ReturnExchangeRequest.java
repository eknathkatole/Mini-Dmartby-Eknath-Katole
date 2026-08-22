package edu.demart_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnExchangeRequest {

    @NotNull(message = "Order item ID is required")
    private Long orderItemId;

    /**
     * RETURN or EXCHANGE
     */
    @NotBlank(message = "Type is required: RETURN or EXCHANGE")
    private String type;

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    /**
     * For EXCHANGE only — the product ID the customer wants instead.
     * If null, the same product (replacement) is assumed.
     */
    private Long targetProductId;
}
