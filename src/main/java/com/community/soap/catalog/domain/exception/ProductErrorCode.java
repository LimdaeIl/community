package com.community.soap.catalog.domain.exception;

import com.community.soap.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_INVALID(HttpStatus.BAD_REQUEST, "상품: 잘못된 상품 정보입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품: 상품 정보를 찾을 수 없습니다."),
    PRODUCT_NAME_DUPLICATED(HttpStatus.CONFLICT, "상품: 이미 사용 중인 상품명입니다."),
    PRODUCT_NAME_INVALID(HttpStatus.BAD_REQUEST, "상품: 상품명 형식이 올바르지 않습니다."),

    PRODUCT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "상품: 상품 상태 값이 올바르지 않습니다."),
    PRODUCT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "상품: 이미 삭제된 상품입니다."),

    SKU_INVALID(HttpStatus.BAD_REQUEST, "상품: SKU 정보가 올바르지 않습니다."),
    SKU_CODE_DUPLICATED(HttpStatus.CONFLICT, "상품: SKU 코드가 중복입니다."),
    STOCK_INVALID(HttpStatus.BAD_REQUEST, "상품: 재고 수량이 올바르지 않습니다."),
    STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "상품: 재고가 부족합니다."),

    CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "상품: 카테고리 정보가 올바르지 않습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "상품: 카테고리 정보를 찾을 수 없습니다."),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "상품: 잘못된 가격 범위입니다."),
    PRODUCT_SOFT_DELETED(HttpStatus.BAD_REQUEST, "상품: 삭제된 상품입니다.");


    private final HttpStatus status;
    private final String message;
}