package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldSaveAndRetrieveTaskWithUserRelationship() {
        User user = userRepository.save(newUser("Alice", "alice@example.com"));
        Task task = new Task(
            "Design database",
            "Create the JPA entity mappings",
            TaskStatus.PENDING,
            LocalDate.now().plusDays(7),
            user
        );

        Task savedTask = taskRepository.save(task);
        Task retrievedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

        assertThat(retrievedTask.getId()).isNotNull();
        assertThat(retrievedTask.getUser().getId()).isEqualTo(user.getId());
        assertThat(retrievedTask.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindTasksByStatusAndUserId() {
        User user = userRepository.save(newUser("Bob", "bob@example.com"));
        taskRepository.save(new Task(
            "Pending task",
            "Must be completed",
            TaskStatus.PENDING,
            LocalDate.now().plusDays(1),
            user
        ));
        taskRepository.save(new Task(
            "Completed task",
            "Already done",
            TaskStatus.COMPLETED,
            LocalDate.now(),
            user
        ));

        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        List<Task> userTasks = taskRepository.findByUserId(user.getId());

        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.getFirst().getTitle()).isEqualTo("Pending task");
        assertThat(userTasks).hasSize(2);
    }

    @Test
    void shouldFindTasksByTitleIgnoringCase() {
        User user = userRepository.save(newUser("Carol", "carol@example.com"));
        taskRepository.save(new Task(
            "Prepare Architecture",
            "Document persistence",
            TaskStatus.IN_PROGRESS,
            LocalDate.now().plusDays(3),
            user
        ));

        List<Task> matches = taskRepository.findByTitleContainingIgnoreCase("architecture");

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getTitle()).isEqualTo("Prepare Architecture");
    }

    private User newUser(String name, String email) {
        return new User(name, email, PASSWORD_ENCODER.encode("test-password"));
    }
}
