package com.ridelink.account.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.dto.AccountDtos.AccountResponse;
import com.ridelink.account.dto.AccountDtos.AuthResponse;
import com.ridelink.account.dto.AccountDtos.LoginRequest;
import com.ridelink.account.dto.AccountDtos.RegisterRequest;
import com.ridelink.account.dto.AccountDtos.UpdateStatusRequest;
import com.ridelink.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    void registerAccountSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "passenger_test", "password123", "test@ridelink.local", "Test User", "0771234567", Role.PASSENGER);

        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();
        AccountResponse response = new AccountResponse(
                accountId, "passenger_test", "test@ridelink.local", "Test User", "0771234567", Role.PASSENGER, AccountStatus.ACTIVE, now, now);

        when(accountService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.username").value("passenger_test"))
                .andExpect(jsonPath("$.role").value("PASSENGER"));
    }

    @Test
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("passenger_test", "password123");
        UUID accountId = UUID.randomUUID();
        AuthResponse authResponse = new AuthResponse("dummy-jwt-token", "Bearer", 86400L, accountId, "passenger_test", Role.PASSENGER);

        when(accountService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/accounts/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("dummy-jwt-token"))
                .andExpect(jsonPath("$.username").value("passenger_test"));
    }

    @Test
    void meReturnsAuthenticatedAccount() throws Exception {
        UUID accountId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Instant now = Instant.now();
        AccountResponse response = new AccountResponse(
                accountId, "passenger1", "passenger1@ridelink.local", "Demo Passenger", "077",
                Role.PASSENGER, AccountStatus.ACTIVE, now, now);
        when(accountService.getById(accountId)).thenReturn(response);

        var principal = new com.ridelink.common.security.UserPrincipal(accountId, "passenger1", "PASSENGER");
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PASSENGER")));

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            mockMvc.perform(get("/api/accounts/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("passenger1"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getByIdReturnsAccount() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        AccountResponse response = new AccountResponse(
                id, "target_user", "target@ridelink.local", "Target User", "0771234567", Role.PASSENGER, AccountStatus.ACTIVE, now, now);

        when(accountService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("target_user"));
    }

    @Test
    void updateStatusUpdatesStatus() throws Exception {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        UpdateStatusRequest request = new UpdateStatusRequest(AccountStatus.SUSPENDED);
        AccountResponse response = new AccountResponse(
                id, "target_user", "target@ridelink.local", "Target User", "0771234567", Role.PASSENGER, AccountStatus.SUSPENDED, now, now);

        when(accountService.updateStatus(eq(id), eq(AccountStatus.SUSPENDED))).thenReturn(response);

        mockMvc.perform(patch("/api/accounts/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }
}
