package com.taskmanager.repository;

import com.taskmanager.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUserById() {
        User user = new User("Alice", "alice@example.com");

        User savedUser = userRepository.save(user);
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getName()).isEqualTo("Alice");
        assertThat(retrievedUser.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(retrievedUser.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByEmail() {
        userRepository.save(new User("Bob", "bob@example.com"));

        Optional<User> result = userRepository.findByEmail("bob@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bob");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userRepository.saveAndFlush(new User("First", "duplicate@example.com"));

        assertThatThrownBy(() ->
            userRepository.saveAndFlush(new User("Second", "duplicate@example.com"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
