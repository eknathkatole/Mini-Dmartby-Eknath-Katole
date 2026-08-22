package edu.demart_api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReturnExchangeResponse {

    private Long id;

    // ─── Order & Item context ─────────────────────────────────────────────────
    private Long orderId;
    private Long orderItemId;
    private String productName;
    private int quantity;

    // ─── Request details ──────────────────────────────────────────────────────
    private String type;       // RETURN or EXCHANGE
    private String status;     // PENDING / APPROVED / REJECTED
    private String reason;

    // ─── Exchange-specific ────────────────────────────────────────────────────
    private Long targetProductId;
    private String targetProductName;

    // ─── Eligibility ─────────────────────────────────────────────────────────
    private LocalDateTime eligibleUntil;

    /** Whether the return window is still open (eligibleUntil is in the future) */
    private boolean withinEligibilityWindow;

    // ─── Staff response ───────────────────────────────────────────────────────
    private String staffNote;
    private LocalDateTime processedAt;

    // ─── Metadata ─────────────────────────────────────────────────────────────
    private LocalDateTime requestedAt;  // = createdAt
    private LocalDateTime updatedAt;
}
