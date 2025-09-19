package com.community.soap.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void markApproved(String method, String status, String approvedAt, String receiptUrl) {
        this.method = method;
        this.status = status;
        this.receiptUrl = receiptUrl;

        if (approvedAt != null) {
            try {
                // "+09:00" 같은 오프셋 포함 문자열 처리
                OffsetDateTime odt = OffsetDateTime.parse(approvedAt);
                this.approvedAt = odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            } catch (DateTimeParseException e) {
                // 오프셋 없는 "yyyy-MM-dd'T'HH:mm:ss" 형태 대응
                this.approvedAt = LocalDateTime.parse(approvedAt);
            }
        }
    }

    public void markCanceled(String status) {
        this.status = status;
    }
}

