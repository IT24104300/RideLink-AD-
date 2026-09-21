package com.ridelink.fare.service;

import com.ridelink.common.exception.BadRequestException;
import com.ridelink.fare.client.RideServiceClient;
import com.ridelink.fare.config.FareProperties;
import com.ridelink.fare.domain.Payment;
import com.ridelink.fare.domain.PaymentStatus;
import com.ridelink.fare.dto.FareDtos.EstimateRequest;
import com.ridelink.fare.dto.FareDtos.PaymentRequest;
import com.ridelink.fare.repo.FareQuoteRepository;
import com.ridelink.fare.repo.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarePaymentServiceTest {

    @Mock
    private FareQuoteRepository quotes;
    @Mock
    private PaymentRepository payments;
    @Mock
    private RideServiceClient rideServiceClient;

    private FarePaymentService service;

    @BeforeEach
    void setUp() {
        FareProperties properties = new FareProperties();
        service = new FarePaymentService(quotes, payments, properties, rideServiceClient);
    }

    @Test
    void finalFareRequiresRideId() {
        assertThrows(BadRequestException.class,
                () -> service.finalFare(new EstimateRequest("A", "B", null)));
    }

    @Test
    void simulatedCard0000Fails() {
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        var result = service.pay(UUID.randomUUID(), new PaymentRequest(
                UUID.randomUUID(), null, new BigDecimal("500.00"), "LKR", "CARD_SIMULATED", "0000", false), "Bearer test");
        assertEquals(PaymentStatus.FAILED, result.status());
        verify(rideServiceClient, never()).attachPayment(any(), any(), any());
    }

    @Test
    void successfulPaymentIssuesReceipt() {
        UUID rideId = UUID.randomUUID();
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        var result = service.pay(UUID.randomUUID(), new PaymentRequest(
                rideId, null, new BigDecimal("500.00"), "LKR", "CARD_SIMULATED", "4242", false), "Bearer test");
        assertEquals(PaymentStatus.COMPLETED, result.status());
        assertNotNull(result.receiptNumber());
        verify(rideServiceClient).attachPayment(rideId, result.id(), "Bearer test");
    }
}
