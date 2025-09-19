package com.community.soap.payment.application;

import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.payment.application.request.ConfirmPaymentRequest;
import com.community.soap.payment.application.response.ConfirmPaymentResponse;
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

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final TossPaymentsClient toss;
    private final OrderingPort orderingPort;      // 변경: 직접 Repo → Port
    private final PaymentRepository paymentRepo;
    private final Snowflake snowflake;

    @Transactional
    public ConfirmPaymentResponse confirm(ConfirmPaymentRequest req) {
        // 0) 동일 paymentKey 멱등 처리
        var existing = paymentRepo.findByPaymentKey(req.paymentKey());
        if (existing.isPresent()) {
            Payment p = existing.get();
            return new ConfirmPaymentResponse(
                    p.getPaymentKey(),
                    p.getOrderId(),
                    p.getAmount(),
                    p.getMethod(),
                    p.getStatus(),  // Payment.status: DONE / CANCELED ...
                    p.getReceiptUrl(),
                    p.getApprovedAt() != null ? p.getApprovedAt().toString() : null
            );
        }

        // 1) 주문 스냅샷 조회 + 유효성/금액 검증
        Long orderId = Long.valueOf(req.orderId());
        OrderSnapshot order = orderingPort.getOrderSnapshot(orderId);

        // (선택) 주문 상태 가드 — CREATED가 아니라면 거절
        if (order.status() != com.community.soap.ordering.domain.entity.OrderStatus.CREATED) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        if (!Objects.equals(order.totalAmount(), req.amount())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 2) 토스 승인 호출(서버 멱등키)
        String idemKey = "confirm:" + req.orderId() + ":" + req.paymentKey();

        Map<String, Object> res;
        try {
            res = toss.confirm(req.paymentKey(), req.orderId(), req.amount(), idemKey);
        } catch (Exception e) {
            // 네트워크/권한 등 호출 자체 실패
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 2-1) 에러 응답이면 code/message로 더 명확히 거절
        Object statusObj = res.get("status");
        if (!(statusObj instanceof String status) || !"DONE".equalsIgnoreCase(status)) {
            // 토스 표준 에러는 {"code":"...","message":"..."}
            String code = String.valueOf(res.getOrDefault("code", "UNKNOWN"));
            String msg  = String.valueOf(res.getOrDefault("message", "Toss confirm failed"));
            // PaymentException에 상세 문자열을 붙일 여지가 없다면 로깅만 하고 일반 코드로 던지세요.
            // log.warn("Toss confirm error: {} - {}", code, msg);
            throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        String method     = (String) res.get("method");
        String approvedAt = (String) res.get("approvedAt"); // ISO-8601(+offset)
        String receiptUrl = (String) res.get("receiptUrl");

        // 3) 결제 저장 (pending→approved)
        Payment pay = Payment.pending(snowflake.nextId(), req.paymentKey(), req.orderId(), req.amount());
        pay.markApproved(method, "DONE", approvedAt, receiptUrl);
        paymentRepo.save(pay);

        // 4) 주문 상태 갱신
        orderingPort.markOrderPaid(orderId);

        // 5) 응답
        return new ConfirmPaymentResponse(
                pay.getPaymentKey(),
                pay.getOrderId(),
                pay.getAmount(),
                pay.getMethod(),
                "DONE",
                pay.getReceiptUrl(),
                pay.getApprovedAt() != null ? pay.getApprovedAt().toString() : null
        );
    }

    @Transactional
    public ConfirmPaymentResponse cancel(String paymentKey, String reason, Integer cancelAmount) {
        var pay = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        Map<String, Object> res;
        try {
            res = toss.cancel(paymentKey,
                    (reason == null || reason.isBlank()) ? "user_request" : reason, cancelAmount);
        } catch (Exception e) {
            // log.error("Toss cancel failed", e);
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }

        String status = (String) res.get("status"); // 기대: CANCELED
        if (status == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }

        pay.markCanceled(status);

        return new ConfirmPaymentResponse(
                pay.getPaymentKey(),
                pay.getOrderId(),
                pay.getAmount(),
                pay.getMethod(),
                status,
                pay.getReceiptUrl(),
                pay.getApprovedAt() != null ? pay.getApprovedAt().toString() : null
        );
    }
}
