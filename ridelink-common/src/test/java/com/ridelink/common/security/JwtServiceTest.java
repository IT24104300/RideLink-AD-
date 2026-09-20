package com.ridelink.common.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void issuesAndParsesTokenWithRoleClaim() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("ridelink-dev-only-change-me-please-32chars-min");
        JwtService jwtService = new JwtService(properties);
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");

        String token = jwtService.issueToken(id, "passenger1", "PASSENGER");
        var claims = jwtService.parse(token);

        assertEquals(id.toString(), claims.getSubject());
        assertEquals("passenger1", claims.get("username", String.class));
        assertEquals("PASSENGER", claims.get("role", String.class));
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void rejectsTamperedToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("ridelink-dev-only-change-me-please-32chars-min");
        JwtService jwtService = new JwtService(properties);
        String token = jwtService.issueToken(UUID.randomUUID(), "driver1", "DRIVER");

        assertThrows(Exception.class, () -> jwtService.parse(token + "x"));
    }
}
