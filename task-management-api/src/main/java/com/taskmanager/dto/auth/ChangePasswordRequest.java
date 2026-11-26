package com.taskmanager.dto.auth;

import com.taskmanager.validation.MaxUtf8ByteLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChangePasswordRequest", description = "Password change payload for the authenticated account.")
public record ChangePasswordRequest(
    @Schema(description = "Current password (maximum 72 UTF-8 bytes).", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY, format = "password")
    @NotBlank
    @Size(max = 72)
    @MaxUtf8ByteLength(72)
    String currentPassword,

    @Schema(description = "New password (8–72 characters and at most 72 UTF-8 bytes).", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.WRITE_ONLY, format = "password")
    @NotBlank
    @Size(min = 8, max = 72)
    @MaxUtf8ByteLength(72)
    String newPassword
) {
}
