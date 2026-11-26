package com.taskmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagementOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Task Management REST API")
                .description("REST API for user accounts, authentication, and owner-scoped task management. "
                    + "Use the JWT access token in the Bearer Authorize dialog; do not use a refresh token. "
                    + "Google and GitHub sign-in use browser-based OAuth2 redirects, not JSON API calls.")
                .version("1.0.0"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT access token only. Do not enter a refresh token.")));
    }
}
