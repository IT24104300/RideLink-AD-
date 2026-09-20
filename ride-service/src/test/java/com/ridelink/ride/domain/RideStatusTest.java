package com.ridelink.ride.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RideStatusTest {

    @Test
    void happyPathTransitionsAreValid() {
        assertTrue(RideStatus.REQUESTED.canTransitionTo(RideStatus.ASSIGNED));
        assertTrue(RideStatus.ASSIGNED.canTransitionTo(RideStatus.ACCEPTED));
        assertTrue(RideStatus.ACCEPTED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertTrue(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.COMPLETED));
    }

    @Test
    void cancellationAllowedUntilCompleted() {
        assertTrue(RideStatus.REQUESTED.canTransitionTo(RideStatus.CANCELLED));
        assertTrue(RideStatus.ASSIGNED.canTransitionTo(RideStatus.CANCELLED));
        assertTrue(RideStatus.ACCEPTED.canTransitionTo(RideStatus.CANCELLED));
        assertTrue(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.CANCELLED));
        assertFalse(RideStatus.COMPLETED.canTransitionTo(RideStatus.CANCELLED));
        assertFalse(RideStatus.CANCELLED.canTransitionTo(RideStatus.REQUESTED));
    }

    @Test
    void skipsAndBackwardsAreRejected() {
        assertFalse(RideStatus.REQUESTED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertFalse(RideStatus.ASSIGNED.canTransitionTo(RideStatus.COMPLETED));
        assertFalse(RideStatus.COMPLETED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertFalse(RideStatus.ACCEPTED.canTransitionTo(RideStatus.ASSIGNED));
    }
}
