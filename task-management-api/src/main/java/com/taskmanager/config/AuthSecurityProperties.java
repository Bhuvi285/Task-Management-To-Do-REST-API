package com.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record AuthSecurityProperties(
    List<String> allowedOrigins,
    String frontendBaseUrl,
    RefreshCookieProperties refreshCookie,
    int loginLimit,
    Duration loginWindow,
    int loginRateLimitMaxKeys,
    int passwordResetIpLimit,
    int passwordResetAccountLimit,
    Duration passwordResetWindow,
    int passwordResetRateLimitMaxKeys,
    Duration passwordResetCooldown,
    Duration passwordResetTokenExpiration
) {
    public record RefreshCookieProperties(
        String name,
        boolean secure,
        String sameSite,
        String path
    ) {
    }
}
