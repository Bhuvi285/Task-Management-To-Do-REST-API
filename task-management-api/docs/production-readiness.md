# Phase 6.8 — Architecture and Production Readiness Audit

This document records the final architecture review and operational findings for the Task Management REST API. It describes the implementation and its remaining deployment limits; it is not a certification that the application is completely production-secure.

## Architecture review

- `TaskManagementApplication` is the Spring Boot entry point. HTTP controllers are separated from service logic, repositories, persistence entities, DTOs, security components, and configuration.
- `AuthController` and `TaskController` delegate to services; controllers do not access repositories directly. Services own transaction boundaries for multi-step persistence and token operations.
- Request/response DTOs are used at API boundaries. Task responses map from entities; ownership is derived from the authenticated principal and every task query/count is constrained by owner ID. Open EntityManager in View is disabled to keep persistence access within service transactions.
- `Task.user`, token-user, and OAuth-account-user relationships are lazy `ManyToOne` associations. The API does not expose entity relationships as JSON.
- Flyway migrations V1–V3 create the relational schema, priority constraint, and owner/default-sort task index. Hibernate schema mode remains `validate`; Flyway clean remains disabled.

## Security review

- Local passwords use BCrypt. JWTs use HS256 with a Base64 key of at least 32 bytes and validate signature, issuer, expiration, issue time, and subject. The bearer filter reloads the account and rejects disabled accounts.
- Refresh tokens are opaque, stored as hashes, rotated transactionally with row locking, and grouped into revocable families. Password changes and resets revoke active refresh sessions.
- OAuth state uses the temporary session; enabled providers require credentials and exact non-wildcard redirect URIs. Provider account identity is unique and email-only auto-linking is rejected. Callback cleanup retains the existing JSON token contract.
- The API is stateless for bearer authentication, disables CSRF for API routes, has explicit CORS origins, denies framing, sets content-type/referrer protections, and emits HSTS on secure requests. OAuth itself uses a short-lived session for state.
- Password-reset token material is hashed in storage. The production SMTP adapter sends an HTTPS reset link asynchronously after the database transaction commits; the API keeps generic responses and does not log tokens. Production refuses to start without enabled reset delivery, SMTP authentication/required STARTTLS, and an HTTPS reset URL.
- Actuator exposes only `health`. Public liveness/readiness routes expose status only; readiness includes the database health indicator. Other actuator endpoints are not exposed.

## Production and deployment readiness

- `prod` requires PostgreSQL URL/credentials, a valid JWT key, explicit CORS origins, and externalized SMTP credentials. Google/GitHub credentials remain conditional on enabling the respective provider.
- OpenAPI is disabled by default in production. Hibernate automatic schema update and Flyway clean are disabled.
- `Dockerfile` builds and tests with Maven/Java 21, then runs the jar under an unprivileged user. `compose.yaml` adds PostgreSQL 16 with a persistent volume, no published database port, health checks, an internal network, and environment-sourced credentials.
- `.env.example` has placeholders only; `.gitignore` excludes local environment and secret files. Production operators should use a secret manager, TLS-terminating trusted proxy, restricted ingress, monitored database backups, and tested restore procedures.
- `.github/workflows/ci.yml` runs Java 21 standard tests and an explicit Testcontainers PostgreSQL 16 test run, then uploads Surefire reports. CI needs no real OAuth or SMTP credentials.

## Findings and accepted limitations

- Before this phase, the README described only a Phase 1 sample, there was no application Docker/Compose configuration, CI workflow, Actuator health exposure, or local environment template/ignore rule. These release-documentation and deployment gaps are addressed in this phase.
- The existing password-reset delivery type was only an abstraction; production had no email adapter. The new production-only SMTP adapter uses external settings and keeps development/test behavior unchanged.
- Login and password-reset throttles use bounded in-memory buckets. They are per instance, reset on restart, and do not provide a shared security limit in a multi-instance deployment. Until a deliberately designed shared limiter or edge control is deployed, document and operate this API as a single-instance rate-limited service.
- Testcontainers is configured for `postgres:16-alpine` and the test checks migration history, schema validation at context startup, task persistence/query behavior, OAuth-account persistence, and registration/login/refresh rotation. Local PostgreSQL verification requires a working Docker engine; a skipped test is not a pass.
- The k6 harness is separate from functional tests. It avoids timing thresholds and attempts task deletion even when an iteration fails, but load-test results depend on the target environment and are not represented as measured until k6 is actually run.
- Provider redirects, live SMTP delivery, TLS/proxy configuration, database backups, horizontal scaling, and production load remain operator/deployment verification items. Do not treat successful H2 tests or CI configuration as evidence for those items.

## Reproducible verification commands

From the `task-management-api` directory, run the ordinary suite and the PostgreSQL opt-in test separately:

```powershell
.\mvnw.cmd test
.\mvnw.cmd '-DrunPostgresIntegration=true' '-Dtest=PostgreSqlIntegrationTest' test
```

Run the optional performance script only against an explicitly authorized non-production instance with a disposable account; see the commands and setup requirements in the root README.

## Verification from this implementation

- Standard Maven/H2 suite: 163 tests, 0 failures, 0 errors, 4 skipped. Flyway V1–V3 applied under H2; this is not PostgreSQL verification.
- PostgreSQL Testcontainers execution was attempted with `-DrunPostgresIntegration=true`. It could not start because Docker reported that `npipe:////./pipe/dockerDesktopLinuxEngine` does not exist; PostgreSQL 16 remains unverified locally. Run the exact command above after starting Docker Desktop.
- `docker compose config --quiet` succeeded with `.env.example`. The Docker image was not built or started because the Docker engine is unavailable.
- k6 was not installed (`Get-Command k6` returned no command); no load run or latency/throughput measurements are claimed. Windows installation: `winget install k6 --source winget`.
- A separate Spring Boot process launch was blocked before startup by Windows `CreateProcess` error 225. The liveness/readiness routes were exercised through MockMvc integration tests instead.
- `git diff --check` exited successfully; Git emitted only LF-to-CRLF conversion notices. No files were staged or committed. Maven refreshed generated `target/` outputs; those outputs were left in place.