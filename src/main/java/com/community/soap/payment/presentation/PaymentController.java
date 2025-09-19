package com.community.soap.payment.presentation;

import com.community.soap.payment.application.PaymentService;
import com.community.soap.payment.application.request.CancelPaymentRequest;
import com.community.soap.payment.application.request.ConfirmPaymentRequest;
import com.community.soap.payment.application.response.ConfirmPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // successUrl 패턴: 토스가 GET 쿼리로 넘겨줌
    @GetMapping("/success")
    public ResponseEntity<ConfirmPaymentResponse> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Integer amount
    ) {
        return ResponseEntity.ok(
                paymentService.confirm(new ConfirmPaymentRequest(paymentKey, orderId, amount))
        );
    }

    // JSON POST로도 가능(테스트/프론트 선택)
    @PostMapping("/confirm")
    public ResponseEntity<ConfirmPaymentResponse> confirm(@RequestBody ConfirmPaymentRequest req) {
        return ResponseEntity.ok(paymentService.confirm(req));
    }

    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<ConfirmPaymentResponse> cancel(
            @PathVariable String paymentKey,
            @RequestBody CancelPaymentRequest req
    ) {
        return ResponseEntity.ok(
                paymentService.cancel(paymentKey, req.cancelReason(), req.cancelAmount()));
    }
}