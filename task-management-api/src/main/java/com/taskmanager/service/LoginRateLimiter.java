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
public class LoginRateLimiter {

    private final int requestLimit;
    private final Duration window;
    private final int maximumKeys;
    private final Clock clock;
    private final RateLimitBucketStore ipBuckets;
    private final RateLimitBucketStore accountBuckets;

    @Autowired
    public LoginRateLimiter(AuthSecurityProperties properties) {
        this(
            properties.loginLimit(),
            properties.loginWindow(),
            properties.loginRateLimitMaxKeys(),
            Clock.systemUTC(),
            null
        );
    }

    LoginRateLimiter(int requestLimit, Duration window, int maximumKeys, Clock clock) {
        this(requestLimit, window, maximumKeys, clock, null);
    }

    LoginRateLimiter(
        int requestLimit,
        Duration window,
        int maximumKeys,
        Clock clock,
        byte[] hashKey
    ) {
        if (requestLimit < 1 || window == null || window.isZero() || window.isNegative()
            || maximumKeys < 2) {
            throw new IllegalStateException("Login rate-limit configuration must be positive.");
        }
        this.requestLimit = requestLimit;
        this.window = window;
        this.maximumKeys = maximumKeys;
        this.clock = clock;
        this.ipBuckets = newStore(maximumKeys, hashKey);
        this.accountBuckets = newStore(maximumKeys, hashKey);
    }

    public synchronized void check(String remoteAddress, String email) {
        Instant now = clock.instant();
        ipBuckets.removeExpired(now, window, Duration.ZERO);
        accountBuckets.removeExpired(now, window, Duration.ZERO);
        if (!increment(ipBuckets, remoteAddress == null ? "unknown" : remoteAddress, now)) {
            throw new RateLimitExceededException();
        }
        if (email != null && !email.isBlank()) {
            String accountKey = email.trim().toLowerCase(Locale.ROOT);
            if (!increment(accountBuckets, accountKey, now)) {
                throw new RateLimitExceededException();
            }
        }
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

    private boolean increment(RateLimitBucketStore store, String identity, Instant now) {
        return store.increment(identity, requestLimit, window, Duration.ZERO, now);
    }

    private RateLimitBucketStore newStore(int capacity, byte[] hashKey) {
        return hashKey == null
            ? new RateLimitBucketStore(capacity)
            : new RateLimitBucketStore(capacity, hashKey);
    }
}
