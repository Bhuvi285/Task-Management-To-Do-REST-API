package com.taskmanager.dto.task;

import com.taskmanager.entity.Task;
import org.springframework.data.domain.Page;

import java.util.List;

public record TaskPageResponse(
        List<TaskResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
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
