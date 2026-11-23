package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PersistenceBehaviorTest {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldTrackEntityLifecycleStates() {
        User transientUser = newUser("Lifecycle", "lifecycle@example.com");
        assertThat(entityManager.contains(transientUser)).isFalse();

        entityManager.persist(transientUser);
        entityManager.flush();
        assertThat(entityManager.contains(transientUser)).isTrue();

        entityManager.detach(transientUser);
        assertThat(entityManager.contains(transientUser)).isFalse();

        User mergedUser = entityManager.merge(transientUser);
        assertThat(entityManager.contains(mergedUser)).isTrue();
    }

    @Test
    void shouldReturnSameInstanceFromFirstLevelCache() {
        User user = entityManager.merge(
            newUser("Cache", "cache@example.com")
        );
        Task task = new Task(
            "First-level cache",
            null,
            TaskStatus.PENDING,
            LocalDate.now(),
            user
        );
        entityManager.persist(task);
        entityManager.flush();
        entityManager.clear();

        Task firstLookup = entityManager.find(Task.class, task.getId());
        Task secondLookup = entityManager.find(Task.class, task.getId());

        assertThat(firstLookup).isSameAs(secondLookup);
    }

    @Test
    void shouldPersistManagedEntityChangesThroughDirtyChecking() {
        User user = entityManager.merge(
            newUser("Dirty", "dirty@example.com")
        );
        Task task = new Task(
            "Original title",
            null,
            TaskStatus.PENDING,
            LocalDate.now(),
            user
        );
        entityManager.persist(task);
        entityManager.flush();
        entityManager.clear();

        Task managedTask = entityManager.find(Task.class, task.getId());
        managedTask.setTitle("Changed title");
        entityManager.flush();
        entityManager.clear();

        Task reloadedTask = entityManager.find(Task.class, task.getId());
        assertThat(reloadedTask.getTitle()).isEqualTo("Changed title");
    }

    @Test
    void shouldLoadLazyUserRelationship() {
        User user = entityManager.merge(
            newUser("Lazy", "lazy@example.com")
        );
        Task task = new Task(
            "Lazy relationship",
            null,
            TaskStatus.PENDING,
            LocalDate.now(),
            user
        );
        entityManager.persist(task);
        entityManager.flush();
        entityManager.clear();

        Task loadedTask = entityManager.find(Task.class, task.getId());
        assertThat(loadedTask).isNotNull();

        User loadedUser = loadedTask.getUser();
        assertThat(loadedUser.getId()).isEqualTo(user.getId());
        assertThat(loadedUser.getEmail()).isEqualTo("lazy@example.com");
    }

    private User newUser(String name, String email) {
        return new User(name, email, PASSWORD_ENCODER.encode("test-password"));
    }
}
