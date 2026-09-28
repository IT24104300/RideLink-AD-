package com.ridelink.fare.client;

import com.ridelink.common.exception.UpstreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.UUID;

@Component
public class RideServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RideServiceClient.class);

    private final RestClient restClient;

    public RideServiceClient(@Qualifier("rideRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void attachPayment(UUID rideId, UUID paymentId, String authorizationHeader) {
        try {
            RestClient.RequestBodySpec spec = restClient.post()
                    .uri("/api/rides/{id}/payment", rideId)
                    .contentType(MediaType.APPLICATION_JSON);
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                spec = spec.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
            }
            spec.body(Map.of("paymentId", paymentId.toString()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.warn("Ride Service rejected payment {} for ride {}: {} {}",
                    paymentId, rideId, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new UpstreamException("Ride Service rejected payment attach: " + ex.getStatusCode());
        } catch (RestClientException ex) {
            log.warn("Ride Service unavailable while attaching payment {} to ride {}: {}",
                    paymentId, rideId, ex.getMessage());
            throw new UpstreamException("Ride Service is unavailable");
        }
    }
}
