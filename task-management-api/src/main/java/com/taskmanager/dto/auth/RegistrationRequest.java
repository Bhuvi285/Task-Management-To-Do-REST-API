package com.taskmanager.dto.auth;

import com.taskmanager.validation.MaxUtf8ByteLength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegistrationRequest", description = "Credentials and display name for local account registration.")
public record RegistrationRequest(
    @Schema(description = "Display name (1–100 characters).", requiredMode = Schema.RequiredMode.REQUIRED, example = "Taylor")
    @NotBlank
    @Size(max = 100)
    String name,

    @Schema(description = "Email address (maximum 254 characters).", requiredMode = Schema.RequiredMode.REQUIRED, example = "taylor@example.com")
    @NotBlank
    @Email
    @Size(max = 254)
    String email,

    @Schema(description = "Password (8–72 characters and at most 72 UTF-8 bytes). Never returned by the API.", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY, format = "password")
    @NotBlank
    @Size(min = 8, max = 72)
    @MaxUtf8ByteLength(72)
    String password
) {
}
