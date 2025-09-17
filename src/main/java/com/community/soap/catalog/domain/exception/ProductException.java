package com.community.soap.catalog.domain.exception;

import com.community.soap.common.exception.AppException;

public class ProductException extends AppException {
    public ProductException(ProductErrorCode code) {
        super(code);
    }
}