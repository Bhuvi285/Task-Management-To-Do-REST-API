package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.exception.InvalidTaskQueryException;
import com.taskmanager.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id", "createdAt", "updatedAt", "dueDate", "title", "status", "priority"
    );

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    public TaskService(
            TaskRepository taskRepository,
            CurrentUserService currentUserService
    ) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Task createTask(
            Authentication authentication,
            String title,
            String description,
            TaskStatus status,
            LocalDate dueDate,
            TaskPriority priority
    ) {
        User user = currentUserService.resolve(authentication);
        TaskPriority resolvedPriority = priority == null ? TaskPriority.MEDIUM : priority;
        Task task = new Task(title, description, status, resolvedPriority, dueDate, user);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Page<Task> getTasksForCurrentUser(
            Authentication authentication,
            int page,
            int size,
            String sortBy,
            String direction,
            String search,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueFrom,
            LocalDate dueTo
    ) {
        if (page < 0) {
            throw new InvalidTaskQueryException("Page must be zero or greater.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidTaskQueryException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE + "."
            );
        }
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidTaskQueryException(
                    "Unsupported sortBy field. Allowed fields: " + String.join(", ", ALLOWED_SORT_FIELDS)
            );
        }
        if (direction == null
                || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            throw new InvalidTaskQueryException("Sort direction must be 'asc' or 'desc'.");
        }
        if (dueFrom != null && dueTo != null && dueFrom.isAfter(dueTo)) {
            throw new InvalidTaskQueryException("dueFrom must not be after dueTo.");
        }

        User user = currentUserService.resolve(authentication);
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy).and(Sort.by(Sort.Direction.ASC, "id"))
        );
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        return taskRepository.findOwnedTasks(
                user.getId(),
                status,
                priority,
                normalizedSearch,
                dueFrom,
                dueTo,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Task getTaskForCurrentUser(Authentication authentication, Long taskId) {
        User user = currentUserService.resolve(authentication);
        return findTaskForUser(taskId, user.getId());
    }

    @Transactional
    public Task updateTask(
            Authentication authentication,
            Long taskId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDate
    ) {
        User user = currentUserService.resolve(authentication);
        Task task = findTaskForUser(taskId, user.getId());
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        return task;
    }

    @Transactional
    public void deleteTask(Authentication authentication, Long taskId) {
        User user = currentUserService.resolve(authentication);
        Task task = findTaskForUser(taskId, user.getId());
        taskRepository.delete(task);
    }

    private Task findTaskForUser(Long taskId, Long userId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found."));
    }
}
