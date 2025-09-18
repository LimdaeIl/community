package com.community.soap.payment.application;

import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.payment.domain.entity.Payment;
import com.community.soap.payment.domain.exception.PaymentErrorCode;
import com.community.soap.payment.domain.exception.PaymentException;
import com.community.soap.payment.infrastructure.OrderSnapshot;
import com.community.soap.payment.infrastructure.OrderingPort;
import com.community.soap.payment.infrastructure.PaymentRepository;
import com.community.soap.payment.infrastructure.TossPaymentsClient;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentsClient toss;
    private final OrderingPort orderingPort;      // 변경: 직접 Repo → Port
    private final PaymentRepository paymentRepo;
    private final Snowflake snowflake;

    @Transactional
    public ConfirmPaymentResponse confirm(ConfirmPaymentRequest req) {
        // 1) 주문 스냅샷 조회
        Long orderId = Long.valueOf(req.orderId());
        OrderSnapshot order = orderingPort.getOrderSnapshot(orderId);

        // 2) 금액 검증
        if (!Objects.equals(order.totalAmount(), req.amount())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 3) 토스 승인 호출 (Idempotency-Key 권장)
        String idemKey = "confirm:" + req.orderId() + ":" + req.paymentKey();
        Map<String, Object> res = toss.confirm(req.paymentKey(), req.orderId(), req.amount(),
                idemKey);

        String status = (String) res.get("status");      // 기대: DONE
        String method = (String) res.get("method");      // CARD/TRANSFER/...
        String approvedAt = (String) res.get("approvedAt");
        String receiptUrl = (String) res.get("receiptUrl");

        if (status == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 4) 결제 저장(Payment)
        Payment pay = Payment.pending(snowflake.nextId(), req.paymentKey(), req.orderId(),
                req.amount());
        pay.markApproved(method, status, approvedAt, receiptUrl);
        paymentRepo.save(pay);

        // 5) 주문 상태 갱신 (PAID)
        orderingPort.markOrderPaid(orderId);

        return new ConfirmPaymentResponse(
                req.paymentKey(), req.orderId(), req.amount(),
                method, status, receiptUrl, approvedAt
        );
    }

    @Transactional
    public ConfirmPaymentResponse cancel(String paymentKey, String reason, Integer cancelAmount) {
        var pay = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        Map<String, Object> res = toss.cancel(paymentKey, reason, cancelAmount);
        String status = (String) res.get("status");
        if (status == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }
        pay.markCanceled(status);

        String approvedAtOut =
                (pay.getApprovedAt() != null) ? pay.getApprovedAt().toString() : null;
        return new ConfirmPaymentResponse(
                pay.getPaymentKey(), pay.getOrderId(), pay.getAmount(),
                pay.getMethod(), status, pay.getReceiptUrl(), approvedAtOut
        );
    }
}