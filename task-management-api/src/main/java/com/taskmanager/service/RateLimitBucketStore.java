package com.taskmanager.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

final class RateLimitBucketStore {

    private static final String HASH_ALGORITHM = "HmacSHA256";

    private final Bucket[] buckets;
    private final Mac mac;

    RateLimitBucketStore(int capacity) {
        this(capacity, randomKey());
    }

    RateLimitBucketStore(int capacity, byte[] hashKey) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Rate-limit bucket capacity must be positive.");
        }
        if (hashKey == null || hashKey.length < 32) {
            throw new IllegalArgumentException("Rate-limit hash key must contain at least 32 bytes.");
        }
        this.buckets = new Bucket[capacity];
        try {
            this.mac = Mac.getInstance(HASH_ALGORITHM);
            this.mac.init(new SecretKeySpec(hashKey.clone(), HASH_ALGORITHM));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HMAC-SHA-256 is unavailable.", exception);
        }
    }

    synchronized boolean increment(
        String identity,
        int limit,
        Duration window,
        Duration cooldown,
        Instant now
    ) {
        int index = indexFor(identity);
        Bucket bucket = buckets[index];
        boolean windowExpired = bucket != null
            && !bucket.startedAt().plus(window).isAfter(now);
        boolean cooldownExpired = cooldown == null || cooldown.isZero()
            || (bucket != null && !bucket.lastRequest().plus(cooldown).isAfter(now));
        if (bucket == null || (windowExpired && cooldownExpired)) {
            buckets[index] = new Bucket(now, now, 1);
            return true;
        }
        if (bucket.requests() >= limit
            || (cooldown != null && !cooldown.isZero()
                && bucket.lastRequest().plus(cooldown).isAfter(now))) {
            return false;
        }
        buckets[index] = new Bucket(
            bucket.startedAt(),
            now,
            bucket.requests() + 1
        );
        return true;
    }

    synchronized void removeExpired(Instant now, Duration window, Duration cooldown) {
        for (int index = 0; index < buckets.length; index++) {
            Bucket bucket = buckets[index];
            boolean windowExpired = bucket != null
                && !bucket.startedAt().plus(window).isAfter(now);
            boolean cooldownExpired = cooldown == null || cooldown.isZero()
                || (bucket != null && !bucket.lastRequest().plus(cooldown).isAfter(now));
            if (windowExpired && cooldownExpired) {
                buckets[index] = null;
            }
        }
    }

    synchronized int occupiedBucketCount() {
        int count = 0;
        for (Bucket bucket : buckets) {
            if (bucket != null) {
                count++;
            }
        }
        return count;
    }

    int capacity() {
        return buckets.length;
    }

    synchronized int slotFor(String identity) {
        return indexFor(identity);
    }

    private int indexFor(String identity) {
        byte[] digest = mac.doFinal(identity.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        long value = 0;
        for (int index = 0; index < Long.BYTES; index++) {
            value = (value << Byte.SIZE) | (digest[index] & 0xffL);
        }
        return (int) Long.remainderUnsigned(value, buckets.length);
    }

    private static byte[] randomKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return key;
    }

    private record Bucket(Instant startedAt, Instant lastRequest, int requests) {
    }
}
