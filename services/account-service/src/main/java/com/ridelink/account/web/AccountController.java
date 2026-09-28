package com.ridelink.account.web;

import com.ridelink.account.dto.AccountDtos.AccountResponse;
import com.ridelink.account.dto.AccountDtos.AuthResponse;
import com.ridelink.account.dto.AccountDtos.LoginRequest;
import com.ridelink.account.dto.AccountDtos.RegisterRequest;
import com.ridelink.account.dto.AccountDtos.UpdateProfileRequest;
import com.ridelink.account.dto.AccountDtos.UpdateStatusRequest;
import com.ridelink.account.service.AccountService;
import com.ridelink.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a passenger or driver")
    public AccountResponse register(@Valid @RequestBody RegisterRequest request) {
        return accountService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return accountService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "View the authenticated profile")
    public AccountResponse me() {
        return accountService.getById(SecurityUtils.currentUser().accountId());
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated profile")
    public AccountResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return accountService.updateProfile(SecurityUtils.currentUser().accountId(), request);
    }

    @GetMapping("/{id:[0-9a-fA-F-]{36}}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: view any account")
    public AccountResponse getById(@PathVariable UUID id) {
        return accountService.getById(id);
    }

    @PatchMapping("/{id:[0-9a-fA-F-]{36}}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: activate or suspend an account")
    public AccountResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        return accountService.updateStatus(id, request.status());
    }
}
