package com.ridelink.ride.domain;

import java.util.Map;
import java.util.Set;

public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    private static final Map<RideStatus, Set<RideStatus>> ALLOWED = Map.of(
            REQUESTED, Set.of(ASSIGNED, CANCELLED),
            ASSIGNED, Set.of(ACCEPTED, CANCELLED),
            ACCEPTED, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(COMPLETED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    public boolean canTransitionTo(RideStatus next) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(next);
    }

    public static Map<RideStatus, Set<RideStatus>> allowedTransitions() {
        return ALLOWED;
    }
}
