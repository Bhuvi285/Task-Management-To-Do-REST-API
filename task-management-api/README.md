# Task Management REST API

A Java 21 / Spring Boot REST API for user-owned task management and local or social authentication. It provides stateless JWT-protected task endpoints, rotating opaque refresh-token sessions, password recovery, PostgreSQL production support, Flyway schema management, OpenAPI documentation, and H2-backed tests.

## Features

- Local account registration, login, BCrypt password hashing, password change, and password reset.
- Short-lived signed JWT access tokens and opaque refresh tokens stored as hashes; refresh rotation detects reuse and revokes the token family.
- Optional Google OIDC and GitHub OAuth2 login. Provider credentials are required only when that provider is enabled.
- User-scoped task create/read/update/delete, status and priority, due dates, title search, filters, sorting, and bounded pagination.
- DTO-based HTTP requests and responses, request validation, consistent API error responses, CORS allowlisting, and security headers.
- H2 for local development and ordinary automated tests; PostgreSQL 16 for production and opt-in database integration verification.
- Flyway migrations, configurable HikariCP, OpenAPI/Swagger UI, restricted Actuator health probes, Docker Compose, and GitHub Actions CI.

## Technology

- Java 21; Spring Boot 3.5.7; Maven wrapper.
- Spring MVC, Spring Security, Spring Data JPA, Hibernate, Bean Validation, and Spring Boot Actuator.
- PostgreSQL JDBC, H2, Flyway, HikariCP, JJWT 0.12.6, and Springdoc OpenAPI 2.8.17.
- JUnit 5, Spring Boot Test, MockMvc, Spring Security Test, Mockito, and Testcontainers PostgreSQL.

## Architecture and packages

The application entry point is `com.taskmanager.TaskManagementApplication`. The code follows a layered structure:

```text
com.taskmanager
├── config       # security, JWT/OAuth, OpenAPI, production and mail configuration
├── controller   # AuthController, TaskController, and the sample TestController
├── dto          # request/response records and safe API error response
├── entity       # User, Task, OAuthAccount, refresh/reset token persistence models
├── enums        # task status, task priority, and OAuth provider
├── exception    # API exception handlers and security response handlers
├── repository   # Spring Data JPA repositories and owner-scoped task queries
├── security     # JWT filter/principal, OAuth handlers, and reset-token delivery
├── service      # authentication, token lifecycle, task and social-account logic
└── validation   # request constraints
```

Controllers validate and translate HTTP requests to DTO responses; they delegate business operations to services and do not access repositories directly. Services own transaction boundaries and business rules. Repositories perform database work, and task queries include the authenticated owner ID. JPA entities are not serialized as API responses; task responses are mapped to DTOs. The `Task.user` association is lazy, and Open EntityManager in View is disabled so web response mapping cannot trigger unplanned lazy queries.

## Authentication flows

### Local JWT and refresh-token flow

1. Register with email, display name, and password. The response is generic and does not reveal whether an email already existed.
2. Log in with email/password. The API returns a signed HS256 access token (default 15 minutes) and a high-entropy opaque refresh token (default 30 days).
3. Send `Authorization: Bearer <access-token>` to protected endpoints. JWT signature, issuer, expiration, issue time, and subject are validated; the account is reloaded and must be enabled.
4. Submit a refresh token to `/api/v1/auth/refresh`. The database stores only its hash; successful refresh rotates it. Reuse of a rotated token revokes active tokens in that family.
5. Logout revokes the submitted refresh-token family. Password change/reset also revokes active refresh sessions.

`JWT_SECRET` must be Base64 that decodes to at least 32 cryptographically random bytes. The issuer defaults to `task-management-api`. Access and refresh expiration values are ISO-8601 durations.

### Google OIDC and GitHub OAuth2

OAuth login starts at Spring Security's `/oauth2/authorization/google` or `/oauth2/authorization/github` route when that provider is enabled. Authorization state is stored in the temporary HTTP session and validated on callback. The provider user service requires a verified usable email. External accounts are uniquely identified by provider plus provider user ID; an existing local account is not silently linked based only on matching email. Successful and failed callbacks clean up the temporary session/cookie and retain the established JSON token response contract. No real provider credentials are needed by automated tests.

