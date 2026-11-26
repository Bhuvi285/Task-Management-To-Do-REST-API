package com.taskmanager.service;

import com.taskmanager.dto.auth.RegistrationRequest;
import com.taskmanager.dto.auth.RegistrationResponse;
import com.taskmanager.entity.User;
import com.taskmanager.exception.DuplicateEmailException;
import com.taskmanager.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenLifecycleService refreshTokenLifecycleService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldEncodePasswordAndReturnSafeRegistrationResponse() {
        RegistrationRequest request =
            new RegistrationRequest("Alice", "alice@example.com", "StrongPass123!");
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-hash");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation ->
            invocation.getArgument(0)
        );

        RegistrationResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        InOrder registrationOrder = inOrder(passwordEncoder, userRepository);
        registrationOrder.verify(passwordEncoder).encode(request.password());
        registrationOrder.verify(userRepository).existsByEmailIgnoreCase(request.email());
        verify(passwordEncoder).encode(request.password());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-hash");
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.toString()).doesNotContain("encoded-hash", request.password());
    }

    @Test
    void shouldEncodeBeforeDuplicateLookupAndReturnDuplicateFailure() {
        RegistrationRequest request =
            new RegistrationRequest("Alice", "alice@example.com", "StrongPass123!");
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(DuplicateEmailException.class);

        InOrder registrationOrder = inOrder(passwordEncoder, userRepository);
        registrationOrder.verify(passwordEncoder).encode(request.password());
        registrationOrder.verify(userRepository).existsByEmailIgnoreCase(request.email());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void shouldTranslateDatabaseEmailConstraintRaceToDuplicateEmailException() {
        RegistrationRequest request =
            new RegistrationRequest("Alice", "alice@example.com", "StrongPass123!");
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-hash");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(
            new DataIntegrityViolationException(
                "Email uniqueness violation",
                new ConstraintViolationException(
                    "Email uniqueness violation",
                    null,
                    "UK_USERS_EMAIL_INDEX_4"
                )
            )
        );

        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(DuplicateEmailException.class);
    }
}
