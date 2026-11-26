package com.taskmanager.dto.auth;

import com.taskmanager.validation.MaxUtf8ByteLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResetPasswordRequest", description = "Password-reset token and replacement password.")
public record ResetPasswordRequest(
    @Schema(description = "Single-use password-reset token.", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank
    @Size(max = 128)
    String resetToken,

    @Schema(description = "New password (8–72 characters and at most 72 UTF-8 bytes).", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY, format = "password")
    @NotBlank
    @Size(min = 8, max = 72)
    @MaxUtf8ByteLength(72)
    String newPassword
) {
}
