package com.ridelink.driver.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class DriverSecurityConfig {

    @Bean
    JwtAuthFilter jwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        return new JwtAuthFilter(jwtService, objectMapper);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, ObjectMapper objectMapper)
            throws Exception {
        return RideLinkSecurity.statelessJwtChain(http, jwtAuthFilter, objectMapper);
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink Driver & Vehicle Service")
                        .version("0.1.0")
                        .description("Driver operational profile, vehicle, availability, location, eligible drivers."))
                .servers(List.of(new Server().url("/").description("This service (same origin as Swagger UI)")))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
