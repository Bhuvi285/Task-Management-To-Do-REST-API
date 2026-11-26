package com.taskmanager.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final OAuth2SessionCleanup sessionCleanup;
    private final RefreshTokenCookieService refreshTokenCookieService;

    public OAuth2AuthenticationFailureHandler(
        OAuth2SessionCleanup sessionCleanup,
        RefreshTokenCookieService refreshTokenCookieService
    ) {
        this.sessionCleanup = sessionCleanup;
        this.refreshTokenCookieService = refreshTokenCookieService;
    }

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        sessionCleanup.clear(request, response);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.sendRedirect(refreshTokenCookieService.getFrontendLoginErrorUrl());
    }
}
