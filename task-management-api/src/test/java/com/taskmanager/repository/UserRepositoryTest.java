package com.taskmanager.repository;

import com.taskmanager.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveUserById() {
        User user = newUser("Alice", "alice@example.com");

        User savedUser = userRepository.save(user);
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getName()).isEqualTo("Alice");
        assertThat(retrievedUser.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(retrievedUser.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByEmail() {
        userRepository.save(newUser("Bob", "bob@example.com"));

        Optional<User> result = userRepository.findByEmail("bob@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bob");
    }

    @Test
    void emailExistenceAndLookupAreCaseInsensitive() {
        userRepository.saveAndFlush(newUser("Case User", "case@example.com"));

        assertThat(userRepository.findByEmailIgnoreCase("CASE@EXAMPLE.COM"))
            .isPresent();
        assertThat(userRepository.existsByEmailIgnoreCase("Case@Example.com")).isTrue();
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userRepository.saveAndFlush(newUser("First", "duplicate@example.com"));

        assertThatThrownBy(() ->
            userRepository.saveAndFlush(newUser("Second", "duplicate@example.com"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    private User newUser(String name, String email) {
        return new User(name, email, PASSWORD_ENCODER.encode("test-password"));
    }
}
