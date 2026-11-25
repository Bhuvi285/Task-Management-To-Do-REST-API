package com.taskmanager.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task workflow status.")
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
