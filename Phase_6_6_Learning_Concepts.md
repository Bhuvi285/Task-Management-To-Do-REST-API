# Phase 6.6 — End-to-End API Integration Testing

This phase validates application workflows through HTTP rather than testing only isolated methods. The goal is to catch integration defects between request binding, validation, security filters, controllers, services, repositories, database state, serialization, and API documentation while preserving the existing production behavior.

## 1. Unit, integration, and end-to-end testing

- A unit test isolates a small unit (for example a token validator) and substitutes collaborators. It is fast and pinpoints local logic, but cannot prove Spring wiring or HTTP behavior.
- An integration test exercises collaborating components. A Spring Boot test can include the real security chain, MVC binding, services, JPA repositories, migrations, and an embedded test database.
- An end-to-end API test sends a request through the externally visible HTTP contract and verifies status, headers, JSON, and resulting state. `MockMvc` is an in-process server boundary, not a real socket or deployed infrastructure test.
- Use each level intentionally: unit tests for branches and edge cases, integration tests for framework/database interactions, and a small set of workflow tests for important user journeys.

## 2. Workflow coverage

Test meaningful sequences rather than a collection of unrelated endpoint calls. A local-auth journey can register, log in, call a protected route, rotate a refresh token, log out, and verify that the rotated credential is no longer usable. A task journey can create, retrieve, update, re-fetch, list, delete, and confirm the deleted resource returns the API's not-found response.

Workflows reveal problems that isolated tests miss: an output field may not match the next request, a security filter may reject a valid token, a repository query may use the wrong owner, or a token lifecycle change may not persist. Each assertion should correspond to an observable contract or a security invariant.

## 3. MockMvc

`MockMvc` builds servlet requests and invokes Spring MVC without starting a network listener. It supports HTTP methods, headers, query parameters, JSON bodies, and assertions on status, headers, and JSONPath expressions.

```java
mockMvc.perform(get("/api/v1/tasks")
        .header("Authorization", "Bearer " + accessToken)
        .param("page", "0")
        .param("size", "10"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.content").isArray());
```

Use the real configured filter chain for authentication tests. Avoid `@WithMockUser` when the purpose is to validate JWT extraction, signature verification, principal loading, or HTTP token behavior.

## 4. SpringBootTest and application boundaries

`@SpringBootTest` loads the application context and provides high-fidelity integration coverage. Pair it with `@AutoConfigureMockMvc` to exercise routing, filters, validation, controllers, advice, and serialization. This is slower than a slice test, so keep the full-context suite focused on behaviors that need the complete wiring.

Use the existing test profile and embedded database for ordinary tests. Provider clients, email delivery, clocks, and other external dependencies should be replaced at an explicit boundary; do not make automated tests require real accounts or network services.

## 5. Integration test lifecycle

Spring caches application contexts between compatible test classes. A test database can therefore retain records for the lifetime of that context unless a transaction, cleanup strategy, or isolated database lifecycle resets it. Do not rely on test execution order or fixed generated IDs.

Use unique email addresses and distinguishable task data, then assert state only for the test's own users and resources. For concurrency tests, be deliberate about transaction propagation: independent worker threads do not automatically share the test thread's transaction. Testcontainers can validate database-specific behavior when explicitly enabled, but its skipped status is not evidence that the container database passed.

## 6. HTTP request and response contracts

An HTTP test should check the elements clients rely on:

- Status code and `Content-Type`.
- Required JSON fields, field names, enum values, optional/null behavior, and DTO mapping.
- Response headers such as `Location`, `Cache-Control`, and authentication headers where part of the current contract.
- Validation response shape, including status, message, path, and field-level details.
- State changes that prove an operation completed (for example persisted task updates or refresh-token revocation).

Assert the API DTO rather than serializing entities. This catches accidental exposure of database relationships, owner IDs, password hashes, or token internals.

## 7. Authentication workflows

