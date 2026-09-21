package com.ridelink.fare.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Documented fare rule:
 *   total = base + (distanceKm * perKm) + (durationMin * perMin)
 *
 * Distance is simulated from pickup/destination strings (no live maps):
 *   distanceKm = 2.00 + ((hash % 1800) / 100.0)  → 2.00 .. 19.99 km
 * Duration assumes ~30 km/h average:
 *   durationMin = distanceKm / 0.5
 */
public final class FareCalculator {

    private FareCalculator() {
    }

    public record Breakdown(
            BigDecimal distanceKm,
            BigDecimal durationMin,
            BigDecimal base,
            BigDecimal perKm,
            BigDecimal perMin,
            BigDecimal total
    ) {
    }

    public static Breakdown calculate(String pickup, String destination, BigDecimal base, BigDecimal perKm, BigDecimal perMin) {
        BigDecimal distanceKm = simulateDistanceKm(pickup, destination);
        BigDecimal durationMin = distanceKm.divide(new BigDecimal("0.5"), 2, RoundingMode.HALF_UP);
        BigDecimal distanceComponent = distanceKm.multiply(perKm);
        BigDecimal durationComponent = durationMin.multiply(perMin);
        BigDecimal total = base.add(distanceComponent).add(durationComponent).setScale(2, RoundingMode.HALF_UP);
        return new Breakdown(
                distanceKm.setScale(2, RoundingMode.HALF_UP),
                durationMin,
                base.setScale(2, RoundingMode.HALF_UP),
                perKm.setScale(2, RoundingMode.HALF_UP),
                perMin.setScale(2, RoundingMode.HALF_UP),
                total
        );
    }

    public static BigDecimal simulateDistanceKm(String pickup, String destination) {
        String left = pickup == null ? "" : pickup.trim().toLowerCase();
        String right = destination == null ? "" : destination.trim().toLowerCase();
        int hash = Math.abs((left + "|" + right).hashCode());
        double km = 2.00 + (hash % 1800) / 100.0;
        return BigDecimal.valueOf(km).setScale(2, RoundingMode.HALF_UP);
    }
}
