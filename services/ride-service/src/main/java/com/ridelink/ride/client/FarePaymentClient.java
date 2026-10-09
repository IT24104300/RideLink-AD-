package com.ridelink.ride.client;

import com.ridelink.common.exception.UpstreamException;
import com.ridelink.ride.dto.RideDtos.FareView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

/**
 * HTTP REST client communicating with the Fare & Payment Service.
 * Used when completing a ride to trigger final fare calculation.
 */
@Component
public class FarePaymentClient {

    private final RestClient restClient;

    /**
     * Constructs the client with the preconfigured RestClient for the fare service.
     *
     * @param restClient configured REST client pointing to Fare & Payment Service
     */
    public FarePaymentClient(@Qualifier("fareRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Calls Fare & Payment Service to calculate the final fare amount for a completed ride.
     *
     * @param rideId the unique identifier of the completed ride
     * @param pickup the ride start location
     * @param destination the ride end location
     * @param authorizationHeader the incoming Bearer JWT header
     * @return the calculated FareView containing total amount and currency
     * @throws UpstreamException if the fare service is unreachable or responds with error
     */
    public FareView calculateFinal(UUID rideId, String pickup, String destination, String authorizationHeader) {
        try {
            // Post ride details to /api/fares/final with propagated authentication header
            return restClient.post()
                    .uri("/api/fares/final")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "rideId", rideId,
                            "pickup", pickup,
                            "destination", destination
                    ))
                    .retrieve()
                    .body(FareView.class);
        } catch (RestClientException ex) {
            // Translate network/HTTP error to UpstreamException for graceful handling in RideService
            throw new UpstreamException("Fare & Payment Service is unavailable");
        }
    }
}

