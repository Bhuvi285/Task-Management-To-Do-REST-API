package com.taskmanager.dto.task;

import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "UpdateTaskRequest", description = "Replacement values for an existing task owned by the authenticated user.")
public record UpdateTaskRequest(
    @Schema(description = "Task title.", requiredMode = Schema.RequiredMode.REQUIRED, example = "Prepare release notes")
    @NotBlank(message = "Title must not be blank.")
    @Size(max = 255, message = "Title must not exceed 255 characters.")
    String title,

    @Schema(description = "Optional task description.", example = "Summarize the changes for this release.")
    @Size(max = 255, message = "Description must not exceed 255 characters.")
    String description,

    @Schema(description = "Task status.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Status is required.")
    TaskStatus status,

    @Schema(description = "Task priority.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Priority is required.")
    TaskPriority priority,

    @Schema(description = "Optional due date in ISO format (yyyy-MM-dd).", example = "2026-12-31")
    LocalDate dueDate
) {
}
