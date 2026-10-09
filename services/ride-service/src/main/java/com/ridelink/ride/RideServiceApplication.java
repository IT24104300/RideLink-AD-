package com.ridelink.ride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the RideLink Ride Management Microservice.
 * Handles ride booking, driver assignment, state lifecycle transitions,
 * and coordinates with the Driver and Fare microservices.
 */
@SpringBootApplication(scanBasePackages = "com.ridelink")
public class RideServiceApplication {

    /**
     * Bootstraps the Ride Management Service Spring Boot application.
     *
     * @param args command-line arguments passed during startup
     */
    public static void main(String[] args) {
        SpringApplication.run(RideServiceApplication.class, args);
    }
}

