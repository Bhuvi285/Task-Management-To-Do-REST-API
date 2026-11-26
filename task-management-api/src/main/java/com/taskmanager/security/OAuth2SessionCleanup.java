package com.taskmanager.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OAuth2SessionCleanup {

    private static final String AUTHORIZATION_REQUEST_ATTRIBUTE =
        "org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository"
            + ".AUTHORIZATION_REQUEST";

    private final String cookieName;
    private final String cookiePath;
    private final String cookieDomain;
    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;
    private final String cookieSameSite;

    public OAuth2SessionCleanup(
        @Value("${server.servlet.session.cookie.name:JSESSIONID}") String cookieName,
        @Value("${server.servlet.session.cookie.path:}") String cookiePath,
        @Value("${server.servlet.session.cookie.domain:}") String cookieDomain,
        @Value("${server.servlet.session.cookie.secure:false}") boolean cookieSecure,
        @Value("${server.servlet.session.cookie.http-only:true}") boolean cookieHttpOnly,
        @Value("${server.servlet.session.cookie.same-site:lax}") String cookieSameSite
    ) {
        this.cookieName = cookieName;
        this.cookiePath = cookiePath;
        this.cookieDomain = cookieDomain;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSameSite = cookieSameSite;
    }

    public void clear(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(AUTHORIZATION_REQUEST_ATTRIBUTE);
            session.invalidate();
        }

        String path = cookiePath.isBlank()
            ? (request.getContextPath().isBlank() ? "/" : request.getContextPath())
            : cookiePath;
        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(cookieName, "")
            .path(path)
            .httpOnly(cookieHttpOnly)
            .secure(cookieSecure)
            .sameSite(cookieSameSite)
            .maxAge(Duration.ZERO);
        if (!cookieDomain.isBlank()) {
            cookie.domain(cookieDomain);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.build().toString());
    }
}