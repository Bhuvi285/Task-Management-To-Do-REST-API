package com.taskmanager.service;

import com.taskmanager.entity.OAuthAccount;
import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.repository.OAuthAccountRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SocialAccountServiceTest {

    @Test
    void provisionsWithUnpredictableBcryptPasswordAndStableProviderIdentity() {
        OAuthAccountRepository oauthRepository = mock(OAuthAccountRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        SocialAccountService service = new SocialAccountService(
            oauthRepository,
            userRepository,
            passwordEncoder,
            new SecureRandom()
        );
        when(oauthRepository.findByProviderAndProviderUserId(
            OAuthProvider.GOOGLE,
            "google-stable-id"
        )).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("person@example.com"))
            .thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(oauthRepository.saveAndFlush(any(OAuthAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GOOGLE,
            "google-stable-id",
            " Person@Example.com ",
            true,
            "Person Example"
        ));

        assertThat(user.getEmail()).isEqualTo("person@example.com");
        assertThat(user.getName()).isEqualTo("Person Example");
        assertThat(user.getPasswordHash()).startsWith("$2");
        verify(oauthRepository).saveAndFlush(any(OAuthAccount.class));
    }

    @Test
    void returnsExistingIdentityAndRejectsDisabledUser() {
        OAuthAccountRepository oauthRepository = mock(OAuthAccountRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        SocialAccountService service = new SocialAccountService(
            oauthRepository,
            userRepository,
            new BCryptPasswordEncoder()
        );
        User enabledUser = new User("Existing", "existing@example.com", "bcrypt-hash");
        OAuthAccount account = new OAuthAccount(
            enabledUser,
            OAuthProvider.GITHUB,
            "github-123"
        );
        when(oauthRepository.findByProviderAndProviderUserId(
            OAuthProvider.GITHUB,
            "github-123"
        )).thenReturn(Optional.of(account));

        assertThat(service.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GITHUB,
            "github-123",
            "ignored@example.com",
            true,
            "Ignored"
        ))).isSameAs(enabledUser);
        verify(userRepository, never()).findByEmailIgnoreCase(any());

        enabledUser.setEnabled(false);
        assertThatThrownBy(() -> service.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GITHUB,
            "github-123",
            "ignored@example.com",
            true,
            "Ignored"
        ))).isInstanceOf(OAuth2AuthenticationException.class)
            .hasMessage("Social authentication failed.");
    }

    @Test
    void rejectsEmailCollisionAndInvalidIdentityWithoutLinking() {
        OAuthAccountRepository oauthRepository = mock(OAuthAccountRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        SocialAccountService service = new SocialAccountService(
            oauthRepository,
            userRepository,
            new BCryptPasswordEncoder()
        );
        when(oauthRepository.findByProviderAndProviderUserId(
            OAuthProvider.GOOGLE,
            "different-google-id"
        )).thenReturn(Optional.empty());
        User localUser = new User("Local", "person@example.com", "existing-password-hash");
        when(userRepository.findByEmailIgnoreCase("person@example.com"))
            .thenReturn(Optional.of(localUser));

        assertThatThrownBy(() -> service.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GOOGLE,
            "different-google-id",
            "person@example.com",
            true,
            "Provider Name"
        ))).isInstanceOf(OAuth2AuthenticationException.class)
            .hasMessage("Social authentication failed.");
        assertThat(localUser.getPasswordHash()).isEqualTo("existing-password-hash");
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(oauthRepository, never()).saveAndFlush(any(OAuthAccount.class));

        assertThatThrownBy(() -> service.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GOOGLE,
            "invalid-google-id",
            "unverified@example.com",
            false,
            "Provider Name"
        ))).isInstanceOf(OAuth2AuthenticationException.class);
        assertThatThrownBy(() -> service.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GOOGLE,
            "malformed-email-id",
            "person@@example.com",
            true,
            "Provider Name"
        ))).isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    void databaseConflictFailsClosed() {
        OAuthAccountRepository oauthRepository = mock(OAuthAccountRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        SocialAccountService service = new SocialAccountService(
            oauthRepository,
            userRepository,
            new BCryptPasswordEncoder()
        );
        when(oauthRepository.findByProviderAndProviderUserId(
            OAuthProvider.GOOGLE,
            "racing-id"
        )).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("racing@example.com"))
            .thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(oauthRepository.saveAndFlush(any(OAuthAccount.class)))
            .thenThrow(new DataIntegrityViolationException("database conflict"));

        assertThatThrownBy(() -> service.resolveOrProvision(new SocialIdentity(
            OAuthProvider.GOOGLE,
            "racing-id",
            "racing@example.com",
            true,
            "Racing User"
        ))).isInstanceOf(OAuth2AuthenticationException.class)
            .hasMessage("Social authentication failed.");
    }
}
