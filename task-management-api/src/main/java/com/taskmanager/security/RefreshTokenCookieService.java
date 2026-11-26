package com.taskmanager.security;

import com.taskmanager.config.AuthSecurityProperties;
import com.taskmanager.config.JwtProperties;
import com.taskmanager.exception.CsrfOriginValidationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class RefreshTokenCookieService {

    private final AuthSecurityProperties securityProperties;
    private final JwtProperties jwtProperties;

    public RefreshTokenCookieService(
        AuthSecurityProperties securityProperties,
        JwtProperties jwtProperties
    ) {
        this.securityProperties = securityProperties;
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie createRefreshCookie(String rawRefreshToken) {
        AuthSecurityProperties.RefreshCookieProperties cfg = cookieConfig();
        return ResponseCookie.from(cfg.name(), rawRefreshToken)
            .httpOnly(true)
            .secure(cfg.secure())
            .sameSite(cfg.sameSite())
            .path(cfg.path())
            .maxAge(jwtProperties.refreshTokenExpiration())
            .build();
    }

    public ResponseCookie clearRefreshCookie() {
        AuthSecurityProperties.RefreshCookieProperties cfg = cookieConfig();
        return ResponseCookie.from(cfg.name(), "")
            .httpOnly(true)
            .secure(cfg.secure())
            .sameSite(cfg.sameSite())
            .path(cfg.path())
            .maxAge(Duration.ZERO)
            .build();
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        String cookieName = cookieConfig().name();
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())
                && cookie.getValue() != null
                && !cookie.getValue().isBlank()) {
                return Optional.of(cookie.getValue().trim());
            }
        }
        return Optional.empty();
    }

    public void validateCookieRequestOrigin(HttpServletRequest request) {
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if (fetchSite != null && "cross-site".equalsIgnoreCase(fetchSite.trim())) {
            throw new CsrfOriginValidationException("Cross-site cookie request rejected.");
        }

        String origin = request.getHeader("Origin");
        String candidateOrigin = normalizeOrigin(origin);
        if (candidateOrigin == null) {
            candidateOrigin = normalizeOrigin(request.getHeader("Referer"));
        }

        if (candidateOrigin == null || !isAllowedOrigin(candidateOrigin)) {
            throw new CsrfOriginValidationException(
                "Invalid or missing origin for cookie-authenticated request."
            );
        }
    }

    public String getFrontendDashboardUrl() {
        return resolveFrontendBaseUrl() + "/dashboard";
    }

    public String getFrontendLoginErrorUrl() {
        return resolveFrontendBaseUrl() + "/auth/login?error=oauth_failed";
    }

    private String resolveFrontendBaseUrl() {
        String configured = securityProperties.frontendBaseUrl();
        if (configured != null && !configured.isBlank()) {
            return stripTrailingSlash(configured.trim());
        }
        List<String> origins = securityProperties.allowedOrigins();
        if (origins != null && !origins.isEmpty()) {
            return stripTrailingSlash(origins.get(0).trim());
        }
        return "http://localhost:3000";
    }

    private boolean isAllowedOrigin(String candidateOrigin) {
        List<String> allowed = securityProperties.allowedOrigins();
        if (allowed == null || allowed.isEmpty()) {
            return false;
        }
        for (String origin : allowed) {
            String normalizedAllowed = normalizeOrigin(origin);
            if (normalizedAllowed != null && normalizedAllowed.equalsIgnoreCase(candidateOrigin)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeOrigin(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(value.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            return port == -1 ? scheme + "://" + host : scheme + "://" + host + ":" + port;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private AuthSecurityProperties.RefreshCookieProperties cookieConfig() {
        AuthSecurityProperties.RefreshCookieProperties cfg = securityProperties.refreshCookie();
        if (cfg == null) {
            return new AuthSecurityProperties.RefreshCookieProperties(
                "taskflow_refresh_token",
                false,
                "Lax",
                "/api/v1/auth"
            );
        }
        return cfg;
    }
}
