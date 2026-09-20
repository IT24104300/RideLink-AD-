package com.ridelink.driver.web;

import com.ridelink.common.security.SecurityUtils;
import com.ridelink.common.security.UserPrincipal;
import com.ridelink.driver.dto.DriverDtos.DriverResponse;
import com.ridelink.driver.dto.DriverDtos.EligibleDriverResponse;
import com.ridelink.driver.dto.DriverDtos.UpsertDriverRequest;
import com.ridelink.driver.service.DriverProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Drivers")
public class DriverController {

    private final DriverProfileService driverProfileService;

    public DriverController(DriverProfileService driverProfileService) {
        this.driverProfileService = driverProfileService;
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Upsert the authenticated driver's vehicle, availability, and location")
    public DriverResponse upsertMine(@Valid @RequestBody UpsertDriverRequest request) {
        UserPrincipal user = SecurityUtils.currentUser();
        return driverProfileService.upsertForAccount(user.accountId(), user.username(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "View the authenticated driver's operational profile")
    public DriverResponse me() {
        return driverProfileService.getMine(SecurityUtils.currentUser().accountId());
    }

    @GetMapping("/eligible")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "List eligible available drivers for a pickup (used by Ride Service)")
    public List<EligibleDriverResponse> eligible(@RequestParam(required = false) String pickup) {
        return driverProfileService.findEligible(pickup);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "Get a driver profile by id")
    public DriverResponse getById(@PathVariable UUID id) {
        return driverProfileService.getById(id);
    }
}
