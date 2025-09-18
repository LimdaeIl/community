package com.community.soap.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
@Table(name = "p_payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "payment_key", nullable = false, unique = true, length = 200)
    private String paymentKey;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "method", length = 40)
    private String method;

    @Column(name = "status", length = 40)
    private String status;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Payment(Long id, String paymentKey, String orderId, Integer amount) {
        this.paymentId = id;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
    public static Payment pending(Long id, String paymentKey, String orderId, Integer amount) {
        return new Payment(id, paymentKey, orderId, amount);
    }
    public void markApproved(String method, String status, String approvedAtIso, String receiptUrl) {
        this.method = method;
        this.status = status;
        this.receiptUrl = receiptUrl;
        if (approvedAtIso != null) this.approvedAt = LocalDateTime.parse(approvedAtIso);
    }
    public void markCanceled(String status) {
        this.status = status;
    }
}

