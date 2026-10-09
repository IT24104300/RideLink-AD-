package com.ridelink.ride.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.common.security.UserPrincipal;
import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.dto.RideDtos.CreateRideRequest;
import com.ridelink.ride.dto.RideDtos.RideResponse;
import com.ridelink.ride.service.RideService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvc slice tests for {@link RideController}.
 * Verifies HTTP endpoint routing, status codes, and serialization using MockMvc.
 */
@WebMvcTest(controllers = RideController.class)
@AutoConfigureMockMvc(addFilters = false)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RideService rideService;

    private UUID passengerAccountId;

    /**
     * Sets up mock security context with a test passenger principal before each test.
     */
    @BeforeEach
    void setUp() {
        passengerAccountId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(passengerAccountId, "passenger1", "PASSENGER");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Clears security context after test execution.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Tests GET /api/rides/{id} returns 200 OK and expected JSON payload.
     */
    @Test
    void getRideByIdReturnsRide() throws Exception {
        UUID rideId = UUID.randomUUID();
        Instant now = Instant.now();

        RideResponse response = new RideResponse(
                rideId, passengerAccountId, null, null, "Colombo Fort", "Kandy",
                RideStatus.REQUESTED, null, null, null, null, now, now);

        when(rideService.get(eq(rideId), any())).thenReturn(response);

        mockMvc.perform(get("/api/rides/" + rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rideId.toString()))
                .andExpect(jsonPath("$.pickup").value("Colombo Fort"))
                .andExpect(jsonPath("$.destination").value("Kandy"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void createRideReturnsCreated() throws Exception {
        UUID rideId = UUID.randomUUID();
        Instant now = Instant.now();

        CreateRideRequest request = new CreateRideRequest("Colombo Fort", "Galle");
        RideResponse response = new RideResponse(
                rideId, passengerAccountId, null, null, "Colombo Fort", "Galle",
                RideStatus.REQUESTED, null, null, null, null, now, now);

        when(rideService.create(any(), any(CreateRideRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(rideId.toString()))
                .andExpect(jsonPath("$.pickup").value("Colombo Fort"))
                .andExpect(jsonPath("$.destination").value("Galle"));
    }
}
