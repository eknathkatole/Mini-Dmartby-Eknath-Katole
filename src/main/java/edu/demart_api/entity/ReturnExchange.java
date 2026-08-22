package edu.demart_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_exchanges")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReturnExchange extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnExchangeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnExchangeStatus status = ReturnExchangeStatus.PENDING;

    @Column(nullable = false, length = 500)
    private String reason;

    /**
     * For EXCHANGE requests — the product the customer wants instead.
     * If null on an EXCHANGE request, the same product (replacement) is assumed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_product_id")
    private Product targetProduct;

    /**
     * Return eligibility deadline = order.deliveredAt + 7 days.
     * Stored on this entity for easy querying and customer display.
     */
    @Column(nullable = false)
    private LocalDateTime eligibleUntil;

    /** Staff message when approving or rejecting — mandatory on rejection */
    @Column(length = 500)
    private String staffNote;

    /** Timestamp set when staff approves or rejects this request */
    private LocalDateTime processedAt;
}
