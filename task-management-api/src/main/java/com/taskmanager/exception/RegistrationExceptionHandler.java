package com.taskmanager.exception;

import com.taskmanager.controller.AuthController;
import com.taskmanager.dto.error.ApiErrorResponse;
import com.taskmanager.exception.InvalidPasswordResetTokenException;
import com.taskmanager.exception.PasswordChangeException;
import com.taskmanager.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class RegistrationExceptionHandler {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(RegistrationExceptionHandler.class);

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
        DuplicateEmailException exception,
        HttpServletRequest request
    ) {
        return errorResponse(
            HttpStatus.ACCEPTED,
            "If registration can be completed, the account is ready to use.",
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
        InvalidCredentialsException exception,
        HttpServletRequest request
    ) {
        return errorResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password.",
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(
        InvalidRefreshTokenException exception,
        HttpServletRequest request
    ) {
        return errorResponse(
            HttpStatus.UNAUTHORIZED,
            "Refresh token is invalid or expired.",
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(PasswordChangeException.class)
    public ResponseEntity<ApiErrorResponse> handlePasswordChangeFailure(
        PasswordChangeException exception,
        HttpServletRequest request
    ) {
        return errorResponse(
            HttpStatus.BAD_REQUEST,
            "Current password is incorrect or the new password is not acceptable.",
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPasswordResetToken(
        InvalidPasswordResetTokenException exception,
        HttpServletRequest request
    ) {
        return errorResponse(
            HttpStatus.BAD_REQUEST,
            "Password reset request is invalid or expired.",
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(
        RateLimitExceededException exception,
        HttpServletRequest request
    ) {
        LOGGER.warn("Authentication rate limit exceeded.");
        return errorResponse(
            HttpStatus.TOO_MANY_REQUESTS,
            exception.getSafeMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
            details.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return errorResponse(
            HttpStatus.BAD_REQUEST,
            "Request validation failed.",
            request.getRequestURI(),
            details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        return errorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid request body.",
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedFailure(
        Exception exception,
        HttpServletRequest request
    ) {
        LOGGER.error("Unexpected error while processing registration request.", exception);
        return errorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred.",
            request.getRequestURI(),
            Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(
        HttpStatus status,
        String message,
        String path,
        Map<String, String> details
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            details
        );
        return ResponseEntity.status(status)
            .cacheControl(CacheControl.noStore())
            .body(response);
    }
}
