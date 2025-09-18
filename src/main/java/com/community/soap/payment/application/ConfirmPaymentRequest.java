package com.community.soap.payment.application;

public record ConfirmPaymentRequest(
        String paymentKey,
        String orderId, // 토스 요구: 6~64자 영문/숫자/-/_  (우리는 Snowflake Long을 문자열화해 사용해도 OK)
        Integer amount
) {}