package com.community.soap.user.application.exception;

import com.community.soap.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_INVALID(HttpStatus.BAD_REQUEST, "회원: 잘못된 회원 정보입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원: 회원 정보를 찾을 수 없습니다."),

    PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "비밀번호: 비밀번호 형식이 올바르지 않습니다."),
    PASSWORD_INCORRECT(HttpStatus.BAD_REQUEST, "비밀번호: 비밀번호가 틀립니다."),
    PASSWORD_NULL(HttpStatus.BAD_REQUEST, "비밀번호: 비밀번호 입력은 필수입니다."),

    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이메일: 이미 사용 중인 이메일입니다."),
    EMAIL_INVALID(HttpStatus.BAD_REQUEST, "이메일: 이메일 형식이 올바르지 않습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일: 이메일을 찾을 수 없습니다."),

    EMAIL_VERIFICATION_COOLTIME(HttpStatus.TOO_MANY_REQUESTS, "이메일 인증: 재요청 대기 시간입니다."),
    EMAIL_VERIFICATION_BLOCKED(HttpStatus.FORBIDDEN, "이메일 인증: 시도 횟수 초과로 잠시 차단되었습니다."),
    EMAIL_VERIFICATION_NOT_REQUESTED(HttpStatus.BAD_REQUEST, "이메일 인증: 먼저 인증 코드를 요청해 주세요."),
    EMAIL_VERIFY_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "이메일 인증: 인증 코드가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
