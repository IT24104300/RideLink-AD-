package com.ridelink.common.exception;

import org.springframework.http.HttpStatus;

public final class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}
