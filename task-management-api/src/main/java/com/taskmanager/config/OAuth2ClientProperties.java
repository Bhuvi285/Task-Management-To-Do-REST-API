package com.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.oauth2")
public record OAuth2ClientProperties(
    Provider google,
    Provider github
) {

    public OAuth2ClientProperties {
        if (google == null) {
            google = new Provider(
                false,
                null,
                null,
                "{baseUrl}/login/oauth2/code/google"
            );
        }
        if (github == null) {
            github = new Provider(
                false,
                null,
                null,
                "{baseUrl}/login/oauth2/code/github"
            );
        }
    }

    public record Provider(
        boolean enabled,
        String clientId,
        String clientSecret,
        String redirectUri
    ) {
    }
}
