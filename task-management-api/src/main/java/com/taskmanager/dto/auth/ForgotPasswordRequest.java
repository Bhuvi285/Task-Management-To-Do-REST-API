package com.taskmanager.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ForgotPasswordRequest", description = "Email address for password recovery.")
public record ForgotPasswordRequest(
    @Schema(description = "Account email address.", requiredMode = Schema.RequiredMode.REQUIRED, example = "taylor@example.com")
    @NotBlank
    @Email
    @Size(max = 254)
    String email
) {
}
