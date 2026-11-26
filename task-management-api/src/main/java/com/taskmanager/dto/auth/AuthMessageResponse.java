package com.taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthMessageResponse", description = "Non-sensitive authentication operation message.")
public record AuthMessageResponse(String message) {
}
