package com.taskmanager.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2ClientConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(OAuth2ClientConfiguration.class);

    @Test
    void disabledProvidersNeedNoCredentialsAndKeepOAuthStateRepositoryAvailable() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            ClientRegistrationRepository registrations =
                context.getBean(ClientRegistrationRepository.class);
            assertThat(registrations.findByRegistrationId("google")).isNull();
            assertThat(registrations.findByRegistrationId("github")).isNull();
            assertThat(context.getBean(AuthorizationRequestRepository.class))
                .isInstanceOf(HttpSessionOAuth2AuthorizationRequestRepository.class);
        });
    }

    @Test
    void enabledGoogleRegistrationUsesOidcScopesAndExactCallback() {
        contextRunner
            .withPropertyValues(
                "app.security.oauth2.google.enabled=true",
                "app.security.oauth2.google.client-id=google-test-client",
                "app.security.oauth2.google.client-secret=google-test-secret",
                "app.security.oauth2.google.redirect-uri="
                    + "http://localhost:8080/login/oauth2/code/google"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                ClientRegistration registration = context
                    .getBean(ClientRegistrationRepository.class)
                    .findByRegistrationId("google");
                assertThat(registration).isNotNull();
                assertThat(registration.getProviderDetails().getIssuerUri())
                    .isEqualTo("https://accounts.google.com");
                assertThat(registration.getScopes())
                    .containsExactlyInAnyOrder("openid", "profile", "email");
                assertThat(registration.getRedirectUri())
                    .isEqualTo("http://localhost:8080/login/oauth2/code/google");
            });
    }

    @Test
    void enabledGithubRegistrationUsesGithubScopesAndCallback() {
        contextRunner
            .withPropertyValues(
                "app.security.oauth2.github.enabled=true",
                "app.security.oauth2.github.client-id=github-test-client",
                "app.security.oauth2.github.client-secret=github-test-secret",
                "app.security.oauth2.github.redirect-uri="
                    + "https://api.example.test/login/oauth2/code/github"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                ClientRegistration registration = context
                    .getBean(ClientRegistrationRepository.class)
                    .findByRegistrationId("github");
                assertThat(registration).isNotNull();
                assertThat(registration.getProviderDetails().getAuthorizationUri())
                    .isEqualTo("https://github.com/login/oauth/authorize");
                assertThat(registration.getProviderDetails().getTokenUri())
                    .isEqualTo("https://github.com/login/oauth/access_token");
                assertThat(registration.getScopes())
                    .containsExactlyInAnyOrder("read:user", "user:email");
                assertThat(registration.getRedirectUri())
                    .isEqualTo("https://api.example.test/login/oauth2/code/github");
            });
    }

    @Test
    void enabledProviderWithoutCredentialsFailsClearly() {
        contextRunner
            .withPropertyValues("app.security.oauth2.google.enabled=true")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining(
                        "OAuth2 provider 'google' is enabled but its client ID or client secret is missing."
                    );
            });
    }

    @Test
    void enabledGithubWithoutCredentialsFailsClearly() {
        contextRunner
            .withPropertyValues("app.security.oauth2.github.enabled=true")
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining(
                        "OAuth2 provider 'github' is enabled but its client ID or client secret is missing."
                    );
            });
    }
}
