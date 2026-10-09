package com.ridelink.ride.client;

import com.ridelink.common.exception.UpstreamException;
import com.ridelink.ride.dto.RideDtos.EligibleDriverView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

/**
 * HTTP REST client communicating with the Driver & Vehicle Service.
 * Used to query for available and eligible drivers matching a pickup location.
 */
@Component
public class DriverVehicleClient {

    private final RestClient restClient;

    /**
     * Constructs the client with the preconfigured RestClient for the driver service.
     *
     * @param restClient configured REST client pointing to Driver & Vehicle Service
     */
    public DriverVehicleClient(@Qualifier("driverRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Queries the Driver & Vehicle Service for eligible drivers near the specified pickup location.
     * Propagates the user's Authorization Bearer token for inter-service authentication.
     *
     * @param pickup the pickup location string
     * @param authorizationHeader the incoming Bearer JWT header
     * @return a list of eligible driver views, or empty list if none found
     * @throws UpstreamException if the driver service is unreachable or returns an error
     */
    public List<EligibleDriverView> findEligible(String pickup, String authorizationHeader) {
        try {
            // Send GET request to /api/drivers/eligible with pickup query parameter and forwarded JWT
            EligibleDriverView[] body = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/drivers/eligible").queryParam("pickup", pickup).build())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(EligibleDriverView[].class);
            return body == null ? List.of() : Arrays.asList(body);
        } catch (RestClientException ex) {
            // Gracefully translate connection/HTTP failures into an UpstreamException
            throw new UpstreamException("Driver & Vehicle Service is unavailable");
        }
    }
}

