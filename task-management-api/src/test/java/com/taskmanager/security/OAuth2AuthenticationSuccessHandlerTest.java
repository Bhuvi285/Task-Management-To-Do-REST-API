package com.taskmanager.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.auth.LoginResponse;
import com.taskmanager.entity.User;
import com.taskmanager.service.RefreshTokenLifecycleService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

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

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RefreshTokenLifecycleService refreshTokenLifecycleService =
        mock(RefreshTokenLifecycleService.class);
    private final OAuth2SessionCleanup sessionCleanup = new OAuth2SessionCleanup(
        "JSESSIONID", "", "", false, true, "Lax"
    );
    private final OAuth2AuthenticationFailureHandler failureHandler =
        new OAuth2AuthenticationFailureHandler(objectMapper, sessionCleanup);
    private final OAuth2AuthenticationSuccessHandler successHandler =
        new OAuth2AuthenticationSuccessHandler(
            refreshTokenLifecycleService,
            failureHandler,
            sessionCleanup,
            objectMapper
        );

    @Test
    void googleOidcAuthenticationReturnsApplicationTokenPairAsNonCacheableJson()
        throws Exception {
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

        assertSuccessResponse(response, "google-provider-access-token");
        verify(refreshTokenLifecycleService).issueForUser(localUser.getEmail());
    }

    @Test
    void githubAuthenticationUsesSameApplicationTokenResponseContract() throws Exception {
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

        assertSuccessResponse(response, "github-provider-access-token");
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

        assertFailureResponse(response);
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

        assertFailureResponse(response);
        verify(refreshTokenLifecycleService, never()).issueForUser("provider-user");
    }

    private void assertSuccessResponse(
        MockHttpServletResponse response,
        String providerAccessToken
    ) throws Exception {
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(response.getHeader("Location")).isNull();
        assertThat(response.getHeader("Set-Cookie"))
            .contains("JSESSIONID=;", "Max-Age=0", "HttpOnly", "SameSite=Lax");
        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.fieldNames()).toIterable()
            .containsExactlyInAnyOrder("accessToken", "refreshToken", "tokenType", "expiresIn");
        assertThat(json.get("accessToken").asText()).isEqualTo(TOKEN_PAIR.accessToken());
        assertThat(json.get("refreshToken").asText()).isEqualTo(TOKEN_PAIR.refreshToken());
        assertThat(json.get("tokenType").asText()).isEqualTo(TOKEN_PAIR.tokenType());
        assertThat(json.get("expiresIn").asLong()).isEqualTo(TOKEN_PAIR.expiresIn());
        assertThat(response.getContentAsString()).doesNotContain(providerAccessToken);
    }

    private void assertFailureResponse(MockHttpServletResponse response) throws Exception {
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(objectMapper.readTree(response.getContentAsString()).get("message").asText())
            .isEqualTo("Social authentication failed.");
    }

    private MockHttpServletRequest request(String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        request.setRequestURI(requestUri);
        return request;
    }
}
