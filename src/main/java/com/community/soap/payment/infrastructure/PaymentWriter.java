package com.community.soap.payment.infrastructure;

import com.community.soap.payment.domain.entity.Payment;
import com.community.soap.payment.domain.exception.PaymentErrorCode;
import com.community.soap.payment.domain.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentWriter {

    private final PaymentRepository paymentRepo;
    private final OrderingPort orderingPort;

    @Transactional // 기본 REQUIRED: 호출 시 "여기서" 트랜잭션 시작
    public Payment saveApprovedAndMarkOrderPaid(Payment pay, Long orderId) {
        Payment saved = paymentRepo.save(pay);
        orderingPort.markOrderPaid(orderId);
        return saved;
    }

    @Transactional
    public Payment markCanceled(String paymentKey, String canceledStatus) {
        Payment pay = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        pay.markCanceled(canceledStatus);
        return pay;
    }
}
