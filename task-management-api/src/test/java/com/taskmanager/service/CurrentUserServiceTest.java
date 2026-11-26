package com.taskmanager.service;

import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @Test
    void resolvesAuthenticatedUserUsingNormalizedEmail() {
        User user = new User("Alice", "alice@example.com", "encoded-password");
        Authentication authentication = authenticated("  ALICE@Example.COM  ");
        when(userRepository.findByEmailIgnoreCase("alice@example.com"))
            .thenReturn(Optional.of(user));

        assertThat(currentUserService.resolve(authentication)).isSameAs(user);
        verify(userRepository).findByEmailIgnoreCase("alice@example.com");
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThatThrownBy(() -> currentUserService.resolve(null))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void rejectsUnauthenticatedAuthentication() {
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(
            "alice@example.com",
            "password"
        );

        assertThatThrownBy(() -> currentUserService.resolve(authentication))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void rejectsAnonymousAuthentication() {
        Authentication authentication = new AnonymousAuthenticationToken(
            "test-key",
            "anonymousUser",
            List.of(() -> "ROLE_ANONYMOUS")
        );

        assertThatThrownBy(() -> currentUserService.resolve(authentication))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void rejectsMissingLocalUser() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.resolve(authenticated("missing@example.com")))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void rejectsDisabledLocalUser() {
        User user = new User("Alice", "alice@example.com", "encoded-password");
        user.setEnabled(false);
        when(userRepository.findByEmailIgnoreCase("alice@example.com"))
            .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> currentUserService.resolve(authenticated("alice@example.com")))
            .isInstanceOf(EntityNotFoundException.class);
    }

    private Authentication authenticated(String email) {
        return UsernamePasswordAuthenticationToken.authenticated(email, null, List.of());
    }
}
