package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.entity.PasswordResetToken;
import com.taskmanager.entity.RefreshToken;
import com.taskmanager.entity.User;
import com.taskmanager.repository.PasswordResetTokenRepository;
import com.taskmanager.repository.RefreshTokenRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.PasswordResetTokenDelivery;
import com.taskmanager.service.JwtService;
import com.taskmanager.service.RefreshTokenGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "app.security.login-limit=3",
    "app.security.password-reset-ip-limit=3",
    "app.security.password-reset-account-limit=3",
    "app.security.password-reset-window=PT1H",
    "app.security.password-reset-cooldown=PT1S"
})
@AutoConfigureMockMvc
@Import(AuthHardeningIntegrationTest.TestConfigurationBeans.class)
class AuthHardeningIntegrationTest {

    private static final String PASSWORD = "StrongPassword123!";
    private static final String PROTECTED_PATH = "/api/v1/security-probe";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private RefreshTokenGenerator tokenGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestPasswordResetDelivery resetDelivery;

    @Test
    void allowedCorsPreflightSupportsAuthorizationAndDisallowedOriginIsRejected()
        throws Exception {
        mockMvc.perform(options(PROTECTED_PATH)
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization,content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
            .andExpect(header().string(
                "Access-Control-Allow-Headers",
                containsString("authorization")
            ))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

        mockMvc.perform(options(PROTECTED_PATH)
                .header("Origin", "https://attacker.example")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isForbidden());
    }

    @Test
    void apiResponsesHaveAppropriateSecurityAndNoStoreHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/test"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string(
                "Referrer-Policy",
                "strict-origin-when-cross-origin"
            ));

        mockMvc.perform(get(PROTECTED_PATH))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string("Cache-Control", containsString("no-store")));
    }

    @Test
    void enabledUserCanLoginButDisabledUserCannotLoginRefreshOrUseAccessToken()
        throws Exception {
        User user = createUser();
        String email = user.getEmail();
        String login = login(email, PASSWORD);
        String refreshToken = objectMapper.readTree(login).get("refreshToken").asText();
        String accessToken = objectMapper.readTree(login).get("accessToken").asText();

        assertThat(userRepository.findByEmail(email).orElseThrow().isEnabled()).isTrue();
        user.setEnabled(false);
        userRepository.saveAndFlush(user);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, PASSWORD)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password."));
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(refreshToken)))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get(PROTECTED_PATH).header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRateLimitReturns429AfterConfiguredAttempts() throws Exception {
        String email = createUser().getEmail();
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .with(request -> {
                        request.setRemoteAddr("192.0.2.44");
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody(email, "WrongPassword123!")))
                .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.44");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, "WrongPassword123!")))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message")
                .value("Too many login attempts. Please try again later."));
    }

    @Test
    void passwordChangeUsesAuthenticatedPrincipalAndRevokesAllRefreshSessions()
        throws Exception {
        User user = createUser();
        String email = user.getEmail();
        String firstLogin = login(email, PASSWORD);
        String secondLogin = login(email, PASSWORD);
        String accessToken = objectMapper.readTree(firstLogin).get("accessToken").asText();
        User otherUser = createUser();
        String firstFamily = findRefreshToken(
            objectMapper.readTree(firstLogin).get("refreshToken").asText()
        ).getTokenFamilyId();
        String secondFamily = findRefreshToken(
            objectMapper.readTree(secondLogin).get("refreshToken").asText()
        ).getTokenFamilyId();
        String newPassword = "ChangedPassword123!";

        mockMvc.perform(post("/api/v1/auth/change-password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "userId", otherUser.getId(),
                    "currentPassword", PASSWORD,
                    "newPassword", newPassword
                ))))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("no-store")));

        User updated = userRepository.findByEmail(email).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updated.getPasswordHash())).isTrue();
        assertThat(updated.getPasswordHash()).isNotEqualTo(newPassword);
        assertThat(passwordEncoder.matches(
            PASSWORD,
            userRepository.findById(otherUser.getId()).orElseThrow().getPasswordHash()
        )).isTrue();
        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(firstFamily)).isZero();
        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(secondFamily)).isZero();

        mockMvc.perform(post("/api/v1/auth/change-password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordChangeBody("WrongPassword123!", "OtherPassword123!")))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get(PROTECTED_PATH).header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk());
    }

    @Test
    void passwordChangeInvalidatesOldResetTokenAndNewResetTokenStillWorks()
        throws Exception {
        User user = createUser();
        String email = user.getEmail();
        resetDelivery.clear();
        String login = login(email, PASSWORD);
        String accessToken = objectMapper.readTree(login).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(login).get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.80");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(email)))
            .andExpect(status().isAccepted());
        String oldResetToken = awaitResetDelivery();

        mockMvc.perform(post("/api/v1/auth/change-password")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordChangeBody(PASSWORD, "ChangedPassword123!")))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetPasswordBody(oldResetToken, "OldResetPassword123!")))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(refreshToken)))
            .andExpect(status().isUnauthorized());

        Thread.sleep(1100);
        resetDelivery.clear();
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.81");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(email)))
            .andExpect(status().isAccepted());
        String newResetToken = awaitResetDelivery();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetPasswordBody(newResetToken, "NewResetPassword123!")))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", containsString("no-store")));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, "NewResetPassword123!")))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, "ChangedPassword123!")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void accountLimitIsSilentAcrossIpsAndUnknownAccountsRemainGeneric()
        throws Exception {
        String email = createUser().getEmail();

        resetDelivery.clear();
        String firstResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.90");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(email)))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String deliveredToken = awaitResetDelivery();
        long persistedTokenCount = resetTokenRepository.count();
        int deliveryCount = resetDelivery.deliveryCount();

        String suppressedResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.91");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(email)))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String secondSuppressedResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.94");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(email)))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String unknownResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.92");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(uniqueEmail())))
            .andExpect(status().isAccepted())
            .andExpect(header().string("Cache-Control", containsString("no-store")))
            .andReturn()
            .getResponse()
            .getContentAsString();
        String genericMessage =
            "If the account is eligible, password-reset instructions will be sent.";
        assertThat(objectMapper.readTree(firstResponse).get("message").asText())
            .isEqualTo(genericMessage);
        assertThat(objectMapper.readTree(suppressedResponse).get("message").asText())
            .isEqualTo(genericMessage);
        assertThat(objectMapper.readTree(secondSuppressedResponse).get("message").asText())
            .isEqualTo(genericMessage);
        assertThat(objectMapper.readTree(unknownResponse).get("message").asText())
            .isEqualTo(genericMessage);
        assertThat(
            firstResponse + suppressedResponse + secondSuppressedResponse + unknownResponse
        )
            .doesNotContain(deliveredToken);
        assertThat(resetTokenRepository.count()).isEqualTo(persistedTokenCount);
        assertThat(resetDelivery.deliveryCount()).isEqualTo(deliveryCount);
    }

    @Test
    void passwordResetIpLimitStillReturns429() throws Exception {
        String remoteAddress = "192.0.2.93";
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                    .with(request -> {
                        request.setRemoteAddr(remoteAddress);
                        return request;
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(forgotPasswordBody(uniqueEmail())))
                .andExpect(status().isAccepted());
        }
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr(remoteAddress);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(uniqueEmail())))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message")
                .value("Too many password reset requests. Please try again later."));
    }

    @Test
    void forgotPasswordDoesNotEnumerateAndResetTokenIsSingleUse() throws Exception {
        User user = createUser();
        String email = user.getEmail();
        resetDelivery.clear();
        String refreshToken = objectMapper.readTree(login(email, PASSWORD))
            .get("refreshToken").asText();
        String familyId = findRefreshToken(refreshToken).getTokenFamilyId();

        String unknownResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.101");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(uniqueEmail())))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
        resetDelivery.clear();
        String knownResponse = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.102");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(email)))
            .andExpect(status().isAccepted())
            .andExpect(header().string("Cache-Control", containsString("no-store")))
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertThat(objectMapper.readTree(unknownResponse).get("message").asText())
            .isEqualTo(objectMapper.readTree(knownResponse).get("message").asText());

        String rawResetToken = awaitResetDelivery();
        assertThat(rawResetToken).isNotBlank();
        String storedHash = tokenGenerator.hashToken(rawResetToken);
        assertThat(resetTokenRepository.findAll())
            .anyMatch(token -> token.getTokenHash().equals(storedHash));
        assertThat(resetTokenRepository.findAll())
            .noneMatch(token -> token.getTokenHash().equals(rawResetToken));

        String otherActiveToken = tokenGenerator.generateToken();
        resetTokenRepository.saveAndFlush(new PasswordResetToken(
            user,
            tokenGenerator.hashToken(otherActiveToken),
            LocalDateTime.now().plusMinutes(15)
        ));
        String newPassword = "ResetPassword123!";
        String resetBody = resetPasswordBody(rawResetToken, newPassword);
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetPasswordBody(otherActiveToken, "AnotherPassword123!")))
            .andExpect(status().isBadRequest());

        assertThat(passwordEncoder.matches(
            newPassword,
            userRepository.findByEmail(email).orElseThrow().getPasswordHash()
        )).isTrue();
        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(familyId)).isZero();
    }

    @Test
    void passwordResetAccountLookupIsCaseInsensitive() throws Exception {
        User user = createUser();
        resetDelivery.clear();

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(request -> {
                    request.setRemoteAddr("192.0.2.109");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(forgotPasswordBody(user.getEmail().toUpperCase(java.util.Locale.ROOT))))
            .andExpect(status().isAccepted());

        assertThat(awaitResetDelivery()).isNotBlank();
    }

    @Test
    void expiredPasswordResetTokenIsRejected() throws Exception {
        User user = createUser();
        String unknownToken = tokenGenerator.generateToken();
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetPasswordBody(unknownToken, "ResetPassword123!")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Password reset request is invalid or expired."));

        String rawToken = tokenGenerator.generateToken();
        resetTokenRepository.saveAndFlush(new PasswordResetToken(
            user,
            tokenGenerator.hashToken(rawToken),
            LocalDateTime.now().minusSeconds(1)
        ));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetPasswordBody(rawToken, "ResetPassword123!")))
            .andExpect(status().isBadRequest());
    }

    private User createUser() {
        return userRepository.saveAndFlush(
            new User("Hardening Test", uniqueEmail(), passwordEncoder.encode(PASSWORD))
        );
    }

    private String login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .with(request -> {
                    request.setRemoteAddr("192.0.2." + (Math.floorMod(email.hashCode(), 250) + 1));
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, password)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    private RefreshToken findRefreshToken(String rawToken) {
        return refreshTokenRepository.findByTokenHash(tokenGenerator.hashToken(rawToken))
            .orElseThrow();
    }

    private String loginBody(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of("email", email, "password", password));
    }

    private String refreshBody(String refreshToken) throws Exception {
        return objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
    }

    private String passwordChangeBody(String currentPassword, String newPassword)
        throws Exception {
        return objectMapper.writeValueAsString(
            Map.of("currentPassword", currentPassword, "newPassword", newPassword)
        );
    }

    private String forgotPasswordBody(String email) throws Exception {
        return objectMapper.writeValueAsString(Map.of("email", email));
    }

    private String resetPasswordBody(String resetToken, String newPassword) throws Exception {
        return objectMapper.writeValueAsString(
            Map.of("resetToken", resetToken, "newPassword", newPassword)
        );
    }

    private String awaitResetDelivery() throws InterruptedException {
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (resetDelivery.lastToken() == null
            && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        return resetDelivery.lastToken();
    }

    private String uniqueEmail() {
        return "hardening-" + UUID.randomUUID() + "@example.com";
    }

    @TestConfiguration
    static class TestConfigurationBeans {

        @Bean
        TestPasswordResetDelivery passwordResetDelivery() {
            return new TestPasswordResetDelivery();
        }

        @Bean
        SecurityProbe securityProbe() {
            return new SecurityProbe();
        }
    }

    static class TestPasswordResetDelivery implements PasswordResetTokenDelivery {

        private final AtomicReference<String> rawToken = new AtomicReference<>();
        private final java.util.concurrent.atomic.AtomicInteger deliveryCount =
            new java.util.concurrent.atomic.AtomicInteger();

        @Override
        public void deliver(String email, String rawResetToken) {
            deliveryCount.incrementAndGet();
            rawToken.set(rawResetToken);
        }

        String lastToken() {
            return rawToken.get();
        }

        void clear() {
            rawToken.set(null);
            deliveryCount.set(0);
        }

        int deliveryCount() {
            return deliveryCount.get();
        }
    }

    @RestController
    static class SecurityProbe {

        @GetMapping(PROTECTED_PATH)
        Map<String, String> probe() {
            return Map.of("status", "authenticated");
        }
    }
}
