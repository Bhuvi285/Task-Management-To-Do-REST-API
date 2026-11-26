package com.taskmanager.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityErrorResponseTest {

    private static final String INTERNAL_DETAIL =
        "org.example.DatabaseException: password=must-not-be-returned";

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void taskAndAuthentication500ResponsesDoNotExposeExceptionDetails() throws Exception {
        MockHttpServletRequest taskRequest = request("/api/v1/tasks");
        var taskResponse = new TaskExceptionHandler().handleUnexpectedFailure(
            new IllegalStateException(INTERNAL_DETAIL),
            taskRequest
        );

        assertThat(taskResponse.getStatusCode().value())
            .isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertSafeErrorBody(objectMapper.writeValueAsString(taskResponse.getBody()));

        MockHttpServletRequest authRequest = request("/api/v1/auth/login");
        var authResponse = new RegistrationExceptionHandler().handleUnexpectedFailure(
            new IllegalStateException(INTERNAL_DETAIL),
            authRequest
        );

        assertThat(authResponse.getStatusCode().value())
            .isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertSafeErrorBody(objectMapper.writeValueAsString(authResponse.getBody()));
    }

    @Test
    void authenticationAndAuthorizationHandlersReturnSafe401And403Responses()
        throws Exception {
        MockHttpServletResponse authenticationResponse = new MockHttpServletResponse();
        new RestAuthenticationEntryPoint(objectMapper).commence(
            request("/api/v1/tasks"),
            authenticationResponse,
            new BadCredentialsException(INTERNAL_DETAIL)
        );

        assertThat(authenticationResponse.getStatus())
            .isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertSafeErrorBody(authenticationResponse.getContentAsString());

        MockHttpServletResponse accessDeniedResponse = new MockHttpServletResponse();
        new RestAccessDeniedHandler(objectMapper).handle(
            request("/api/v1/tasks"),
            accessDeniedResponse,
            new AccessDeniedException(INTERNAL_DETAIL)
        );

        assertThat(accessDeniedResponse.getStatus())
            .isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertSafeErrorBody(accessDeniedResponse.getContentAsString());
    }

    private MockHttpServletRequest request(String path) {
        return new MockHttpServletRequest("GET", path);
    }

    private void assertSafeErrorBody(String body) {
        assertThat(body)
            .doesNotContain(INTERNAL_DETAIL, "DatabaseException", "password=", "stackTrace");
    }
}