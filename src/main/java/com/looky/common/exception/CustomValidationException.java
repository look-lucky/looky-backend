package com.looky.common.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class CustomValidationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, String> details; // 여기가 detail 역할

    public CustomValidationException(ErrorCode errorCode, Map<String, String> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
}
