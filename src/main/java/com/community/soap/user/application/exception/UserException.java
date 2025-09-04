package com.community.soap.user.application.exception;

import com.community.soap.common.exception.AppException;

public class UserException extends AppException {
    public UserException(UserErrorCode code) { super(code); }
}
