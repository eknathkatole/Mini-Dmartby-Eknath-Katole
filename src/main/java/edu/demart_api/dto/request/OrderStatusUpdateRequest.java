package edu.demart_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequest {

    /**
     * The new status to transition to.
     * Valid values depend on current status and fulfillment type —
     * transition rules are enforced in the service layer.
     */
    @NotBlank(message = "New status is required")
    private String status;

    /** Optional note (e.g. reason for cancellation, delivery note) */
    private String note;
}
