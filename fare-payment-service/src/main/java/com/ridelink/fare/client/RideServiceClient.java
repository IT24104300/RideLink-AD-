package com.ridelink.fare.client;

import com.ridelink.common.exception.UpstreamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.UUID;

@Component
public class RideServiceClient {

    private final RestClient restClient;

    public RideServiceClient(@Qualifier("rideRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void attachPayment(UUID rideId, UUID paymentId, String authorizationHeader) {
        try {
            restClient.post()
                    .uri("/api/rides/{id}/payment", rideId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentId", paymentId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new UpstreamException("Ride Service is unavailable");
        }
    }
}
