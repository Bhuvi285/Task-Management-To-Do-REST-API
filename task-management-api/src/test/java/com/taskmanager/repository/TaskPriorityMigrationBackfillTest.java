package com.taskmanager.repository;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskPriorityMigrationBackfillTest {

    @Test
    void assignsMediumToExistingTasksWhenPriorityIsAdded() {
        String databaseName = "priority_backfill_" + UUID.randomUUID().toString().replace("-", "");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        dataSource.setDriverClassName("org.h2.Driver");

        Flyway initialSchema = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("1"))
                .load();
        initialSchema.migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(
                """
                        INSERT INTO users (name, email, password_hash, enabled, created_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                "Existing User",
                "existing@example.com",
                "hashed-password",
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        jdbcTemplate.update(
                """
                        INSERT INTO tasks (title, description, status, due_date, created_at, updated_at, user_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                "Existing task",
                "Created before priority existed",
                "PENDING",
                null,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                1L
        );

        Flyway completeSchema = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        completeSchema.migrate();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT priority FROM tasks WHERE title = 'Existing task'",
                String.class
        )).isEqualTo("MEDIUM");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT is_nullable FROM information_schema.columns "
                        + "WHERE table_schema = 'PUBLIC' AND table_name = 'TASKS' AND column_name = 'PRIORITY'",
                String.class
        )).isEqualTo("NO");
    }
}
