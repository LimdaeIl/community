package com.community.soap.payment.application;

import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.payment.application.request.ConfirmPaymentRequest;
import com.community.soap.payment.application.response.ConfirmPaymentResponse;
import com.community.soap.payment.domain.entity.Payment;
import com.community.soap.payment.domain.exception.PaymentErrorCode;
import com.community.soap.payment.domain.exception.PaymentException;
import com.community.soap.payment.infrastructure.OrderingPort;
import com.community.soap.payment.infrastructure.PaymentRepository;
import com.community.soap.payment.infrastructure.PaymentWriter;
import com.community.soap.payment.infrastructure.TossPaymentsClient;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final TossPaymentsClient toss;
    private final OrderingPort orderingPort;
    private final PaymentRepository paymentRepo;
    private final Snowflake snowflake;
    private final PaymentWriter paymentWriter; // 트랜잭션 경계 담당

    // @Transactional 없음
    public ConfirmPaymentResponse confirm(ConfirmPaymentRequest req) {
        // 0) 멱등 fast-path (트랜잭션 없이 빠르게)
        var existing = paymentRepo.findByPaymentKey(req.paymentKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        // 1) 주문 검증 (트랜잭션 불필요)
        Long orderId = Long.valueOf(req.orderId());
        var order = orderingPort.getOrderSnapshot(orderId);
        if (order.status() != com.community.soap.ordering.domain.entity.OrderStatus.CREATED) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        if (!Objects.equals(order.totalAmount(), req.amount())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 2) Toss 승인 호출 (네트워크 I/O – 트랜잭션 밖)
        String idemKey = "confirm:" + req.orderId() + ":" + req.paymentKey();
        Map<String, Object> res;
        try {
            res = toss.confirm(req.paymentKey(), req.orderId(), req.amount(), idemKey);
        } catch (Exception e) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        Object statusObj = res.get("status");
        if (!(statusObj instanceof String status) || !"DONE".equalsIgnoreCase(status)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        String method = (String) res.get("method");
        String approvedAt = (String) res.get("approvedAt");
        String receiptUrl = (String) res.get("receiptUrl");

        // 3) 저장/상태 갱신은 "짧은 트랜잭션"으로 위임
        Payment pay = Payment.pending(snowflake.nextId(), req.paymentKey(), req.orderId(),
                req.amount());
        pay.markApproved(method, "DONE", approvedAt, receiptUrl);

        Payment saved = paymentWriter.saveApprovedAndMarkOrderPaid(pay, orderId);

        return toResponse(saved);
    }

    // @Transactional 없음
    public ConfirmPaymentResponse cancel(String paymentKey, String reason, Integer cancelAmount) {
        var pay = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        Map<String, Object> res;
        try {
            res = toss.cancel(paymentKey,
                    (reason == null || reason.isBlank()) ? "user_request" : reason,
                    cancelAmount);
        } catch (Exception e) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }

        String status = (String) res.get("status");
        if (status == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }

        Payment canceled = paymentWriter.markCanceled(paymentKey, status);
        return toResponse(canceled);
    }

    private ConfirmPaymentResponse toResponse(Payment p) {
        return new ConfirmPaymentResponse(
                p.getPaymentKey(),
                p.getOrderId(),
                p.getAmount(),
                p.getMethod(),
                p.getStatus(),
                p.getReceiptUrl(),
                p.getApprovedAt() != null ? p.getApprovedAt().toString() : null
        );
    }
}
