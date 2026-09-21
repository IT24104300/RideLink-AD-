package com.ridelink.account.service;

import com.ridelink.account.domain.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.dto.AccountDtos.LoginRequest;
import com.ridelink.account.dto.AccountDtos.RegisterRequest;
import com.ridelink.account.repo.AccountRepository;
import com.ridelink.common.exception.ForbiddenException;
import com.ridelink.common.exception.UnauthorizedException;
import com.ridelink.common.security.JwtProperties;
import com.ridelink.common.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accounts;

    private PasswordEncoder encoder;
    private AccountService service;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        JwtProperties properties = new JwtProperties();
        properties.setSecret("ridelink-dev-only-change-me-please-32chars-min");
        service = new AccountService(accounts, encoder, new JwtService(properties));
    }

    @Test
    void rejectsAdminSelfRegistration() {
        RegisterRequest request = new RegisterRequest(
                "admin2", "password", "admin2@ridelink.local", "Admin", null, Role.ADMIN);
        assertThrows(ForbiddenException.class, () -> service.register(request));
    }

    @Test
    void registersPassengerAndHashesPassword() {
        when(accounts.existsByUsernameIgnoreCase("passenger2")).thenReturn(false);
        when(accounts.existsByEmailIgnoreCase("p2@ridelink.local")).thenReturn(false);
        when(accounts.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.register(new RegisterRequest(
                "passenger2", "password", "p2@ridelink.local", "Pat", "077", Role.PASSENGER));

        assertEquals(Role.PASSENGER, response.role());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(captor.capture());
        assertTrue(encoder.matches("password", captor.getValue().getPasswordHash()));
    }

    @Test
    void loginRejectsSuspendedAccount() {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUsername("passenger1");
        account.setPasswordHash(encoder.encode("password"));
        account.setRole(Role.PASSENGER);
        account.setStatus(AccountStatus.SUSPENDED);
        when(accounts.findByUsernameIgnoreCase("passenger1")).thenReturn(Optional.of(account));

        assertThrows(ForbiddenException.class,
                () -> service.login(new LoginRequest("passenger1", "password")));
    }

    @Test
    void loginRejectsWrongPassword() {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setUsername("passenger1");
        account.setPasswordHash(encoder.encode("password"));
        account.setRole(Role.PASSENGER);
        account.setStatus(AccountStatus.ACTIVE);
        when(accounts.findByUsernameIgnoreCase("passenger1")).thenReturn(Optional.of(account));

        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("passenger1", "nope")));
    }
}
