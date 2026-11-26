package com.taskmanager.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OAuth2ClientConfiguration {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
        OAuth2ClientProperties properties
    ) {
        Map<String, ClientRegistration> registrations = new HashMap<>();
        if (properties.google().enabled()) {
            registrations.put("google", googleRegistration(properties.google()));
        }
        if (properties.github().enabled()) {
            registrations.put("github", githubRegistration(properties.github()));
        }

        return registrations::get;
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest>
        authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    private ClientRegistration googleRegistration(
        OAuth2ClientProperties.Provider provider
    ) {
        validateProvider(provider, "google");
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
            .clientId(provider.clientId().trim())
            .clientSecret(provider.clientSecret().trim())
            .redirectUri(provider.redirectUri().trim())
            .scope("openid", "profile", "email")
            .build();
    }

    private ClientRegistration githubRegistration(
        OAuth2ClientProperties.Provider provider
    ) {
        validateProvider(provider, "github");
        return CommonOAuth2Provider.GITHUB.getBuilder("github")
            .clientId(provider.clientId().trim())
            .clientSecret(provider.clientSecret().trim())
            .redirectUri(provider.redirectUri().trim())
            .scope("read:user", "user:email")
            .build();
    }

    private void validateProvider(
        OAuth2ClientProperties.Provider provider,
        String registrationId
    ) {
        if (provider.clientId() == null || provider.clientId().isBlank()
            || provider.clientSecret() == null || provider.clientSecret().isBlank()) {
            throw new IllegalStateException(
                "OAuth2 provider '" + registrationId
                    + "' is enabled but its client ID or client secret is missing."
            );
        }
        String redirectUri = provider.redirectUri();
        if (redirectUri == null || redirectUri.isBlank()
            || redirectUri.contains("*")
            || !redirectUri.endsWith("/login/oauth2/code/" + registrationId)
            || (!redirectUri.startsWith("{baseUrl}/")
                && !redirectUri.matches("^https?://[^/]+/login/oauth2/code/"
                    + registrationId + "$"))) {
            throw new IllegalStateException(
                "OAuth2 provider '" + registrationId
                    + "' must use an exact, non-wildcard callback URI."
            );
        }
    }
}
