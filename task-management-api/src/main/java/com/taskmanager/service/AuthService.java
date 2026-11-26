package com.taskmanager.service;

import com.taskmanager.dto.auth.RegistrationRequest;
import com.taskmanager.dto.auth.RegistrationResponse;
import com.taskmanager.dto.auth.LoginRequest;
import com.taskmanager.dto.auth.LoginResponse;
import com.taskmanager.dto.auth.RefreshTokenRequest;
import com.taskmanager.dto.auth.ChangePasswordRequest;
import com.taskmanager.entity.User;
import com.taskmanager.exception.DuplicateEmailException;
import com.taskmanager.exception.InvalidCredentialsException;
import com.taskmanager.exception.PasswordChangeException;
import com.taskmanager.exception.InvalidRefreshTokenException;
import com.taskmanager.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenLifecycleService refreshTokenLifecycleService;
    private final LoginRateLimiter loginRateLimiter;
    private final PasswordResetRateLimiter passwordResetRateLimiter;
    private final PasswordResetService passwordResetService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        RefreshTokenLifecycleService refreshTokenLifecycleService,
        LoginRateLimiter loginRateLimiter,
        PasswordResetRateLimiter passwordResetRateLimiter,
        PasswordResetService passwordResetService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.refreshTokenLifecycleService = refreshTokenLifecycleService;
        this.loginRateLimiter = loginRateLimiter;
        this.passwordResetRateLimiter = passwordResetRateLimiter;
        this.passwordResetService = passwordResetService;
    }

    public LoginResponse login(LoginRequest request, String remoteAddress) {
        String normalizedEmail = normalizeEmail(request.email());
        loginRateLimiter.check(remoteAddress, normalizedEmail);
        final String authenticatedEmail;
        try {
            authenticatedEmail = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    normalizedEmail,
                    request.password()
                )
            ).getName();
        } catch (AuthenticationException exception) {
            LOGGER.warn("Authentication attempt failed.");
            throw new InvalidCredentialsException();
        }

        LOGGER.info("Authentication succeeded.");
        return refreshTokenLifecycleService.issueForUser(authenticatedEmail);
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshTokenLifecycleService.RotationResult result =
            refreshTokenLifecycleService.rotate(request.refreshToken());
        if (result.status() != RefreshTokenLifecycleService.RotationStatus.SUCCESS) {
            throw new InvalidRefreshTokenException();
        }
        return result.tokenPair();
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenLifecycleService.revokeFamilyForToken(request.refreshToken());
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
            .orElseThrow(InvalidCredentialsException::new);
        if (!user.isEnabled() || !passwordEncoder.matches(
            request.currentPassword(),
            user.getPasswordHash()
        )) {
            throw new PasswordChangeException();
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new PasswordChangeException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        passwordResetService.invalidateAllForUserId(user.getId());
        refreshTokenLifecycleService.revokeAllForUserId(user.getId());
        LOGGER.info("Password changed; refresh sessions were revoked.");
    }

    public void requestPasswordReset(String email, String remoteAddress) {
        String normalizedEmail = normalizeEmail(email);
        if (passwordResetRateLimiter.check(remoteAddress, normalizedEmail)) {
            passwordResetService.requestReset(normalizedEmail);
        }
    }

    public void resetPassword(String token, String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
    }

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(request.email());
        }

        User user = new User(
            request.name(),
            normalizedEmail,
            passwordHash
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return new RegistrationResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
            );
        } catch (DataIntegrityViolationException exception) {
            if (isEmailUniqueConstraintViolation(exception)) {
                throw new DuplicateEmailException(request.email(), exception);
            }
            throw exception;
        }
    }

    private boolean isEmailUniqueConstraintViolation(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();
                if (constraintName != null
                    && constraintName.toLowerCase(Locale.ROOT)
                        .contains(EMAIL_UNIQUE_CONSTRAINT)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
