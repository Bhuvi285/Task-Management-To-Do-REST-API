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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetRateLimiterTest {

    private static final byte[] TEST_HASH_KEY = new byte[32];

    @Test
    void separateAccountAndIpLimitsPreserveCooldown() {
        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(
            3,
            1,
            Duration.ofHours(1),
            100,
            Duration.ofMinutes(1),
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );

        assertThat(limiter.check("192.0.2.1", " User@example.com ")).isTrue();
        assertThat(limiter.check("192.0.2.2", "user@example.com")).isFalse();

        assertThat(limiter.check("192.0.2.1", "second@example.com")).isTrue();
        assertThat(limiter.check("192.0.2.1", "third@example.com")).isTrue();
        assertThatThrownBy(() -> limiter.check("192.0.2.1", "fourth@example.com"))
            .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void accountLimitSuppressesRequestsAcrossIpsAfterCooldown() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(
            10,
            2,
            Duration.ofHours(1),
            8,
            Duration.ofSeconds(1),
            clock,
            TEST_HASH_KEY
        );

        assertThat(limiter.check("192.0.2.1", "user@example.com")).isTrue();
        clock.advance(Duration.ofSeconds(2));
        assertThat(limiter.check("192.0.2.2", " USER@example.com ")).isTrue();
        clock.advance(Duration.ofSeconds(2));
        assertThat(limiter.check("192.0.2.3", "user@example.com")).isFalse();
        assertThat(limiter.check("192.0.2.4", "unrelated@example.com")).isTrue();
    }

    @Test
    void fillingCapacityDoesNotGloballyBlockUnrelatedClient() {
        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(
            5,
            5,
            Duration.ofMinutes(10),
            4,
            Duration.ofSeconds(30),
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );

        Set<String> occupiedSlots = new HashSet<>();
        int candidate = 1;
        while (occupiedSlots.size() < limiter.bucketCapacity()) {
            String ip = "198.51.100." + candidate;
            String email = "client-" + candidate + "@example.com";
            candidate++;
            String ipSlot = "ip:" + limiter.slotFor("ip", ip);
            String accountSlot = "account:" + limiter.slotFor("account", email);
            if (!occupiedSlots.contains(ipSlot)
                && !occupiedSlots.contains(accountSlot)) {
                assertThat(limiter.check(ip, email)).isTrue();
                occupiedSlots.add(ipSlot);
                occupiedSlots.add(accountSlot);
            }
        }
        assertThat(limiter.occupiedBucketCount()).isEqualTo(limiter.bucketCapacity());

        assertThat(limiter.check("203.0.113.10", null)).isTrue();
        assertThat(limiter.occupiedBucketCount()).isLessThanOrEqualTo(
            limiter.bucketCapacity()
        );
    }

    @Test
    void concurrentRequestsAreSafeAndBounded() throws Exception {
        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(
            10,
            10,
            Duration.ofMinutes(10),
            20,
            Duration.ofSeconds(30),
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            var futures = IntStream.range(0, 200)
                .mapToObj(index -> executor.submit(() -> {
                    try {
                        if (limiter.check("198.51.100.20", null)) {
                            accepted.incrementAndGet();
                        }
                    } catch (RateLimitExceededException exception) {
                        rejected.incrementAndGet();
                    }
                }))
                .toList();
            for (var future : futures) {
                future.get();
            }
        }

        assertThat(accepted.get()).isEqualTo(10);
        assertThat(rejected.get()).isEqualTo(190);
    }

    @Test
    void blockedAccountAndIpStateSurviveIdentityChurnUntilWindowExpiry() {
        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(
            100,
            1,
            Duration.ofHours(1),
            4,
            Duration.ofSeconds(30),
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );

        assertThat(limiter.check("198.51.100.1", "blocked@example.com")).isTrue();
        assertThat(limiter.check("198.51.100.2", "blocked@example.com")).isFalse();

        for (int index = 0; index < 100; index++) {
            try {
                limiter.check(
                    "203.0.113." + index,
                    "churn-" + index + "@example.com"
                );
            } catch (RateLimitExceededException exception) {
                assertThat(exception.getMessage()).contains("password reset");
            }
        }

        assertThat(limiter.check("198.51.100.4", "blocked@example.com")).isFalse();

        PasswordResetRateLimiter ipLimiter = new PasswordResetRateLimiter(
            2,
            100,
            Duration.ofHours(1),
            4,
            Duration.ofSeconds(30),
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
            TEST_HASH_KEY
        );
        ipLimiter.check("198.51.100.20", null);
        ipLimiter.check("198.51.100.20", null);
        assertThatThrownBy(() -> ipLimiter.check("198.51.100.20", null))
            .isInstanceOf(RateLimitExceededException.class);
        for (int index = 0; index < 100; index++) {
            try {
                ipLimiter.check("203.0.114." + index, null);
            } catch (RateLimitExceededException exception) {
                assertThat(exception.getMessage()).contains("password reset");
            }
        }
        assertThatThrownBy(() -> ipLimiter.check("198.51.100.20", null))
            .isInstanceOf(RateLimitExceededException.class);
        assertThat(limiter.occupiedBucketCount()).isLessThanOrEqualTo(
            limiter.bucketCapacity()
        );
    }

    @Test
    void expiredIpBucketsAreCleanedWhenAnotherIdentityArrives() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        PasswordResetRateLimiter limiter = new PasswordResetRateLimiter(
            3,
            3,
            Duration.ofSeconds(30),
            8,
            Duration.ofSeconds(5),
            clock,
            TEST_HASH_KEY
        );

        assertThat(limiter.check("198.51.100.1", null)).isTrue();
        assertThat(limiter.occupiedBucketCount()).isEqualTo(1);
        clock.advance(Duration.ofSeconds(31));

        assertThat(limiter.check("198.51.100.2", null)).isTrue();
        assertThat(limiter.occupiedBucketCount()).isEqualTo(1);
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
