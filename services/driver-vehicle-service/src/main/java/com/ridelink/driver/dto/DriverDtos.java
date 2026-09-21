package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class DriverDtos {

    private DriverDtos() {
    }

    public record UpsertDriverRequest(
            String displayName,
            @NotBlank String vehicleMake,
            @NotBlank String vehicleModel,
            @NotBlank String vehiclePlate,
            String vehicleColor,
            @NotNull Boolean available,
            @NotBlank String serviceArea,
            String locationLabel,
            Double latitude,
            Double longitude
    ) {
    }

    public record DriverResponse(
            UUID id,
            UUID accountId,
            String displayName,
            String vehicleMake,
            String vehicleModel,
            String vehiclePlate,
            String vehicleColor,
            boolean available,
            String serviceArea,
            String locationLabel,
            Double latitude,
            Double longitude,
            Instant updatedAt
    ) {
    }

    public record EligibleDriverResponse(
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
}
