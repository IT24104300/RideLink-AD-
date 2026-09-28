package com.ridelink.ride.service;

import com.ridelink.common.exception.BadRequestException;
import com.ridelink.common.exception.ConflictException;
import com.ridelink.common.security.UserPrincipal;
import com.ridelink.ride.client.DriverVehicleClient;
import com.ridelink.ride.client.FarePaymentClient;
import com.ridelink.ride.domain.Ride;
import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.dto.RideDtos.AssignRideRequest;
import com.ridelink.ride.dto.RideDtos.EligibleDriverView;
import com.ridelink.ride.dto.RideDtos.RecordPaymentRequest;
import com.ridelink.ride.repo.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rides;
    @Mock
    private DriverVehicleClient driverVehicleClient;
    @Mock
    private FarePaymentClient farePaymentClient;

    private RideService service;
    private UserPrincipal passenger;

    @BeforeEach
    void setUp() {
        service = new RideService(rides, driverVehicleClient, farePaymentClient);
        passenger = new UserPrincipal(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "passenger1",
                "PASSENGER"
        );
    }

    @Test
    void assignUsesFirstEligibleDriver() {
        Ride ride = requestedRide();
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rides.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        UUID profileId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID driverAccount = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(driverVehicleClient.findEligible(eq("Colombo Fort"), any())).thenReturn(List.of(
                new EligibleDriverView(profileId, driverAccount, "Demo Driver", "CAB-1234", "Colombo", "Colombo Fort", true, 10)
        ));

        var result = service.assign(ride.getId(), new AssignRideRequest(null, null), passenger, "Bearer test");

        assertEquals(RideStatus.ASSIGNED, result.status());
        assertEquals(driverAccount, result.driverAccountId());
        assertEquals(profileId, result.driverProfileId());
    }

    @Test
    void assignFailsWhenNoEligibleDriver() {
        Ride ride = requestedRide();
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(driverVehicleClient.findEligible(eq("Colombo Fort"), any())).thenReturn(List.of());

        assertThrows(ConflictException.class,
                () -> service.assign(ride.getId(), null, passenger, "Bearer test"));
    }

    @Test
    void rejectsInvalidTransition() {
        Ride ride = requestedRide();
        assertThrows(BadRequestException.class, () -> RideService.transition(ride, RideStatus.COMPLETED));
        assertEquals(RideStatus.REQUESTED, ride.getStatus());
    }

    @Test
    void completeRequestedRideReturnsInvalidTransition() {
        Ride ride = requestedRide();
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));
        UserPrincipal driver = new UserPrincipal(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "driver1",
                "DRIVER"
        );
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.complete(ride.getId(), driver, "Bearer test"));
        assertEquals("INVALID_TRANSITION", ex.getCode());
        assertEquals(RideStatus.REQUESTED, ride.getStatus());
    }

    @Test
    void recordPaymentAttachesIdOnCompletedRide() {
        Ride ride = requestedRide();
        ride.setStatus(RideStatus.COMPLETED);
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rides.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        UUID paymentId = UUID.fromString("55555555-5555-5555-5555-555555555555");

        var result = service.recordPayment(ride.getId(), new RecordPaymentRequest(paymentId), passenger);

        assertEquals(paymentId, result.paymentId());
    }

    @Test
    void recordPaymentRejectedUnlessCompleted() {
        Ride ride = requestedRide();
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));

        assertThrows(BadRequestException.class,
                () -> service.recordPayment(ride.getId(), new RecordPaymentRequest(UUID.randomUUID()), passenger));
    }

    @Test
    void cancelAllowedForPassenger() {
        Ride ride = requestedRide();
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rides.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.cancel(ride.getId(), passenger);
        assertEquals(RideStatus.CANCELLED, result.status());
    }

    @Test
    void cancelRejectsUnrelatedUser() {
        Ride ride = requestedRide();
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));
        UserPrincipal stranger = new UserPrincipal(UUID.randomUUID(), "stranger", "PASSENGER");

        assertThrows(com.ridelink.common.exception.ForbiddenException.class,
                () -> service.cancel(ride.getId(), stranger));
    }

    @Test
    void acceptRejectsUnassignedDriver() {
        Ride ride = requestedRide();
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverAccountId(UUID.randomUUID());
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));

        UserPrincipal otherDriver = new UserPrincipal(UUID.randomUUID(), "otherDriver", "DRIVER");
        assertThrows(com.ridelink.common.exception.ForbiddenException.class,
                () -> service.accept(ride.getId(), otherDriver));
    }

    @Test
    void completeGracefullyDegradesWhenFareServiceFails() {
        Ride ride = requestedRide();
        ride.setStatus(RideStatus.IN_PROGRESS);
        UUID driverId = UUID.randomUUID();
        ride.setDriverAccountId(driverId);
        when(rides.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rides.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        when(farePaymentClient.calculateFinal(eq(ride.getId()), any(), any(), any()))
                .thenThrow(new com.ridelink.common.exception.UpstreamException("Fare service down"));

        UserPrincipal driver = new UserPrincipal(driverId, "driver1", "DRIVER");
        var result = service.complete(ride.getId(), driver, "Bearer test");

        assertEquals(RideStatus.COMPLETED, result.status());
        org.junit.jupiter.api.Assertions.assertTrue(result.fareNote().contains("pending"));
    }

    private Ride requestedRide() {
        Ride ride = new Ride();
        ride.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        ride.setPassengerAccountId(passenger.accountId());
        ride.setPickup("Colombo Fort");
        ride.setDestination("Kandy");
        ride.setStatus(RideStatus.REQUESTED);
        return ride;
    }
}
