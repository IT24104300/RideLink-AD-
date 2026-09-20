package com.ridelink.common.exception;

import org.springframework.http.HttpStatus;

public final class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }
}
