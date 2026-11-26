package com.taskmanager.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task priority level.")
public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
