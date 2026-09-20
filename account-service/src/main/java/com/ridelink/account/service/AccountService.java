package com.ridelink.account.service;

import com.ridelink.account.domain.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.dto.AccountDtos.AccountResponse;
import com.ridelink.account.dto.AccountDtos.AuthResponse;
import com.ridelink.account.dto.AccountDtos.LoginRequest;
import com.ridelink.account.dto.AccountDtos.RegisterRequest;
import com.ridelink.account.dto.AccountDtos.UpdateProfileRequest;
import com.ridelink.account.repo.AccountRepository;
import com.ridelink.common.exception.BadRequestException;
import com.ridelink.common.exception.ConflictException;
import com.ridelink.common.exception.ForbiddenException;
import com.ridelink.common.exception.NotFoundException;
import com.ridelink.common.exception.UnauthorizedException;
import com.ridelink.common.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(AccountRepository accounts, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AccountResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new ForbiddenException("Admin accounts cannot be self-registered");
        }
        if (request.role() == null) {
            throw new BadRequestException("Role is required");
        }
        if (accounts.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("Username already exists");
        }
        if (accounts.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already exists");
        }

        Account account = new Account();
        account.setUsername(request.username().trim());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setEmail(request.email().trim().toLowerCase());
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone());
        account.setRole(request.role());
        account.setStatus(AccountStatus.ACTIVE);
        return toResponse(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Account account = accounts.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Account is " + account.getStatus());
        }
        String token = jwtService.issueToken(account.getId(), account.getUsername(), account.getRole().name());
        return new AuthResponse(
                token,
                "Bearer",
                jwtService.expirationMs(),
                account.getId(),
                account.getUsername(),
                account.getRole()
        );
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID id) {
        return toResponse(require(id));
    }

    @Transactional
    public AccountResponse updateProfile(UUID id, UpdateProfileRequest request) {
        Account account = require(id);
        if (request.fullName() != null && !request.fullName().isBlank()) {
            account.setFullName(request.fullName().trim());
        }
        if (request.phone() != null) {
            account.setPhone(request.phone());
        }
        if (request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase();
            if (accounts.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                throw new ConflictException("Email already exists");
            }
            account.setEmail(email);
        }
        return toResponse(accounts.save(account));
    }

    @Transactional
    public AccountResponse updateStatus(UUID id, AccountStatus status) {
        Account account = require(id);
        account.setStatus(status);
        return toResponse(accounts.save(account));
    }

    private Account require(UUID id) {
        return accounts.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getFullName(),
                account.getPhone(),
                account.getRole(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
