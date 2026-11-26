package com.taskmanager.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.taskmanager.validation.MaxUtf8ByteLength;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "Email and password for local account login.")
public record LoginRequest(
    @Schema(description = "Email address.", requiredMode = Schema.RequiredMode.REQUIRED, example = "taylor@example.com")
    @NotBlank
    @Email
    @Size(max = 254)
    String email,

    @Schema(description = "Password (maximum 72 UTF-8 bytes).", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY, format = "password")
    @NotBlank
    @Size(max = 72)
    @MaxUtf8ByteLength(72)
    String password
) {
}
