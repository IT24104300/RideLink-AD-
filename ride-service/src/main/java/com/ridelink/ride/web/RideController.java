package com.ridelink.ride.web;

import com.ridelink.common.security.SecurityUtils;
import com.ridelink.common.security.UserPrincipal;
import com.ridelink.ride.dto.RideDtos.AssignRideRequest;
import com.ridelink.ride.dto.RideDtos.CreateRideRequest;
import com.ridelink.ride.dto.RideDtos.RideResponse;
import com.ridelink.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
@Tag(name = "Rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Create a ride request")
    public RideResponse create(@Valid @RequestBody CreateRideRequest request) {
        return rideService.create(SecurityUtils.currentUser(), request);
    }

    @GetMapping("/me")
    @Operation(summary = "List rides for the authenticated user")
    public List<RideResponse> mine() {
        return rideService.listMine(SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a ride by id")
    public RideResponse get(@PathVariable UUID id) {
        return rideService.get(id, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @Operation(summary = "Assign the first eligible driver (or a specific eligible driver)")
    public RideResponse assign(
            @PathVariable UUID id,
            @RequestBody(required = false) AssignRideRequest request,
            HttpServletRequest httpRequest
    ) {
        return rideService.assign(id, request, SecurityUtils.currentUser(), httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Assigned driver accepts the ride")
    public RideResponse accept(@PathVariable UUID id) {
        return rideService.accept(id, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Start the ride (IN_PROGRESS)")
    public RideResponse start(@PathVariable UUID id) {
        return rideService.start(id, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Complete the ride and request a final fare")
    public RideResponse complete(@PathVariable UUID id, HttpServletRequest httpRequest) {
        return rideService.complete(id, SecurityUtils.currentUser(), httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a ride if the transition is valid")
    public RideResponse cancel(@PathVariable UUID id) {
        return rideService.cancel(id, SecurityUtils.currentUser());
    }
}
