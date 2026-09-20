package com.ridelink.account.dto;

import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class AccountDtos {

    private AccountDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank @Email String email,
            @NotBlank @Size(max = 120) String fullName,
            @Size(max = 30) String phone,
            @NotNull Role role
    ) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record UpdateProfileRequest(
            @Size(max = 120) String fullName,
            @Email String email,
            @Size(max = 30) String phone
    ) {
    }

    public record UpdateStatusRequest(
            @NotNull AccountStatus status
    ) {
    }

    public record AccountResponse(
            UUID id,
            String username,
            String email,
            String fullName,
            String phone,
            Role role,
            AccountStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            UUID accountId,
            String username,
            Role role
    ) {
    }
}
