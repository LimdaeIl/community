package com.community.soap.common.jwt;

import com.community.soap.common.exception.AppException;
import com.community.soap.common.exception.ErrorCode;

public class TokenException extends AppException {

    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
