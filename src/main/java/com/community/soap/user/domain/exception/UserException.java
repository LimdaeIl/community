package com.community.soap.user.domain.exception;

import com.community.soap.common.exception.AppException;

public class UserException extends AppException {
    public UserException(UserErrorCode code) { super(code); }
}
