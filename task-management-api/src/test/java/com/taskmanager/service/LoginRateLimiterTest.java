package com.taskmanager.service;

import com.taskmanager.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTest {

    private static final byte[] TEST_HASH_KEY = new byte[32];

    @Test
    void appliesLimitIndependentlyToIpAndNormalizedAccount() {
        LoginRateLimiter limiter = new LoginRateLimiter(
            2,
            Duration.ofMinutes(1),
            256,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );

        limiter.check("192.0.2.1", " User@example.com ");
        limiter.check("192.0.2.2", "USER@example.com");
        assertThatThrownBy(() -> limiter.check("192.0.2.3", "user@example.com"))
            .isInstanceOf(RateLimitExceededException.class);
        limiter.check("192.0.2.1", "another@example.com");
        assertThatThrownBy(() -> limiter.check("192.0.2.1", "third@example.com"))
            .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void expiredWindowStartsFreshAndKeyStorageIsBounded() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        LoginRateLimiter limiter = new LoginRateLimiter(
            1,
            Duration.ofSeconds(30),
            2,
            clock,
            TEST_HASH_KEY
        );

        limiter.check("192.0.2.1", null);
        assertThatThrownBy(() -> limiter.check("192.0.2.1", null))
            .isInstanceOf(RateLimitExceededException.class);

        clock.advance(Duration.ofSeconds(31));
        assertThatCode(() -> limiter.check("192.0.2.2", null))
            .doesNotThrowAnyException();
        assertThat(limiter.occupiedBucketCount()).isEqualTo(1);
    }

    @Test
    void fixedBucketCapacityDoesNotBlockNewKeysAndChurnDoesNotEvictBlockedState() {
        LoginRateLimiter limiter = new LoginRateLimiter(
            100,
            Duration.ofMinutes(1),
            4,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );
        Set<Integer> occupiedIpSlots = new HashSet<>();
        int candidate = 1;
        while (occupiedIpSlots.size() < 4) {
            String ip = "198.51.100." + candidate++;
            int slot = limiter.slotFor("ip", ip);
            if (occupiedIpSlots.add(slot)) {
                limiter.check(ip, null);
            }
        }
        Set<Integer> occupiedAccountSlots = new HashSet<>();
        candidate = 1;
        while (occupiedAccountSlots.size() < 4) {
            String email = "client-" + candidate++ + "@example.com";
            int slot = limiter.slotFor("account", email);
            if (occupiedAccountSlots.add(slot)) {
                limiter.check("198.51.101.1", email);
            }
        }
        assertThat(limiter.occupiedBucketCount()).isEqualTo(limiter.bucketCapacity());

        assertThatCode(() -> limiter.check("203.0.113.250", null))
            .doesNotThrowAnyException();
        assertThat(limiter.occupiedBucketCount()).isLessThanOrEqualTo(
            limiter.bucketCapacity()
        );

        LoginRateLimiter churnLimiter = new LoginRateLimiter(
            2,
            Duration.ofMinutes(1),
            4,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );
        String blockedIdentity = "203.0.113.1";
        churnLimiter.check(blockedIdentity, null);
        churnLimiter.check(blockedIdentity, null);
        assertThatThrownBy(() -> churnLimiter.check(blockedIdentity, null))
            .isInstanceOf(RateLimitExceededException.class);
        for (int index = 0; index < 100; index++) {
            try {
                churnLimiter.check("192.0.2." + index, null);
            } catch (RateLimitExceededException ignored) {
                // Hashed collisions may throttle unrelated identities, never evict a blocked slot.
            }
        }
        assertThatThrownBy(() -> churnLimiter.check(blockedIdentity, null))
            .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void concurrentChecksRemainThreadSafe() throws Exception {
        LoginRateLimiter limiter = new LoginRateLimiter(
            100,
            Duration.ofMinutes(1),
            1000,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            var futures = IntStream.range(0, 500)
                .mapToObj(index -> executor.submit(() -> {
                    try {
                        limiter.check("198.51.100.10", "concurrent@example.com");
                        accepted.incrementAndGet();
                    } catch (RateLimitExceededException exception) {
                        rejected.incrementAndGet();
                    }
                }))
                .toList();
            for (var future : futures) {
                future.get();
            }
        }

        assertThat(accepted.get()).isEqualTo(100);
        assertThat(rejected.get()).isEqualTo(400);
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
