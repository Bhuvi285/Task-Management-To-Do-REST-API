package com.taskmanager.service;

import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.TaskManagerUserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User resolve(Authentication authentication) {
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException(
                "An authenticated user is required."
            );
        }

        if (authentication.getPrincipal() instanceof TaskManagerUserPrincipal principal) {
            if (!principal.isEnabled()) {
                throw new EntityNotFoundException("User not found.");
            }
            return userRepository.getReferenceById(principal.getUserId());
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException(
                "An authenticated user is required."
            );
        }

        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
            .orElseThrow(() -> new EntityNotFoundException("User not found."));
        if (!user.isEnabled()) {
            throw new EntityNotFoundException("User not found.");
        }
        return user;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
