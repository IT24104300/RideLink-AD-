package com.ridelink.ride.web;

import com.ridelink.common.security.SecurityUtils;
import com.ridelink.common.security.UserPrincipal;
import com.ridelink.ride.dto.RideDtos.AssignRideRequest;
import com.ridelink.ride.dto.RideDtos.CreateRideRequest;
import com.ridelink.ride.dto.RideDtos.RecordPaymentRequest;
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

/**
 * REST controller exposing endpoints for Ride Management operations.
 * Handles ride requests, role-based queries, driver assignments, lifecycle transitions,
 * and payment associations.
 */
@RestController
@RequestMapping("/api/rides")
@Tag(name = "Rides")
public class RideController {

    private final RideService rideService;

    /**
     * Constructs RideController with the required RideService business delegate.
     *
     * @param rideService service handling ride business logic
     */
    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    /**
     * Creates a new ride booking request.
     * Restricted to authenticated passengers.
     *
     * @param request creation request payload containing pickup and destination
     * @return the newly created ride response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PASSENGER')")
    @Operation(summary = "Create a ride request")
    public RideResponse create(@Valid @RequestBody CreateRideRequest request) {
        return rideService.create(SecurityUtils.currentUser(), request);
    }

    /**
     * Lists rides relevant to the current authenticated user (passenger, driver, or admin).
     *
     * @return list of rides for the caller
     */
    @GetMapping("/me")
    @Operation(summary = "List rides for the authenticated user")
    public List<RideResponse> mine() {
        return rideService.listMine(SecurityUtils.currentUser());
    }

    /**
     * Retrieves ride details by UUID.
     * Accessible by the associated passenger, driver, or admin.
     *
     * @param id ride UUID
     * @return ride details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a ride by id")
    public RideResponse get(@PathVariable UUID id) {
        return rideService.get(id, SecurityUtils.currentUser());
    }

    /**
     * Assigns an eligible driver to the ride.
     * Can be invoked by the passenger who created the ride or an admin.
     *
     * @param id ride UUID
     * @param request optional driver selection request
     * @param httpRequest HTTP servlet request used to forward the Bearer token
     * @return updated ride response with status ASSIGNED
     */
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

    /**
     * Accepts the assigned ride.
     * Restricted to the assigned driver or admin.
     *
     * @param id ride UUID
     * @return updated ride response with status ACCEPTED
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Assigned driver accepts the ride")
    public RideResponse accept(@PathVariable UUID id) {
        return rideService.accept(id, SecurityUtils.currentUser());
    }

    /**
     * Starts the ride, setting its status to IN_PROGRESS.
     * Restricted to the assigned driver or admin.
     *
     * @param id ride UUID
     * @return updated ride response with status IN_PROGRESS
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Start the ride (IN_PROGRESS)")
    public RideResponse start(@PathVariable UUID id) {
        return rideService.start(id, SecurityUtils.currentUser());
    }

    /**
     * Completes the ride and triggers final fare calculation with Fare & Payment Service.
     * Restricted to the assigned driver or admin.
     *
     * @param id ride UUID
     * @param httpRequest HTTP servlet request to extract Authorization header
     * @return updated ride response with status COMPLETED
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Complete the ride and request a final fare")
    public RideResponse complete(@PathVariable UUID id, HttpServletRequest httpRequest) {
        return rideService.complete(id, SecurityUtils.currentUser(), httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * Cancels the ride if valid within the current state transition model.
     * Can be called by the passenger, assigned driver, or admin.
     *
     * @param id ride UUID
     * @return updated ride response with status CANCELLED
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a ride if the transition is valid")
    public RideResponse cancel(@PathVariable UUID id) {
        return rideService.cancel(id, SecurityUtils.currentUser());
    }

    /**
     * Attaches a completed payment transaction ID to a completed ride.
     * Restricted to passenger or admin.
     *
     * @param id ride UUID
     * @param request payload containing payment ID
     * @return updated ride response with payment details attached
     */
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @Operation(summary = "Attach a completed payment id to this ride")
    public RideResponse recordPayment(@PathVariable UUID id, @Valid @RequestBody RecordPaymentRequest request) {
        return rideService.recordPayment(id, request, SecurityUtils.currentUser());
    }
}
