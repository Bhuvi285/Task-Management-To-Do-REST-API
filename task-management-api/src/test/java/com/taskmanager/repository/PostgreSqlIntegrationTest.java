package com.taskmanager.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.OAuthProvider;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.OAuthAccountRepository;
import com.taskmanager.repository.RefreshTokenRepository;
import com.taskmanager.service.RefreshTokenGenerator;
import com.taskmanager.service.SocialAccountService;
import com.taskmanager.service.SocialIdentity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnabledIfSystemProperty(named = "runPostgresIntegration", matches = "true")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
class PostgreSqlIntegrationTest {

    private static final String PASSWORD = "PostgresTestPassword123!";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenGenerator refreshTokenGenerator;

    @Autowired
    private SocialAccountService socialAccountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void persistsUsersAndTasksAndKeepsPriorityOwnershipAndFiltering() {
        assertThat(dataSource).isNotNull();
        assertThat(jdbcTemplate.queryForObject("select current_database()", String.class))
                .isEqualTo(POSTGRES.getDatabaseName());
        Integer serverVersion = jdbcTemplate.queryForObject(
                "SELECT current_setting('server_version_num')::integer", Integer.class);
        assertThat(serverVersion).isBetween(160000, 169999);

        String unique = UUID.randomUUID().toString();
        User owner = userRepository.saveAndFlush(new User(
                "Owner",
                "owner-" + unique + "@example.com",
                passwordEncoder.encode(PASSWORD)
        ));
        User otherOwner = userRepository.saveAndFlush(
                new User(
                        "Other",
                        "other-" + unique + "@example.com",
                        passwordEncoder.encode(PASSWORD)
                )
        );

        LocalDate dueDate = LocalDate.of(2026, 12, 1);
        Task firstMatch = new Task(
                "PostgreSQL task one", "first", TaskStatus.IN_PROGRESS,
                TaskPriority.URGENT, dueDate, owner
        );
        Task secondMatch = new Task(
                "PostgreSQL task two", "second", TaskStatus.IN_PROGRESS,
                TaskPriority.URGENT, dueDate, owner
        );
        Task wrongStatus = new Task(
                "PostgreSQL pending task", "excluded by status", TaskStatus.PENDING,
                TaskPriority.URGENT, dueDate, owner
        );
        Task wrongOwner = new Task(
                "PostgreSQL other owner", "excluded by owner", TaskStatus.IN_PROGRESS,
                TaskPriority.URGENT, dueDate, otherOwner
        );
        taskRepository.saveAllAndFlush(List.of(firstMatch, secondMatch, wrongStatus, wrongOwner));

        entityManager.clear();

        User reloadedOwner = userRepository.findById(owner.getId()).orElseThrow();
        Task reloadedTask = taskRepository.findById(firstMatch.getId()).orElseThrow();
        assertThat(reloadedOwner.getEmail()).isEqualTo(owner.getEmail());
        assertThat(reloadedTask.getPriority()).isEqualTo(TaskPriority.URGENT);
        assertThat(reloadedTask.getUser().getId()).isEqualTo(owner.getId());

        Page<Task> firstPage = taskRepository.findOwnedTasks(
                owner.getId(),
                TaskStatus.IN_PROGRESS,
                TaskPriority.URGENT,
                "postgresql",
                dueDate,
                dueDate,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id"))
        );

        assertThat(firstPage.getTotalElements()).isEqualTo(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(firstPage.getContent().getFirst().getUser().getId()).isEqualTo(owner.getId());
    }

    @Test
    void flywayAppliedAllMigrationsAndCreatedProductionIndexesAndConstraints() {
        List<String> appliedMigrations = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class
        );
        assertThat(appliedMigrations).contains("1", "2", "3");

        Integer taskIndexCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM pg_indexes
                WHERE schemaname = current_schema()
                  AND tablename = 'tasks'
                  AND indexname = 'idx_tasks_user_created_at_id'
                """, Integer.class);
        assertThat(taskIndexCount).isEqualTo(1);

        Integer priorityConstraintCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.table_constraints
                WHERE table_schema = current_schema()
                  AND table_name = 'tasks'
                  AND constraint_name = 'ck_tasks_priority'
                """, Integer.class);
        assertThat(priorityConstraintCount).isEqualTo(1);
    }

    @Test
    void registrationLoginAndRefreshRotationPersistCorrectlyInPostgres() throws Exception {
        String unique = UUID.randomUUID().toString();
        String email = "postgres-auth-" + unique + "@example.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "PostgreSQL Auth User",
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isAccepted());

        User registeredUser = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(passwordEncoder.matches(PASSWORD, registeredUser.getPasswordHash())).isTrue();

        JsonNode login = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString());
        String originalRefreshToken = login.get("refreshToken").asText();
        var storedOriginal = refreshTokenRepository.findByTokenHash(
                refreshTokenGenerator.hashToken(originalRefreshToken)
        ).orElseThrow();

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + login.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        JsonNode rotated = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", originalRefreshToken
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
        String replacementRefreshToken = rotated.get("refreshToken").asText();

        assertThat(replacementRefreshToken).isNotEqualTo(originalRefreshToken);
        assertThat(refreshTokenRepository.findByTokenHash(
                refreshTokenGenerator.hashToken(replacementRefreshToken)
        )).isPresent();
        assertThat(refreshTokenRepository.findById(storedOriginal.getId()).orElseThrow().getRevokedAt())
                .isNotNull();
    }

    @Test
    void socialProviderAccountCanBePersistedWithoutCallingAnExternalProvider() {
        String unique = UUID.randomUUID().toString();
        String providerUserId = "github-user-" + unique;
        String email = "github-" + unique + "@example.com";

        var user = socialAccountService.resolveOrProvision(new SocialIdentity(
                OAuthProvider.GITHUB,
                providerUserId,
                email,
                true,
                "PostgreSQL OAuth User"
        ));

        var account = oauthAccountRepository
                .findByProviderAndProviderUserId(OAuthProvider.GITHUB, providerUserId)
                .orElseThrow();
        assertThat(account.getUser().getId()).isEqualTo(user.getId());
        assertThat(account.getProvider()).isEqualTo(OAuthProvider.GITHUB);
    }
}
