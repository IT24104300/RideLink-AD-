package com.ridelink.account.config;

import com.ridelink.account.domain.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.repo.AccountRepository;
import com.ridelink.common.demo.DemoAccounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AccountDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(AccountDataSeeder.class);

    @Bean
    CommandLineRunner seedAccounts(AccountRepository accounts, PasswordEncoder encoder) {
        return args -> {
            seed(accounts, encoder, DemoAccounts.PASSENGER_ID, "passenger1", "passenger1@ridelink.local", "Demo Passenger", Role.PASSENGER);
            seed(accounts, encoder, DemoAccounts.DRIVER_ID, "driver1", "driver1@ridelink.local", "Demo Driver", Role.DRIVER);
            seed(accounts, encoder, DemoAccounts.ADMIN_ID, "admin1", "admin1@ridelink.local", "Demo Admin", Role.ADMIN);
        };
    }

    private void seed(
            AccountRepository accounts,
            PasswordEncoder encoder,
            java.util.UUID id,
            String username,
            String email,
            String fullName,
            Role role
    ) {
        if (accounts.existsById(id) || accounts.existsByUsernameIgnoreCase(username)) {
            return;
        }
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setPasswordHash(encoder.encode(DemoAccounts.PASSWORD));
        account.setEmail(email);
        account.setFullName(fullName);
        account.setPhone("+94000000000");
        account.setRole(role);
        account.setStatus(AccountStatus.ACTIVE);
        accounts.save(account);
        log.info("Seeded demo account {} / {}", username, DemoAccounts.PASSWORD);
    }
}
