package com.ridelink.fare.dto;

import com.ridelink.fare.domain.FareType;
import com.ridelink.fare.domain.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class FareDtos {

    private FareDtos() {
    }

    public record EstimateRequest(
            @NotBlank String pickup,
            @NotBlank String destination,
            UUID rideId
    ) {
    }

    public record FareResponse(
            UUID id,
            UUID rideId,
            FareType type,
            String pickup,
            String destination,
            BigDecimal distanceKm,
            BigDecimal durationMin,
            BigDecimal baseFare,
            BigDecimal perKmRate,
            BigDecimal perMinRate,
            BigDecimal total,
            String currency,
            String formula,
            Instant createdAt
    ) {
    }

    public record PaymentRequest(
            @NotNull UUID rideId,
            UUID fareId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            String currency,
            String method,
            String cardLast4,
            Boolean simulateFailure
    ) {
    }

    public record PaymentResponse(
            UUID id,
            UUID rideId,
            UUID fareId,
            UUID accountId,
            BigDecimal amount,
            String currency,
            String method,
            PaymentStatus status,
            String receiptNumber,
            String failureReason,
            Instant createdAt
    ) {
    }
}
