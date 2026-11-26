package com.taskmanager.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void applicationContextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testEndpointIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/test"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
    }

    @Test
    void hstsIsAppliedToSecureRequestsButNotLocalHttpRequests() throws Exception {
        mockMvc.perform(get("/api/v1/test").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Strict-Transport-Security",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("max-age=31536000"),
                                org.hamcrest.Matchers.containsString("includeSubDomains")
                        )
                ));

        mockMvc.perform(get("/api/v1/test").secure(false))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
    }

    @Test
    void otherEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void oauthStateSessionCookieHasSecureDefaultsForRestApi() {
        assertThat(environment.getProperty("server.servlet.session.cookie.http-only"))
                .isEqualTo("true");
        assertThat(environment.getProperty("server.servlet.session.cookie.secure"))
                .isEqualTo("false");
        assertThat(environment.getProperty("server.servlet.session.cookie.same-site"))
                .isEqualTo("lax");
        assertThat(environment.getProperty("server.servlet.session.timeout"))
                .isEqualTo("10m");
    }

    @Test
    void corsAllowsOnlyTheConfiguredFrontendWithoutCredentials() {
        CorsConfiguration cors = corsConfigurationSource.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/v1/test")
        );

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:3000");
        assertThat(cors.getAllowedOrigins()).doesNotContain("*");
        assertThat(cors.getAllowedMethods())
                .containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders())
                .containsExactly("Authorization", "Content-Type");
        assertThat(cors.getAllowCredentials()).isFalse();
    }
}
