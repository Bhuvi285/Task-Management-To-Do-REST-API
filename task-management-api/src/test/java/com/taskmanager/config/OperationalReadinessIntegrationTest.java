package com.taskmanager.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "management.endpoint.health.probes.enabled=true",
    "management.endpoint.health.group.liveness.include=livenessState",
    "management.endpoint.health.group.readiness.include=readinessState,db",
    "management.endpoint.health.show-details=never",
    "management.endpoint.health.show-components=never"
})
@AutoConfigureMockMvc
class OperationalReadinessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HealthEndpointGroups healthEndpointGroups;

    @Test
    void livenessAndDatabaseReadinessArePublicAndExposeOnlyStatus() throws Exception {
        assertThat(healthEndpointGroups.getNames())
            .contains("liveness", "readiness");

        mockMvc.perform(get("/actuator/health/liveness"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(
                MediaType.valueOf("application/vnd.spring-boot.actuator.v3+json")
            ))
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components").doesNotExist());

        mockMvc.perform(get("/actuator/health/readiness"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void nonHealthManagementEndpointsAreNotExposedPublicly() throws Exception {
        mockMvc.perform(get("/actuator/env"))
            .andExpect(status().isUnauthorized());
    }
}