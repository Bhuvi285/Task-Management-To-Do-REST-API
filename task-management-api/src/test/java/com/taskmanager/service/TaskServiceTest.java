package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskForExistingUser() {
        User user = new User("Alice", "alice@example.com");
        Task unsavedTask = new Task(
            "Test",
            "Description",
            TaskStatus.PENDING,
            LocalDate.now().plusDays(1),
            user
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(unsavedTask);

        Task result = taskService.createTask(
            1L,
            "Test",
            "Description",
            TaskStatus.PENDING,
            LocalDate.now().plusDays(1)
        );

        assertThat(result).isSameAs(unsavedTask);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldRejectTaskCreationForMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(
            99L,
            "Test",
            "Description",
            TaskStatus.PENDING,
            LocalDate.now()
        )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldRetrieveAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(
            new Task("A", null, TaskStatus.PENDING, null, new User("A", "a@example.com")),
            new Task("B", null, TaskStatus.COMPLETED, null, new User("B", "b@example.com"))
        ));

        assertThat(taskService.getAllTasks()).hasSize(2);
    }

    @Test
    void shouldRetrieveTaskById() {
        Task task = new Task("A", null, TaskStatus.PENDING, null,
            new User("A", "a@example.com"));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThat(taskService.getTaskById(1L)).isSameAs(task);
    }

    @Test
    void shouldRejectMissingTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Old", "Old description", TaskStatus.PENDING, null,
            new User("A", "a@example.com"));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        Task result = taskService.updateTask(
            1L,
            "New",
            "New description",
            TaskStatus.IN_PROGRESS,
            LocalDate.now()
        );

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository, never()).save(task);
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("A", null, TaskStatus.PENDING, null,
            new User("A", "a@example.com"));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldRetrieveTasksByUserId() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByUserId(1L)).thenReturn(List.of(
            new Task("A", null, TaskStatus.PENDING, null,
                new User("A", "a@example.com"))
        ));

        assertThat(taskService.getTasksByUserId(1L)).hasSize(1);
    }
}
