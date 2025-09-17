package com.community.soap.ordering.domain.exception;

public class OrderException extends RuntimeException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode.getMessage());
    }
}
