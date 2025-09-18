package com.community.soap.payment.application;

public record ConfirmPaymentResponse(
        String paymentKey,
        String orderId,
        Integer approvedAmount,
        String method,           // CARD, TRANSFER, ...
        String status,           // DONE 등
        String receiptUrl,       // 영수증/전표 URL(결제수단에 따라 다름)
        String approvedAt        // ISO-8601
) {}
