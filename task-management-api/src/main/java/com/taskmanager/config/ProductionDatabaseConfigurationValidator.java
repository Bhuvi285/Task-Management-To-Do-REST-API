package com.taskmanager.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@Profile("prod")
public class ProductionDatabaseConfigurationValidator implements InitializingBean {

    private final DataSourceProperties dataSourceProperties;
    private final Environment environment;

    public ProductionDatabaseConfigurationValidator(
        DataSourceProperties dataSourceProperties,
        Environment environment
    ) {
        this.dataSourceProperties = dataSourceProperties;
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        for (String activeProfile : environment.getActiveProfiles()) {
            if ("dev".equals(activeProfile)) {
                throw new IllegalStateException(
                    "The prod profile cannot be combined with the dev profile."
                );
            }
        }

        requireConfigured(dataSourceProperties.getUrl(), "DATABASE_URL");
        requireConfigured(dataSourceProperties.getUsername(), "DATABASE_USERNAME");
        requireConfigured(dataSourceProperties.getPassword(), "DATABASE_PASSWORD");

        if (!dataSourceProperties.getUrl().startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException(
                    "The prod profile requires DATABASE_URL to use the PostgreSQL JDBC driver."
            );
        }

        if (!Boolean.parseBoolean(
            environment.getProperty("app.security.password-reset-email.enabled")
        )) {
            throw new IllegalStateException(
                "The prod profile requires PASSWORD_RESET_EMAIL_ENABLED=true."
            );
        }
        requireConfigured(environment.getProperty("spring.mail.host"), "SMTP_HOST");
        requireConfigured(environment.getProperty("spring.mail.username"), "SMTP_USERNAME");
        requireConfigured(environment.getProperty("spring.mail.password"), "SMTP_PASSWORD");
        String smtpPort = environment.getProperty("spring.mail.port");
        requireConfigured(smtpPort, "SMTP_PORT");
        validateSmtpPort(smtpPort);
        requireConfigured(
            environment.getProperty("app.security.password-reset-email.from"),
            "PASSWORD_RESET_EMAIL_FROM"
        );
        requireConfigured(
            environment.getProperty("app.security.password-reset-email.reset-url"),
            "PASSWORD_RESET_RESET_URL"
        );
        requireTrue(environment, "spring.mail.properties.mail.smtp.auth", "SMTP authentication");
        requireTrue(
            environment,
            "spring.mail.properties.mail.smtp.starttls.enable",
            "SMTP STARTTLS"
        );
        requireTrue(
            environment,
            "spring.mail.properties.mail.smtp.starttls.required",
            "SMTP STARTTLS"
        );

        URI resetUri;
        try {
            resetUri = URI.create(
                environment.getProperty("app.security.password-reset-email.reset-url").trim()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                "The prod profile requires PASSWORD_RESET_RESET_URL to be a valid HTTPS URL."
            );
        }
        if (!"https".equalsIgnoreCase(resetUri.getScheme())
            || resetUri.getHost() == null
            || resetUri.getUserInfo() != null
            || resetUri.getFragment() != null) {
            throw new IllegalStateException(
                "The prod profile requires PASSWORD_RESET_RESET_URL to be a valid HTTPS URL."
            );
        }
    }

    private void requireConfigured(String value, String environmentVariable) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "The prod profile requires a non-blank " + environmentVariable + "."
            );
        }
    }

    private void requireTrue(Environment environment, String property, String description) {
        if (!Boolean.parseBoolean(environment.getProperty(property))) {
            throw new IllegalStateException(
                "The prod profile requires " + description + " to be enabled."
            );
        }
    }

    private void validateSmtpPort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalStateException(
                    "The prod profile requires SMTP_PORT to be between 1 and 65535."
                );
            }
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                "The prod profile requires SMTP_PORT to be between 1 and 65535."
            );
        }
    }
}
