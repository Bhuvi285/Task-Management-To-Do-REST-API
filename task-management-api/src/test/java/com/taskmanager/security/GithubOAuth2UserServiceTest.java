package com.taskmanager.security;

import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.service.SocialAccountService;
import com.taskmanager.service.SocialIdentity;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GithubOAuth2UserServiceTest {

    @Test
    void usesStableNumericIdAndVerifiedPrimaryEmailNotPublicProfileEmail() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
            mock(OAuth2UserService.class);
        GithubEmailClient emailClient = mock(GithubEmailClient.class);
        SocialAccountService accountService = mock(SocialAccountService.class);
        User localUser = new User("Grace Hopper", "grace@example.com", "hash");
        when(delegate.loadUser(any())).thenReturn(githubUser(Map.of(
            "id", 123456L,
            "login", "grace",
            "name", "Grace Hopper",
            "email", "unverified-public@example.com"
        )));
        when(emailClient.findEmails("github-access-token")).thenReturn(List.of(
            new GithubEmailClient.GithubEmail("secondary@example.com", false, true),
            new GithubEmailClient.GithubEmail("grace@example.com", true, true),
            new GithubEmailClient.GithubEmail("unverified@example.com", true, false)
        ));
        when(accountService.resolveOrProvision(any())).thenReturn(localUser);

        GithubOAuth2UserService service =
            new GithubOAuth2UserService(delegate, emailClient, accountService);
        OAuth2User result = service.loadUser(githubRequest());

        assertThat(result).isInstanceOf(LocalOAuth2User.class);
        assertThat(((LocalUserOAuth2Principal) result).getLocalUser()).isSameAs(localUser);
        assertThat(result.getName()).isEqualTo("grace");
        verify(accountService).resolveOrProvision(new SocialIdentity(
            OAuthProvider.GITHUB,
            "123456",
            "grace@example.com",
            true,
            "Grace Hopper"
        ));
    }

    @Test
    void rejectsMissingIdAndNoVerifiedEmail() {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
            mock(OAuth2UserService.class);
        GithubEmailClient emailClient = mock(GithubEmailClient.class);
        SocialAccountService accountService = mock(SocialAccountService.class);
        GithubOAuth2UserService service =
            new GithubOAuth2UserService(delegate, emailClient, accountService);

        when(delegate.loadUser(any())).thenReturn(githubUser(Map.of("login", "grace")));
        assertThatThrownBy(() -> service.loadUser(githubRequest()))
            .isInstanceOf(OAuth2AuthenticationException.class);

        when(delegate.loadUser(any())).thenReturn(githubUser(Map.of(
            "id", 123L,
            "login", "grace",
            "email", "public@example.com"
        )));
        when(emailClient.findEmails("github-access-token")).thenReturn(List.of(
            new GithubEmailClient.GithubEmail("unverified@example.com", true, false)
        ));
        assertThatThrownBy(() -> service.loadUser(githubRequest()))
            .isInstanceOf(OAuth2AuthenticationException.class)
            .hasMessage("Social authentication failed.");
        verify(accountService, never()).resolveOrProvision(any());
    }

    private OAuth2User githubUser(Map<String, Object> attributes) {
        return new DefaultOAuth2User(
            AuthorityUtils.NO_AUTHORITIES,
            attributes,
            "login"
        );
    }

    private OAuth2UserRequest githubRequest() {
        var registration = org.springframework.security.oauth2.client.registration.ClientRegistration
            .withRegistrationId("github")
            .clientId("test-client")
            .clientSecret("test-secret")
            .clientAuthenticationMethod(
                org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC
            )
            .authorizationGrantType(
                org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE
            )
            .redirectUri("{baseUrl}/login/oauth2/code/github")
            .scope("read:user", "user:email")
            .authorizationUri("https://github.example.test/authorize")
            .tokenUri("https://github.example.test/token")
            .userInfoUri("https://github.example.test/user")
            .userNameAttributeName("login")
            .clientName("GitHub")
            .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "github-access-token",
            Instant.now(),
            Instant.now().plusSeconds(300),
            Set.of("read:user", "user:email")
        );
        return new OAuth2UserRequest(registration, accessToken);
    }
}
