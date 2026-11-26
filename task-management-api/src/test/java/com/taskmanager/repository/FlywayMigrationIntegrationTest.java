package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FlywayMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void migratesCompleteSchemaAndJpaPersistsPriority() throws Exception {
        assertThat(flyway.info().applied())
                .extracting(org.flywaydb.core.api.MigrationInfo::getVersion)
                .contains(MigrationVersion.fromVersion("1"), MigrationVersion.fromVersion("2"));

        Set<String> applicationTables = new HashSet<>(jdbcTemplate.queryForList(
                """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'PUBLIC'
                          AND table_type = 'BASE TABLE'
                          AND LOWER(table_name) <> 'flyway_schema_history'
                        """,
                String.class
        ));
        assertThat(applicationTables).containsExactlyInAnyOrder(
                "USERS",
                "TASKS",
                "OAUTH_ACCOUNTS",
                "PASSWORD_RESET_TOKENS",
                "REFRESH_TOKENS"
        );

        assertThat(constraintNames()).contains(
                "UK_USERS_EMAIL",
                "UK_OAUTH_ACCOUNTS_PROVIDER_USER",
                "UK_PASSWORD_RESET_TOKENS_HASH",
                "UK_REFRESH_TOKENS_TOKEN_HASH",
                "FK_TASKS_USER",
                "FK_OAUTH_ACCOUNTS_USER",
                "FK_PASSWORD_RESET_TOKENS_USER",
                "FK_REFRESH_TOKENS_USER",
                "CK_TASKS_PRIORITY"
        );
        assertThat(indexNames()).contains(
                "IDX_OAUTH_ACCOUNTS_USER",
                "IDX_PASSWORD_RESET_TOKENS_HASH",
                "IDX_REFRESH_TOKENS_HASH",
                "IDX_REFRESH_TOKENS_FAMILY",
                "IDX_REFRESH_TOKENS_USER"
        );

        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columns = metadata.getColumns(null, "PUBLIC", "TASKS", "PRIORITY")) {
                assertThat(columns.next()).isTrue();
                assertThat(columns.getInt("NULLABLE")).isEqualTo(DatabaseMetaData.columnNoNulls);
            }
            try (ResultSet foreignKeys = metadata.getImportedKeys(null, "PUBLIC", "TASKS")) {
                assertThat(foreignKeys.next()).isTrue();
                assertThat(foreignKeys.getString("PKTABLE_NAME")).isEqualTo("USERS");
            }
        }

        User user = userRepository.saveAndFlush(
                new User(
                        "Flyway User",
                        "flyway-" + System.nanoTime() + "@example.com",
                        "hashed-password"
                )
        );
        Task task = taskRepository.saveAndFlush(new Task(
                "Flyway priority task",
                "Persisted by JPA after Flyway schema creation",
                TaskStatus.IN_PROGRESS,
                TaskPriority.URGENT,
                LocalDate.of(2026, 12, 1),
                user
        ));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT priority FROM tasks WHERE id = ?",
                String.class,
                task.getId()
        )).isEqualTo("URGENT");
        assertThat(taskRepository.findById(task.getId()).orElseThrow().getPriority())
                .isEqualTo(TaskPriority.URGENT);
        assertThat(taskRepository.findByIdAndUserId(task.getId(), user.getId()))
                .isPresent();
        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE tasks SET priority = 'INVALID' WHERE id = ?",
                task.getId()
        )).isInstanceOf(DataIntegrityViolationException.class);

        jdbcTemplate.update(
                """
                        INSERT INTO tasks (title, description, status, due_date, created_at, updated_at, user_id)
                        VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)
                        """,
                "Database priority default",
                "Priority omitted",
                TaskStatus.PENDING.name(),
                null,
                user.getId()
        );
        assertThat(jdbcTemplate.queryForObject(
                "SELECT priority FROM tasks WHERE title = 'Database priority default'",
                String.class
        )).isEqualTo("MEDIUM");
    }

    private Set<String> constraintNames() {
        return new HashSet<>(jdbcTemplate.queryForList(
                """
                        SELECT constraint_name
                        FROM information_schema.table_constraints
                        WHERE constraint_schema = 'PUBLIC'
                        """,
                String.class
        ));
    }

    private Set<String> indexNames() throws Exception {
        Set<String> names = new HashSet<>();
        try (var connection = dataSource.getConnection();
             ResultSet indexes = connection.getMetaData().getIndexInfo(null, "PUBLIC", "REFRESH_TOKENS", false, false)) {
            while (indexes.next()) {
                String name = indexes.getString("INDEX_NAME");
                if (name != null) {
                    names.add(name);
                }
            }
        }
        try (var connection = dataSource.getConnection();
             ResultSet indexes = connection.getMetaData().getIndexInfo(null, "PUBLIC", "OAUTH_ACCOUNTS", false, false)) {
            while (indexes.next()) {
                String name = indexes.getString("INDEX_NAME");
                if (name != null) {
                    names.add(name);
                }
            }
        }
        try (var connection = dataSource.getConnection();
             ResultSet indexes = connection.getMetaData().getIndexInfo(null, "PUBLIC", "PASSWORD_RESET_TOKENS", false, false)) {
            while (indexes.next()) {
                String name = indexes.getString("INDEX_NAME");
                if (name != null) {
                    names.add(name);
                }
            }
        }
        return names;
    }
}
