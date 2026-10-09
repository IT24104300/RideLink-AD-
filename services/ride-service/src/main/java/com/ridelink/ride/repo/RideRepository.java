package com.ridelink.ride.repo;

import com.ridelink.ride.domain.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Ride} entities.
 * Provides custom query methods to retrieve rides by participant.
 */
public interface RideRepository extends JpaRepository<Ride, UUID> {

    /**
     * Finds all rides requested by a given passenger, ordered chronologically newest first.
     *
     * @param passengerAccountId account UUID of the passenger
     * @return list of rides requested by this passenger
     */
    List<Ride> findByPassengerAccountIdOrderByCreatedAtDesc(UUID passengerAccountId);

    /**
     * Finds all rides assigned to a given driver, ordered chronologically newest first.
     *
     * @param driverAccountId account UUID of the driver
     * @return list of rides driven by this driver
     */
    List<Ride> findByDriverAccountIdOrderByCreatedAtDesc(UUID driverAccountId);
}

