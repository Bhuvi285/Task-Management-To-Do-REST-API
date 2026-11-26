# Phase 6.7 — Performance Testing and API Optimization

This document records the performance concepts reviewed for the Task Management API and distinguishes measured evidence from assumptions. Performance changes should follow workload evidence and preserve correctness, authorization, security, and API contracts.

## Implemented optimizations and evidence

### Reusing the authenticated user identity

- **Original problem:** A protected task request loaded `User` by email in `TaskManagerUserDetailsService` while the JWT filter established the principal, then `CurrentUserService` performed the same email lookup again.
- **Why it affected performance:** It added one database round trip to every service operation that resolves the current user, including task create, list, read, update, and delete. The extra lookup is independent of page size.
- **Change and mechanism:** `TaskManagerUserPrincipal` now carries the database user ID loaded during the existing request authentication. `CurrentUserService` returns a lazy `getReferenceById` reference for this principal, so task services can use its ID without another select. Other principal implementations retain the email-based fallback. The JWT filter still loads the user and rejects disabled accounts on every request; no cross-request identity or authorization cache was introduced.
- **Expected benefit:** One fewer SQL select for a normal JWT-authenticated task request. No token claim, response field, endpoint, or owner predicate changes.
- **Trade-offs:** The principal has a small internal implementation change and task services depend on the filter-provided ID snapshot within that request. A deleted-user race between filter lookup and service execution is not made more likely in a meaningful way than the corresponding race after the former second lookup; account status is still rechecked by the filter on the next request.
- **Test evidence:** With 25 tasks and a full page, Hibernate statistics recorded four prepared statements before the change and three afterward through the actual `MockMvc` security and MVC path. The response still contains 10 page rows and `totalElements=25`. Existing authentication, security, and ownership tests passed.

### Owner/default-order task index

- **Original problem:** V1/V2 defined a primary key on task ID but no index beginning with `tasks.user_id`. The common list query filters tasks by owner and normally orders by creation time descending with ID as a stable tie-breaker.
- **Why it affected performance:** Without an owner index, a database may scan unrelated users' rows to find one user's tasks and then sort the matches. This work grows with the whole task table rather than only the owner's matching rows.
- **Change and mechanism:** Flyway V3 creates `idx_tasks_user_created_at_id` on `(user_id, created_at DESC, id ASC)`. Equality on the leading owner column narrows the index range; the trailing columns align with the common default order and deterministic tie-breaker. V1 and V2 were not edited.
- **Expected benefit:** Less work for the common owner-scoped default list and a possible index-assisted ordering/page scan. Other allowed sort orders can still require sorting; optional status, priority, date, and contains-search filters remain correct and owner-scoped.
- **Trade-offs:** The index consumes disk and adds insert/update maintenance. A B-tree is not a suitable general index for `lower(title) LIKE '%term%'`; adding trigram or full-text support would require separate evidence and database-specific design.
- **Test evidence:** H2 successfully applied V1–V3, and an integration check asserts the index exists. A separate isolated H2 `EXPLAIN` comparison used 100,000 synthetic rows distributed over 100 owners: the pre-index plan reported `tasks.tableScan`, while the indexed plan reported `idx_tasks_user_created_at_id`. This does not claim PostgreSQL plan or latency verification.

## 1. Backend performance fundamentals

Performance is a property of a workload and deployment, not a code-style score. Define representative requests, data volume, concurrency, and acceptable service objectives; then measure. A slow endpoint can be limited by CPU, database I/O, locks, connection waits, network, serialization, or an external dependency. Optimizing a component that is not the bottleneck may add complexity without improving user-visible latency.

## 2. Database query execution

An ORM method eventually becomes SQL, which the database plans and executes. Query shape, predicates, indexes, data distribution, statistics, sorting, and requested page size all influence the plan. Inspect generated SQL and a database `EXPLAIN` plan before redesigning a repository method. The H2 plan is useful for H2 evidence only; PostgreSQL should be evaluated on PostgreSQL with representative data.

## 3. N+1 query problem

An N+1 occurs when one query loads a collection of N rows and code then executes another query for each row, often through lazy relationship access. Statement count grows with result size. Detect it using Hibernate statistics, SQL instrumentation, or a test that varies page contents and verifies a bounded query count. Do not infer an N+1 merely because a relationship is lazy; it occurs only when iteration triggers repeated loads.

## 4. Lazy loading and eager loading

