package com.ridelink.ride.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying state machine transition rules defined in {@link RideStatus}.
 */
class RideStatusTest {

    /**
     * Verifies that the standard linear progression of ride states is valid.
     */
    @Test
    void happyPathTransitionsAreValid() {
        assertTrue(RideStatus.REQUESTED.canTransitionTo(RideStatus.ASSIGNED));
        assertTrue(RideStatus.ASSIGNED.canTransitionTo(RideStatus.ACCEPTED));
        assertTrue(RideStatus.ACCEPTED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertTrue(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.COMPLETED));
    }

    /**
     * Verifies that rides can be cancelled from any non-terminal state,
     * but completed or already cancelled rides cannot be cancelled or reopened.
     */
    @Test
    void cancellationAllowedUntilCompleted() {
        assertTrue(RideStatus.REQUESTED.canTransitionTo(RideStatus.CANCELLED));
        assertTrue(RideStatus.ASSIGNED.canTransitionTo(RideStatus.CANCELLED));
        assertTrue(RideStatus.ACCEPTED.canTransitionTo(RideStatus.CANCELLED));
        assertTrue(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.CANCELLED));
        assertFalse(RideStatus.COMPLETED.canTransitionTo(RideStatus.CANCELLED));
        assertFalse(RideStatus.CANCELLED.canTransitionTo(RideStatus.REQUESTED));
    }

    /**
     * Verifies that skipping stages or moving backwards through states is prohibited.
     */
    @Test
    void skipsAndBackwardsAreRejected() {
        assertFalse(RideStatus.REQUESTED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertFalse(RideStatus.ASSIGNED.canTransitionTo(RideStatus.COMPLETED));
        assertFalse(RideStatus.COMPLETED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertFalse(RideStatus.ACCEPTED.canTransitionTo(RideStatus.ASSIGNED));
    }
}
