package com.ridelink.common.exception;

import org.springframework.http.HttpStatus;

public final class UpstreamException extends ApiException {
    public UpstreamException(String message) {
        super(HttpStatus.BAD_GATEWAY, "UPSTREAM_UNAVAILABLE", message);
    }
}
