package com.ridelink.ride.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.common.security.JwtAuthFilter;
import com.ridelink.common.security.JwtService;
import com.ridelink.common.security.RideLinkSecurity;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Security and Bean configuration for the Ride Management Service.
 * Configures stateless JWT authentication, OpenAPI / Swagger UI specs,
 * and REST client beans for downstream microservices.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class RideSecurityConfig {

    /**
     * Filter that intercepts incoming requests, parses and validates JWT Bearer tokens,
     * and sets up the Spring SecurityContext.
     */
    @Bean
    JwtAuthFilter jwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        return new JwtAuthFilter(jwtService, objectMapper);
    }

    /**
     * Configures the stateless HTTP security filter chain using shared RideLink security defaults.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, ObjectMapper objectMapper)
            throws Exception {
        return RideLinkSecurity.statelessJwtChain(http, jwtAuthFilter, objectMapper);
    }

    /**
     * Configures the RestClient.Builder with connection and read timeouts.
     */
    @Bean
    RestClient.Builder restClientBuilder() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return RestClient.builder().requestFactory(factory);
    }

    /**
     * RestClient bean configured with the base URL for the Driver & Vehicle Service.
     */
    @Bean
    RestClient driverRestClient(
            RestClient.Builder builder,
            @Value("${ridelink.clients.driver-base-url}") String baseUrl
    ) {
        return builder.clone().baseUrl(baseUrl).build();
    }

    /**
     * RestClient bean configured with the base URL for the Fare & Payment Service.
     */
    @Bean
    RestClient fareRestClient(
            RestClient.Builder builder,
            @Value("${ridelink.clients.fare-base-url}") String baseUrl
    ) {
        return builder.clone().baseUrl(baseUrl).build();
    }

    /**
     * OpenAPI specification definition enabling Swagger UI with Bearer JWT authorization support.
     */
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink Ride Management Service")
                        .version("0.1.0")
                        .description("Ride requests, driver assignment, and lifecycle transitions."))
                .servers(List.of(new Server().url("/").description("This service (same origin as Swagger UI)")))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
