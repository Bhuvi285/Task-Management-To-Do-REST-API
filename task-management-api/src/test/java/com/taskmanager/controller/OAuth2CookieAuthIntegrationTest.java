package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.security.login-limit=1000")
@AutoConfigureMockMvc
class OAuth2CookieAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cookieRefreshLogoutAndCsrfOriginProtectionWorkEndToEnd() throws Exception {
        String email = "cookie-user-" + UUID.randomUUID() + "@example.com";
        userRepository.saveAndFlush(new User("Cookie User", email, passwordEncoder.encode("StrongPassword123!")));

        // 1. Login sets HttpOnly refresh cookie
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", "StrongPassword123!"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andReturn();

        Cookie loginCookie = loginResult.getResponse().getCookie("taskflow_refresh_token");
        assertThat(loginCookie).isNotNull();
        assertThat(loginCookie.isHttpOnly()).isTrue();
        assertThat(loginCookie.getPath()).isEqualTo("/api/v1/auth");

        // 2. CSRF protection rejects cookie-based refresh from untrusted or missing Origin
        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(loginCookie))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "https://evil.example.com")
                .cookie(loginCookie))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "http://localhost:3000")
                .header("Sec-Fetch-Site", "cross-site")
                .cookie(loginCookie))
            .andExpect(status().isForbidden());

        // 3. Valid Origin rotates refresh cookie and returns accessToken
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "http://localhost:3000")
                .cookie(loginCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andReturn();

        Cookie rotatedCookie = refreshResult.getResponse().getCookie("taskflow_refresh_token");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(loginCookie.getValue());

        // 4. Invalid cookie is rejected with 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "http://localhost:3000")
                .cookie(new Cookie("taskflow_refresh_token", "invalid-token-value")))
            .andExpect(status().isUnauthorized());

        // 5. Logout revokes session and clears cookie (Max-Age=0)
        MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
                .header("Origin", "http://localhost:3000")
                .cookie(rotatedCookie))
            .andExpect(status().isNoContent())
            .andReturn();

        Cookie cleared = logoutResult.getResponse().getCookie("taskflow_refresh_token");
        assertThat(cleared).isNotNull();
        assertThat(cleared.getMaxAge()).isZero();

        // 6. Refreshing with the revoked cookie fails with 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "http://localhost:3000")
                .cookie(rotatedCookie))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAllowsCredentialsForConfiguredOriginOnly() throws Exception {
        mockMvc.perform(get("/api/v1/test")
                .header("Origin", "http://localhost:3000"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

        mockMvc.perform(get("/api/v1/test")
                .header("Origin", "https://untrusted.example.com"))
            .andExpect(status().isForbidden());
    }

    @Test
    void optionsPreflightForAuthLoginAndRefreshReturnsCredentialsAndOriginHeaders() throws Exception {
        for (String endpoint : new String[]{"/api/v1/auth/login", "/api/v1/auth/refresh"}) {
            mockMvc.perform(options(endpoint)
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "content-type,accept,x-csrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));

            mockMvc.perform(options(endpoint)
                    .header("Origin", "https://evil.example.com")
                    .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
        }
    }
}
