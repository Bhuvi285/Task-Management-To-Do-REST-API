package com.taskmanager.security;

import com.taskmanager.config.AuthSecurityProperties;
import com.taskmanager.config.JwtProperties;
import com.taskmanager.dto.auth.LoginResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.RefreshTokenLifecycleService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2AuthenticationSuccessHandlerTest {

    private static final LoginResponse TOKEN_PAIR =
        new LoginResponse("application-jwt", "opaque-refresh-token", "Bearer", 900);

    private final RefreshTokenLifecycleService refreshTokenLifecycleService =
        mock(RefreshTokenLifecycleService.class);
    private final OAuth2SessionCleanup sessionCleanup = new OAuth2SessionCleanup(
        "JSESSIONID", "", "", false, true, "Lax"
    );
    private final AuthSecurityProperties securityProperties = new AuthSecurityProperties(
        List.of("http://localhost:3000"),
        "http://localhost:3000",
        new AuthSecurityProperties.RefreshCookieProperties(
            "taskflow_refresh_token",
            false,
            "Lax",
            "/api/v1/auth"
        ),
        10,
        Duration.ofMinutes(1),
        10000,
        10,
        3,
        Duration.ofHours(1),
        10000,
        Duration.ofMinutes(1),
        Duration.ofMinutes(15)
    );
    private final JwtProperties jwtProperties = new JwtProperties(
        "dGVzdC1vbmx5LXNpZ25pbmctc2VjcmV0LWZvci1qd3QtMzJieXRlcyE=",
        Duration.ofMinutes(15),
        Duration.ofDays(30),
        "task-management-api"
    );
    private final RefreshTokenCookieService cookieService =
        new RefreshTokenCookieService(securityProperties, jwtProperties);
    private final OAuth2AuthenticationFailureHandler failureHandler =
        new OAuth2AuthenticationFailureHandler(sessionCleanup, cookieService);
    private final OAuth2AuthenticationSuccessHandler successHandler =
        new OAuth2AuthenticationSuccessHandler(
            refreshTokenLifecycleService,
            failureHandler,
            sessionCleanup,
            cookieService
        );

    @Test
    void googleOidcAuthenticationSetsHttpOnlyCookieAndRedirectsToFrontend() throws Exception {
        User localUser = new User("Google User", "google@example.com", "bcrypt-hash");
        OidcUser providerUser = mock(OidcUser.class);
        when(providerUser.getAttributes()).thenReturn(Map.of(
            "access_token",
            "google-provider-access-token"
        ));
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
            new LocalOidcUser(providerUser, localUser),
            AuthorityUtils.NO_AUTHORITIES,
            "google"
        );
        when(refreshTokenLifecycleService.issueForUser(localUser.getEmail()))
            .thenReturn(TOKEN_PAIR);
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/google"),
            response,
            authentication
        );

        assertSuccessRedirect(response, "google-provider-access-token");
        verify(refreshTokenLifecycleService).issueForUser(localUser.getEmail());
    }

    @Test
    void githubAuthenticationSetsHttpOnlyCookieAndRedirectsToFrontend() throws Exception {
        User localUser = new User("GitHub User", "github@example.com", "bcrypt-hash");
        OAuth2User providerUser = mock(OAuth2User.class);
        when(providerUser.getAttributes()).thenReturn(Map.of(
            "access_token",
            "github-provider-access-token"
        ));
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
            new LocalOAuth2User(providerUser, localUser),
            AuthorityUtils.NO_AUTHORITIES,
            "github"
        );
        when(refreshTokenLifecycleService.issueForUser(localUser.getEmail()))
            .thenReturn(TOKEN_PAIR);
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/github"),
            response,
            authentication
        );

        assertSuccessRedirect(response, "github-provider-access-token");
        verify(refreshTokenLifecycleService).issueForUser(localUser.getEmail());
    }

    @Test
    void disabledUserIsRejectedWithoutIssuingTokens() throws Exception {
        User localUser = new User("Disabled", "disabled@example.com", "bcrypt-hash");
        localUser.setEnabled(false);
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
            new LocalOAuth2User(mock(OAuth2User.class), localUser),
            AuthorityUtils.NO_AUTHORITIES,
            "github"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/github"),
            response,
            authentication
        );

        assertFailureRedirect(response);
        verify(refreshTokenLifecycleService, never()).issueForUser(localUser.getEmail());
    }

    @Test
    void unsupportedPrincipalIsRejectedWithoutIssuingTokens() throws Exception {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
            "provider-user",
            null,
            List.of()
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/provider"),
            response,
            authentication
        );

        assertFailureRedirect(response);
        verify(refreshTokenLifecycleService, never()).issueForUser("provider-user");
    }

    private void assertSuccessRedirect(
        MockHttpServletResponse response,
        String providerAccessToken
    ) {
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        String location = response.getHeader("Location");
        assertThat(location).isEqualTo("http://localhost:3000/dashboard");
        assertThat(location).doesNotContain(
            TOKEN_PAIR.accessToken(),
            TOKEN_PAIR.refreshToken(),
            providerAccessToken
        );
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(response.getHeaders("Set-Cookie"))
            .anyMatch(c -> c.contains("JSESSIONID=;") && c.contains("Max-Age=0"))
            .anyMatch(c ->
                c.contains("taskflow_refresh_token=" + TOKEN_PAIR.refreshToken())
                    && c.contains("HttpOnly")
                    && c.contains("SameSite=Lax")
                    && c.contains("Path=/api/v1/auth")
            );
        Cookie refreshCookie = response.getCookie("taskflow_refresh_token");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.isHttpOnly()).isTrue();
    }

    private void assertFailureRedirect(MockHttpServletResponse response) {
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