Lazy loading defers related data until accessed, reducing unnecessary data transfer when the association is unused. It can cause N+1 queries if accessed per row, or fail outside an open persistence context. Eager loading retrieves relationships earlier but can add joins, duplicate root rows, and increase payload/work. Choose fetch behavior per use case rather than making every association eager.

## 5. Fetch joins and entity graphs

A JPQL fetch join or JPA entity graph can load relationships in a bounded query when a use case truly requires them. Collection fetch joins combined with pagination can multiply rows or force in-memory pagination; verify generated SQL and page behavior. A task response that maps only scalar task fields does not need the `User` association fetched, so adding a fetch join there would be unnecessary.

## 6. Database indexing

An index helps a database locate or order relevant rows without scanning the full table. It is not automatically beneficial: indexes consume storage, must be maintained on writes, and may not be selected if the predicate is unselective or the table is small. A primary-key or unique constraint already supplies an index in the supported databases; avoid duplicating it.

## 7. Composite indexes

A composite index stores multiple key columns in a defined order. It is most useful when its leading column(s) match equality predicates and later columns support range predicates or ordering. An index on `(user_id, created_at, id)` can serve owner lookup followed by a creation-time order more effectively than separate indexes in some plans. Confirm real query plans rather than indexing every possible combination.

## 8. Selectivity and index column ordering

Selectivity describes how effectively a predicate narrows rows. For this API, `user_id` is an equality predicate shared by the task list and is placed first; creation time and ID follow for the common order. Database cardinality estimates and real user/task distribution matter. A low-selectivity column alone may not justify an index, while a leading owner column makes the composite useful to owner-scoped lookups.

## 9. Pagination performance

The API uses `Pageable`, so limit/offset and sorting are performed in the database, rather than loading every task into memory. The service validates a nonnegative page, limits size to 100, and validates sort fields against an allowlist. A deterministic ID tie-breaker prevents equal sort values from arbitrarily moving between pages. Large offsets still require the database to walk past earlier rows; the size cap does not remove that cost.

## 10. Offset versus cursor pagination

Offset pagination is easy for clients and supports arbitrary page navigation, but high offsets can become expensive and concurrent inserts/deletes can shift page boundaries. Cursor/keyset pagination continues from the last seen ordering key and can scale better for sequential traversal, but it changes the API contract and needs a stable unique ordering plus cursor validation. This phase preserves the existing offset-based contract; no cursor endpoint was added.

## 11. Count queries

Spring Data `Page<T>` generally needs a count query to populate total elements and total pages. Counts can be expensive for complex filters and large tables, but removing them changes useful response metadata. `Slice<T>` or cursor responses can avoid a total count but are contract changes. This phase retains the count query and scopes it to the same owner and filters as the result query.

## 12. Sorting and filtering optimization

Apply owner and requested filters in SQL before pagination; do not retrieve all records for application-side filtering or sorting. Whitelist sort names to avoid arbitrary property paths and preserve a stable tie-breaker. Optional predicates written as `(:x is null OR column = :x)` may complicate planner choices; search with `lower(title) LIKE '%text%'` typically cannot use a normal B-tree. Change query construction or adopt specialized indexes only after the actual database plan and workload justify it.

## 13. Hibernate SQL statistics

Hibernate statistics can report prepared-statement counts and entity/query activity. Enable them only in tests or controlled diagnostic environments because detailed statistics add overhead and may expose sensitive SQL information if paired with logging. This project’s query-count integration test enables `hibernate.generate_statistics` for its test context, clears counters immediately before an HTTP request, and asserts a stable statement budget rather than timing.

## 14. HikariCP connection pooling

HikariCP reuses established database connections instead of opening one for every request. Current configurable defaults are maximum pool size 10, minimum idle 2, connection timeout 30 seconds, idle timeout 10 minutes, maximum lifetime 30 minutes, and validation timeout 5 seconds. Pool size must fit database capacity and total application replicas; multiplying a large pool by many replicas can exhaust PostgreSQL connections and increase contention.

## 15. Connection acquisition timeout

When every pooled connection is busy, a new borrower waits for an available connection until `connectionTimeout`. If one becomes free, the request proceeds; otherwise acquisition fails and should be handled/observed as a capacity or long-transaction signal. Increasing the timeout may only make requests wait longer and does not create database capacity.

## 16. Connection leaks and long transactions

A connection leak occurs when application code does not return a borrowed connection, starving later requests. Spring transactions and connection pools normally handle release, but unmanaged JDBC resources, stuck transactions, slow external work inside a transaction, and unclosed streams can hold resources too long. Keep transactions focused on database work, use try-with-resources for direct JDBC, and monitor active/idle/pending connections and leak warnings.

