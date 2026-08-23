package edu.demart_api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveStaffApplicationRequest {

    private String customPassword;
    private String adminNote;
}
