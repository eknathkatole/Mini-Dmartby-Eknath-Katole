package edu.demart_api.dto.response;

import edu.demart_api.entity.StaffApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffApplicationResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String storeName;
    private String reason;
    private StaffApplicationStatus status;
    private String adminNote;
    private String generatedPassword;
    private LocalDateTime createdAt;
}
