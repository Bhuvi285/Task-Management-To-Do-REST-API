package com.taskmanager.service;

import com.taskmanager.config.AuthSecurityProperties;
import com.taskmanager.entity.PasswordResetToken;
import com.taskmanager.entity.User;
import com.taskmanager.exception.InvalidPasswordResetTokenException;
import com.taskmanager.repository.PasswordResetTokenRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.PasswordResetTokenDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenLifecycleService refreshTokenLifecycleService;
    private final RefreshTokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuthSecurityProperties properties;
    private final ObjectProvider<PasswordResetTokenDelivery> deliveryProvider;
    private final TaskExecutor taskExecutor;

    public PasswordResetService(
        PasswordResetTokenRepository resetTokenRepository,
        UserRepository userRepository,
        RefreshTokenLifecycleService refreshTokenLifecycleService,
        RefreshTokenGenerator tokenGenerator,
        PasswordEncoder passwordEncoder,
        AuthSecurityProperties properties,
        ObjectProvider<PasswordResetTokenDelivery> deliveryProvider,
        TaskExecutor taskExecutor
    ) {
        this.resetTokenRepository = resetTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenLifecycleService = refreshTokenLifecycleService;
        this.tokenGenerator = tokenGenerator;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.deliveryProvider = deliveryProvider;
        this.taskExecutor = taskExecutor;
    }

    @Transactional
    public void requestReset(String email) {
        PasswordResetTokenDelivery delivery = deliveryProvider.getIfAvailable();
        if (delivery == null) {
            LOGGER.warn("Password reset requested but no delivery provider is configured.");
            return;
        }

        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || !user.isEnabled()) {
            return;
        }

        String rawToken = tokenGenerator.generateToken();
        LocalDateTime now = LocalDateTime.now();
        resetTokenRepository.invalidateUnusedByUserId(user.getId(), now);
        resetTokenRepository.saveAndFlush(new PasswordResetToken(
            user,
            tokenGenerator.hashToken(rawToken),
            now.plus(properties.passwordResetTokenExpiration())
        ));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        taskExecutor.execute(() ->
                            delivery.deliver(user.getEmail(), rawToken)
                        );
                    }
                }
            );
        } else {
            taskExecutor.execute(() ->
                delivery.deliver(user.getEmail(), rawToken)
            );
        }
    }

    @Transactional
    public void invalidateAllForUserId(Long userId) {
        resetTokenRepository.invalidateUnusedByUserId(userId, LocalDateTime.now());
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        LocalDateTime now = LocalDateTime.now();
        PasswordResetToken resetToken = resetTokenRepository
            .findByTokenHashForUpdate(tokenGenerator.hashToken(rawToken))
            .orElseThrow(InvalidPasswordResetTokenException::new);
        if (resetToken.getUsedAt() != null || !resetToken.getExpiresAt().isAfter(now)
            || !resetToken.getUser().isEnabled()) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.markUsed(now);
        resetTokenRepository.saveAndFlush(resetToken);
        resetTokenRepository.invalidateUnusedByUserId(user.getId(), now);
        refreshTokenLifecycleService.revokeAllForUserId(user.getId());
        LOGGER.info("Password reset completed; refresh sessions were revoked.");
    }
}
