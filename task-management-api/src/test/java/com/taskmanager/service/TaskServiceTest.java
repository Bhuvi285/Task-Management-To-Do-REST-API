package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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
    private CurrentUserService currentUserService;

    @InjectMocks
    private TaskService taskService;

    private final Authentication authentication =
            UsernamePasswordAuthenticationToken.authenticated("alice@example.com", null, List.of());

    @Test
    void createsTaskUsingResolvedAuthenticatedUser() {
        User user = new User("Alice", "alice@example.com", "encoded-password");
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.createTask(
                authentication,
                "Test",
                "Description",
                TaskStatus.PENDING,
                LocalDate.now().plusDays(1),
                null
        );

        assertThat(created.getUser()).isSameAs(user);
        assertThat(created.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void createsTaskWithEachExplicitPriority() {
        User user = new User("Alice", "alice@example.com", "encoded-password");
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        for (TaskPriority priority : TaskPriority.values()) {
            Task created = taskService.createTask(
                    authentication, "Task", null, TaskStatus.PENDING, null, priority
            );
            assertThat(created.getPriority()).isEqualTo(priority);
        }
    }

    @Test
    void retrievesTasksOnlyByResolvedOwnerId() {
        User user = userWithId();
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.findOwnedTasks(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(
                new Task("Alice task", null, TaskStatus.PENDING, null, user)
        )));

        Page<Task> result = taskService.getTasksForCurrentUser(
                authentication, 0, 10, "createdAt", "desc", null, null, null, null, null
        );
        assertThat(result.getContent())
                .extracting(Task::getTitle)
                .containsExactly("Alice task");
        verify(taskRepository).findOwnedTasks(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                any(PageRequest.class)
        );
        verify(taskRepository, never()).findAll();
    }

    @Test
    void rejectsUnsupportedSortAndInvalidPagination() {
        assertThatThrownBy(() -> taskService.getTasksForCurrentUser(
                authentication, 0, 10, "user.passwordHash", "asc", null, null, null, null, null
        )).isInstanceOf(com.taskmanager.exception.InvalidTaskQueryException.class);

        assertThatThrownBy(() -> taskService.getTasksForCurrentUser(
                authentication, 0, 101, "title", "asc", null, null, null, null, null
        )).isInstanceOf(com.taskmanager.exception.InvalidTaskQueryException.class);
    }

    @Test
    void retrievesTaskWithTaskAndOwnerIdsTogether() {
        User user = userWithId();
        Task task = new Task("Alice task", null, TaskStatus.PENDING, null, user);
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(task));

        assertThat(taskService.getTaskForCurrentUser(authentication, 10L)).isSameAs(task);
    }

    @Test
    void otherUsersTaskUsesSameNotFoundBehavior() {
        User user = userWithId();
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskForCurrentUser(authentication, 20L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found.");
    }

    @Test
    void updatesOnlyOwnedTaskAndDoesNotChangeOwner() {
        User owner = userWithId();
        Task task = new Task("Old", "Old description", TaskStatus.PENDING, null, owner);
        when(currentUserService.resolve(authentication)).thenReturn(owner);
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(task));

        Task result = taskService.updateTask(
                authentication,
                10L,
                "New",
                "New description",
                TaskStatus.IN_PROGRESS,
                TaskPriority.URGENT,
                LocalDate.now()
        );

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.URGENT);
        assertThat(result.getUser()).isSameAs(owner);
        verify(taskRepository, never()).findById(10L);
        verify(taskRepository, never()).save(task);
    }

    @Test
    void cannotUpdateTaskOwnedByAnotherUser() {
        User user = userWithId();
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(
                authentication, 20L, "Changed", null, TaskStatus.COMPLETED, TaskPriority.HIGH, null
        )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void cannotDeleteTaskOwnedByAnotherUser() {
        User user = userWithId();
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(authentication, 20L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void deletesOwnedTask() {
        User user = userWithId();
        Task task = new Task("Alice task", null, TaskStatus.PENDING, null, user);
        when(currentUserService.resolve(authentication)).thenReturn(user);
        when(taskRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(authentication, 10L);

        verify(taskRepository).delete(task);
    }

    private User userWithId() {
        User user = org.mockito.Mockito.mock(User.class);
        org.mockito.Mockito.when(user.getId()).thenReturn(1L);
        return user;
    }
}
