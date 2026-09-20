package com.ridelink.ride.dto;

import com.ridelink.ride.domain.RideStatus;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class RideDtos {

    private RideDtos() {
    }

    public record CreateRideRequest(
            @NotBlank String pickup,
            @NotBlank String destination
    ) {
    }

    public record AssignRideRequest(
            UUID driverProfileId,
            UUID driverAccountId
    ) {
    }

    public record RideResponse(
            UUID id,
            UUID passengerAccountId,
            UUID driverAccountId,
            UUID driverProfileId,
            String pickup,
            String destination,
            RideStatus status,
            UUID fareId,
            BigDecimal finalFareAmount,
            String fareNote,
            UUID paymentId,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record EligibleDriverView(
            UUID driverProfileId,
            UUID accountId,
            String displayName,
            String vehiclePlate,
            String serviceArea,
            String locationLabel,
            boolean available,
            int dummyDistanceScore
    ) {
    }

    public record FareView(
            UUID id,
            BigDecimal total,
            String currency,
            String formula
    ) {
    }
}
