package com.taskmanager.service;

import com.taskmanager.config.AuthSecurityProperties;
import com.taskmanager.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Component
public class PasswordResetRateLimiter {

    private final int ipRequestLimit;
    private final int accountRequestLimit;
    private final Duration window;
    private final Duration resendCooldown;
    private final Clock clock;
    private final RateLimitBucketStore ipBuckets;
    private final RateLimitBucketStore accountBuckets;

    @Autowired
    public PasswordResetRateLimiter(AuthSecurityProperties properties) {
        this(
            properties.passwordResetIpLimit(),
            properties.passwordResetAccountLimit(),
            properties.passwordResetWindow(),
            properties.passwordResetRateLimitMaxKeys(),
            properties.passwordResetCooldown(),
            Clock.systemUTC(),
            null
        );
    }

    PasswordResetRateLimiter(
        int ipRequestLimit,
        int accountRequestLimit,
        Duration window,
        int maximumKeys,
        Duration resendCooldown,
        Clock clock
    ) {
        this(
            ipRequestLimit,
            accountRequestLimit,
            window,
            maximumKeys,
            resendCooldown,
            clock,
            null
        );
    }

    PasswordResetRateLimiter(
        int ipRequestLimit,
        int accountRequestLimit,
        Duration window,
        int maximumKeys,
        Duration resendCooldown,
        Clock clock,
        byte[] hashKey
    ) {
        if (ipRequestLimit < 1 || accountRequestLimit < 1 || window == null
            || window.isZero() || window.isNegative() || maximumKeys < 2
            || resendCooldown == null || resendCooldown.isNegative()
            || resendCooldown.isZero()) {
            throw new IllegalStateException(
                "Password reset rate-limit configuration must be positive."
            );
        }
        this.ipRequestLimit = ipRequestLimit;
        this.accountRequestLimit = accountRequestLimit;
        this.window = window;
        this.resendCooldown = resendCooldown;
        this.clock = clock;
        this.ipBuckets = newStore(maximumKeys, hashKey);
        this.accountBuckets = newStore(maximumKeys, hashKey);
    }

    public synchronized boolean check(String remoteAddress, String email) {
        Instant now = clock.instant();
        ipBuckets.removeExpired(now, window, Duration.ZERO);
        accountBuckets.removeExpired(now, window, resendCooldown);
        String ipKey = remoteAddress == null ? "unknown" : remoteAddress;
        if (!ipBuckets.increment(
            ipKey,
            ipRequestLimit,
            window,
            Duration.ZERO,
            now
        )) {
            throw new RateLimitExceededException(
                "Too many password reset requests. Please try again later."
            );
        }
        if (email == null || email.isBlank()) {
            return true;
        }
        String accountKey = email.trim().toLowerCase(Locale.ROOT);
        return accountBuckets.increment(
            accountKey,
            accountRequestLimit,
            window,
            resendCooldown,
            now
        );
    }

    int occupiedBucketCount() {
        return ipBuckets.occupiedBucketCount() + accountBuckets.occupiedBucketCount();
    }

    int bucketCapacity() {
        return ipBuckets.capacity() + accountBuckets.capacity();
    }

    int slotFor(String type, String identity) {
        return (type.equals("ip") ? ipBuckets : accountBuckets).slotFor(identity);
    }

    private RateLimitBucketStore newStore(int capacity, byte[] hashKey) {
        return hashKey == null
            ? new RateLimitBucketStore(capacity)
            : new RateLimitBucketStore(capacity, hashKey);
    }
}
