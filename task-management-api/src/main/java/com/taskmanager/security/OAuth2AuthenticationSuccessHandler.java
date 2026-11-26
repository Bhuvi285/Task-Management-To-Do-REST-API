package com.taskmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.auth.LoginResponse;
import com.taskmanager.entity.User;
import com.taskmanager.exception.InvalidCredentialsException;
import com.taskmanager.service.RefreshTokenLifecycleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenLifecycleService refreshTokenLifecycleService;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final OAuth2SessionCleanup sessionCleanup;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(
        RefreshTokenLifecycleService refreshTokenLifecycleService,
        OAuth2AuthenticationFailureHandler failureHandler,
        OAuth2SessionCleanup sessionCleanup,
        ObjectMapper objectMapper
    ) {
        this.refreshTokenLifecycleService = refreshTokenLifecycleService;
        this.failureHandler = failureHandler;
        this.sessionCleanup = sessionCleanup;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken)
            || !authentication.isAuthenticated()) {
            writeFailure(request, response);
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof LocalUserOAuth2Principal localPrincipal)) {
            writeFailure(request, response);
            return;
        }

        User localUser = localPrincipal.getLocalUser();
        if (localUser == null || !localUser.isEnabled()
            || localUser.getEmail() == null || localUser.getEmail().isBlank()) {
            writeFailure(request, response);
            return;
        }

        final LoginResponse loginResponse;
        try {
            loginResponse = refreshTokenLifecycleService.issueForUser(localUser.getEmail());
        } catch (InvalidCredentialsException exception) {
            writeFailure(request, response);
            return;
        }

        sessionCleanup.clear(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        objectMapper.writeValue(response.getOutputStream(), loginResponse);
    }

    private void writeFailure(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException, ServletException {
        failureHandler.onAuthenticationFailure(
            request,
            response,
            new OAuth2AuthenticationException("Social authentication failed.")
        );
    }
}
