package com.taskmanager.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "LoginResponse", description = "Token pair returned by login and refresh operations. Refresh tokens are submitted only to refresh/logout; use the access token for Bearer authentication.")
public record LoginResponse(
    @Schema(description = "Short-lived JWT access token for the Authorization: Bearer header.", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,
    @Schema(description = "Opaque refresh token for token rotation or logout (omitted when delivered via HttpOnly cookie). Do not use as a Bearer access token.")
    String refreshToken,
    @Schema(description = "Token type.", example = "Bearer")
    String tokenType,
    @Schema(description = "Access-token lifetime in seconds.", example = "900")
    long expiresIn
) {
}
