package com.taskmanager.dto.error;

import java.time.Instant;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse", description = "Standard API error response; internal exception details are not exposed.")
public record ApiErrorResponse(
    @Schema(description = "UTC time the error response was created.", example = "2026-11-25T12:30:00Z")
    Instant timestamp,
    @Schema(description = "HTTP status code.", example = "400")
    int status,
    @Schema(description = "HTTP status reason phrase.", example = "Bad Request")
    String error,
    @Schema(description = "Safe client-facing error message.")
    String message,
    @Schema(description = "Request path.", example = "/api/v1/tasks")
    String path,
    @Schema(description = "Optional validation or request-field details.")
    Map<String, String> details
) {
}
