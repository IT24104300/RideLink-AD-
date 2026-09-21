package com.ridelink.ride.repo;

import com.ridelink.ride.domain.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {
    List<Ride> findByPassengerAccountIdOrderByCreatedAtDesc(UUID passengerAccountId);

    List<Ride> findByDriverAccountIdOrderByCreatedAtDesc(UUID driverAccountId);
}
