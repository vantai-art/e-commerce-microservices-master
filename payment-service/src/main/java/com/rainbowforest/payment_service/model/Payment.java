package com.rainbowforest.payment_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt  = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
        if (this.status == null) this.status = PaymentStatus.PENDING;
        if (this.transactionId == null)
            this.transactionId = "TXN-" + System.currentTimeMillis()
                    + "-" + (int)(Math.random() * 10000);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum PaymentStatus {
        PENDING,    // Chờ xử lý
        SUCCESS,    // Thành công
        FAILED,     // Thất bại
        REFUNDED    // Đã hoàn tiền
    }

    public enum PaymentMethod {
        CASH,           // Tiền mặt khi nhận hàng
        CREDIT_CARD,    // Thẻ tín dụng
        BANK_TRANSFER,  // Chuyển khoản ngân hàng
        E_WALLET        // Ví điện tử (MoMo, ZaloPay...)
    }
}
