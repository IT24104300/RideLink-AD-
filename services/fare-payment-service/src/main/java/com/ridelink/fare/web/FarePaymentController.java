package com.ridelink.fare.web;

import com.ridelink.common.security.SecurityUtils;
import com.ridelink.fare.dto.FareDtos.EstimateRequest;
import com.ridelink.fare.dto.FareDtos.FareResponse;
import com.ridelink.fare.dto.FareDtos.PaymentRequest;
import com.ridelink.fare.dto.FareDtos.PaymentResponse;
import com.ridelink.fare.dto.FareDtos.ReceiptResponse;
import com.ridelink.fare.service.FarePaymentService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Fares and payments")
public class FarePaymentController {

    private final FarePaymentService farePaymentService;

    public FarePaymentController(FarePaymentService farePaymentService) {
        this.farePaymentService = farePaymentService;
    }

    @PostMapping("/api/fares/estimate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "Estimate a fare for pickup and destination")
    public FareResponse estimate(@Valid @RequestBody EstimateRequest request) {
        return farePaymentService.estimate(request);
    }

    @PostMapping("/api/fares/final")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "Calculate and store the final fare for a completed ride")
    public FareResponse finalFare(@Valid @RequestBody EstimateRequest request) {
        return farePaymentService.finalFare(request);
    }

    @GetMapping("/api/fares/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "Retrieve a fare quote")
    public FareResponse getFare(@PathVariable UUID id) {
        return farePaymentService.getFare(id);
    }

    @PostMapping("/api/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    @Operation(summary = "Record a simulated payment (cardLast4=0000 or simulateFailure=true fails)")
    public PaymentResponse pay(@Valid @RequestBody PaymentRequest request, HttpServletRequest httpRequest) {
        return farePaymentService.pay(
                SecurityUtils.currentUser().accountId(),
                request,
                httpRequest.getHeader(HttpHeaders.AUTHORIZATION)
        );
    }

    @GetMapping("/api/payments/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "Retrieve a payment")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return farePaymentService.getPayment(id);
    }

    @GetMapping("/api/payments/{id}/receipt")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @Operation(summary = "Retrieve a payment receipt record")
    public ReceiptResponse receipt(@PathVariable UUID id) {
        return farePaymentService.getReceipt(id);
    }
}