Production OAuth applications must register the exact callback URI configured by `GOOGLE_REDIRECT_URI` / `GITHUB_REDIRECT_URI`, normally `https://<api-host>/login/oauth2/code/google` or `/github`. Do not enable a provider without its client ID, secret, and registered redirect URI.

### Password recovery

Reset tokens are high-entropy, stored hashed, time-limited, and single-use; forgot-password responses are generic. Production now uses the `PasswordResetTokenDelivery` abstraction with an SMTP adapter enabled by `PASSWORD_RESET_EMAIL_ENABLED=true`. SMTP credentials are external configuration, STARTTLS is required, and the reset link is sent only by email—not in the API response or logs. Local/test profiles do not send email unless a test delivery is supplied.

## REST API

All API routes use `/api/v1`.

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Public | Register; generic `202` response |
| `POST` | `/auth/login` | Public | Authenticate and issue tokens |
| `POST` | `/auth/refresh` | Public | Rotate a refresh token |
| `POST` | `/auth/logout` | Public | Revoke the supplied refresh-token family |
| `POST` | `/auth/change-password` | JWT | Change the authenticated user's password |
| `POST` | `/auth/forgot-password` | Public | Request reset email; generic response |
| `POST` | `/auth/reset-password` | Public | Exchange a valid reset token for a new password |
| `POST` | `/tasks` | JWT | Create task; `201` includes a `Location` header |
| `GET` | `/tasks` | JWT | List the authenticated user's tasks |
| `GET` | `/tasks/{taskId}` | JWT | Read an owned task |
| `PUT` | `/tasks/{taskId}` | JWT | Update an owned task |
| `DELETE` | `/tasks/{taskId}` | JWT | Delete an owned task (`204`) |
| `GET` | `/test` | Public | Sample endpoint |

Task owner identity comes from the authenticated principal, never a request-supplied user ID. Cross-user task lookup/update/delete retains the not-found behavior. List queries, counts, filters, and pagination remain owner-scoped.

### Pagination, sorting, search, and filters

`GET /api/v1/tasks` supports `page` (zero-based, default `0`), `size` (default `10`, maximum `100`), `sortBy`, `direction`, `search`, `status`, `priority`, `dueFrom`, and `dueTo`. Allowed sort fields are `id`, `createdAt`, `updatedAt`, `dueDate`, `title`, `status`, and `priority`; directions are `asc` and `desc`. Sorting adds an ascending ID tie-breaker. Search is case-insensitive title matching; date bounds are inclusive ISO dates and `dueFrom` must not exceed `dueTo`. The response contains task `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, `last`, and `empty`.

## Data model and migrations

- `users`: unique email, enabled flag, BCrypt password hash, and creation time.
- `tasks`: owner foreign key, title/description, status, priority, due date, and timestamps.
- `oauth_accounts`: provider/account identity with a unique provider-user constraint.
- `refresh_tokens`: unique hashed token, family, expiration/revocation state, and replacement link.
- `password_reset_tokens`: unique hashed token, expiration, and single-use timestamp.

Flyway owns schema creation; Hibernate uses `ddl-auto=validate`, not automatic production updates. Migrations are append-only:

1. `V1__create_initial_schema.sql`: core account, task, social-account, password-reset, and refresh-token tables and constraints.
2. `V2__add_task_priority.sql`: priority column, backfill, default, not-null, and allowed-value constraint.
3. `V3__index_tasks_by_owner_and_creation.sql`: composite index `(user_id, created_at DESC, id ASC)` for the common owner/default-order task list.

Never edit applied migrations. `spring.flyway.clean-disabled=true` is retained.

## Prerequisites and local H2 development

- JDK 21 (verify with `java -version`).
- Windows PowerShell: use `.\mvnw.cmd`; Linux/macOS: use `./mvnw`. A separate Maven installation is not required.
- Docker Desktop is needed only for Compose and the opt-in PostgreSQL Testcontainers verification.
- k6 is optional and used only for the separate performance script.

The default profile uses an in-memory H2 database. A valid JWT key is required even for local runs. Generate a temporary key in PowerShell and start the application:

```powershell
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

