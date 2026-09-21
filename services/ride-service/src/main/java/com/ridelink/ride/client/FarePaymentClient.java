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

@Component
public class FarePaymentClient {

    private final RestClient restClient;

    public FarePaymentClient(@Qualifier("fareRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public FareView calculateFinal(UUID rideId, String pickup, String destination, String authorizationHeader) {
        try {
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
            throw new UpstreamException("Fare & Payment Service is unavailable");
        }
    }
}
