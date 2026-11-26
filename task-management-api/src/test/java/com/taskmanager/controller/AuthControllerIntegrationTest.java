package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.config.JwtProperties;
import com.taskmanager.entity.RefreshToken;
import com.taskmanager.entity.User;
import com.taskmanager.repository.RefreshTokenRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.JwtService;
import com.taskmanager.service.RefreshTokenGenerator;
import com.taskmanager.service.RefreshTokenLifecycleService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.login-limit=1000")
@AutoConfigureMockMvc
@Import(AuthControllerIntegrationTest.ProtectedEndpointConfiguration.class)
class AuthControllerIntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String PROTECTED_URL = "/api/v1/test-protected";
    private static final String PASSWORD = "StrongPassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenGenerator refreshTokenGenerator;

    @Autowired
    private RefreshTokenLifecycleService refreshTokenLifecycleService;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void shouldRegisterUserWithoutAuthenticationAndReturnGenericSafeResponse() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Alice", email, PASSWORD)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string(
                "Cache-Control",
                org.hamcrest.Matchers.containsString("no-store")
            ))
            .andExpect(jsonPath("$.message").value(
                "If registration can be completed, the account is ready to use."
            ))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andExpect(jsonPath("$.password_hash").doesNotExist());

        User savedUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        String request = requestBody("Alice", email, PASSWORD);

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isAccepted());

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.message").value(
                "If registration can be completed, the account is ready to use."
            ));
    }

    @Test
    void registrationAndLoginNormalizeEmailCaseConsistently() throws Exception {
        String email = "Mixed.Case-" + UUID.randomUUID() + "@Example.com";
        String normalizedEmail = email.toLowerCase(java.util.Locale.ROOT);

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Alice", email, PASSWORD)))
            .andExpect(status().isAccepted());

        User storedUser = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseThrow();
        assertThat(storedUser.getEmail()).isEqualTo(normalizedEmail);

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Another Alice", normalizedEmail, PASSWORD)))
            .andExpect(status().isAccepted());
        assertThat(userRepository.countByEmailIgnoreCase(normalizedEmail)).isEqualTo(1);

        String loginResponse = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email.toUpperCase(java.util.Locale.ROOT), PASSWORD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertThat(jwtService.extractSubject(
            objectMapper.readTree(loginResponse).get("accessToken").asText()
        )).isEqualTo(normalizedEmail);
    }

    @Test
    void concurrentDuplicateRegistrationReturnsGenericResponsesSafely()
        throws Exception {
        String email = uniqueEmail();
        String request = requestBody("Alice", email, PASSWORD);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Future<String> first = executor.submit(() -> {
                start.await();
                var response = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                    .andReturn().getResponse();
                return response.getStatus() + "|" + response.getContentAsString();
            });
            Future<String> second = executor.submit(() -> {
                start.await();
                var response = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                    .andReturn().getResponse();
                return response.getStatus() + "|" + response.getContentAsString();
            });
            start.countDown();

            String firstResponse = first.get();
            String secondResponse = second.get();
            assertThat(firstResponse).startsWith("202|");
            assertThat(secondResponse).startsWith("202|");
            assertThat(firstResponse).isEqualTo(secondResponse);
        }
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Alice", "not-an-email", PASSWORD)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void missingRegistrationAndLoginFieldsReturnStructuredValidationErrors() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.path").value(REGISTER_URL))
            .andExpect(jsonPath("$.details.name").exists())
            .andExpect(jsonPath("$.details.email").exists())
            .andExpect(jsonPath("$.details.password").exists());

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value(LOGIN_URL))
            .andExpect(jsonPath("$.details.email").exists())
            .andExpect(jsonPath("$.details.password").exists());

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ malformed"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request body."))
            .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    void shouldRejectBlankName() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(" ", uniqueEmail(), PASSWORD)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    void shouldRejectPasswordBelowMinimumLength() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Alice", uniqueEmail(), "short")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void shouldRejectPasswordAboveMaximumLength() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Alice", uniqueEmail(), "a".repeat(73))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void shouldRejectPasswordExceedingBcryptUtf8ByteLimit() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Alice", uniqueEmail(), "é".repeat(37))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void shouldLoginWithValidCredentialsAndPersistOnlyRefreshTokenHash() throws Exception {
        String email = createUser();

        String response = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(header().string(
                "Cache-Control",
                org.hamcrest.Matchers.containsString("no-store")
            ))
            .andExpect(header().string(
                "Set-Cookie",
                org.hamcrest.Matchers.allOf(
                    org.hamcrest.Matchers.containsString("taskflow_refresh_token="),
                    org.hamcrest.Matchers.containsString("HttpOnly"),
                    org.hamcrest.Matchers.containsString("SameSite=Lax"),
                    org.hamcrest.Matchers.containsString("Path=/api/v1/auth")
                )
            ))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andExpect(jsonPath("$.password_hash").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String token = objectMapper.readTree(response).get("accessToken").asText();
        String rawRefreshToken = objectMapper.readTree(response).get("refreshToken").asText();
        assertThat(jwtService.extractSubject(token)).isEqualTo(email);
        assertThat(rawRefreshToken).hasSize(43);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(
            refreshTokenGenerator.hashToken(rawRefreshToken)
        ).orElseThrow();
        assertThat(storedToken.getTokenHash()).isNotEqualTo(rawRefreshToken);
        assertThat(storedToken.getExpiresAt())
            .isAfter(LocalDateTime.now().plus(jwtProperties.refreshTokenExpiration()).minusSeconds(10));
        assertThat(refreshTokenRepository.findByTokenHash(rawRefreshToken)).isEmpty();
    }

    @Test
    void shouldReturnSameSafeErrorForWrongPasswordAndUnknownEmail() throws Exception {
        String email = createUser();

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, "WrongPassword123!")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password."));

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(uniqueEmail(), PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void shouldRejectInvalidEmailAndBlankPasswordOnLogin() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("invalid-email", PASSWORD)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.email").exists());

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(uniqueEmail(), " ")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void shouldRejectProtectedRequestWithoutBearerToken() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldRejectProtectedRequestWithInvalidBearerToken() throws Exception {
        mockMvc.perform(get(PROTECTED_URL).header("Authorization", "Bearer invalid.token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldAuthenticateValidBearerTokenAndSetSecurityContextPrincipal() throws Exception {
        String email = createUser();
        String token = jwtService.generateAccessToken(email);

        mockMvc.perform(get(PROTECTED_URL).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.principal").value(email));
    }

    @Test
    void shouldRejectAccessTokenForUserThatNoLongerExists() throws Exception {
        User user = createUserEntity();
        String token = jwtService.generateAccessToken(user.getEmail());
        userRepository.delete(user);
        userRepository.flush();

        mockMvc.perform(get(PROTECTED_URL).header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRotateRefreshTokenAndPreserveItsFamily() throws Exception {
        String rawToken = loginForRefreshToken(createUser());
        RefreshToken oldToken = findRefreshToken(rawToken);

        String response = mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(rawToken)))
            .andExpect(status().isOk())
            .andExpect(header().string(
                "Cache-Control",
                org.hamcrest.Matchers.containsString("no-store")
            ))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String replacementRawToken =
            objectMapper.readTree(response).get("refreshToken").asText();
        RefreshToken updatedOldToken = refreshTokenRepository.findById(oldToken.getId())
            .orElseThrow();
        RefreshToken replacement = findRefreshToken(replacementRawToken);

        assertThat(updatedOldToken.getRevokedAt()).isNotNull();
        assertThat(updatedOldToken.getReplacedByTokenId()).isEqualTo(replacement.getId());
        assertThat(replacement.getTokenFamilyId()).isEqualTo(oldToken.getTokenFamilyId());
        assertThat(replacement.getTokenHash()).isNotEqualTo(replacementRawToken);
    }

    @Test
    void shouldRejectUnknownExpiredAndRevokedRefreshTokens() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody("unknown-refresh-token")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Refresh token is invalid or expired."));

        User user = createUserEntity();
        RefreshToken expired = new RefreshToken(
            user,
            refreshTokenGenerator.hashToken("expired-refresh-token"),
            UUID.randomUUID().toString(),
            LocalDateTime.now().minusSeconds(1)
        );
        refreshTokenRepository.saveAndFlush(expired);
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody("expired-refresh-token")))
            .andExpect(status().isUnauthorized());

        RefreshToken revoked = new RefreshToken(
            user,
            refreshTokenGenerator.hashToken("revoked-refresh-token"),
            UUID.randomUUID().toString(),
            LocalDateTime.now().plusDays(1)
        );
        revoked.revoke(LocalDateTime.now(), null);
        refreshTokenRepository.saveAndFlush(revoked);
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody("revoked-refresh-token")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void reusedRotatedTokenRevokesOnlyItsFamily() throws Exception {
        String email = createUser();
        String originalToken = loginForRefreshToken(email);
        String unrelatedSessionToken = loginForRefreshToken(email);
        String familyId = findRefreshToken(originalToken).getTokenFamilyId();

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(originalToken)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(originalToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Refresh token is invalid or expired."));

        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(familyId)).isZero();
        assertThat(findRefreshToken(unrelatedSessionToken).getRevokedAt()).isNull();
    }

    @Test
    void logoutIsIdempotentAndScopedToOneSessionFamily() throws Exception {
        String email = createUser();
        String firstSessionToken = loginForRefreshToken(email);
        String secondSessionToken = loginForRefreshToken(email);
        String firstFamily = findRefreshToken(firstSessionToken).getTokenFamilyId();
        String secondFamily = findRefreshToken(secondSessionToken).getTokenFamilyId();

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(firstSessionToken)))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(firstSessionToken)))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody("unknown-refresh-token")))
            .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(firstFamily)).isZero();
        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(secondFamily)).isEqualTo(1);
    }

    @Test
    void logoutRevokesRefreshTokenButDoesNotRevokeAnIssuedAccessToken() throws Exception {
        String email = createUser();
        String loginResponse = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, PASSWORD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String accessToken = objectMapper.readTree(loginResponse)
            .get("accessToken")
            .asText();
        String refreshToken = objectMapper.readTree(loginResponse)
            .get("refreshToken")
            .asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(refreshToken)))
            .andExpect(status().isNoContent())
            .andExpect(header().string(
                "Cache-Control",
                org.hamcrest.Matchers.containsString("no-store")
            ));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(refreshToken)))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get(PROTECTED_URL)
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.principal").value(email));
    }

    @Test
    void simultaneousRefreshesCannotBothSucceedAndReuseRevokesFamily() throws Exception {
        String rawToken = loginForRefreshToken(createUser());
        String familyId = findRefreshToken(rawToken).getTokenFamilyId();
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<RefreshTokenLifecycleService.RotationResult> first = executor.submit(() -> {
                start.await();
                return refreshTokenLifecycleService.rotate(rawToken);
            });
            Future<RefreshTokenLifecycleService.RotationResult> second = executor.submit(() -> {
                start.await();
                return refreshTokenLifecycleService.rotate(rawToken);
            });
            start.countDown();

            List<RefreshTokenLifecycleService.RotationResult> results =
                List.of(first.get(), second.get());
            assertThat(results)
                .extracting(RefreshTokenLifecycleService.RotationResult::status)
                .containsExactlyInAnyOrder(
                    RefreshTokenLifecycleService.RotationStatus.SUCCESS,
                    RefreshTokenLifecycleService.RotationStatus.REUSED
                );
        }

        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(familyId)).isZero();
    }

    @Test
    void shouldRejectInvalidRefreshRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(" ")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.refreshToken").exists());
    }

    @Test
    void shouldRejectMalformedBearerToken() throws Exception {
        mockMvc.perform(get(PROTECTED_URL).header("Authorization", "Bearer invalid.jwt.token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejectsExpiredInvalidSignatureAndMalformedClaimTokensSafely() throws Exception {
        String email = createUser();
        var signingKey = Keys.hmacShaKeyFor(
            Base64.getDecoder().decode(jwtProperties.secret())
        );
        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
            .issuer(jwtProperties.issuer())
            .subject(email)
            .issuedAt(Date.from(now.minusSeconds(120)))
            .expiration(Date.from(now.minusSeconds(60)))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();

        byte[] alternateKeyBytes = new byte[32];
        java.util.Arrays.fill(alternateKeyBytes, (byte) 7);
        String invalidSignatureToken = Jwts.builder()
            .issuer(jwtProperties.issuer())
            .subject(email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(300)))
            .signWith(Keys.hmacShaKeyFor(alternateKeyBytes), Jwts.SIG.HS256)
            .compact();
        String invalidIssuerToken = Jwts.builder()
            .issuer(jwtProperties.issuer() + "-unexpected")
            .subject(email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(300)))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();

        String malformedClaimToken = malformedClaimToken(email, now.plusSeconds(300));

        for (String token : List.of(
            expiredToken,
            invalidSignatureToken,
            invalidIssuerToken,
            malformedClaimToken
        )) {
            String body = mockMvc.perform(get(PROTECTED_URL)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                    .value("Authentication is required or the bearer token is invalid."))
                .andReturn()
                .getResponse()
                .getContentAsString();

            assertThat(body)
                .doesNotContain("Exception", "passwordHash", jwtProperties.secret());
        }
    }

    private String loginBody(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of("email", email, "password", password));
    }

    private String malformedClaimToken(String email, Instant expiresAt) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT"))
        );
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(Map.of(
                "iss", jwtProperties.issuer(),
                "sub", email,
                "iat", List.of("malformed"),
                "exp", expiresAt.getEpochSecond()
            ))
        );
        String signingInput = header + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(
            mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII))
        );
        return signingInput + "." + signature;
    }

    private String refreshBody(String refreshToken) throws Exception {
        return objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
    }

    private String requestBody(String name, String email, String password) throws Exception {
        return objectMapper.writeValueAsString(
            Map.of("name", name, "email", email, "password", password)
        );
    }

    private String createUser() {
        return createUserEntity().getEmail();
    }

    private User createUserEntity() {
        String email = uniqueEmail();
        return userRepository.saveAndFlush(
            new User("Test User", email, passwordEncoder.encode(PASSWORD))
        );
    }

    private String loginForRefreshToken(String email) throws Exception {
        String response = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, PASSWORD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("refreshToken").asText();
    }

    private RefreshToken findRefreshToken(String rawToken) {
        return refreshTokenRepository.findByTokenHash(
            refreshTokenGenerator.hashToken(rawToken)
        ).orElseThrow();
    }

    private String uniqueEmail() {
        return "registration-" + UUID.randomUUID() + "@example.com";
    }

    @TestConfiguration
    static class ProtectedEndpointConfiguration {

        @Bean
        ProtectedTestEndpoint protectedTestEndpoint() {
            return new ProtectedTestEndpoint();
        }
    }

    @RestController
    static class ProtectedTestEndpoint {

        @GetMapping(PROTECTED_URL)
        Map<String, String> principal(Authentication authentication) {
            return Map.of("principal", authentication.getName());
        }
    }
}