Exercise registration and login through the actual endpoints, including valid data, duplicates, malformed JSON, missing fields, invalid values, wrong credentials, and unknown accounts. Compare public error messages where generic behavior is intended. Verify the login response's token fields, token type, expiry, no-store policy, and absence of password fields.

Call a protected probe using the returned access token, then try missing, malformed, expired, wrong-signature, and wrong-issuer tokens. Tests should confirm the correct 401 response and that error bodies do not echo secrets, internal exception details, or stack traces.

## 8. JWT testing

JWT tests should use the configured test signing key and issuer rather than disabling token validation. Generate malformed or expired tokens deterministically with controlled claims and time values. Cover successful principal resolution as well as invalid signature, issuer, expiration, malformed claims, absent `Authorization`, and unsupported/malformed Bearer syntax.

An integration test demonstrates how the filter and user lookup behave together. It does not prove that the production secret is strong or that every deployed proxy/TLS setting is correct; configuration validation and deployment checks are separate.

## 9. Refresh-token rotation and logout

Refresh credentials are bearer secrets. A workflow should prove that the raw token returned to a client is represented by a hash in storage, a successful exchange returns a replacement, and the old token cannot be used again. Reuse detection should be checked against the existing family policy, including whether unrelated sessions remain active.

Test logout by submitting a refresh token, verifying that it can no longer be refreshed, and checking the documented behavior of an access JWT that was already issued. Revoking a refresh family does not inherently revoke a stateless access token; tests should encode the implementation's actual contract rather than assume immediate JWT revocation. Include concurrent refresh coverage when locking or transaction ordering is security-critical.

## 10. Authorization and ownership isolation

Create at least two users with independent credentials and task sets. Authenticate each with a real application JWT. Verify that one user cannot retrieve, update, or delete another user's task and that the consistent not-found response does not distinguish an absent task from a non-owned one.

Check list totals and page metadata as well as records. Search, status/priority filters, sorting, and date ranges must all remain owner-scoped. Include client-supplied ownership fields in a request and prove they do not override the authenticated principal. Also verify unauthenticated access is rejected before protected resource handling.

## 11. Database isolation in tests

H2 is fast and suitable for most deterministic repository and API tests, especially when the same Flyway migrations initialize it. It is not identical to PostgreSQL: SQL dialect, locking, collation, JSON support, isolation behavior, and query planning can differ.

Use unique test data and avoid global cleanup that could delete another test's state. Do not infer PostgreSQL behavior from H2. Keep PostgreSQL Testcontainers opt-in when Docker is unavailable, and report explicitly whether the container test ran, passed, failed, or was skipped.

## 12. Pagination, sorting, and filtering

Test query parameters over HTTP and inspect both `content` and metadata (`number`, `size`, `totalElements`, `totalPages`, `first`, `last`, `empty`, as provided by the response DTO). Exercise page boundaries, empty results, supported sort fields in both directions, and deterministic ties. Verify sorting uses only allowed properties rather than permitting arbitrary entity paths.

Cover independent and combined filters: case-insensitive title search, status, priority, due-date lower and upper bounds, and combinations of these. Negative tests should include negative page numbers, zero or oversized page sizes, unsupported sort properties/directions, invalid enum values, malformed dates, and reversed date ranges.

## 13. Error-response contracts and disclosure

Validate representative 400, 401, 403, 404, 409 (if the API uses it), 429, and 500 responses. Error payloads should be consistent and safe; assert that SQL, exception class names, stack traces, filesystem paths, credentials, password hashes, and token hashes do not escape to clients.

For unexpected failures, assert the generic client message while retaining controlled server-side diagnostics. Avoid brittle assertions on timestamps or localized framework text. Account-enumeration protections may intentionally use a generic 202 or 401 response instead of a 409; test the established contract rather than changing status codes solely to fit a generic convention.

## 14. OAuth callback testing

