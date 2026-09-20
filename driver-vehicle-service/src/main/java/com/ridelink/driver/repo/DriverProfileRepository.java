package com.ridelink.driver.repo;

import com.ridelink.driver.domain.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {
    Optional<DriverProfile> findByAccountId(UUID accountId);

    List<DriverProfile> findByAvailableTrue();
}
