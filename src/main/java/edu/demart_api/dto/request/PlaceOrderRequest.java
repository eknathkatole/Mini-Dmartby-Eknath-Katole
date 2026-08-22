package edu.demart_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PlaceOrderRequest {

    /**
     * Required: STORE_PICKUP or HOME_DELIVERY
     */
    @NotBlank(message = "Fulfillment type is required: STORE_PICKUP or HOME_DELIVERY")
    private String fulfillmentType;

    /**
     * Required for HOME_DELIVERY — validated conditionally in the service layer.
     */
    @Valid
    private DeliveryAddressRequest deliveryAddress;

    /**
     * Required for STORE_PICKUP — must be a future date/time.
     */
    private LocalDateTime pickupSlot;

    @NotNull(message = "Items list is required")
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    private String notes;
}
