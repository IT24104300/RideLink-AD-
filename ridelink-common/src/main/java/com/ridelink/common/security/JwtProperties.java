package com.ridelink.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ridelink.jwt")
public class JwtProperties {

    /**
     * HMAC secret. Must be at least 32 characters. Override with JWT_SECRET in every environment.
     */
    private String secret = "ridelink-dev-only-change-me-please-32chars-min";
    private long expirationMs = 86_400_000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
