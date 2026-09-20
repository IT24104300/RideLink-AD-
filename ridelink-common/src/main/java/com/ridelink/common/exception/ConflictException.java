package com.ridelink.common.exception;

import org.springframework.http.HttpStatus;

public final class ConflictException extends ApiException {
    public ConflictException(String message) {
        this("CONFLICT", message);
    }

    public ConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
