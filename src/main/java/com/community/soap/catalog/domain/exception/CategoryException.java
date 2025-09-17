package com.community.soap.catalog.domain.exception;

import com.community.soap.common.exception.AppException;

public class CategoryException extends AppException {
    public CategoryException(CategoryErrorCode code) {
        super(code);
    }
}