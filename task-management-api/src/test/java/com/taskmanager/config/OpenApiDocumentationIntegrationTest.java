package com.taskmanager.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void openApiDocumentIncludesMetadataSecurityAndExistingEndpoints() throws Exception {
        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode document = objectMapper.readTree(response);

        assertThat(document.path("info").path("title").asText())
                .isEqualTo("Task Management REST API");
        assertThat(document.path("info").path("version").asText())
                .isEqualTo("1.0.0");
        assertThat(document.path("components").path("securitySchemes")
                .path("bearerAuth").path("type").asText()).isEqualTo("http");
        assertThat(document.path("components").path("securitySchemes")
                .path("bearerAuth").path("scheme").asText()).isEqualTo("bearer");
        assertThat(document.path("components").path("securitySchemes")
                .path("bearerAuth").path("bearerFormat").asText()).isEqualTo("JWT");

        JsonNode paths = document.path("paths");
        assertThat(paths.has("/api/v1/auth/register")).isTrue();
        assertThat(paths.has("/api/v1/auth/login")).isTrue();
        assertThat(paths.has("/api/v1/auth/refresh")).isTrue();
        assertThat(paths.has("/api/v1/auth/logout")).isTrue();
        assertThat(paths.has("/api/v1/auth/forgot-password")).isTrue();
        assertThat(paths.has("/api/v1/auth/reset-password")).isTrue();
        assertThat(paths.has("/api/v1/auth/change-password")).isTrue();
        assertThat(paths.has("/api/v1/test")).isTrue();
        assertThat(paths.has("/api/v1/tasks")).isTrue();
        assertThat(paths.has("/api/v1/tasks/{taskId}")).isTrue();

        assertThat(paths.path("/api/v1/auth/register").path("post").has("security"))
                .isFalse();
        assertThat(paths.path("/api/v1/auth/login").path("post").has("security"))
                .isFalse();
        assertThat(paths.path("/api/v1/auth/refresh").path("post").has("security"))
                .isFalse();
        assertThat(paths.path("/api/v1/auth/logout").path("post").has("security"))
                .isFalse();
        assertThat(paths.path("/api/v1/auth/change-password").path("post")
                .path("security").get(0).has("bearerAuth")).isTrue();
        assertThat(paths.path("/api/v1/test").path("get").has("security")).isFalse();
        assertThat(paths.path("/api/v1/tasks").path("get")
                .path("security").get(0).has("bearerAuth")).isTrue();
        assertThat(paths.path("/api/v1/tasks").path("get").path("responses").has("400"))
                .isTrue();
        assertThat(paths.path("/api/v1/tasks").path("get").path("responses").has("401"))
                .isTrue();
        assertThat(paths.path("/api/v1/tasks").path("get").path("responses").has("403"))
                .isTrue();
        assertThat(paths.path("/api/v1/auth/register").path("post")
                .path("responses").path("202").path("content")
                .path("application/json").path("schema").path("$ref").asText())
                .as("registration response: %s", paths.path("/api/v1/auth/register")
                        .path("post").path("responses").toPrettyString())
                .endsWith("/AuthMessageResponse");
        assertThat(paths.path("/api/v1/auth/login").path("post")
                .path("responses").path("200").path("content")
                .path("application/json").path("schema").path("$ref").asText())
                .endsWith("/LoginResponse");
        assertThat(paths.path("/api/v1/tasks").path("post")
                .path("responses").path("201").path("content")
                .path("application/json").path("schema").path("$ref").asText())
                .endsWith("/TaskResponse");
        assertThat(paths.path("/api/v1/tasks").path("get").path("parameters")
                .findValuesAsText("name"))
                .contains("page", "size", "sortBy", "direction", "search", "status",
                        "priority", "dueFrom", "dueTo");
        JsonNode sortBySchema = null;
        for (JsonNode parameter : paths.path("/api/v1/tasks").path("get").path("parameters")) {
            if ("sortBy".equals(parameter.path("name").asText())) {
                sortBySchema = parameter.path("schema");
                break;
            }
        }
        assertThat(sortBySchema).isNotNull();
        assertThat(sortBySchema.path("enum").toString())
                .contains("\"id\"", "\"title\"", "\"dueDate\"");

        JsonNode schemas = document.path("components").path("schemas");
        assertThat(schemas.has("CreateTaskRequest")).isTrue();
        assertThat(schemas.has("TaskResponse")).isTrue();
        assertThat(schemas.has("TaskPageResponse")).isTrue();
        assertThat(schemas.has("ApiErrorResponse")).isTrue();
        assertThat(schemas.path("UpdateTaskRequest").path("properties")
                .path("priority").path("enum").toString())
                .contains("LOW", "MEDIUM", "HIGH", "URGENT");
        assertThat(schemas.path("UpdateTaskRequest").path("properties")
                .path("status").path("enum").toString())
                .contains("PENDING", "IN_PROGRESS", "COMPLETED");
        assertThat(schemas.path("RegistrationRequest").path("properties")
                .path("password").path("writeOnly").asBoolean()).isTrue();
        assertThat(schemas.path("LoginRequest").path("properties")
                .path("password").path("writeOnly").asBoolean()).isTrue();
        assertThat(schemas.path("TaskResponse").path("properties").has("user")).isFalse();
        assertThat(schemas.path("ApiErrorResponse").path("properties").has("status")).isTrue();
        assertThat(schemas.path("ApiErrorResponse").path("properties").has("message")).isTrue();
    }

    @Test
    void swaggerUiIsAvailableInDevelopmentConfiguration() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
