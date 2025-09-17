package com.community.soap.ordering.domain.exception;

import com.community.soap.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    // 주문 자체
    ORDER_INVALID(HttpStatus.BAD_REQUEST, "주문: 잘못된 주문 요청입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문: 주문을 찾을 수 없습니다."),
    ORDER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "주문: 주문 상태 값이 올바르지 않습니다."),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "주문: 이미 결제 완료된 주문입니다."),
    ORDER_ALREADY_SHIPPED(HttpStatus.CONFLICT, "주문: 이미 출고/배송이 시작되었습니다."),
    ORDER_ALREADY_CANCELED(HttpStatus.CONFLICT, "주문: 이미 취소된 주문입니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.CONFLICT, "주문: 현재 상태에서는 취소할 수 없습니다."),

    // 주문 상품(라인)
    ORDER_ITEM_INVALID(HttpStatus.BAD_REQUEST, "주문: 주문 상품 정보가 올바르지 않습니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "주문: 주문 상품을 찾을 수 없습니다."),
    QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "주문: 수량이 올바르지 않습니다."),

    // 배송지
    ADDRESS_INVALID(HttpStatus.BAD_REQUEST, "주문: 배송지 정보가 올바르지 않습니다."),

    // 카탈로그/재고 연동 시
    PRODUCT_NOT_SALEABLE(HttpStatus.CONFLICT, "주문: 판매 불가 상품입니다."),
    SKU_MISMATCH(HttpStatus.BAD_REQUEST, "주문: SKU 정보가 일치하지 않습니다."),
    STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "주문: 재고가 부족합니다."),

    // 결제(선택)
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "주문: 결제 처리에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
