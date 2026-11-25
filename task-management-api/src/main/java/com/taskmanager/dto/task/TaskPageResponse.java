package com.taskmanager.dto.task;

import com.taskmanager.entity.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(name = "TaskPageResponse", description = "Page of tasks owned by the authenticated user, including pagination metadata.")
public record TaskPageResponse(
        @Schema(description = "Tasks on this page.")
        List<TaskResponse> content,
        @Schema(description = "Zero-based current page index.")
        int pageNumber,
        @Schema(description = "Maximum number of tasks requested for this page.")
        int pageSize,
        @Schema(description = "Total number of matching tasks.")
        long totalElements,
        @Schema(description = "Total number of pages.")
        int totalPages,
        @Schema(description = "Whether this is the first page.")
        boolean first,
        @Schema(description = "Whether this is the last page.")
        boolean last,
        @Schema(description = "Whether the result contains no tasks.")
        boolean empty
) {

    public static TaskPageResponse from(Page<Task> page) {
        return new TaskPageResponse(
                page.getContent().stream().map(TaskResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
