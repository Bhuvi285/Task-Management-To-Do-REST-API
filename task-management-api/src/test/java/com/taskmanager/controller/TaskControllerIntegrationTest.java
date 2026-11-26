package com.taskmanager.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    private static final String TASKS_URL = "/api/v1/tasks";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString();
        userA = createUser("Alice", "alice-" + unique + "@example.com");
        userB = createUser("Bob", "bob-" + unique + "@example.com");
        tokenA = jwtService.generateAccessToken(userA.getEmail());
        tokenB = jwtService.generateAccessToken(userB.getEmail());
    }

    @Test
    void createListGetUpdateAndDeleteTaskThroughDtos() throws Exception {
        String createBody = """
                {"title":"Plan release","description":"Prepare notes","dueDate":"2026-12-01"}
                """;

        String createdResponse = mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Plan release"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(header().string("Location", containsString("/api/v1/tasks/")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode created = objectMapper.readTree(createdResponse);
        long taskId = created.get("id").asLong();

        mockMvc.perform(get(TASKS_URL).header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(taskId))
                .andExpect(jsonPath("$.content[0].user").doesNotExist())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
        mockMvc.perform(get(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));

        mockMvc.perform(put(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Release planned","description":"Final notes",
                                 "status":"IN_PROGRESS","priority":"URGENT","dueDate":"2026-12-02"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Release planned"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.user").doesNotExist());

        mockMvc.perform(get(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Release planned"))
                .andExpect(jsonPath("$.description").value("Final notes"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.user").doesNotExist());

        mockMvc.perform(delete(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isNoContent());
        assertThat(taskRepository.existsByIdAndUserId(taskId, userA.getId())).isFalse();
        mockMvc.perform(get(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found."));
    }

    @Test
    void ownershipComesFromAuthenticationAndCrossUserOperationsAreNotFound() throws Exception {
        String createBody = """
                {"title":"Alice private task","description":"Do not expose",
                 "userId":999999,"user":{"id":999999,"email":"attacker@example.com"}}
                """;
        String response = mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long taskId = objectMapper.readTree(response).get("id").asLong();
        Task savedTask = taskRepository.findByIdAndUserId(taskId, userA.getId()).orElseThrow();
        assertThat(savedTask.getUser().getId()).isEqualTo(userA.getId());

        mockMvc.perform(get(TASKS_URL).header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get(TASKS_URL)
                        .param("search", "Alice private task")
                        .param("status", "PENDING")
                        .param("priority", "MEDIUM")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
        mockMvc.perform(get(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found."));
        mockMvc.perform(put(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Attempted takeover","description":"not allowed",
                                 "status":"COMPLETED","priority":"MEDIUM"}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(TASKS_URL + "/" + taskId)
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get(TASKS_URL + "/" + taskId))
                .andExpect(status().isUnauthorized());

        Task unchangedTask = taskRepository.findByIdAndUserId(taskId, userA.getId()).orElseThrow();
        assertThat(unchangedTask.getTitle()).isEqualTo("Alice private task");
        assertThat(unchangedTask.getDescription()).isEqualTo("Do not expose");
    }

    @Test
    void unauthenticatedRequestIsRejectedBySecurity() throws Exception {
        mockMvc.perform(get(TASKS_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void missingTaskReturnsConsistentNotFoundResponse() throws Exception {
        mockMvc.perform(get(TASKS_URL + "/999999999")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value(TASKS_URL + "/999999999"));
    }

    @Test
    void invalidTaskRequestsReturnSafeBadRequestErrors() throws Exception {
        String blankTitle = """
                {"title":"  ","description":null,"dueDate":null}
                """;
        mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankTitle))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details.title").exists());

        mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("x".repeat(256))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());

        mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "Valid title",
                                "description", "x".repeat(256)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.description").exists());

        mockMvc.perform(put(TASKS_URL + "/1")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","description":null,"status":"NOT_A_STATUS"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body."));

        mockMvc.perform(put(TASKS_URL + "/1")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","description":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details.status").exists())
                .andExpect(jsonPath("$.details.priority").exists());

        mockMvc.perform(put(TASKS_URL + "/1")
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","description":null,"status":"PENDING"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.priority").exists());

        mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Valid title","dueDate":"not-a-date"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body."));

        mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ malformed"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    @Test
    void paginationSortingSearchingAndStatusFilteringAreDatabaseBackedAndOwnerScoped()
            throws Exception {
        List<Task> aliceTasks = new ArrayList<>();
        for (int index = 0; index < 12; index++) {
            TaskStatus status = switch (index % 3) {
                case 0 -> TaskStatus.PENDING;
                case 1 -> TaskStatus.IN_PROGRESS;
                default -> TaskStatus.COMPLETED;
            };
            aliceTasks.add(new Task(
                    "Assignment " + String.format("%02d", index),
                    "Alice task " + index,
                    status,
                    LocalDate.of(2026, 12, 1).plusDays(index),
                    userA
            ));
        }
        taskRepository.saveAllAndFlush(aliceTasks);
        taskRepository.saveAndFlush(new Task(
                "Assignment from Bob", "Private Bob task", TaskStatus.PENDING,
                LocalDate.of(2026, 12, 2), userB
        ));

        mockMvc.perform(get(TASKS_URL)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "title")
                        .param("direction", "asc")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].title").value("Assignment 00"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get(TASKS_URL)
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "title")
                        .param("direction", "asc")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].title").value("Assignment 05"));

        String createdAtDescending = mockMvc.perform(get(TASKS_URL)
                        .param("size", "12")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode sortedContent = objectMapper.readTree(createdAtDescending).get("content");
        assertThat(sortedContent.get(0).get("createdAt").asText())
                .isGreaterThanOrEqualTo(sortedContent.get(1).get("createdAt").asText());

        mockMvc.perform(get(TASKS_URL)
                        .param("page", "5")
                        .param("size", "5")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.empty").value(true));

        mockMvc.perform(get(TASKS_URL)
                        .param("search", "  aSsIgNmEnT  ")
                        .param("status", "PENDING")
                        .param("sortBy", "title")
                        .param("direction", "asc")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].title").value("Assignment 00"));

        for (String taskStatus : List.of("PENDING", "IN_PROGRESS", "COMPLETED")) {
            mockMvc.perform(get(TASKS_URL)
                            .param("status", taskStatus)
                            .header("Authorization", bearer(tokenA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(4));
        }

        mockMvc.perform(get(TASKS_URL)
                        .param("search", "not present")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get(TASKS_URL)
                        .param("search", "   ")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(12));

        mockMvc.perform(get(TASKS_URL)
                        .param("status", "COMPLETED")
                        .param("dueFrom", "2026-12-07")
                        .param("dueTo", "2026-12-12")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get(TASKS_URL)
                        .param("search", "assignment")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Assignment from Bob"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get(TASKS_URL)
                        .param("search", "assignment")
                        .param("status", "PENDING")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void idTitleAndDueDateSortingWorkInBothDirectionsThroughHttp() throws Exception {
        long zuluId = createTask("Zulu", TaskPriority.MEDIUM, "2026-03-03");
        long alphaId = createTask("Alpha", TaskPriority.MEDIUM, "2026-01-01");
        long middleId = createTask("Middle", TaskPriority.MEDIUM, "2026-02-02");

        assertThat(sortedFieldValues("id", "asc", "id"))
                .containsExactly(
                        Long.toString(zuluId),
                        Long.toString(alphaId),
                        Long.toString(middleId)
                );
        assertThat(sortedFieldValues("id", "desc", "id"))
                .containsExactly(
                        Long.toString(middleId),
                        Long.toString(alphaId),
                        Long.toString(zuluId)
                );
        assertThat(sortedFieldValues("title", "asc", "title"))
                .containsExactly("Alpha", "Middle", "Zulu");
        assertThat(sortedFieldValues("title", "desc", "title"))
                .containsExactly("Zulu", "Middle", "Alpha");
        assertThat(sortedFieldValues("dueDate", "asc", "dueDate"))
                .containsExactly("2026-01-01", "2026-02-02", "2026-03-03");
        assertThat(sortedFieldValues("dueDate", "desc", "dueDate"))
                .containsExactly("2026-03-03", "2026-02-02", "2026-01-01");
    }

    @Test
    void priorityIsReturnedUpdatedFilteredSortedAndOwnerScoped() throws Exception {
        List<Long> aliceTaskIds = new ArrayList<>();
        for (TaskPriority priority : TaskPriority.values()) {
            aliceTaskIds.add(createTask(
                    "priority-match " + priority.name(), priority, "2026-12-10"
            ));
        }
        aliceTaskIds.add(createTask("priority-match HIGH second", TaskPriority.HIGH, "2026-12-20"));
        taskRepository.saveAndFlush(new Task(
                "Bob high priority task", null, TaskStatus.PENDING, TaskPriority.HIGH,
                LocalDate.of(2026, 12, 10), userB
        ));

        mockMvc.perform(get(TASKS_URL)
                        .param("priority", "HIGH")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"));
        for (TaskPriority priority : List.of(
                TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.URGENT
        )) {
            mockMvc.perform(get(TASKS_URL)
                            .param("priority", priority.name())
                            .header("Authorization", bearer(tokenA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].priority").value(priority.name()));
        }

        mockMvc.perform(get(TASKS_URL)
                        .param("priority", "HIGH")
                        .param("status", "PENDING")
                        .param("search", "priority-match")
                        .param("dueFrom", "2026-12-10")
                        .param("dueTo", "2026-12-10")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("priority-match HIGH"));

        mockMvc.perform(get(TASKS_URL)
                        .param("priority", "HIGH")
                        .param("size", "1")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get(TASKS_URL)
                        .param("sortBy", "priority")
                        .param("direction", "asc")
                        .param("size", "10")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[1].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[2].priority").value("LOW"))
                .andExpect(jsonPath("$.content[3].priority").value("MEDIUM"))
                .andExpect(jsonPath("$.content[4].priority").value("URGENT"));

        mockMvc.perform(get(TASKS_URL)
                        .param("priority", "HIGH")
                        .header("Authorization", bearer(tokenB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Bob high priority task"));

        long updatedTaskId = aliceTaskIds.getFirst();
        mockMvc.perform(put(TASKS_URL + "/" + updatedTaskId)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"priority-match HIGH","description":null,
                                 "status":"PENDING","priority":"URGENT","dueDate":"2026-12-10"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("URGENT"));

        mockMvc.perform(get(TASKS_URL)
                        .param("priority", "NOT_A_PRIORITY")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Invalid priority","priority":"NOT_A_PRIORITY"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body."));
    }

    @Test
    void listingRejectsInvalidPaginationSortingFiltersAndDateRanges() throws Exception {
        for (String[] parameters : List.of(
                new String[]{"sortBy", "user.passwordHash"},
                new String[]{"direction", "sideways"},
                new String[]{"status", "INVALID"},
                new String[]{"page", "-1"},
                new String[]{"size", "101"},
                new String[]{"dueFrom", "2026-12-31"}
        )) {
            var request = get(TASKS_URL).param(parameters[0], parameters[1])
                    .header("Authorization", bearer(tokenA));
            if ("dueFrom".equals(parameters[0])) {
                request.param("dueTo", "2026-12-01");
            }
            mockMvc.perform(request).andExpect(status().isBadRequest());
        }

        mockMvc.perform(get(TASKS_URL)
                        .param("size", "0")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(TASKS_URL)
                        .param("dueFrom", "2026-12-31")
                        .param("dueTo", "2026-12-01")
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void preflightAllowsTaskUpdateAndDeleteMethods() throws Exception {
        mockMvc.perform(options(TASKS_URL + "/1")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "PUT"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Methods",
                        containsString("PUT")
                ));

        mockMvc.perform(options(TASKS_URL + "/1")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Methods",
                        containsString("DELETE")
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankTitlesAreRejected(String title) throws Exception {
        mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(title)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.title").exists());
    }

    private User createUser(String name, String email) {
        return userRepository.saveAndFlush(
                new User(name, email, passwordEncoder.encode("StrongPassword123!"))
        );
    }

    private long createTask(String title, TaskPriority priority, String dueDate) throws Exception {
        String response = mockMvc.perform(post(TASKS_URL)
                        .header("Authorization", bearer(tokenA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", title,
                                "priority", priority.name(),
                                "dueDate", dueDate
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value(priority.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private List<String> sortedFieldValues(String sortBy, String direction, String field)
            throws Exception {
        String response = mockMvc.perform(get(TASKS_URL)
                        .param("size", "10")
                        .param("sortBy", sortBy)
                        .param("direction", direction)
                        .header("Authorization", bearer(tokenA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<String> values = new ArrayList<>();
        objectMapper.readTree(response).path("content")
                .forEach(task -> values.add(task.path(field).asText()));
        return values;
    }

    private String createBody(String title) throws Exception {
        return objectMapper.writeValueAsString(
                java.util.Map.of("title", title, "description", "description")
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
