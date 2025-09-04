package com.community.soap.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int code,
        String message,
        List<FieldError> data
) {
    public static ApiErrorResponse of(HttpStatus status, String message, List<FieldError> data) {
        return new ApiErrorResponse(status.value(), message, (data == null || data.isEmpty()) ? null : data);
    }
    public static ApiErrorResponse of(HttpStatus status, String message) {
        return new ApiErrorResponse(status.value(), message, null);
    }

    public record FieldError(String field, String reason) {
        public static FieldError of(String field, String reason) { return new FieldError(field, reason); }
    }
}

