package com.taskmanager.service;

import com.taskmanager.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String SIGNING_ALGORITHM = "HS256";
    private static final int MINIMUM_KEY_LENGTH_BYTES = 32;

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = createSigningKey(properties.secret());
        validateProperties(properties);
    }

    public String generateAccessToken(String email) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
            .issuer(properties.issuer())
            .subject(email)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(issuedAt.plus(properties.accessTokenExpiration())))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    public String extractSubject(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    public Instant extractExpiration(String token) {
        return parseToken(token).getPayload().getExpiration().toInstant();
    }

    public boolean isTokenValid(String token) {
        try {
            Jws<Claims> parsedToken = parseToken(token);
            return parsedToken.getPayload().getSubject() != null
                && !parsedToken.getPayload().getSubject().isBlank();
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return properties.accessTokenExpiration().toSeconds();
    }

    private Jws<Claims> parseToken(String token) {
        Jws<Claims> parsedToken = Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(properties.issuer())
            .build()
            .parseSignedClaims(token);

        if (!SIGNING_ALGORITHM.equals(parsedToken.getHeader().getAlgorithm())) {
            throw new JwtException("Unexpected token signing algorithm.");
        }
        if (parsedToken.getPayload().getExpiration() == null) {
            throw new JwtException("Token expiration is missing.");
        }
        Date issuedAt = parsedToken.getPayload().getIssuedAt();
        if (issuedAt == null || issuedAt.toInstant().isAfter(Instant.now())) {
            throw new JwtException("Token issue time is missing or invalid.");
        }
        if (parsedToken.getPayload().getSubject() == null
            || parsedToken.getPayload().getSubject().isBlank()) {
            throw new JwtException("Token subject is missing.");
        }
        return parsedToken;
    }

    private SecretKey createSigningKey(String encodedSecret) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException(
                "JWT signing secret is missing. Set JWT_SECRET to a Base64-encoded random key."
            );
        }

        final byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(encodedSecret);
        } catch (DecodingException | IllegalArgumentException exception) {
            throw new IllegalStateException(
                "JWT_SECRET must be a valid Base64-encoded key of at least 32 bytes.",
                exception
            );
        }

        if (keyBytes.length < MINIMUM_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                "JWT_SECRET must decode to at least 32 bytes for HS256."
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void validateProperties(JwtProperties jwtProperties) {
        Duration expiration = jwtProperties.accessTokenExpiration();
        if (expiration == null || expiration.isZero() || expiration.isNegative()
            || expiration.toSeconds() <= 0) {
            throw new IllegalStateException(
                "app.jwt.access-token-expiration must be a positive duration of at least one second."
            );
        }
        Duration refreshExpiration = jwtProperties.refreshTokenExpiration();
        if (refreshExpiration == null || refreshExpiration.isZero() || refreshExpiration.isNegative()
            || refreshExpiration.toSeconds() <= 0) {
            throw new IllegalStateException(
                "app.jwt.refresh-token-expiration must be a positive duration of at least one second."
            );
        }
        if (jwtProperties.issuer() == null || jwtProperties.issuer().isBlank()) {
            throw new IllegalStateException("app.jwt.issuer must not be blank.");
        }
    }
}
