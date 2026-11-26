package com.taskmanager.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenGeneratorTest {

    private final RefreshTokenGenerator generator = new RefreshTokenGenerator();

    @Test
    void shouldGenerateDifferentUrlSafeTokensWithAtLeast256BitsOfEntropy() {
        String first = generator.generateToken();
        String second = generator.generateToken();

        assertThat(first).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(second).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void shouldGenerateStableSha256HashThatDoesNotEqualRawToken() {
        String rawToken = generator.generateToken();

        String firstHash = generator.hashToken(rawToken);
        String secondHash = generator.hashToken(rawToken);

        assertThat(firstHash).hasSize(43);
        assertThat(firstHash).isEqualTo(secondHash);
        assertThat(firstHash).isNotEqualTo(rawToken);
    }
}