The `dev` profile enables the H2 console and SQL logging for local development only. Do not use it in production or combine it with `prod`. The default server port is `8080`. Use `http://localhost:8080/api/v1/test` to check the sample public route.

Run the ordinary H2 test suite:

```powershell
.\mvnw.cmd test
```

## PostgreSQL and Docker deployment

`application-postgres.properties` connects to an externally managed PostgreSQL database using `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`. `application-prod.properties` additionally requires PostgreSQL, explicit CORS origins, a valid JWT secret, secure session cookies, disabled OpenAPI by default, Hibernate validation, Flyway, and configured SMTP password-reset delivery.

The production-oriented Docker Compose setup runs the API and PostgreSQL 16 on an internal network; PostgreSQL has no published host port and uses a persistent volume. The API image is multi-stage, runs as a non-root user, has a read-only root filesystem, and checks readiness. Compose passes secrets from the environment; it does not contain real credentials.

```powershell
Copy-Item .env.example .env
# Edit .env and replace every REPLACE_* value with private deployment values.
docker compose up --build
```

Do not commit `.env`; it is ignored. For real deployments, source secrets from a managed secret store rather than a checked-in file or long-lived shell history. Compose is a single-host baseline, not a substitute for production secret management, TLS termination, backups, network policy, monitoring, or a highly available database.

To build the image alone from this directory, run `docker build -t task-management-api:local .`. The image build executes Maven tests. Docker Compose and image builds require a running Docker engine.

## Production environment variables

`.env.example` contains placeholders only. The table lists names used by the application; production startup fails when required database, password-reset SMTP, or JWT configuration is absent/invalid.

