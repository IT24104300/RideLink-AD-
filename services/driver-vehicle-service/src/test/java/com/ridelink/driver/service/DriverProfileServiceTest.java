package com.ridelink.driver.service;

import com.ridelink.common.exception.ForbiddenException;
import com.ridelink.common.exception.NotFoundException;
import com.ridelink.driver.domain.DriverProfile;
import com.ridelink.driver.dto.DriverDtos.UpsertDriverRequest;
import com.ridelink.driver.repo.DriverProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverProfileServiceTest {

    @Mock
    private DriverProfileRepository profiles;

    private DriverProfileService service;

    @BeforeEach
    void setUp() {
        service = new DriverProfileService(profiles);
    }

    @Test
    void upsertCreatesNewDriverProfile() {
        UUID accountId = UUID.randomUUID();
        when(profiles.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(profiles.save(any(DriverProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpsertDriverRequest request = new UpsertDriverRequest(
                "Driver Dave", "Toyota", "Prius", "WP-ABC-9999", "Silver",
                true, "Colombo", "Colombo Fort", 6.9344, 79.8428
        );

        var result = service.upsertForAccount(accountId, "dave1", request);

        assertNotNull(result);
        assertEquals(accountId, result.accountId());
        assertEquals("Driver Dave", result.displayName());
        assertEquals("WP-ABC-9999", result.vehiclePlate());
        assertTrue(result.available());
    }

    @Test
    void getMineThrowsNotFoundWhenMissing() {
        UUID accountId = UUID.randomUUID();
        when(profiles.findByAccountId(accountId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getMine(accountId));
    }

    @Test
    void getByIdReturnsDriverProfile() {
        UUID profileId = UUID.randomUUID();
        DriverProfile profile = new DriverProfile();
        profile.setId(profileId);
        profile.setAccountId(UUID.randomUUID());
        profile.setDisplayName("Demo Driver");
        profile.setVehiclePlate("CAB-1234");
        profile.setAvailable(true);

        when(profiles.findById(profileId)).thenReturn(Optional.of(profile));

        var result = service.getById(profileId);
        assertEquals(profileId, result.id());
        assertEquals("CAB-1234", result.vehiclePlate());
    }

    @Test
    void assertOwnerRejectsDifferentAccountId() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        assertThrows(ForbiddenException.class, () -> service.assertOwner(other, owner));
    }
}
