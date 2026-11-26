package com.taskmanager.controller;

import com.taskmanager.dto.task.CreateTaskRequest;
import com.taskmanager.dto.task.TaskPageResponse;
import com.taskmanager.dto.task.TaskResponse;
import com.taskmanager.dto.task.UpdateTaskRequest;
import com.taskmanager.entity.Task;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task operations scoped to the authenticated user; ownership is derived from the JWT principal.")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a task for the authenticated user. Owner identity is never supplied by the client; priority defaults to MEDIUM when omitted.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Task created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "JWT access token missing or invalid", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ) {
        Task createdTask = taskService.createTask(
                authentication,
                request.title(),
                request.description(),
                TaskStatus.PENDING,
                request.dueDate(),
                request.priority()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/v1/tasks/" + createdTask.getId()))
                .body(TaskResponse.from(createdTask));
    }

    @GetMapping
    @Operation(summary = "List the current user's tasks", description = "Returns a page of tasks belonging only to the authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task page returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskPageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid pagination, sort, enum or date range", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "JWT access token missing or invalid", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Authenticated request is not permitted", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public TaskPageResponse getTasks(
            Authentication authentication,
            @Parameter(description = "Zero-based page index.", schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of tasks per page (1–100).", schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10"))
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field.", schema = @Schema(allowableValues = {"id", "createdAt", "updatedAt", "dueDate", "title", "status", "priority"}, defaultValue = "createdAt"))
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction.", schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "desc"))
            @RequestParam(defaultValue = "desc") String direction,
            @Parameter(description = "Optional case-insensitive title search.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Optional task status filter.")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Optional task priority filter.")
            @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "Optional inclusive lower due-date bound (ISO date, yyyy-MM-dd).", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueFrom,
            @Parameter(description = "Optional inclusive upper due-date bound (ISO date, yyyy-MM-dd). Must not be before dueFrom.", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueTo
    ) {
        return TaskPageResponse.from(taskService.getTasksForCurrentUser(
                authentication,
                page,
                size,
                sortBy,
                direction,
                search,
                status,
                priority,
                dueFrom,
                dueTo
        ));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get a task", description = "Gets a task only if it belongs to the authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "401", description = "JWT access token missing or invalid", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Authenticated request is not permitted", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found for the current user", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public TaskResponse getTask(
            @Parameter(description = "Task identifier owned by the authenticated user.", required = true, example = "42")
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return TaskResponse.from(taskService.getTaskForCurrentUser(authentication, taskId));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Replace a task", description = "Updates the task only if it belongs to the authenticated user. All required update fields must be supplied.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
        @ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "JWT access token missing or invalid", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Authenticated request is not permitted", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found for the current user", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public TaskResponse updateTask(
            @Parameter(description = "Task identifier owned by the authenticated user.", required = true, example = "42")
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
    ) {
        Task updatedTask = taskService.updateTask(
                authentication,
                taskId,
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate()
        );
        return TaskResponse.from(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task", description = "Deletes the task only if it belongs to the authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Task deleted"),
        @ApiResponse(responseCode = "401", description = "JWT access token missing or invalid", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Authenticated request is not permitted", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found for the current user", content = @Content(schema = @Schema(implementation = com.taskmanager.dto.error.ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task identifier owned by the authenticated user.", required = true, example = "42")
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        taskService.deleteTask(authentication, taskId);
        return ResponseEntity.noContent().build();
    }
}