Do not call Google or GitHub in automated tests. Exercise authorization-state handling with synthetic requests and mock provider identities, and verify missing/invalid state and callback failure paths. On success, check the established token-pair JSON shape; on failure, check generic errors, no token issuance, temporary session invalidation, and session-cookie expiration.

Mocking the provider boundary proves application behavior after a synthetic identity is supplied. It does not validate provider registration, redirect URI configuration, real TLS, consent screens, or remote provider availability.

## 15. OpenAPI contract validation

Fetch the generated `/v3/api-docs` document and assert that important paths, methods, security requirements, request/response schema references, error responses, and query parameter descriptions are present. Check that public operations remain public, protected task/password operations advertise bearer authentication, and password inputs are marked write-only.

OpenAPI tests validate the published description, not the runtime endpoint by themselves. Pair schema assertions with HTTP tests so documentation and observed responses are compared. Keep assertions focused on contract details rather than incidental JSON ordering.

## 16. Test data and external dependencies

Prefer existing fixtures and helpers, but keep helper behavior transparent. Give each test unique identities and descriptive resource values; avoid fixed database IDs and hidden mutable shared fixtures. Use a test delivery adapter to capture reset tokens only when the application already supports an injectable delivery boundary, and never add a real email provider for test convenience.

Keep default tests independent of Docker, external PostgreSQL, OAuth credentials, real email, and public network access. Set up authentication with the actual application tokens when testing the security filter, not an artificial principal shortcut.

## 17. Common integration testing mistakes

- Testing only service methods and assuming that request binding, filters, and error advice work.
- Disabling security or mocking the JWT filter to make an integration test pass.
- Reusing fixed users, IDs, or database state and accidentally depending on test order.
- Asserting only HTTP 200 without checking the response contract or resulting persistence state.
- Testing ownership on point reads while forgetting list totals, search, filters, and pagination.
- Making tests contact external OAuth/email services or require Docker by default.
- Treating H2 as proof of PostgreSQL behavior, or a skipped container test as a pass.
- Overfitting to implementation details instead of stable behavior, except where an implementation detail is itself a security invariant (such as token hashes at rest).
- Ignoring response headers and documentation even though clients and browsers depend on them.

## 18. Interview questions and answers

### Q1. What distinguishes a unit test from an integration test?

A unit test isolates one component and substitutes collaborators. An integration test verifies that multiple real components, often including framework configuration or persistence, work together.

### Q2. Is MockMvc a true end-to-end network test?

No. MockMvc exercises Spring MVC and filters in-process without a socket. It gives strong API integration coverage but does not validate the deployed server, network, TLS termination, or gateway.

### Q3. Why test an entire authentication workflow?

The workflow verifies token issuance, serialization, filter parsing, user lookup, authorization, refresh rotation, and revocation as a connected chain rather than independent methods.

### Q4. Why test two users for resource ownership?

One user's successful access does not prove another user's data is isolated. Two independent principals expose missing owner predicates and metadata leaks.

### Q5. Why check pagination metadata for ownership leaks?

Even if the returned records are filtered, `totalElements` or page counts can reveal that other users have matching resources.

### Q6. What does an H2 integration test not prove?

It does not prove PostgreSQL-specific SQL, locking, collation, or deployment behavior. Those need PostgreSQL-backed verification when relevant.

### Q7. How should tests handle OAuth and password-reset delivery?

Mock the external identity boundary and inject/capture reset delivery through an existing test seam. Do not use real provider credentials, authorization servers, or email services in the default suite.

### Q8. Why validate OpenAPI as well as runtime responses?

The runtime is the actual behavior; OpenAPI is the client-facing contract. Testing both helps find drift in security declarations, schemas, parameters, and error descriptions.

### Q9. Does a passing integration suite prove production readiness?

No. It proves only the tested cases in the tested configuration. Production secrets, PostgreSQL, TLS/proxy behavior, provider configuration, monitoring, backups, load, and operational procedures still require separate validation.

This concludes Phase 6.6. No subsequent phase is included.