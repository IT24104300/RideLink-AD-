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

@Entity
@Table(name = "rides")
public class Ride {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID passengerAccountId;

    private UUID driverAccountId;
    private UUID driverProfileId;

    @Column(nullable = false)
    private String pickup;

    @Column(nullable = false)
    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status = RideStatus.REQUESTED;

    private UUID fareId;
    private BigDecimal finalFareAmount;
    private String fareNote;
    private UUID paymentId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

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