## 17. Load testing versus integration testing

Integration tests validate behavior and wiring, not throughput. A load test drives concurrent realistic requests and collects response distributions, throughput, errors, and resource saturation. The supplied optional k6 script is a reproducible workflow, not part of the Maven suite; it requires a running non-production API, k6, and a pre-provisioned test account. Avoid sending it to production without explicit authorization and safeguards.

Run from the API directory after preparing a local test account and a local API server:

```powershell
k6 run -e BASE_URL=http://localhost:8080 -e PERF_EMAIL=perf@example.test -e PERF_PASSWORD=replace-with-test-secret -e VUS=1 -e DURATION=1m .\performance\k6\task-api.js
```

Keep the default virtual-user count low because the API rate-limits login. Each VU logs in separately and owns its refresh-token family; the script never shares a refresh token across concurrent VUs. Use disposable non-production accounts and data. The script creates and deletes a task each iteration, but accumulated logs/rate-limit buckets and other application state still merit a disposable environment. Do not put real credentials in source control or command history.

## 18. Response-time measurements

Latency includes client, network, server queueing, application work, and database time. Report percentiles such as p50/p95/p99 rather than only a mean, and state the environment, data size, warm-up, concurrency, and duration. Compare repeated runs under the same conditions. The k6 output records timings but this repository has no claimed before/after endpoint latency results; the H2 `EXPLAIN` plan is not a latency measurement.

## 19. Throughput and concurrency

Throughput is completed requests per unit time; concurrency is the number of requests in flight. Raising virtual users can increase throughput until a resource saturates, after which latency and error rates may rise. Test realistic mixes and check database connections, CPU, memory, locks, and rate limits. A single-user benchmark does not establish multi-user or production capacity.

## 20. Bottleneck identification

Start with endpoint-level measurements and telemetry, then narrow the bottleneck with SQL statistics, query plans, pool metrics, thread/CPU profiles, and logs. A table scan, repeated query, pool wait, lock wait, slow password hash, or external provider call requires a different remedy. Use a representative dataset and isolate one change at a time; don't interpret one noisy timing as causal evidence.

## 21. Optimization trade-offs

Every optimization has costs: indexes trade write time and storage for reads; caching trades consistency and invalidation complexity for fewer reads; fetch joins trade query count for larger joined results; larger pools trade queueing for database concurrency pressure; cursor pagination trades random page access for scalable sequential access. Preserve ownership predicates, authorization checks, token rotation, password hashing, validation, and response semantics while optimizing.

## 22. Practical interview questions and answers

### Q1. How do you find an N+1 query?

Count statements while loading a representative page, inspect generated SQL, and see whether the count grows with the number of returned records. Then identify which relationship access triggers the extra queries before choosing a fetch strategy.

### Q2. Why not add an index to every filter column?

Indexes cost disk and write work and may not be used. Choose a key order from actual predicates and ordering, inspect plans on representative data, and verify that the improvement justifies maintenance costs.

### Q3. Why keep a count query in a paginated endpoint?

The total pages/elements are part of the current `Page` response contract. Removing the count may improve some workloads but changes the contract; first measure it and consider a separate cursor or slice API if requirements support that trade-off.

### Q4. Why is a leading-wildcard search hard to index?

A B-tree is ordered by the beginning of a value, while `%term%` may match anywhere. A database-specific trigram or full-text index can help but changes schema/operations and should be based on search volume and measured plans.

### Q5. What happens when Hikari's pool is full?

Borrowers wait up to the acquisition timeout. A timeout indicates excessive concurrent demand, long-held connections, leaks, or insufficient database capacity; blindly increasing pool size can worsen contention.

### Q6. How would you make pagination scale to very deep pages?

Consider keyset/cursor pagination using a stable unique ordering key, after measuring offset cost. The API contract must define cursor encoding, ordering, and behavior under concurrent writes.

### Q7. What does an integration test prove about performance?

It can verify deterministic properties such as bounded SQL statements or query-plan/index presence for the test database. It does not establish response-time capacity, production data behavior, or another database’s planner behavior.

### Q8. What should a useful load-test report contain?

The tested build and environment, dataset, request mix, concurrency, duration, warm-up, p50/p95/p99, throughput, error rate, resource saturation, and any rate limits or skipped cases. Include enough detail to reproduce the run.