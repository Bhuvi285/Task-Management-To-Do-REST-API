package com.taskmanager.security;

import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.service.SocialAccountService;
import com.taskmanager.service.SocialIdentity;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleOidcUserServiceTest {

    @Test
    void mapsGoogleSubjectAndVerifiedEmailAndPreservesOidcPrincipal() {
        SocialAccountService accountService = mock(SocialAccountService.class);
        OAuth2UserService<OidcUserRequest, OidcUser> delegate = mock(OAuth2UserService.class);
        User localUser = new User("Ada Lovelace", "ada@example.com", "hash");
        OidcUser providerUser = googleUser(Map.of(
            "sub", "google-sub-123",
            "email", "ada@example.com",
            "email_verified", true,
            "name", "Ada Lovelace"
        ));
        when(delegate.loadUser(any())).thenReturn(providerUser);
        when(accountService.resolveOrProvision(any())).thenReturn(localUser);

        GoogleOidcUserService service = new GoogleOidcUserService(delegate, accountService);
        OidcUser result = service.loadUser(googleRequest());

        assertThat(result).isInstanceOf(LocalOidcUser.class);
        assertThat(((LocalUserOAuth2Principal) result).getLocalUser()).isSameAs(localUser);
        assertThat(result.getSubject()).isEqualTo("google-sub-123");
        verify(accountService).resolveOrProvision(new SocialIdentity(
            OAuthProvider.GOOGLE,
            "google-sub-123",
            "ada@example.com",
            true,
            "Ada Lovelace"
        ));
    }

    @Test
    void rejectsUnverifiedOrMissingGoogleEmailAndSubjectBeforeProvisioning() {
        SocialAccountService accountService = mock(SocialAccountService.class);
        OAuth2UserService<OidcUserRequest, OidcUser> delegate = mock(OAuth2UserService.class);
        GoogleOidcUserService service = new GoogleOidcUserService(delegate, accountService);
        OidcUser unverifiedUser = googleUser(Map.of(
            "sub", "google-sub-123",
            "email", "ada@example.com",
            "email_verified", false
        ));
        when(delegate.loadUser(any())).thenReturn(unverifiedUser);

        assertThatThrownBy(() -> service.loadUser(googleRequest()))
            .isInstanceOf(OAuth2AuthenticationException.class)
            .hasMessage("Social authentication failed.");

        OidcUser missingSubjectUser = googleUser(Map.of(
            "email", "ada@example.com",
            "email_verified", true
        ));
        when(delegate.loadUser(any())).thenReturn(missingSubjectUser);
        assertThatThrownBy(() -> service.loadUser(googleRequest()))
            .isInstanceOf(OAuth2AuthenticationException.class);
        OidcUser missingEmailUser = googleUser(Map.of(
            "sub", "google-sub-123",
            "email_verified", true
        ));
        when(delegate.loadUser(any())).thenReturn(missingEmailUser);
        assertThatThrownBy(() -> service.loadUser(googleRequest()))
            .isInstanceOf(OAuth2AuthenticationException.class);
        verify(accountService, never()).resolveOrProvision(any());
    }

    private OidcUser googleUser(Map<String, Object> claims) {
        OidcUser user = mock(OidcUser.class);
        when(user.getClaims()).thenReturn(claims);
        when(user.getAttributes()).thenReturn(claims);
        when(user.getSubject()).thenReturn((String) claims.get("sub"));
        doReturn(AuthorityUtils.NO_AUTHORITIES).when(user).getAuthorities();
        return user;
    }

    private OidcUserRequest googleRequest() {
        var registration = org.springframework.security.oauth2.client.registration.ClientRegistration
            .withRegistrationId("google")
            .clientId("test-client")
            .clientSecret("test-secret")
            .clientAuthenticationMethod(
                org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC
            )
            .authorizationGrantType(
                org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE
            )
            .redirectUri("{baseUrl}/login/oauth2/code/google")
            .scope("openid", "email")
            .authorizationUri("https://accounts.example.test/authorize")
            .tokenUri("https://accounts.example.test/token")
            .userInfoUri("https://accounts.example.test/userinfo")
            .userNameAttributeName("sub")
            .clientName("Google")
            .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test-access-token",
            Instant.now(),
            Instant.now().plusSeconds(300),
            Set.of("openid", "email")
        );
        OidcIdToken idToken = new OidcIdToken(
            "test-id-token",
            Instant.now(),
            Instant.now().plusSeconds(300),
            Map.of("sub", "token-subject")
        );
        return new OidcUserRequest(registration, accessToken, idToken);
    }
}
