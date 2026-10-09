package com.ridelink.ride.dto;

import com.ridelink.ride.domain.RideStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Objects (DTOs) for the Ride Management Service.
 * Groups request payloads, response bodies, and upstream service integration models.
 */
public final class RideDtos {

    private RideDtos() {
    }

    /**
     * Request payload to create/request a new ride booking.
     */
    public record CreateRideRequest(
            @NotBlank String pickup,
            @NotBlank String destination
    ) {
    }

    /**
     * Optional payload for assigning a specific driver to a ride.
     */
    public record AssignRideRequest(
            UUID driverProfileId,
            UUID driverAccountId
    ) {
    }

    /**
     * Request payload to record a completed payment ID against a finished ride.
     */
    public record RecordPaymentRequest(
            @NotNull UUID paymentId
    ) {
    }

    /**
     * Complete response payload representing the ride state and associated details.
     */
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

    /**
     * View model of an eligible driver returned from Driver & Vehicle Service.
     */
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

    /**
     * View model of a calculated fare returned from Fare & Payment Service.
     */
    public record FareView(
            UUID id,
            BigDecimal total,
            String currency,
            String formula
    ) {
    }
}
