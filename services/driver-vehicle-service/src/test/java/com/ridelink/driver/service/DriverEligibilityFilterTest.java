package com.ridelink.driver.service;

import com.ridelink.driver.domain.DriverProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverEligibilityFilterTest {

    @Test
    void unavailableDriverIsExcluded() {
        DriverProfile profile = availableColombo();
        profile.setAvailable(false);
        assertFalse(DriverEligibilityFilter.isEligible(profile, "Colombo Fort"));
    }

    @Test
    void missingPlateIsExcluded() {
        DriverProfile profile = availableColombo();
        profile.setVehiclePlate(" ");
        assertFalse(DriverEligibilityFilter.isEligible(profile, "Colombo"));
    }

    @Test
    void serviceAreaMustRelateToPickup() {
        DriverProfile profile = availableColombo();
        assertTrue(DriverEligibilityFilter.isEligible(profile, "Near Colombo Town Hall"));
        assertFalse(DriverEligibilityFilter.isEligible(profile, "Kandy City Center"));
    }

    @Test
    void sortsByDummyDistanceScore() {
        DriverProfile near = availableColombo();
        near.setLocationLabel("Colombo Fort");
        DriverProfile far = availableColombo();
        far.setLocationLabel("Kottawa");
        far.setServiceArea("Colombo");

        List<DriverProfile> sorted = DriverEligibilityFilter.filterAndSort(List.of(far, near), "Colombo Fort");
        assertEquals(2, sorted.size());
        assertTrue(DriverEligibilityFilter.dummyDistanceScore(sorted.get(0), "Colombo Fort")
                <= DriverEligibilityFilter.dummyDistanceScore(sorted.get(1), "Colombo Fort"));
    }

    private DriverProfile availableColombo() {
        DriverProfile profile = new DriverProfile();
        profile.setAvailable(true);
        profile.setVehiclePlate("CAB-1234");
        profile.setServiceArea("Colombo");
        profile.setLocationLabel("Colombo");
        return profile;
    }
}
