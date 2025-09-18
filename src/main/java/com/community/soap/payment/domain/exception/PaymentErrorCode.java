package com.community.soap.payment.domain.exception;

import com.community.soap.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_INVALID(HttpStatus.BAD_REQUEST, "결제: 잘못된 결제 요청입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제: 결제 내역을 찾을 수 없습니다."),
    PAYMENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "결제: 결제 상태가 올바르지 않습니다."),
    PAYMENT_ALREADY_APPROVED(HttpStatus.CONFLICT, "결제: 이미 승인된 결제입니다."),
    PAYMENT_ALREADY_CANCELED(HttpStatus.CONFLICT, "결제: 이미 취소된 결제입니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "결제: 결제 승인 처리에 실패했습니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_REQUEST, "결제: 결제 취소 처리에 실패했습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제: 결제 금액이 주문 금액과 일치하지 않습니다."),

    // 주문 연동 관련(결제 관점에서 노출)
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "결제: 주문을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}