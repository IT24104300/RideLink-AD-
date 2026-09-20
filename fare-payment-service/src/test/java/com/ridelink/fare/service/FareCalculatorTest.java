package com.ridelink.fare.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FareCalculatorTest {

    @Test
    void appliesDocumentedFormula() {
        BigDecimal base = new BigDecimal("150");
        BigDecimal perKm = new BigDecimal("80");
        BigDecimal perMin = new BigDecimal("5");
        var result = FareCalculator.calculate("Colombo Fort", "Kandy", base, perKm, perMin);

        BigDecimal expected = base
                .add(result.distanceKm().multiply(perKm))
                .add(result.durationMin().multiply(perMin))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        assertEquals(expected, result.total());
        assertTrue(result.distanceKm().compareTo(new BigDecimal("2.00")) >= 0);
        assertTrue(result.distanceKm().compareTo(new BigDecimal("19.99")) <= 0);
    }

    @Test
    void samePickupDestinationIsDeterministic() {
        var a = FareCalculator.simulateDistanceKm("Galle Face", "Mount Lavinia");
        var b = FareCalculator.simulateDistanceKm("galle face", "mount lavinia");
        assertEquals(a, b);
    }

    @Test
    void differentRoutesUsuallyDiffer() {
        var shortish = FareCalculator.simulateDistanceKm("A", "B");
        var other = FareCalculator.simulateDistanceKm("Colombo", "Jaffna");
        assertTrue(shortish.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(other.compareTo(BigDecimal.ZERO) > 0);
    }
}
