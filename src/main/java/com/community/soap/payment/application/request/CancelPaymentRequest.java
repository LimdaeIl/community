package com.community.soap.payment.application.request;

public record CancelPaymentRequest(
        String cancelReason,
        Integer cancelAmount // null이면 전액
) {}
