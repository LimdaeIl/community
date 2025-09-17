package com.community.soap.catalog.domain.exception;

import com.community.soap.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {
    CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "카테고리: 잘못된 카테고리 정보입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리: 카테고리 정보를 찾을 수 없습니다."),
    CATEGORY_NAME_DUPLICATED(HttpStatus.CONFLICT, "카테고리: 이미 사용 중인 카테고리명입니다."),
    CATEGORY_NAME_INVALID(HttpStatus.BAD_REQUEST, "카테고리: 카테고리명 형식이 올바르지 않습니다."),
    CATEGORY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "카테고리: 이미 삭제된 카테고리입니다.");

    private final HttpStatus status;
    private final String message;
}