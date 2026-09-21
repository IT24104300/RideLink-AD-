package com.ridelink.common.security;

import java.util.UUID;

public record UserPrincipal(UUID accountId, String username, String role) {
}
