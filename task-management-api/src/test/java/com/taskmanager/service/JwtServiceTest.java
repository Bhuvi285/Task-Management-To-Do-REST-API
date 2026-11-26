package com.taskmanager.service;

import com.taskmanager.config.JwtProperties;
import com.taskmanager.config.JwtConfiguration;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = loadTestSecret();
    private static final String ISSUER = "task-management-api";

    private final JwtService jwtService = new JwtService(
        new JwtProperties(
            TEST_SECRET,
            Duration.ofMinutes(15),
            Duration.ofDays(30),
            ISSUER
        )
    );

    @Test
    void shouldGenerateValidTokenWithEmailSubjectAndExpiration() {
        String token = jwtService.generateAccessToken("alice@example.com");

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).isEqualTo("alice@example.com");
        assertThat(jwtService.extractExpiration(token))
            .isAfter(Instant.now())
            .isBefore(Instant.now().plus(Duration.ofMinutes(16)));
    }

    @Test
    void shouldRejectExpiredToken() {
        String expiredToken = createToken(
            TEST_SECRET,
            "alice@example.com",
            Instant.now().minusSeconds(120),
            Instant.now().minusSeconds(60),
            false
        );

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
        assertThatThrownBy(() -> jwtService.extractSubject(expiredToken))
            .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
        assertThatThrownBy(() -> jwtService.extractSubject("not.a.jwt"))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldRejectTokenSignedWithAnotherSecret() {
        byte[] otherKey = new byte[32];
        java.util.Arrays.fill(otherKey, (byte) 9);
        String otherSecret = Base64.getEncoder().encodeToString(otherKey);
        String token = createToken(
            otherSecret,
            "alice@example.com",
            Instant.now(),
            Instant.now().plusSeconds(300),
            false
        );

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldRejectTokenWithUnexpectedIssuer() {
        String token = Jwts.builder()
            .issuer("unexpected-issuer")
            .subject("alice@example.com")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(300)))
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET)))
            .compact();

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldRejectTokenMissingIssueTime() {
        String token = Jwts.builder()
            .issuer(ISSUER)
            .subject("alice@example.com")
            .expiration(Date.from(Instant.now().plusSeconds(300)))
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_SECRET)))
            .compact();

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldRejectTokenUsingUnexpectedSigningAlgorithm() {
        byte[] keyBytes = new byte[64];
        java.util.Arrays.fill(keyBytes, (byte) 7);
        String longSecret = Base64.getEncoder().encodeToString(keyBytes);
        JwtService service = new JwtService(
            new JwtProperties(
                longSecret,
                Duration.ofMinutes(15),
                Duration.ofDays(30),
                ISSUER
            )
        );
        String token = createToken(
            longSecret,
            "alice@example.com",
            Instant.now(),
            Instant.now().plusSeconds(300),
            true
        );

        assertThat(service.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldFailFastWhenSigningSecretIsMissingOrWeak() {
        assertJwtContextFails(null, "JWT signing secret is missing");
        assertJwtContextFails("", "JWT signing secret is missing");
        assertJwtContextFails("dG9vLXNob3J0", "at least 32 bytes");
    }

    @Test
    void shouldFailFastWhenSigningSecretIsInvalidBase64() {
        assertJwtContextFails("not-valid-base64-%%%!", "valid Base64");
    }

    private void assertJwtContextFails(String secret, String expectedMessage) {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtConfiguration.class, JwtService.class)
            .withInitializer(context -> {
                context.getEnvironment().getPropertySources().remove("systemEnvironment");
                context.getEnvironment().getPropertySources().remove("systemProperties");
            });
        if (secret != null) {
            contextRunner = contextRunner.withPropertyValues("app.jwt.secret=" + secret);
        }

        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                .hasStackTraceContaining(expectedMessage);
        });
    }

    private String createToken(
        String secret,
        String subject,
        Instant issuedAt,
        Instant expiresAt,
        boolean sha384
    ) {
        var builder = Jwts.builder()
            .issuer(ISSUER)
            .subject(subject)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt));
        var key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        if (sha384) {
            return builder.signWith(key, Jwts.SIG.HS384).compact();
        }
        return builder.signWith(key, Jwts.SIG.HS256).compact();
    }

    private static String loadTestSecret() {
        Properties properties = new Properties();
        try (InputStream input = JwtServiceTest.class.getResourceAsStream(
            "/application.properties"
        )) {
            if (input == null) {
                throw new IllegalStateException("Test application.properties is missing.");
            }
            properties.load(input);
            return properties.getProperty("app.jwt.secret");
        } catch (IOException exception) {
            throw new IllegalStateException("Test JWT secret could not be loaded.", exception);
        }
    }
}
