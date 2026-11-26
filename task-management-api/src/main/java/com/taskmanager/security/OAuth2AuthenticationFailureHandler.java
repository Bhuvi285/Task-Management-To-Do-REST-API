package com.taskmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.error.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String FAILURE_MESSAGE = "Social authentication failed.";

    private final ObjectMapper objectMapper;
    private final OAuth2SessionCleanup sessionCleanup;

    public OAuth2AuthenticationFailureHandler(
        ObjectMapper objectMapper,
        OAuth2SessionCleanup sessionCleanup
    ) {
        this.objectMapper = objectMapper;
        this.sessionCleanup = sessionCleanup;
    }

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        sessionCleanup.clear(request, response);
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        objectMapper.writeValue(
            response.getOutputStream(),
            new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                FAILURE_MESSAGE,
                request.getRequestURI(),
                Map.of()
            )
        );
    }
}
