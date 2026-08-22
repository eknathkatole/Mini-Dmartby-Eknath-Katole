package edu.demart_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessReturnRequest {

    /**
     * APPROVE or REJECT
     */
    @NotBlank(message = "Action is required: APPROVE or REJECT")
    private String action;

    /**
     * Staff note — mandatory when rejecting so the customer understands why.
     * Optional (but recommended) for approvals.
     */
    private String staffNote;
}
