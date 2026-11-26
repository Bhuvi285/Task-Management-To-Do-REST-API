package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({CurrentUserService.class, TaskService.class})
class TaskServiceIntegrationTest {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void usersCanOnlyReadAndMutateTasksOwnedByTheirAuthenticatedIdentity() {
        User userA = saveUser("Alice", "alice@example.com");
        User userB = saveUser("Bob", "bob@example.com");
        Authentication authenticationA = authenticated(" ALICE@EXAMPLE.COM ");
        Authentication authenticationB = authenticated("bob@example.com");

        Task taskA = taskService.createTask(
                authenticationA,
                "Alice task",
                "Private to Alice",
                TaskStatus.PENDING,
                LocalDate.now().plusDays(1),
                null
        );
        Task taskB = taskService.createTask(
                authenticationB,
                "Bob task",
                "Private to Bob",
                TaskStatus.IN_PROGRESS,
                LocalDate.now().plusDays(2),
                TaskPriority.HIGH
        );

        assertThat(taskA.getUser().getId()).isEqualTo(userA.getId());
        assertThat(taskB.getUser().getId()).isEqualTo(userB.getId());
        assertThat(listTasks(authenticationA))
                .extracting(Task::getTitle)
                .containsExactly("Alice task");
        assertThat(listTasks(authenticationB))
                .extracting(Task::getTitle)
                .containsExactly("Bob task");

        assertThatThrownBy(() -> taskService.getTaskForCurrentUser(authenticationA, taskB.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found.");
        assertThatThrownBy(() -> taskService.updateTask(
                authenticationA,
                taskB.getId(),
                "Stolen title",
                "Modified by Alice",
                TaskStatus.COMPLETED,
                TaskPriority.URGENT,
                null
        )).isInstanceOf(EntityNotFoundException.class);
        assertThatThrownBy(() -> taskService.deleteTask(authenticationA, taskB.getId()))
                .isInstanceOf(EntityNotFoundException.class);

        taskService.updateTask(
                authenticationA,
                taskA.getId(),
                "Alice updated her task",
                "Still private to Alice",
                TaskStatus.COMPLETED,
                TaskPriority.LOW,
                LocalDate.now().plusDays(3)
        );
        Task unchangedTaskB = taskRepository.findByIdAndUserId(taskB.getId(), userB.getId())
                .orElseThrow();
        assertThat(unchangedTaskB.getTitle()).isEqualTo("Bob task");
        assertThat(unchangedTaskB.getDescription()).isEqualTo("Private to Bob");
        assertThat(unchangedTaskB.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(unchangedTaskB.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(unchangedTaskB.getUser().getId()).isEqualTo(userB.getId());

        Task updatedTaskA = taskRepository.findByIdAndUserId(taskA.getId(), userA.getId())
                .orElseThrow();
        assertThat(updatedTaskA.getTitle()).isEqualTo("Alice updated her task");
        assertThat(updatedTaskA.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(updatedTaskA.getUser().getId()).isEqualTo(userA.getId());
    }

    @Test
    void disabledUserCannotOperateOnTasks() {
        User disabledUser = saveUser("Disabled", "disabled@example.com");
        disabledUser.setEnabled(false);
        userRepository.saveAndFlush(disabledUser);

        assertThatThrownBy(() -> taskService.getTasksForCurrentUser(
                authenticated(disabledUser.getEmail()), 0, 10, "createdAt", "desc",
                null, null, null, null, null
        )).isInstanceOf(EntityNotFoundException.class);
    }

    private List<Task> listTasks(Authentication authentication) {
        Page<Task> page = taskService.getTasksForCurrentUser(
                authentication, 0, 10, "createdAt", "desc", null, null, null, null, null
        );
        return page.getContent();
    }

    private User saveUser(String name, String email) {
        return userRepository.saveAndFlush(
                new User(name, email, PASSWORD_ENCODER.encode("test-password"))
        );
    }

    private Authentication authenticated(String email) {
        return UsernamePasswordAuthenticationToken.authenticated(email, null, List.of());
    }
}
