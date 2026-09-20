package com.ridelink.driver.service;

import com.ridelink.common.exception.ForbiddenException;
import com.ridelink.common.exception.NotFoundException;
import com.ridelink.driver.domain.DriverProfile;
import com.ridelink.driver.dto.DriverDtos.DriverResponse;
import com.ridelink.driver.dto.DriverDtos.EligibleDriverResponse;
import com.ridelink.driver.dto.DriverDtos.UpsertDriverRequest;
import com.ridelink.driver.repo.DriverProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DriverProfileService {

    private final DriverProfileRepository profiles;

    public DriverProfileService(DriverProfileRepository profiles) {
        this.profiles = profiles;
    }

    @Transactional
    public DriverResponse upsertForAccount(UUID accountId, String username, UpsertDriverRequest request) {
        DriverProfile profile = profiles.findByAccountId(accountId).orElseGet(DriverProfile::new);
        profile.setAccountId(accountId);
        profile.setDisplayName(request.displayName() == null || request.displayName().isBlank() ? username : request.displayName());
        profile.setVehicleMake(request.vehicleMake());
        profile.setVehicleModel(request.vehicleModel());
        profile.setVehiclePlate(request.vehiclePlate());
        profile.setVehicleColor(request.vehicleColor());
        profile.setAvailable(Boolean.TRUE.equals(request.available()));
        profile.setServiceArea(request.serviceArea());
        profile.setLocationLabel(request.locationLabel());
        profile.setLatitude(request.latitude());
        profile.setLongitude(request.longitude());
        return toResponse(profiles.save(profile));
    }

    @Transactional(readOnly = true)
    public DriverResponse getMine(UUID accountId) {
        return toResponse(profiles.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("Driver profile not found")));
    }

    @Transactional(readOnly = true)
    public DriverResponse getById(UUID id) {
        return toResponse(profiles.findById(id)
                .orElseThrow(() -> new NotFoundException("Driver profile not found")));
    }

    @Transactional(readOnly = true)
    public List<EligibleDriverResponse> findEligible(String pickup) {
        return DriverEligibilityFilter.filterAndSort(profiles.findByAvailableTrue(), pickup).stream()
                .map(p -> new EligibleDriverResponse(
                        p.getId(),
                        p.getAccountId(),
                        p.getDisplayName(),
                        p.getVehiclePlate(),
                        p.getServiceArea(),
                        p.getLocationLabel(),
                        p.isAvailable(),
                        DriverEligibilityFilter.dummyDistanceScore(p, pickup)
                ))
                .toList();
    }

    public void assertOwner(UUID accountId, UUID profileAccountId) {
        if (!accountId.equals(profileAccountId)) {
            throw new ForbiddenException("Not the owner of this driver profile");
        }
    }

    private DriverResponse toResponse(DriverProfile p) {
        return new DriverResponse(
                p.getId(),
                p.getAccountId(),
                p.getDisplayName(),
                p.getVehicleMake(),
                p.getVehicleModel(),
                p.getVehiclePlate(),
                p.getVehicleColor(),
                p.isAvailable(),
                p.getServiceArea(),
                p.getLocationLabel(),
                p.getLatitude(),
                p.getLongitude(),
                p.getUpdatedAt()
        );
    }
}
