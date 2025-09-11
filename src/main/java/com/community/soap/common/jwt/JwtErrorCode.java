package com.community.soap.common.jwt;

import com.community.soap.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum JwtErrorCode implements ErrorCode {

    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT: 만료된 토큰입니다."),
    MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "JWT: JWT 형식이 잘못되었습니다."),
    TAMPERED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT: JWT 서명이 위조되었거나 무결성이 손상되었습니다."),
    NOT_FOUND_TOKEN(HttpStatus.UNAUTHORIZED, "JWT: 토큰을 찾을 수 없습니다."),
    INVALID_BEARER_TOKEN(HttpStatus.UNAUTHORIZED, "JWT: 유효하지 않은 토큰입니다.");

    private final HttpStatus status;
    private final String message;
}
