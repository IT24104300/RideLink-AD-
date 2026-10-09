package com.ridelink.ride.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a Ride booking in the system.
 * Tracks the complete lifecycle from initial REQUESTED state to COMPLETED or CANCELLED,
 * including passenger, assigned driver, location details, fare, and payment reference.
 */
@Entity
@Table(name = "rides")
public class Ride {

    /** Unique identifier for the ride */
    @Id
    private UUID id;

    /** Account ID of the passenger who requested the ride */
    @Column(nullable = false)
    private UUID passengerAccountId;

    /** Account ID of the assigned driver */
    private UUID driverAccountId;

    /** Profile ID of the assigned driver in Driver & Vehicle Service */
    private UUID driverProfileId;

    /** Pickup address or landmark */
    @Column(nullable = false)
    private String pickup;

    /** Drop-off destination address or landmark */
    @Column(nullable = false)
    private String destination;

    /** Current lifecycle status of the ride */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status = RideStatus.REQUESTED;

    /** Reference ID to the calculated fare from Fare & Payment Service */
    private UUID fareId;

    /** Calculated total fare amount */
    private BigDecimal finalFareAmount;

    /** Descriptive note regarding fare calculation status */
    private String fareNote;

    /** Reference ID of the completed payment record */
    private UUID paymentId;

    /** Timestamp when the ride record was created */
    @Column(nullable = false)
    private Instant createdAt;

    /** Timestamp when the ride record was last updated */
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Entity lifecycle callback before initial persist.
     * Generates a UUID if not set and initializes creation/update timestamps.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Entity lifecycle callback before update to refresh updatedAt timestamp.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPassengerAccountId() {
        return passengerAccountId;
    }

    public void setPassengerAccountId(UUID passengerAccountId) {
        this.passengerAccountId = passengerAccountId;
    }

    public UUID getDriverAccountId() {
        return driverAccountId;
    }

    public void setDriverAccountId(UUID driverAccountId) {
        this.driverAccountId = driverAccountId;
    }

    public UUID getDriverProfileId() {
        return driverProfileId;
    }

    public void setDriverProfileId(UUID driverProfileId) {
        this.driverProfileId = driverProfileId;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public UUID getFareId() {
        return fareId;
    }

    public void setFareId(UUID fareId) {
        this.fareId = fareId;
    }

    public BigDecimal getFinalFareAmount() {
        return finalFareAmount;
    }

    public void setFinalFareAmount(BigDecimal finalFareAmount) {
        this.finalFareAmount = finalFareAmount;
    }

    public String getFareNote() {
        return fareNote;
    }

    public void setFareNote(String fareNote) {
        this.fareNote = fareNote;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
