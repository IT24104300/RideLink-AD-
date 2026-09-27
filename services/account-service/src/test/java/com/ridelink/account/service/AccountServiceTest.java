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

    @Test
    void registerRejectsDuplicateUsername() {
        when(accounts.existsByUsernameIgnoreCase("passenger1")).thenReturn(true);
        RegisterRequest request = new RegisterRequest(
                "passenger1", "password", "new@ridelink.local", "Passenger", "077", Role.PASSENGER);
        assertThrows(com.ridelink.common.exception.ConflictException.class, () -> service.register(request));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(accounts.existsByUsernameIgnoreCase("passenger2")).thenReturn(false);
        when(accounts.existsByEmailIgnoreCase("p2@ridelink.local")).thenReturn(true);
        RegisterRequest request = new RegisterRequest(
                "passenger2", "password", "p2@ridelink.local", "Passenger", "077", Role.PASSENGER);
        assertThrows(com.ridelink.common.exception.ConflictException.class, () -> service.register(request));
    }

    @Test
    void loginRejectsNonExistentUser() {
        when(accounts.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequest("ghost", "password")));
    }

    @Test
    void updateProfileUpdatesFields() {
        UUID id = UUID.randomUUID();
        Account account = new Account();
        account.setId(id);
        account.setUsername("user1");
        account.setEmail("old@ridelink.local");
        account.setFullName("Old Name");
        account.setRole(Role.PASSENGER);
        account.setStatus(AccountStatus.ACTIVE);

        when(accounts.findById(id)).thenReturn(Optional.of(account));
        when(accounts.existsByEmailIgnoreCaseAndIdNot("new@ridelink.local", id)).thenReturn(false);
        when(accounts.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var updated = service.updateProfile(id, new com.ridelink.account.dto.AccountDtos.UpdateProfileRequest(
                "New Name", "new@ridelink.local", "0779999999"));

        assertEquals("New Name", updated.fullName());
        assertEquals("new@ridelink.local", updated.email());
        assertEquals("0779999999", updated.phone());
    }

    @Test
    void updateStatusModifiesAccountStatus() {
        UUID id = UUID.randomUUID();
        Account account = new Account();
        account.setId(id);
        account.setUsername("user1");
        account.setStatus(AccountStatus.ACTIVE);
        account.setRole(Role.PASSENGER);

        when(accounts.findById(id)).thenReturn(Optional.of(account));
        when(accounts.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var updated = service.updateStatus(id, AccountStatus.SUSPENDED);
        assertEquals(AccountStatus.SUSPENDED, updated.status());
    }
}
