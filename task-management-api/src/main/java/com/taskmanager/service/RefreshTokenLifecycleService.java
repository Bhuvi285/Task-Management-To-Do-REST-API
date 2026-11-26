package com.taskmanager.service;

import com.taskmanager.config.JwtProperties;
import com.taskmanager.dto.auth.LoginResponse;
import com.taskmanager.entity.RefreshToken;
import com.taskmanager.entity.User;
import com.taskmanager.exception.InvalidCredentialsException;
import com.taskmanager.repository.RefreshTokenRepository;
import com.taskmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenLifecycleService {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(RefreshTokenLifecycleService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public RefreshTokenLifecycleService(
        RefreshTokenRepository refreshTokenRepository,
        UserRepository userRepository,
        RefreshTokenGenerator refreshTokenGenerator,
        JwtService jwtService,
        JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public LoginResponse issueForUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
            .filter(User::isEnabled)
            .orElseThrow(InvalidCredentialsException::new);

        String rawToken = refreshTokenGenerator.generateToken();
        RefreshToken refreshToken = new RefreshToken(
            user,
            refreshTokenGenerator.hashToken(rawToken),
            UUID.randomUUID().toString(),
            LocalDateTime.now().plus(jwtProperties.refreshTokenExpiration())
        );
        refreshTokenRepository.saveAndFlush(refreshToken);
        return createTokenPair(user.getEmail(), rawToken);
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        String tokenHash = refreshTokenGenerator.hashToken(rawToken);
        RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
            .orElse(null);
        if (current == null) {
            return RotationResult.failure(RotationStatus.INVALID);
        }

        LocalDateTime now = LocalDateTime.now();
        if (!current.getUser().isEnabled()) {
            refreshTokenRepository.revokeActiveByTokenFamilyId(
                current.getTokenFamilyId(),
                now
            );
            return RotationResult.failure(RotationStatus.INVALID);
        }
        if (current.getRevokedAt() != null) {
            LOGGER.warn("Refresh-token reuse detected; the affected session family was revoked.");
            refreshTokenRepository.revokeActiveByTokenFamilyId(
                current.getTokenFamilyId(),
                now
            );
            return RotationResult.failure(RotationStatus.REUSED);
        }
        if (!current.getExpiresAt().isAfter(now)) {
            return RotationResult.failure(RotationStatus.EXPIRED);
        }

        String replacementRawToken = refreshTokenGenerator.generateToken();
        RefreshToken replacement = new RefreshToken(
            current.getUser(),
            refreshTokenGenerator.hashToken(replacementRawToken),
            current.getTokenFamilyId(),
            now.plus(jwtProperties.refreshTokenExpiration())
        );
        refreshTokenRepository.saveAndFlush(replacement);
        current.revoke(now, replacement.getId());
        refreshTokenRepository.saveAndFlush(current);

        return RotationResult.success(
            createTokenPair(current.getUser().getEmail(), replacementRawToken)
        );
    }

    @Transactional
    public void revokeFamilyForToken(String rawToken) {
        String tokenHash = refreshTokenGenerator.hashToken(rawToken);
        refreshTokenRepository.findByTokenHashForUpdate(tokenHash).ifPresent(token -> {
            int revoked = refreshTokenRepository.revokeActiveByTokenFamilyId(
                token.getTokenFamilyId(),
                LocalDateTime.now()
            );
            if (revoked > 0) {
                LOGGER.info("Refresh-token session family revoked.");
            }
        });
    }

    @Transactional
    public void revokeAllForUserId(Long userId) {
        refreshTokenRepository.revokeActiveByUserId(userId, LocalDateTime.now());
    }

    private LoginResponse createTokenPair(String email, String rawRefreshToken) {
        return new LoginResponse(
            jwtService.generateAccessToken(email),
            rawRefreshToken,
            "Bearer",
            jwtService.getAccessTokenExpirationSeconds()
        );
    }

    public enum RotationStatus {
        SUCCESS,
        INVALID,
        EXPIRED,
        REUSED
    }

    public record RotationResult(RotationStatus status, LoginResponse tokenPair) {

        private static RotationResult success(LoginResponse tokenPair) {
            return new RotationResult(RotationStatus.SUCCESS, tokenPair);
        }

        private static RotationResult failure(RotationStatus status) {
            return new RotationResult(status, null);
        }
    }
}
