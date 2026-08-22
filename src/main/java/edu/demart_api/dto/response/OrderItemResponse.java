package edu.demart_api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productUnit;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;
    private boolean returned;
}
