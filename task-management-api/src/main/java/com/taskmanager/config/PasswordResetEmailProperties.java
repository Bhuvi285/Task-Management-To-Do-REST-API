package com.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.password-reset-email")
public record PasswordResetEmailProperties(
    boolean enabled,
    String from,
    String resetUrl
) {
}