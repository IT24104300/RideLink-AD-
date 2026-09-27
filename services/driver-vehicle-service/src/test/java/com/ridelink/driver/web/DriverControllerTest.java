package com.ridelink.driver.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.dto.DriverDtos.DriverResponse;
import com.ridelink.driver.dto.DriverDtos.EligibleDriverResponse;
import com.ridelink.driver.dto.DriverDtos.UpsertDriverRequest;
import com.ridelink.driver.service.DriverProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DriverProfileService driverProfileService;

    @Test
    void getByIdReturnsDriverProfile() throws Exception {
        UUID driverId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();

        DriverResponse response = new DriverResponse(
                driverId, accountId, "Driver One", "Toyota", "Axio", "CAB-1234", "White",
                true, "Colombo", "Colombo Fort", 6.9344, 79.8428, now);

        when(driverProfileService.getById(driverId)).thenReturn(response);

        mockMvc.perform(get("/api/drivers/" + driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(driverId.toString()))
                .andExpect(jsonPath("$.vehiclePlate").value("CAB-1234"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void findEligibleDriversReturnsList() throws Exception {
        UUID driverId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        EligibleDriverResponse eligibleDriver = new EligibleDriverResponse(
                driverId, accountId, "Driver One", "CAB-1234", "Colombo", "Colombo Fort", true, 10);

        when(driverProfileService.findEligible("Colombo")).thenReturn(List.of(eligibleDriver));

        mockMvc.perform(get("/api/drivers/eligible?pickup=Colombo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverProfileId").value(driverId.toString()))
                .andExpect(jsonPath("$[0].displayName").value("Driver One"))
                .andExpect(jsonPath("$[0].dummyDistanceScore").value(10));
    }
}
