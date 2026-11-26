package com.taskmanager.dto.task;

import com.taskmanager.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(name = "CreateTaskRequest", description = "Fields used to create a task. Ownership is determined by the authenticated user.")
public record CreateTaskRequest(
    @Schema(description = "Task title.", requiredMode = Schema.RequiredMode.REQUIRED, example = "Prepare release notes")
    @NotBlank(message = "Title must not be blank.")
    @Size(max = 255, message = "Title must not exceed 255 characters.")
    String title,

    @Schema(description = "Optional task description.", example = "Summarize the changes for this release.")
    @Size(max = 255, message = "Description must not exceed 255 characters.")
    String description,

    @Schema(description = "Optional due date in ISO format (yyyy-MM-dd).", example = "2026-12-31")
    LocalDate dueDate,

    @Schema(description = "Optional task priority; defaults to MEDIUM when omitted.", defaultValue = "MEDIUM")
    TaskPriority priority
) {
}
