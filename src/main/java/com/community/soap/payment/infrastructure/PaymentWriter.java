package com.community.soap.payment.infrastructure;

import com.community.soap.payment.domain.entity.Payment;
import com.community.soap.payment.domain.exception.PaymentErrorCode;
import com.community.soap.payment.domain.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentWriter {

    private final PaymentRepository paymentRepo;
    private final OrderingPort orderingPort;

    private final com.community.soap.payment.infrastructure.CatalogPort catalogPort;

    @Transactional
    public Payment markCanceled(String paymentKey, String canceledStatus) {
        var pay = paymentRepo.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if ("CANCELED".equalsIgnoreCase(pay.getStatus())) {
            return pay;        // 멱등
        }
        if (!"DONE".equalsIgnoreCase(pay.getStatus())) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_STATUS_INVALID);
        }

        pay.markCanceled(canceledStatus);

        Long orderId = Long.valueOf(pay.getOrderId());
        orderingPort.markOrderCanceled(orderId);

        for (OrderLineItem it : orderingPort.getOrderLineItems(orderId)) {
            catalogPort.increaseStock(it.productId(), it.skuCode(), it.quantity());
        }
        return pay;
    }

    @Transactional
    public Payment saveApprovedAndMarkOrderPaid(Payment pay, Long orderId) {
        Payment saved = paymentRepo.save(pay);
        orderingPort.markOrderPaid(orderId);
        return saved;
    }
}
