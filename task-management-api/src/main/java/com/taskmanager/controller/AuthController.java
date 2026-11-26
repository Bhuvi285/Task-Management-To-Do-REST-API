package com.taskmanager.controller;

import com.taskmanager.dto.auth.AuthMessageResponse;
import com.taskmanager.dto.auth.ChangePasswordRequest;
import com.taskmanager.dto.auth.ForgotPasswordRequest;
import com.taskmanager.dto.auth.LoginRequest;
import com.taskmanager.dto.auth.LoginResponse;
import com.taskmanager.dto.auth.RefreshTokenRequest;
import com.taskmanager.dto.auth.RegistrationRequest;
import com.taskmanager.dto.auth.ResetPasswordRequest;
import com.taskmanager.exception.DuplicateEmailException;
import com.taskmanager.exception.InvalidRefreshTokenException;
import com.taskmanager.security.RefreshTokenCookieService;
import com.taskmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Registration, credentials, token lifecycle and password recovery.")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final Validator validator;

    public AuthController(
        AuthService authService,
        RefreshTokenCookieService refreshTokenCookieService,
        Validator validator
    ) {
        this.authService = authService;
        this.refreshTokenCookieService = refreshTokenCookieService;
        this.validator = validator;
    }

    @PostMapping("/register")
    @Operation(summary = "Register an account", description = "Public endpoint. Returns a non-enumerating confirmation response.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Registration request accepted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthMessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Registration rate limit exceeded", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthMessageResponse> register(
        @Valid @RequestBody RegistrationRequest request
    ) {
        try {
            authService.register(request);
        } catch (DuplicateEmailException exception) {
            return noStoreResponse(
                HttpStatus.ACCEPTED,
                new AuthMessageResponse(REGISTRATION_MESSAGE)
            );
        }
        return noStoreResponse(
            HttpStatus.ACCEPTED,
            new AuthMessageResponse(REGISTRATION_MESSAGE)
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Public endpoint. Issues an access token and an HttpOnly refresh cookie (plus refresh token for API clients).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credentials accepted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Login rate limit exceeded", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest servletRequest
    ) {
        LoginResponse loginResponse = authService.login(request, servletRequest.getRemoteAddr());
        ResponseCookie refreshCookie = refreshTokenCookieService.createRefreshCookie(
            loginResponse.refreshToken()
        );
        return ResponseEntity.status(HttpStatus.OK)
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(loginResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate a refresh token", description = "Public endpoint. Accepts either the HttpOnly refresh cookie (browser flow) or a JSON refreshToken payload (API clients).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token pair issued and refresh token rotated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Refresh token invalid, expired or reused", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Cookie origin validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> refresh(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest servletRequest
    ) {
        if (request != null) {
            validateRefreshTokenRequest(request);
            LoginResponse rotated = authService.refresh(request);
            return noStoreResponse(HttpStatus.OK, rotated);
        }

        refreshTokenCookieService.validateCookieRequestOrigin(servletRequest);
        String cookieToken = refreshTokenCookieService.extractRefreshToken(servletRequest)
            .orElseThrow(InvalidRefreshTokenException::new);

        try {
            LoginResponse rotated = authService.refresh(new RefreshTokenRequest(cookieToken));
            ResponseCookie newCookie = refreshTokenCookieService.createRefreshCookie(
                rotated.refreshToken()
            );
            LoginResponse browserResponse = new LoginResponse(
                rotated.accessToken(),
                null,
                rotated.tokenType(),
                rotated.expiresIn()
            );
            return ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(browserResponse);
        } catch (InvalidRefreshTokenException exception) {
            throw exception;
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out", description = "Public endpoint. Revokes the refresh-token session family from either the HttpOnly refresh cookie or JSON payload.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Session revoked"),
        @ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Cookie origin validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> logout(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest servletRequest
    ) {
        if (request != null) {
            validateRefreshTokenRequest(request);
            authService.logout(request);
            return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
        }

        refreshTokenCookieService.validateCookieRequestOrigin(servletRequest);
        refreshTokenCookieService.extractRefreshToken(servletRequest)
            .ifPresent(token -> authService.logout(new RefreshTokenRequest(token)));

        ResponseCookie clearedCookie = refreshTokenCookieService.clearRefreshCookie();
        return ResponseEntity.noContent()
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.SET_COOKIE, clearedCookie.toString())
            .build();
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Change the current user's password",
        description = "Requires the current user's JWT access token.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthMessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Password validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required or password rejected", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Authenticated request is not permitted", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthMessageResponse> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        Authentication authentication
    ) {
        authService.changePassword(authentication.getName(), request);
        ResponseCookie clearedCookie = refreshTokenCookieService.clearRefreshCookie();
        return ResponseEntity.status(HttpStatus.OK)
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.SET_COOKIE, clearedCookie.toString())
            .body(new AuthMessageResponse("Password changed successfully."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Public endpoint. Always returns a generic response to avoid revealing whether an account exists.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Request accepted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthMessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Email validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Password-reset rate limit exceeded", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthMessageResponse> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request,
        HttpServletRequest servletRequest
    ) {
        authService.requestPasswordReset(request.email(), servletRequest.getRemoteAddr());
        return noStoreResponse(
            HttpStatus.ACCEPTED,
            new AuthMessageResponse(
                "If the account is eligible, password-reset instructions will be sent."
            )
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset a password", description = "Public endpoint. Exchanges a valid password-reset token for a new password.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthMessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Request invalid or reset token expired", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthMessageResponse> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request.resetToken(), request.newPassword());
        return noStoreResponse(
            HttpStatus.OK,
            new AuthMessageResponse("Password reset successfully.")
        );
    }

    private void validateRefreshTokenRequest(RefreshTokenRequest request) {
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private static final String REGISTRATION_MESSAGE =
        "If registration can be completed, the account is ready to use.";

    private <T> ResponseEntity<T> noStoreResponse(HttpStatus status, T body) {
        return ResponseEntity.status(status)
            .cacheControl(CacheControl.noStore())
            .body(body);
    }
}
