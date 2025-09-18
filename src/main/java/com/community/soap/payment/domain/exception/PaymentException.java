package com.community.soap.payment.domain.exception;


import com.community.soap.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final ErrorCode errorCode;

    public PaymentException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}