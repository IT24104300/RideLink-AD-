package com.ridelink.ride.domain;

import java.util.Map;
import java.util.Set;

/**
 * Enum defining valid states and state transition rules for a ride lifecycle.
 * Valid forward progression: REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED.
 * Any non-terminal state may also transition to CANCELLED.
 */
public enum RideStatus {
    /** Ride requested by passenger; awaiting driver assignment */
    REQUESTED,
    /** Eligible driver assigned to the ride */
    ASSIGNED,
    /** Driver accepted the assigned ride */
    ACCEPTED,
    /** Ride currently in progress / underway */
    IN_PROGRESS,
    /** Ride completed; fare calculated and finalized */
    COMPLETED,
    /** Ride cancelled by passenger, driver, or admin */
    CANCELLED;

    /** Map of valid destination states for each origin state */
    private static final Map<RideStatus, Set<RideStatus>> ALLOWED = Map.of(
            REQUESTED, Set.of(ASSIGNED, CANCELLED),
            ASSIGNED, Set.of(ACCEPTED, CANCELLED),
            ACCEPTED, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(COMPLETED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    /**
     * Checks if transitioning from this status to the target status is allowed.
     *
     * @param next the candidate destination status
     * @return true if the transition is valid according to the state machine rules
     */
    public boolean canTransitionTo(RideStatus next) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(next);
    }

    /**
     * Returns an unmodifiable map of all allowed state transitions.
     *
     * @return map of status to allowed target statuses
     */
    public static Map<RideStatus, Set<RideStatus>> allowedTransitions() {
        return ALLOWED;
    }
}
