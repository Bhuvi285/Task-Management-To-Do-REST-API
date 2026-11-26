    package com.taskmanager.dto.task;

    import com.taskmanager.entity.Task;
    import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

    import java.time.LocalDate;
    import java.time.LocalDateTime;

    @Schema(name = "TaskResponse", description = "Public task representation; does not expose entity ownership or persistence relationships.")
    public record TaskResponse(
        @Schema(description = "Task identifier.", example = "42")
        Long id,
        @Schema(description = "Task title.")
        String title,
        @Schema(description = "Task description.")
        String description,
        @Schema(description = "Task status.")
        TaskStatus status,
        @Schema(description = "Task priority.")
        TaskPriority priority,
        @Schema(description = "Due date, when set.", example = "2026-12-31")
        LocalDate dueDate,
        @Schema(description = "Creation timestamp.", example = "2026-11-25T18:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update timestamp.", example = "2026-11-25T18:00:00")
        LocalDateTime updatedAt
    ) {

        public static TaskResponse from(Task task) {
            return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
            );
        }
    }
