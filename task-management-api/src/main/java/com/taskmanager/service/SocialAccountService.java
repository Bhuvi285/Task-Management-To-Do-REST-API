package com.taskmanager.service;

import com.taskmanager.entity.OAuthAccount;
import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.repository.OAuthAccountRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Service
public class SocialAccountService {

    private static final String GENERIC_ERROR_CODE = "social_authentication_failed";
    private static final String GENERIC_ERROR_MESSAGE = "Social authentication failed.";

    private final OAuthAccountRepository oauthAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    @Autowired
    public SocialAccountService(
        OAuthAccountRepository oauthAccountRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this(
            oauthAccountRepository,
            userRepository,
            passwordEncoder,
            new SecureRandom()
        );
    }

    SocialAccountService(
        OAuthAccountRepository oauthAccountRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        SecureRandom secureRandom
    ) {
        this.oauthAccountRepository = oauthAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public User resolveOrProvision(SocialIdentity identity) {
        validateIdentity(identity);
        OAuthAccount existingAccount = oauthAccountRepository
            .findByProviderAndProviderUserId(identity.provider(), identity.providerUserId())
            .orElse(null);
        if (existingAccount != null) {
            return requireEnabled(existingAccount.getUser());
        }

        String normalizedEmail = identity.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw authenticationFailure();
        }

        byte[] passwordBytes = new byte[32];
        secureRandom.nextBytes(passwordBytes);
        String unusablePassword = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(passwordBytes);
        String passwordHash = passwordEncoder.encode(unusablePassword);
        String name = identity.name() == null || identity.name().isBlank()
            ? normalizedEmail.substring(0, normalizedEmail.indexOf('@'))
            : identity.name().trim();

        try {
            User user = userRepository.saveAndFlush(
                new User(name, normalizedEmail, passwordHash)
            );
            oauthAccountRepository.saveAndFlush(new OAuthAccount(
                user,
                identity.provider(),
                identity.providerUserId()
            ));
            return user;
        } catch (DataIntegrityViolationException exception) {
            throw authenticationFailure();
        }
    }

    private void validateIdentity(SocialIdentity identity) {
        if (identity == null
            || identity.provider() == null
            || identity.providerUserId() == null
            || identity.providerUserId().isBlank()
            || !isUsableEmail(identity.email())
            || !identity.emailVerified()
        ) {
            throw authenticationFailure();
        }
        if (identity.provider() != OAuthProvider.GOOGLE
            && identity.provider() != OAuthProvider.GITHUB) {
            throw authenticationFailure();
        }
    }

    private boolean isUsableEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String normalizedEmail = email.trim();
        int atIndex = normalizedEmail.indexOf('@');
        return atIndex > 0
            && atIndex == normalizedEmail.lastIndexOf('@')
            && atIndex < normalizedEmail.length() - 1
            && normalizedEmail.chars().noneMatch(Character::isWhitespace);
    }

    private User requireEnabled(User user) {
        if (user == null || !user.isEnabled()) {
            throw authenticationFailure();
        }
        return user;
    }

    public static OAuth2AuthenticationException authenticationFailure() {
        return new OAuth2AuthenticationException(
            new OAuth2Error(GENERIC_ERROR_CODE),
            GENERIC_ERROR_MESSAGE
        );
    }
}
