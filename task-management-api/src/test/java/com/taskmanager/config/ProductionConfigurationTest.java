package com.taskmanager.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionConfigurationTest {

    private final ApplicationContextRunner validatorRunner = new ApplicationContextRunner()
            .withUserConfiguration(ValidatorTestConfiguration.class)
            .withPropertyValues(
                    "spring.profiles.active=prod",
                    "app.security.password-reset-email.enabled=true",
                    "app.security.password-reset-email.from=no-reply@example.test",
                    "app.security.password-reset-email.reset-url=https://app.example.test/reset-password",
                    "spring.mail.host=smtp.example.test",
                    "spring.mail.port=587",
                    "spring.mail.username=test-user",
                    "spring.mail.password=test-only-mail-password",
                    "spring.mail.properties.mail.smtp.auth=true",
                    "spring.mail.properties.mail.smtp.starttls.enable=true",
                    "spring.mail.properties.mail.smtp.starttls.required=true"
            );

    @Test
    void productionStartupRequiresNonBlankDatabaseSettings() {
        validatorRunner
                .withPropertyValues(
                        "spring.datasource.url=",
                        "spring.datasource.username=taskmanager",
                        "spring.datasource.password=test-only-password"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("DATABASE_URL");
                });

        validatorRunner
                .withPropertyValues(
                        "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                        "spring.datasource.username=",
                        "spring.datasource.password="
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("DATABASE_USERNAME");
                });

        validatorRunner
                .withPropertyValues(
                        "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                        "spring.datasource.username=taskmanager",
                        "spring.datasource.password="
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("DATABASE_PASSWORD");
                });
    }

    @Test
    void productionStartupRejectsNonPostgresDatabaseUrl() {
        validatorRunner
                .withPropertyValues(
                        "spring.datasource.url=jdbc:h2:mem:production",
                        "spring.datasource.username=taskmanager",
                        "spring.datasource.password=not-a-real-password"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining(
                                    "requires DATABASE_URL to use the PostgreSQL JDBC driver"
                            );
                    assertThat(context.getStartupFailure().getMessage())
                            .doesNotContain("not-a-real-password");
                });
    }

    @Test
    void productionDatabaseSettingsAreAcceptedWithoutExposingCredentials() {
        validatorRunner
                .withPropertyValues(
                        "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                        "spring.datasource.username=taskmanager",
                        "spring.datasource.password=test-only-password"
                )
            .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void productionRequiresConfiguredSecurePasswordResetDelivery() {
        validatorRunner
            .withPropertyValues(
                "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                "spring.datasource.username=taskmanager",
                "spring.datasource.password=test-only-password",
                "app.security.password-reset-email.enabled=false"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining("PASSWORD_RESET_EMAIL_ENABLED=true");
            });

        validatorRunner
            .withPropertyValues(
                "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                "spring.datasource.username=taskmanager",
                "spring.datasource.password=test-only-password",
                "spring.mail.password="
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining("SMTP_PASSWORD");
                assertThat(context.getStartupFailure().getMessage())
                    .doesNotContain("test-only-mail-password");
            });

        validatorRunner
            .withPropertyValues(
                "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                "spring.datasource.username=taskmanager",
                "spring.datasource.password=test-only-password",
                "app.security.password-reset-email.reset-url=http://app.example.test/reset-password"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining("PASSWORD_RESET_RESET_URL");
            });

        validatorRunner
            .withPropertyValues(
                "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                "spring.datasource.username=taskmanager",
                "spring.datasource.password=test-only-password",
                "spring.mail.properties.mail.smtp.starttls.enable=false"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining("SMTP STARTTLS");
            });

        validatorRunner
            .withPropertyValues(
                "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                "spring.datasource.username=taskmanager",
                "spring.datasource.password=test-only-password",
                "spring.mail.port=70000"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining("SMTP_PORT");
            });
    }

    @Test
    void productionCannotBeCombinedWithTheDevelopmentProfile() {
        validatorRunner
            .withPropertyValues("spring.profiles.active=prod,dev")
            .withPropertyValues(
                "spring.datasource.url=jdbc:postgresql://localhost:5432/tasks",
                "spring.datasource.username=taskmanager",
                "spring.datasource.password=test-only-password"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasStackTraceContaining("cannot be combined with the dev profile");
            });
    }

    @Test
    void persistentProfilesUseFlywayAndHibernateValidation() {
        assertProfileConfiguration("postgres", "jdbc:postgresql://localhost:5432/tasks", environment -> {
            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://localhost:5432/tasks");
            assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
                    .isEqualTo("validate");
            assertThat(environment.getProperty("spring.flyway.enabled"))
                    .isEqualTo("true");
            assertThat(environment.getProperty("spring.jpa.show-sql"))
                    .isEqualTo("false");
        });

        assertProfileConfiguration("prod", "jdbc:postgresql://localhost:5432/tasks", environment -> {
            assertThat(environment.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://localhost:5432/tasks");
            assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
                    .isEqualTo("validate");
            assertThat(environment.getProperty("spring.flyway.enabled"))
                    .isEqualTo("true");
            assertThat(environment.getProperty("spring.flyway.baseline-on-migrate"))
                    .isEqualTo("false");
            assertThat(environment.getProperty("spring.flyway.clean-disabled"))
                    .isEqualTo("true");
            assertThat(environment.getProperty("spring.jpa.show-sql"))
                    .isEqualTo("false");
            assertThat(environment.getProperty("app.security.allowed-origins"))
                    .isEqualTo("https://app.example.test");
            assertThat(environment.getProperty("server.servlet.session.cookie.secure"))
                    .isEqualTo("true");
            assertThat(environment.getProperty("server.servlet.session.cookie.http-only"))
                    .isEqualTo("true");
            assertThat(environment.getProperty("server.servlet.session.cookie.same-site"))
                    .isEqualTo("lax");
            assertThat(environment.getProperty("app.security.password-reset-email.enabled"))
                    .isEqualTo("false");
        });

        assertProfileConfiguration("dev", "jdbc:h2:mem:taskmanagerdb", environment -> {
            assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
                    .isEqualTo("validate");
            assertThat(environment.getProperty("spring.flyway.enabled"))
                    .isEqualTo("true");
            assertThat(environment.getProperty("spring.jpa.show-sql"))
                    .isEqualTo("true");
            assertThat(environment.getProperty("spring.h2.console.enabled"))
                    .isEqualTo("true");
        });
    }

    private void assertProfileConfiguration(
            String profile,
            String databaseUrl,
            java.util.function.Consumer<ConfigurableEnvironment> assertions
    ) {
        new ApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues(
                        "spring.profiles.active=" + profile,
                        "spring.datasource.url=" + databaseUrl,
                        "spring.datasource.username=taskmanager",
                        "spring.datasource.password=test-only-password",
                        "app.jwt.secret=dGVzdC1vbmx5LXNpZ25pbmctc2VjcmV0LWZvci1qd3QtMzJieXRlcyE=",
                        "app.security.allowed-origins=https://app.example.test"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertions.accept(context.getEnvironment());
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(DataSourceProperties.class)
    @Import(ProductionDatabaseConfigurationValidator.class)
    static class ValidatorTestConfiguration {
    }
}
