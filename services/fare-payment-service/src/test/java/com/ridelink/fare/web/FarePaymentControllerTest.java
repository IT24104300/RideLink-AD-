package com.ridelink.fare.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.common.security.UserPrincipal;
import com.ridelink.fare.domain.FareType;
import com.ridelink.fare.domain.PaymentStatus;
import com.ridelink.fare.dto.FareDtos.EstimateRequest;
import com.ridelink.fare.dto.FareDtos.FareResponse;
import com.ridelink.fare.dto.FareDtos.ReceiptResponse;
import com.ridelink.fare.service.FarePaymentService;
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

import java.math.BigDecimal;
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

@WebMvcTest(controllers = FarePaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class FarePaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FarePaymentService farePaymentService;

    private UUID userAccountId;

    @BeforeEach
    void setUp() {
        userAccountId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userAccountId, "passenger1", "PASSENGER");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void estimateFareReturnsFareResponse() throws Exception {
        EstimateRequest request = new EstimateRequest("Colombo Fort", "Kandy", null);
        UUID fareId = UUID.randomUUID();
        Instant now = Instant.now();

        FareResponse response = new FareResponse(
                fareId, null, FareType.ESTIMATE, "Colombo Fort", "Kandy",
                new BigDecimal("115.00"), new BigDecimal("180.00"), new BigDecimal("200.00"),
                new BigDecimal("100.00"), new BigDecimal("5.00"), new BigDecimal("12600.00"),
                "LKR", "Base 200 + 115.00km*100.00 + 180.00min*5.00", now);

        when(farePaymentService.estimate(any(EstimateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/fares/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(fareId.toString()))
                .andExpect(jsonPath("$.type").value("ESTIMATE"))
                .andExpect(jsonPath("$.currency").value("LKR"));
    }

    @Test
    void getPaymentReceiptReturnsReceipt() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID receiptId = UUID.randomUUID();
        UUID rideId = UUID.randomUUID();
        UUID fareId = UUID.randomUUID();
        Instant now = Instant.now();

        ReceiptResponse response = new ReceiptResponse(
                receiptId, paymentId, rideId, fareId, userAccountId,
                "RCP-123456", PaymentStatus.COMPLETED, new BigDecimal("1250.00"),
                "LKR", "CARD_SIMULATED", "RideLink Payments Inc.",
                "Simulated payment for ride " + rideId, now);

        when(farePaymentService.getReceipt(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/payments/" + paymentId + "/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.receiptNumber").value("RCP-123456"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
