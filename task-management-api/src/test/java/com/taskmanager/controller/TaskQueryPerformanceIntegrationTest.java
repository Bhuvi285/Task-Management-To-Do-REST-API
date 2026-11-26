package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.JwtService;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class TaskQueryPerformanceIntegrationTest {

    private static final String TASKS_URL = "/api/v1/tasks";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String accessToken;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString();
        User user = userRepository.saveAndFlush(new User(
                "Performance user",
                "performance-" + unique + "@example.com",
                passwordEncoder.encode("PerformancePassword123!")
        ));
        accessToken = jwtService.generateAccessToken(user.getEmail());
        taskRepository.saveAllAndFlush(IntStream.range(0, 25)
                .mapToObj(index -> new Task(
                        "Task " + index,
                        null,
                        TaskStatus.PENDING,
                        TaskPriority.MEDIUM,
                        null,
                        user
                ))
                .toList());
    }

    @Test
    void paginatedTaskListUsesThreeStatementsForAFullPage() throws Exception {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        mockMvc.perform(get(TASKS_URL)
                        .param("size", "10")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(25));

        assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
    }

    @Test
    void ownerAndDefaultSortIndexIsCreatedByMigration() {
        Integer indexCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.INDEXES
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND TABLE_NAME = 'TASKS'
                  AND INDEX_NAME = 'IDX_TASKS_USER_CREATED_AT_ID'
                """, Integer.class);

        assertThat(indexCount).isEqualTo(1);
    }
}