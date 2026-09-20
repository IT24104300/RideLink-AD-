package com.ridelink.driver.service;

import com.ridelink.driver.domain.DriverProfile;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Eligibility: driver must be available, have a vehicle plate, and match the pickup
 * against service area (case-insensitive contains). Results are ordered by a dummy
 * distance score derived from location vs pickup strings — not real maps.
 */
public final class DriverEligibilityFilter {

    private DriverEligibilityFilter() {
    }

    public static boolean isEligible(DriverProfile profile, String pickup) {
        if (profile == null || !profile.isAvailable()) {
            return false;
        }
        if (isBlank(profile.getVehiclePlate())) {
            return false;
        }
        if (isBlank(pickup) || isBlank(profile.getServiceArea())) {
            return true;
        }
        String p = pickup.toLowerCase(Locale.ROOT);
        String area = profile.getServiceArea().toLowerCase(Locale.ROOT);
        String location = profile.getLocationLabel() == null ? "" : profile.getLocationLabel().toLowerCase(Locale.ROOT);
        return p.contains(area) || area.contains(p) || location.contains(p) || p.contains(location);
    }

    public static int dummyDistanceScore(DriverProfile profile, String pickup) {
        String left = safe(profile.getLocationLabel()) + "|" + safe(profile.getServiceArea());
        String right = safe(pickup);
        return Math.abs((left + "->" + right).hashCode() % 500);
    }

    public static List<DriverProfile> filterAndSort(List<DriverProfile> profiles, String pickup) {
        return profiles.stream()
                .filter(p -> isEligible(p, pickup))
                .sorted(Comparator.comparingInt(p -> dummyDistanceScore(p, pickup)))
                .toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
