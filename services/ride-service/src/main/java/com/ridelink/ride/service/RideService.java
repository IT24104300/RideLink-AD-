package com.ridelink.ride.service;

import com.ridelink.common.exception.BadRequestException;
import com.ridelink.common.exception.ConflictException;
import com.ridelink.common.exception.ForbiddenException;
import com.ridelink.common.exception.NotFoundException;
import com.ridelink.common.exception.UpstreamException;
import com.ridelink.common.security.UserPrincipal;
import com.ridelink.ride.client.DriverVehicleClient;
import com.ridelink.ride.client.FarePaymentClient;
import com.ridelink.ride.domain.Ride;
import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.dto.RideDtos.AssignRideRequest;
import com.ridelink.ride.dto.RideDtos.CreateRideRequest;
import com.ridelink.ride.dto.RideDtos.EligibleDriverView;
import com.ridelink.ride.dto.RideDtos.FareView;
import com.ridelink.ride.dto.RideDtos.RecordPaymentRequest;
import com.ridelink.ride.dto.RideDtos.RideResponse;
import com.ridelink.ride.repo.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Core business service for managing rides.
 * Orchestrates ride creation, driver matching and assignment, state machine transitions,
 * final fare calculation via FarePaymentClient, and payment attachment.
 */
@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rides;
    private final DriverVehicleClient driverVehicleClient;
    private final FarePaymentClient farePaymentClient;

    /**
     * Constructs RideService with necessary repository and downstream clients.
     *
     * @param rides the ride entity repository
     * @param driverVehicleClient client for Driver & Vehicle Service
     * @param farePaymentClient client for Fare & Payment Service
     */
    public RideService(
            RideRepository rides,
            DriverVehicleClient driverVehicleClient,
            FarePaymentClient farePaymentClient
    ) {
        this.rides = rides;
        this.driverVehicleClient = driverVehicleClient;
        this.farePaymentClient = farePaymentClient;
    }

    /**
     * Creates a new ride request with status REQUESTED.
     * Only passengers are authorized to create ride requests.
     *
     * @param user the authenticated passenger principal
     * @param request the ride creation request with pickup and destination
     * @return the created ride response
     */
    @Transactional
    public RideResponse create(UserPrincipal user, CreateRideRequest request) {
        requireRole(user, "PASSENGER");
        Ride ride = new Ride();
        ride.setPassengerAccountId(user.accountId());
        ride.setPickup(request.pickup().trim());
        ride.setDestination(request.destination().trim());
        ride.setStatus(RideStatus.REQUESTED);
        return toResponse(rides.save(ride));
    }

    /**
     * Retrieves a single ride by its UUID.
     * Accessible only by the ride's passenger, assigned driver, or an administrator.
     *
     * @param id the UUID of the ride
     * @param user the authenticated user principal
     * @return the ride response
     */
    @Transactional(readOnly = true)
    public RideResponse get(UUID id, UserPrincipal user) {
        Ride ride = require(id);
        assertCanView(ride, user);
        return toResponse(ride);
    }

    /**
     * Lists rides associated with the authenticated user based on role:
     * - DRIVER: rides where driverAccountId matches
     * - ADMIN: all rides across the system
     * - PASSENGER: rides where passengerAccountId matches
     *
     * @param user the authenticated user principal
     * @return list of rides ordered newest first
     */
    @Transactional(readOnly = true)
    public List<RideResponse> listMine(UserPrincipal user) {
        if ("DRIVER".equals(user.role())) {
            return rides.findByDriverAccountIdOrderByCreatedAtDesc(user.accountId()).stream().map(this::toResponse).toList();
        }
        if ("ADMIN".equals(user.role())) {
            return rides.findAll().stream().map(this::toResponse).toList();
        }
        return rides.findByPassengerAccountIdOrderByCreatedAtDesc(user.accountId()).stream().map(this::toResponse).toList();
    }

    /**
     * Assignment rule: call Driver & Vehicle Service for eligible available drivers,
     * then pick the first (lowest dummy distance score). If driverProfileId is supplied,
     * that driver must appear in the eligible list.
     *
     * @param rideId the UUID of the ride to assign
     * @param request optional specific driver profile ID to assign
     * @param user the authenticated passenger or admin
     * @param authorizationHeader the incoming Bearer JWT header to forward to driver service
     * @return updated ride response with status ASSIGNED
     */
    @Transactional
    public RideResponse assign(UUID rideId, AssignRideRequest request, UserPrincipal user, String authorizationHeader) {
        Ride ride = require(rideId);
        assertPassengerOrAdmin(ride, user);
        transition(ride, RideStatus.ASSIGNED);

        // Fetch eligible drivers from Driver & Vehicle Service near the pickup location
        List<EligibleDriverView> eligible = driverVehicleClient.findEligible(ride.getPickup(), authorizationHeader);
        if (eligible.isEmpty()) {
            throw new ConflictException("NO_DRIVER_AVAILABLE", "No eligible available driver for this pickup");
        }

        // Default to first eligible driver (lowest dummy distance score)
        EligibleDriverView chosen = eligible.get(0);
        if (request != null && request.driverProfileId() != null) {
            // If a specific driver was requested, verify they are in the eligible list
            chosen = eligible.stream()
                    .filter(d -> request.driverProfileId().equals(d.driverProfileId()))
                    .findFirst()
                    .orElseThrow(() -> new ConflictException("NO_DRIVER_AVAILABLE", "Requested driver is not eligible or available"));
        }

        ride.setDriverProfileId(chosen.driverProfileId());
        ride.setDriverAccountId(chosen.accountId());
        return toResponse(rides.save(ride));
    }

    /**
     * Accepts an assigned ride. Only the assigned driver (or admin) can accept.
     *
     * @param rideId the UUID of the ride
     * @param user the authenticated driver principal
     * @return updated ride response with status ACCEPTED
     */
    @Transactional
    public RideResponse accept(UUID rideId, UserPrincipal user) {
        Ride ride = require(rideId);
        assertAssignedDriver(ride, user);
        transition(ride, RideStatus.ACCEPTED);
        return toResponse(rides.save(ride));
    }

    /**
     * Starts the ride, moving its state to IN_PROGRESS.
     *
     * @param rideId the UUID of the ride
     * @param user the authenticated driver principal
     * @return updated ride response with status IN_PROGRESS
     */
    @Transactional
    public RideResponse start(UUID rideId, UserPrincipal user) {
        Ride ride = require(rideId);
        assertAssignedDriver(ride, user);
        transition(ride, RideStatus.IN_PROGRESS);
        return toResponse(rides.save(ride));
    }

    /**
     * Completes the ride and triggers final fare calculation with Fare & Payment Service.
     * Gracefully degrades if the Fare service is temporarily down, still completing the ride.
     *
     * @param rideId the UUID of the ride
     * @param user the authenticated driver principal
     * @param authorizationHeader Bearer JWT header to forward to fare service
     * @return updated ride response with status COMPLETED and calculated fare
     */
    @Transactional
    public RideResponse complete(UUID rideId, UserPrincipal user, String authorizationHeader) {
        Ride ride = require(rideId);
        if (!ride.getStatus().canTransitionTo(RideStatus.COMPLETED)) {
            throw new BadRequestException(
                    "INVALID_TRANSITION",
                    "Cannot transition from " + ride.getStatus() + " to " + RideStatus.COMPLETED
            );
        }
        assertAssignedDriver(ride, user);
        ride.setStatus(RideStatus.COMPLETED);
        try {
            // Call Fare & Payment Service to compute final fare based on actual route
            FareView fare = farePaymentClient.calculateFinal(ride.getId(), ride.getPickup(), ride.getDestination(), authorizationHeader);
            if (fare != null) {
                ride.setFareId(fare.id());
                ride.setFinalFareAmount(fare.total());
                ride.setFareNote("Final fare calculated via Fare & Payment Service");
            }
        } catch (UpstreamException ex) {
            // Downstream failure fallback: log warning and note pending fare so the ride completes
            log.warn("Fare service unavailable while completing ride {}: {}", rideId, ex.getMessage());
            ride.setFareNote("Fare service unavailable; final fare pending. Ride still completed.");
        }
        return toResponse(rides.save(ride));
    }

    /**
     * Cancels an existing ride. Allowed for the passenger, assigned driver, or admin.
     *
     * @param rideId the UUID of the ride
     * @param user the authenticated user principal
     * @return updated ride response with status CANCELLED
     */
    @Transactional
    public RideResponse cancel(UUID rideId, UserPrincipal user) {
        Ride ride = require(rideId);
        boolean passenger = ride.getPassengerAccountId().equals(user.accountId());
        boolean driver = ride.getDriverAccountId() != null && ride.getDriverAccountId().equals(user.accountId());
        boolean admin = "ADMIN".equals(user.role());
        if (!passenger && !driver && !admin) {
            throw new ForbiddenException("Only the passenger, assigned driver, or admin may cancel");
        }
        transition(ride, RideStatus.CANCELLED);
        return toResponse(rides.save(ride));
    }

    /**
     * Attaches a completed payment record UUID to a finished ride.
     *
     * @param rideId the UUID of the ride
     * @param request the payment details containing paymentId
     * @param user the authenticated passenger or admin
     * @return updated ride response with payment reference
     */
    @Transactional
    public RideResponse recordPayment(UUID rideId, RecordPaymentRequest request, UserPrincipal user) {
        Ride ride = require(rideId);
        assertPassengerOrAdmin(ride, user);
        if (ride.getStatus() != RideStatus.COMPLETED) {
            throw new BadRequestException(
                    "INVALID_TRANSITION",
                    "Payment can only be attached to a COMPLETED ride"
            );
        }
        ride.setPaymentId(request.paymentId());
        return toResponse(rides.save(ride));
    }

    /**
     * Validates and applies a status transition against the state machine.
     *
     * @param ride the ride entity
     * @param next the desired next status
     * @throws BadRequestException if the transition is illegal
     */
    static void transition(Ride ride, RideStatus next) {
        if (!ride.getStatus().canTransitionTo(next)) {
            throw new BadRequestException(
                    "INVALID_TRANSITION",
                    "Cannot transition from " + ride.getStatus() + " to " + next
            );
        }
        ride.setStatus(next);
    }

    /**
     * Helper to retrieve a ride by UUID or throw NotFoundException.
     */
    private Ride require(UUID id) {
        return rides.findById(id).orElseThrow(() -> new NotFoundException("Ride not found"));
    }

    /**
     * Helper to verify user possesses a specific required role.
     */
    private void requireRole(UserPrincipal user, String role) {
        if (!role.equals(user.role())) {
            throw new ForbiddenException("Requires role " + role);
        }
    }

    /**
     * Asserts that the authenticated user is either an admin or directly involved in the ride.
     */
    private void assertCanView(Ride ride, UserPrincipal user) {
        if ("ADMIN".equals(user.role())) {
            return;
        }
        if (ride.getPassengerAccountId().equals(user.accountId())) {
            return;
        }
        if (ride.getDriverAccountId() != null && ride.getDriverAccountId().equals(user.accountId())) {
            return;
        }
        throw new ForbiddenException("Not allowed to view this ride");
    }

    /**
     * Asserts that the authenticated user is the requesting passenger or an admin.
     */
    private void assertPassengerOrAdmin(Ride ride, UserPrincipal user) {
        if ("ADMIN".equals(user.role()) || ride.getPassengerAccountId().equals(user.accountId())) {
            return;
        }
        throw new ForbiddenException("Only the passenger or an admin may assign a driver");
    }

    /**
     * Asserts that the authenticated user is the assigned driver or an admin.
     */
    private void assertAssignedDriver(Ride ride, UserPrincipal user) {
        if ("ADMIN".equals(user.role())) {
            return;
        }
        if (!"DRIVER".equals(user.role()) || ride.getDriverAccountId() == null
                || !ride.getDriverAccountId().equals(user.accountId())) {
            throw new ForbiddenException("Only the assigned driver may perform this action");
        }
    }

    /**
     * Converts a JPA {@link Ride} entity into a {@link RideResponse} DTO.
     */
    private RideResponse toResponse(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getPassengerAccountId(),
                ride.getDriverAccountId(),
                ride.getDriverProfileId(),
                ride.getPickup(),
                ride.getDestination(),
                ride.getStatus(),
                ride.getFareId(),
                ride.getFinalFareAmount(),
                ride.getFareNote(),
                ride.getPaymentId(),
                ride.getCreatedAt(),
                ride.getUpdatedAt()
        );
    }
}
