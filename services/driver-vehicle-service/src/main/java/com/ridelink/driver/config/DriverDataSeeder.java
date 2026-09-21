package com.ridelink.driver.config;

import com.ridelink.common.demo.DemoAccounts;
import com.ridelink.driver.domain.DriverProfile;
import com.ridelink.driver.repo.DriverProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DriverDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DriverDataSeeder.class);
    public static final UUID DRIVER1_PROFILE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Bean
    CommandLineRunner seedDriver(DriverProfileRepository profiles) {
        return args -> {
            if (profiles.findByAccountId(DemoAccounts.DRIVER_ID).isPresent()) {
                return;
            }
            DriverProfile profile = new DriverProfile();
            profile.setId(DRIVER1_PROFILE_ID);
            profile.setAccountId(DemoAccounts.DRIVER_ID);
            profile.setDisplayName("Demo Driver");
            profile.setVehicleMake("Toyota");
            profile.setVehicleModel("Axio");
            profile.setVehiclePlate("CAB-1234");
            profile.setVehicleColor("White");
            profile.setAvailable(true);
            profile.setServiceArea("Colombo");
            profile.setLocationLabel("Colombo Fort");
            profile.setLatitude(6.9344);
            profile.setLongitude(79.8428);
            profiles.save(profile);
            log.info("Seeded demo driver profile for account {}", DemoAccounts.DRIVER_ID);
        };
    }
}
