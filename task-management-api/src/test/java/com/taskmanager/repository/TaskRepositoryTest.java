package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private EntityManager entityManager;

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
    void taskQueriesAreScopedToTheOwningUser() {
        User user = userRepository.save(newUser("Bob", "bob@example.com"));
        User otherUser = userRepository.save(newUser("Carol", "carol@example.com"));
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
        Task otherUsersTask = taskRepository.save(new Task(
                "Other user's pending task",
                "Must not be visible to Bob",
                TaskStatus.PENDING,
                LocalDate.now().plusDays(2),
                otherUser
        ));

        Page<Task> pendingTasks = taskRepository.findOwnedTasks(
                user.getId(), TaskStatus.PENDING, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("title"))
        );
        Page<Task> userTasks = taskRepository.findOwnedTasks(
                user.getId(), null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("title"))
        );

        assertThat(pendingTasks.getContent()).hasSize(1);
        assertThat(pendingTasks.getContent().getFirst().getTitle()).isEqualTo("Pending task");
        assertThat(userTasks.getContent()).hasSize(2);
        assertThat(userTasks.getTotalElements()).isEqualTo(2);
        assertThat(taskRepository.findByIdAndUserId(otherUsersTask.getId(), user.getId()))
                .isEmpty();
        assertThat(taskRepository.existsByIdAndUserId(otherUsersTask.getId(), user.getId()))
                .isFalse();
        assertThat(taskRepository.findByIdAndUserId(otherUsersTask.getId(), otherUser.getId()))
                .get()
                .extracting(Task::getUser)
                .extracting(User::getId)
                .isEqualTo(otherUser.getId());
    }

    @Test
    void shouldFindTasksByOwnerAndTitleIgnoringCase() {
        User user = userRepository.save(newUser("Carol", "carol@example.com"));
        taskRepository.save(new Task(
                "Prepare Architecture",
                "Document persistence",
                TaskStatus.IN_PROGRESS,
                LocalDate.now().plusDays(3),
                user
        ));

        Page<Task> matches = taskRepository.findOwnedTasks(
                user.getId(), null, null, "architecture", null, null,
                PageRequest.of(0, 10, Sort.by("title"))
        );

        assertThat(matches.getContent()).hasSize(1);
        assertThat(matches.getContent().getFirst().getTitle()).isEqualTo("Prepare Architecture");
    }

    @Test
    void priorityFilteringCombinesWithStatusSearchPaginationAndOwnerScope() {
        User user = userRepository.saveAndFlush(newUser("Priority owner", "priority@example.com"));
        User otherUser = userRepository.saveAndFlush(newUser("Other owner", "other@example.com"));
        taskRepository.saveAllAndFlush(List.of(
                new Task("High match one", null, TaskStatus.PENDING, TaskPriority.HIGH,
                        LocalDate.now(), user),
                new Task("High match two", null, TaskStatus.PENDING, TaskPriority.HIGH,
                        LocalDate.now(), user),
                new Task("High different status", null, TaskStatus.COMPLETED, TaskPriority.HIGH,
                        LocalDate.now(), user),
                new Task("Medium match", null, TaskStatus.PENDING, TaskPriority.MEDIUM,
                        LocalDate.now(), user),
                new Task("High match owned elsewhere", null, TaskStatus.PENDING, TaskPriority.HIGH,
                        LocalDate.now(), otherUser)
        ));

        Page<Task> firstPage = taskRepository.findOwnedTasks(
                user.getId(), TaskStatus.PENDING, TaskPriority.HIGH, "match", null, null,
                PageRequest.of(0, 1, Sort.by("title"))
        );
        Page<Task> secondPage = taskRepository.findOwnedTasks(
                user.getId(), TaskStatus.PENDING, TaskPriority.HIGH, "match", null, null,
                PageRequest.of(1, 1, Sort.by("title"))
        );

        assertThat(firstPage.getTotalElements()).isEqualTo(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).extracting(Task::getTitle).containsExactly("High match one");
        assertThat(secondPage.getContent()).extracting(Task::getTitle).containsExactly("High match two");
    }

    @Test
    void savesAndRetrievesEachPriorityByEnumName() {
        User user = userRepository.saveAndFlush(newUser("Priorities", "priorities@example.com"));
        List<Task> tasks = java.util.Arrays.stream(TaskPriority.values())
                .map(priority -> new Task(
                        "Task " + priority, null, TaskStatus.PENDING, priority, null, user
                ))
                .toList();

        taskRepository.saveAllAndFlush(tasks);
        entityManager.clear();

        List<Task> reloaded = taskRepository.findOwnedTasks(
                user.getId(), null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("priority").ascending())
        ).getContent();
        assertThat(reloaded).extracting(Task::getPriority)
                .containsExactly(TaskPriority.HIGH, TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.URGENT);
    }

    private User newUser(String name, String email) {
        return new User(name, email, PASSWORD_ENCODER.encode("test-password"));
    }
}