| Area | Variables | Notes |
| --- | --- | --- |
| Runtime | `SPRING_PROFILES_ACTIVE`, `SERVER_PORT` | Use `prod`; Compose listens on container port 8080. |
| Database | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `POSTGRES_DB` | Production JDBC URL must use `jdbc:postgresql:`; `POSTGRES_DB` is for the Compose service. |
| JWT | `JWT_SECRET`, `JWT_ISSUER`, `JWT_ACCESS_TOKEN_EXPIRATION`, `JWT_REFRESH_TOKEN_EXPIRATION` | Secret must be Base64 with at least 32 decoded random bytes; issuer defaults to `task-management-api`, durations default to `PT15M` and `P30D`. |
| CORS/docs | `CORS_ALLOWED_ORIGINS`, `API_DOCS_ENABLED` | CORS origins must be explicit, non-wildcard values; OpenAPI defaults off in `prod`. |
| Google | `GOOGLE_OAUTH2_ENABLED`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI` | Credentials required only when enabled; use the exact HTTPS callback in the provider console. |
| GitHub | `GITHUB_OAUTH2_ENABLED`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `GITHUB_REDIRECT_URI` | Credentials required only when enabled; use the exact HTTPS callback in the provider console. |
| Password reset | `PASSWORD_RESET_EMAIL_ENABLED`, `PASSWORD_RESET_EMAIL_FROM`, `PASSWORD_RESET_RESET_URL`, `PASSWORD_RESET_TOKEN_EXPIRATION` | Production requires email enabled and an HTTPS reset-page URL. The reset page must submit its `token` to `/api/v1/auth/reset-password`. |
| SMTP | `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` | Production requires credentials; SMTP authentication and STARTTLS are enabled, with STARTTLS required. |
| Pool | `DB_POOL_MAX_SIZE`, `DB_POOL_MIN_IDLE`, `DB_POOL_CONNECTION_TIMEOUT_MS`, `DB_POOL_IDLE_TIMEOUT_MS`, `DB_POOL_MAX_LIFETIME_MS`, `DB_POOL_VALIDATION_TIMEOUT_MS` | Defaults are conservative/configurable; size total pools across replicas against PostgreSQL connection capacity. |
| Rate limits | `LOGIN_RATE_LIMIT`, `LOGIN_RATE_WINDOW`, `LOGIN_RATE_LIMIT_MAX_KEYS`, `PASSWORD_RESET_IP_LIMIT`, `PASSWORD_RESET_ACCOUNT_LIMIT`, `PASSWORD_RESET_RATE_WINDOW`, `PASSWORD_RESET_RATE_LIMIT_MAX_KEYS`, `PASSWORD_RESET_COOLDOWN` | Limits are bounded but in-memory and per-instance; see operational limitations. |

OAuth redirect URI property names and JWT issuer are not all mandatory environment values because the application supplies safe defaults when the provider is disabled. See `application.properties` and `application-prod.properties` for the precise binding defaults. Never store real values in `.env.example`.

## Health and operational readiness

Only Actuator `health` is exposed over HTTP. Public `/actuator/health/liveness` reports whether the process is alive; `/actuator/health/readiness` includes the database indicator and is appropriate for traffic readiness and container health checks. Health details and component names are suppressed. Environment, configuration, metrics, and other management endpoints are not exposed.

## OpenAPI

When `API_DOCS_ENABLED=true`, the API documentation is available at `/swagger-ui.html` and `/v3/api-docs`. Production defaults to disabled. OpenAPI annotations describe DTOs and bearer authentication; do not enable public documentation in a deployment without reviewing its disclosure implications.

## CI and test coverage

GitHub Actions at `.github/workflows/ci.yml` configures Java 21/Maven caching, runs the standard Maven suite, runs the opt-in PostgreSQL 16 Testcontainers class, and uploads Surefire reports. No Google/GitHub credentials or real SMTP server are required by the ordinary tests. PostgreSQL tests use Docker through Testcontainers and are explicitly invoked with:

```powershell
.\mvnw.cmd '-DrunPostgresIntegration=true' '-Dtest=PostgreSqlIntegrationTest' test
```

The PostgreSQL suite checks the server major version, Flyway V1–V3, Hibernate schema validation at context startup, persistence and constraints/indexes, task ownership/filtering/pagination, OAuth-account persistence, and HTTP registration/login/refresh-token rotation. A skipped opt-in test is not evidence that PostgreSQL was verified.

## Optional k6 performance workflow

`performance/k6/task-api.js` performs login, refresh rotation, task create/list/read/update/delete against a non-production environment. It requires k6 and a disposable test account. Install k6 on Windows with `winget install k6 --source winget` (or follow the official instructions at <https://grafana.com/docs/k6/latest/set-up/install-k6/>). Then, from PowerShell in this directory:

```powershell
$env:BASE_URL = "http://localhost:8080"
$env:PERF_EMAIL = "perf@example.test"
$env:PERF_PASSWORD = "replace-with-a-disposable-test-password"
$env:VUS = "1"
$env:DURATION = "1m"
k6 run .\performance\k6\task-api.js
```

The script defaults to one virtual user and one minute and does not impose laptop-specific latency thresholds. Keep VUs low enough to respect the login IP rate limit. Created tasks are deleted; each VU's last refresh-token family remains active until expiry because the script does not print credentials for manual cleanup. Do not run against production without explicit authorization. A k6 result is valid only when the script is actually executed and its environment/configuration/results are recorded.

## Operational limitations and security notes

- Login and password-reset rate-limit counters use bounded in-memory storage. They reset on restart and are not shared across instances; multi-instance deployments need an intentionally designed shared limiter or edge-level controls.
- Real Google/GitHub credentials and SMTP delivery must be configured and verified by the operator. CI does not contact those providers or an SMTP server.
- The PostgreSQL Testcontainers test is opt-in locally; Docker must be running. Do not infer PostgreSQL compatibility from H2 results.
- The k6 script is a benchmark harness, not a measured capacity claim. This repository does not assert production throughput or latency.
- Use HTTPS at the public edge, restrict network access, rotate secrets, back up PostgreSQL, monitor database/pool and mail delivery, and test restore and incident procedures. This application has not been declared completely production-secure.
