package com.taskmanager.security;

import com.taskmanager.entity.RefreshToken;
import com.taskmanager.entity.User;
import com.taskmanager.exception.InvalidCredentialsException;
import com.taskmanager.repository.RefreshTokenRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.RefreshTokenGenerator;
import com.taskmanager.service.RefreshTokenLifecycleService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OAuth2AuthenticationSuccessHandlerIntegrationTest {

    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler failureHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenLifecycleService refreshTokenLifecycleService;

    @Autowired
    private RefreshTokenGenerator refreshTokenGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void googleAndGithubIssueHttpOnlyRefreshCookiesAndIndependentSessions() throws Exception {
        User user = createUser("social-session");
        String googleRefresh = authenticateAndExtractRefreshCookie(
            "google",
            new LocalOidcUser(mock(OidcUser.class), user)
        );
        String githubRefresh = authenticateAndExtractRefreshCookie(
            "github",
            new LocalOAuth2User(mock(OAuth2User.class), user)
        );

        assertThat(googleRefresh).isNotEqualTo(githubRefresh);
        RefreshToken googleStored = storedToken(googleRefresh);
        RefreshToken githubStored = storedToken(githubRefresh);
        assertThat(googleStored.getTokenFamilyId()).isNotEqualTo(githubStored.getTokenFamilyId());

        // Cookie-based refresh rotates the refresh cookie and returns accessToken
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "http://localhost:3000")
                .cookie(new Cookie("taskflow_refresh_token", googleRefresh)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andReturn();

        Cookie rotatedCookie = refreshResult.getResponse().getCookie("taskflow_refresh_token");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.isHttpOnly()).isTrue();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(googleRefresh);
        assertThat(storedToken(rotatedCookie.getValue()).getTokenFamilyId())
            .isEqualTo(googleStored.getTokenFamilyId());

        // Reusing the old Google refresh cookie revokes the Google family, leaving GitHub intact
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Origin", "http://localhost:3000")
                .cookie(new Cookie("taskflow_refresh_token", googleRefresh)))
            .andExpect(status().isUnauthorized());

        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(
            googleStored.getTokenFamilyId()
        )).isZero();
        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(
            githubStored.getTokenFamilyId()
        )).isEqualTo(1);
    }

    @Test
    void disabledOrMissingLocalUserCannotObtainRefreshToken() throws Exception {
        User disabledUser = createUser("disabled-social");
        disabledUser.setEnabled(false);
        userRepository.saveAndFlush(disabledUser);
        long tokenCountBefore = refreshTokenRepository.count();
        assertThatThrownBy(() ->
            refreshTokenLifecycleService.issueForUser(disabledUser.getEmail())
        ).isInstanceOf(InvalidCredentialsException.class);

        MockHttpServletResponse disabledResponse = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/google"),
            disabledResponse,
            authentication(
                "google",
                new LocalOidcUser(mock(OidcUser.class), disabledUser)
            )
        );
        assertGenericFailureRedirect(disabledResponse);

        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/github"),
            missingResponse,
            authentication(
                "github",
                new LocalOAuth2User(
                    mock(OAuth2User.class),
                    new User("Missing", "missing-" + UUID.randomUUID() + "@example.com", "hash")
                )
            )
        );
        assertGenericFailureRedirect(missingResponse);
        assertThat(refreshTokenRepository.count()).isEqualTo(tokenCountBefore);
    }

    @Test
    void providerFailureRedirectsSafelyToLoginAndNeverIssuesTokens() throws Exception {
        long tokenCountBefore = refreshTokenRepository.count();
        MockHttpServletResponse response = new MockHttpServletResponse();
        failureHandler.onAuthenticationFailure(
            request("/login/oauth2/code/google"),
            response,
            new org.springframework.security.oauth2.core.OAuth2AuthenticationException(
                "provider-specific-secret-bearing-error"
            )
        );

        assertGenericFailureRedirect(response);
        assertThat(response.getHeader("Location"))
            .doesNotContain("provider-specific-secret-bearing-error");
        assertThat(refreshTokenRepository.count()).isEqualTo(tokenCountBefore);
    }

    private String authenticateAndExtractRefreshCookie(
        String registrationId,
        OAuth2User principal
    ) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/" + registrationId),
            response,
            authentication(registrationId, principal)
        );

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost:3000/dashboard");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        Cookie cookie = response.getCookie("taskflow_refresh_token");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).isNotBlank();
        return cookie.getValue();
    }

    private OAuth2AuthenticationToken authentication(
        String registrationId,
        OAuth2User principal
    ) {
        return new OAuth2AuthenticationToken(
            principal,
            AuthorityUtils.NO_AUTHORITIES,
            registrationId
        );
    }

    private User createUser(String prefix) {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        return userRepository.saveAndFlush(
            new User(prefix, email, passwordEncoder.encode("test-password"))
        );
    }

    private RefreshToken storedToken(String rawToken) {
        String hash = refreshTokenGenerator.hashToken(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash).orElseThrow();
        assertThat(token.getTokenHash()).isEqualTo(hash);
        assertThat(token.getTokenHash()).isNotEqualTo(rawToken);
        return token;
    }

    private void assertGenericFailureRedirect(MockHttpServletResponse response) {
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getHeader("Location"))
            .isEqualTo("http://localhost:3000/auth/login?error=oauth_failed");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(response.getCookie("taskflow_refresh_token")).isNull();
    }

    private MockHttpServletRequest request(String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        request.setRequestURI(requestUri);
        return request;
    }
}
