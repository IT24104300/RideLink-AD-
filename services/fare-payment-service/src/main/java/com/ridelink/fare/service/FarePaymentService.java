package com.ridelink.fare.service;

import com.ridelink.common.exception.BadRequestException;
import com.ridelink.common.exception.NotFoundException;
import com.ridelink.common.exception.UpstreamException;
import com.ridelink.fare.client.RideServiceClient;
import com.ridelink.fare.config.FareProperties;
import com.ridelink.fare.domain.FareQuote;
import com.ridelink.fare.domain.FareType;
import com.ridelink.fare.domain.Payment;
import com.ridelink.fare.domain.PaymentStatus;
import com.ridelink.fare.dto.FareDtos.EstimateRequest;
import com.ridelink.fare.dto.FareDtos.FareResponse;
import com.ridelink.fare.dto.FareDtos.PaymentRequest;
import com.ridelink.fare.dto.FareDtos.PaymentResponse;
import com.ridelink.fare.dto.FareDtos.ReceiptResponse;
import com.ridelink.fare.repo.FareQuoteRepository;
import com.ridelink.fare.repo.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FarePaymentService {

    public static final String FORMULA = "base + (distanceKm * perKm) + (durationMin * perMin)";

    private static final Logger log = LoggerFactory.getLogger(FarePaymentService.class);

    private final FareQuoteRepository quotes;
    private final PaymentRepository payments;
    private final FareProperties fareProperties;
    private final RideServiceClient rideServiceClient;

    public FarePaymentService(
            FareQuoteRepository quotes,
            PaymentRepository payments,
            FareProperties fareProperties,
            RideServiceClient rideServiceClient
    ) {
        this.quotes = quotes;
        this.payments = payments;
        this.fareProperties = fareProperties;
        this.rideServiceClient = rideServiceClient;
    }

    @Transactional
    public FareResponse estimate(EstimateRequest request) {
        return persistQuote(request, FareType.ESTIMATE);
    }

    @Transactional
    public FareResponse finalFare(EstimateRequest request) {
        if (request.rideId() == null) {
            throw new BadRequestException("rideId is required for a final fare");
        }
        return persistQuote(request, FareType.FINAL);
    }

    @Transactional(readOnly = true)
    public FareResponse getFare(UUID id) {
        return toFare(quotes.findById(id).orElseThrow(() -> new NotFoundException("Fare quote not found")));
    }

    @Transactional
    public PaymentResponse pay(UUID accountId, PaymentRequest request, String authorizationHeader) {
        boolean fail = Boolean.TRUE.equals(request.simulateFailure())
                || "0000".equals(request.cardLast4());
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setRideId(request.rideId());
        payment.setFareId(request.fareId());
        payment.setAccountId(accountId);
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency() == null ? fareProperties.getCurrency() : request.currency());
        payment.setMethod(request.method() == null ? "CARD_SIMULATED" : request.method());
        if (fail) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Simulated payment failure");
        } else {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setReceiptNumber("RL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        Payment saved = payments.save(payment);
        if (saved.getStatus() == PaymentStatus.COMPLETED && saved.getId() != null) {
            try {
                rideServiceClient.attachPayment(saved.getRideId(), saved.getId(), authorizationHeader);
            } catch (UpstreamException ex) {
                log.warn("Ride Service unavailable while attaching payment {} to ride {}: {}",
                        saved.getId(), saved.getRideId(), ex.getMessage());
            }
        }
        return toPayment(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID id) {
        return toPayment(payments.findById(id).orElseThrow(() -> new NotFoundException("Payment not found")));
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceipt(UUID id) {
        Payment payment = payments.findById(id).orElseThrow(() -> new NotFoundException("Payment not found"));
        String merchant = "RideLink Platforms Ltd";
        String summary = payment.getStatus() == PaymentStatus.COMPLETED
                ? "Official payment receipt for ride " + payment.getRideId() + " (" + payment.getAmount() + " " + payment.getCurrency() + ")"
                : "Payment attempt status: " + payment.getStatus() + " - " + (payment.getFailureReason() != null ? payment.getFailureReason() : "Declined");
        return new ReceiptResponse(
                payment.getId(),
                payment.getId(),
                payment.getRideId(),
                payment.getFareId(),
                payment.getAccountId(),
                payment.getReceiptNumber(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                merchant,
                summary,
                payment.getCreatedAt()
        );
    }

    private FareResponse persistQuote(EstimateRequest request, FareType type) {
        var breakdown = FareCalculator.calculate(
                request.pickup(),
                request.destination(),
                fareProperties.getBase(),
                fareProperties.getPerKm(),
                fareProperties.getPerMin()
        );
        FareQuote quote = new FareQuote();
        quote.setRideId(request.rideId());
        quote.setPickup(request.pickup().trim());
        quote.setDestination(request.destination().trim());
        quote.setDistanceKm(breakdown.distanceKm());
        quote.setDurationMin(breakdown.durationMin());
        quote.setBaseFare(breakdown.base());
        quote.setPerKmRate(breakdown.perKm());
        quote.setPerMinRate(breakdown.perMin());
        quote.setTotal(breakdown.total());
        quote.setCurrency(fareProperties.getCurrency());
        quote.setType(type);
        return toFare(quotes.save(quote));
    }

    private FareResponse toFare(FareQuote quote) {
        return new FareResponse(
                quote.getId(),
                quote.getRideId(),
                quote.getType(),
                quote.getPickup(),
                quote.getDestination(),
                quote.getDistanceKm(),
                quote.getDurationMin(),
                quote.getBaseFare(),
                quote.getPerKmRate(),
                quote.getPerMinRate(),
                quote.getTotal(),
                quote.getCurrency(),
                FORMULA,
                quote.getCreatedAt()
        );
    }

    private PaymentResponse toPayment(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getFareId(),
                payment.getAccountId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getReceiptNumber(),
                payment.getFailureReason(),
                payment.getCreatedAt()
        );
    }
}
