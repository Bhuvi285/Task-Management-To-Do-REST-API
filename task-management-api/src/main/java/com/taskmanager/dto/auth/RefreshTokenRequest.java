package com.taskmanager.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RefreshTokenRequest", description = "Refresh credential payload for rotation or logout.")
public record RefreshTokenRequest(
    @Schema(description = "Refresh token returned by login or refresh; not an access token.", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank
    @Size(max = 128)
    String refreshToken
) {
}
