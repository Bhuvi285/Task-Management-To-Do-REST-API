package com.taskmanager.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.auth.LoginResponse;
import com.taskmanager.entity.RefreshToken;
import com.taskmanager.entity.User;
import com.taskmanager.exception.InvalidCredentialsException;
import com.taskmanager.repository.RefreshTokenRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.JwtService;
import com.taskmanager.service.RefreshTokenGenerator;
import com.taskmanager.service.RefreshTokenLifecycleService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;

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
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void googleAndGithubIssueApplicationTokenPairsAndIndependentRefreshSessions()
        throws Exception {
        User user = createUser("social-session");
        LoginResponse google = authenticate(
            user,
            "google",
            new LocalOidcUser(mock(OidcUser.class), user)
        );
        LoginResponse github = authenticate(
            user,
            "github",
            new LocalOAuth2User(mock(OAuth2User.class), user)
        );

        assertThat(jwtService.extractSubject(google.accessToken())).isEqualTo(user.getEmail());
        assertThat(jwtService.extractSubject(github.accessToken())).isEqualTo(user.getEmail());
        assertThat(google.refreshToken()).isNotEqualTo(github.refreshToken());
        RefreshToken googleStored = storedToken(google.refreshToken());
        RefreshToken githubStored = storedToken(github.refreshToken());
        assertThat(googleStored.getTokenFamilyId()).isNotEqualTo(githubStored.getTokenFamilyId());

        RefreshTokenLifecycleService.RotationResult rotation =
            refreshTokenLifecycleService.rotate(google.refreshToken());
        assertThat(rotation.status())
            .isEqualTo(RefreshTokenLifecycleService.RotationStatus.SUCCESS);
        LoginResponse rotated = rotation.tokenPair();
        assertThat(rotated).isNotNull();
        assertThat(storedToken(rotated.refreshToken()).getTokenFamilyId())
            .isEqualTo(googleStored.getTokenFamilyId());
        refreshTokenLifecycleService.revokeFamilyForToken(rotated.refreshToken());
        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(
            googleStored.getTokenFamilyId()
        )).isZero();
        assertThat(refreshTokenLifecycleService.rotate(google.refreshToken()).status())
            .isEqualTo(RefreshTokenLifecycleService.RotationStatus.REUSED);
        assertThat(refreshTokenRepository.countActiveByTokenFamilyId(
            githubStored.getTokenFamilyId()
        )).isEqualTo(1);

        mockMvc.perform(get("/api/v1/protected")
                .header("Authorization", "Bearer " + github.accessToken()))
            .andExpect(status().isNotFound());
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
        assertGenericFailure(disabledResponse);

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
        assertGenericFailure(missingResponse);
        assertThat(refreshTokenRepository.count()).isEqualTo(tokenCountBefore);
    }

    @Test
    void providerFailureReturnsGenericJsonAndNeverIssuesTokens() throws Exception {
        long tokenCountBefore = refreshTokenRepository.count();
        MockHttpServletResponse response = new MockHttpServletResponse();
        failureHandler.onAuthenticationFailure(
            request("/login/oauth2/code/google"),
            response,
            new org.springframework.security.oauth2.core.OAuth2AuthenticationException(
                "provider-specific-secret-bearing-error"
            )
        );

        assertGenericFailure(response);
        assertThat(response.getContentAsString())
            .doesNotContain("provider-specific-secret-bearing-error");
        assertThat(refreshTokenRepository.count()).isEqualTo(tokenCountBefore);
    }

    private LoginResponse authenticate(
        User user,
        String registrationId,
        OAuth2User principal
    ) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(
            request("/login/oauth2/code/" + registrationId),
            response,
            authentication(registrationId, principal)
        );

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
        assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
        assertThat(jwtService.extractSubject(
            objectMapper.readTree(response.getContentAsString())
                .get("accessToken").asText()
        )).isEqualTo(user.getEmail());
        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.fieldNames()).toIterable()
            .containsExactlyInAnyOrder("accessToken", "refreshToken", "tokenType", "expiresIn");
        assertThat(json.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(json.get("expiresIn").asLong()).isEqualTo(900);
        return objectMapper.treeToValue(json, LoginResponse.class);
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

    private void assertGenericFailure(MockHttpServletResponse response) throws Exception {
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
