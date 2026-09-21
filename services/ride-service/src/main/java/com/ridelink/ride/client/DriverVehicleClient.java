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

@Component
public class DriverVehicleClient {

    private final RestClient restClient;

    public DriverVehicleClient(@Qualifier("driverRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<EligibleDriverView> findEligible(String pickup, String authorizationHeader) {
        try {
            EligibleDriverView[] body = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/drivers/eligible").queryParam("pickup", pickup).build())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(EligibleDriverView[].class);
            return body == null ? List.of() : Arrays.asList(body);
        } catch (RestClientException ex) {
            throw new UpstreamException("Driver & Vehicle Service is unavailable");
        }
    }
}
