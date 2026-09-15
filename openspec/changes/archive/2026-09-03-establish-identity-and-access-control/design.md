## Context

See `proposal.md` for motivation and the delta specs for normative behavior.

The backend foundation currently has no domain tables or Flyway migrations, disables form/basic/logout, permits every request in `FoundationSecurityConfiguration` and excludes `UserDetailsServiceAutoConfiguration` in the application entry point. `OpenApiConfiguration` already declares a preparatory Bearer scheme, while `GlobalExceptionHandler` owns the sanitized `ProblemDetail` shape. PostgreSQL/Testcontainers integration tests already protect startup, Flyway validation, public health/OpenAPI/Swagger and missing database configuration.

The Graphify impact traversal identified these as the relevant seams, so the design confines changes to identity, security, persistence, presentation contracts, configuration and their tests. The existing health and container readiness chain must remain intact.

## Goals / Non-Goals

**Goals:**

- Establish explicit `application` use cases, services and ports as the transactional boundary for registration, login, current-principal lookup and administrator provisioning.
- Make the database protect canonical identity, allowed roles and the structural `1 -> 0..1` wallet cardinality.
- Use standard Spring Security JWT primitives while resolving the current persisted user on every protected request.
- Give MVC exceptions and security filter failures one sanitized `ProblemDetail` representation.
- Make security auditing typed and safe by construction, rather than accepting arbitrary request payloads.
- Preserve the existing PostgreSQL/Flyway/Testcontainers validation and public technical endpoints.

**Non-Goals:**

- Generalize wallets for future financial behavior or add any wallet endpoint.
- Build a generic RBAC/permission model, role table or multi-role membership.
- Add token lifecycle state, refresh, revocation lists or server-side logout.
- Add user administration, profile/password workflows or audit-query APIs.
- Change frontend behavior or introduce future catalog and financial capabilities.

## Decisions

### 1. Separate domain, application, infrastructure and presentation

`domain` contains `Usuario`, `Carteira`, `Role`, pure rules, enums and domain exceptions, without dependencies on frameworks. `application` contains use cases, application services, transactional orchestration and the ports/contracts they require. Registration, login, current-principal lookup and administrator provisioning are application use cases.

JPA mappings, implementations of repository ports, Spring Security, JWT, BCrypt, configuration and external adapters live in `infrastructure`. `presentation` owns controllers, DTOs and HTTP error translation. Controllers accept request DTOs and return dedicated response DTOs; neither domain nor JPA objects are serialized directly.

This separation follows the project architecture and prevents fields such as `senha_hash` from becoming accidentally serializable. Reusing JPA entities as API models was rejected because field annotations alone are a fragile privacy boundary.

### 2. Add forward-only Flyway migrations with PostgreSQL-native integrity

Because the foundation has no executed domain migration, add new ordered migrations for `usuarios`, `carteiras` and `logs_auditoria` rather than modifying an existing script. Use UUID primary keys, `TIMESTAMPTZ` timestamps and `NUMERIC(18,2)` for `saldo_caixa_brl`.

For `usuarios`, persist only canonical e-mail and combine a check equivalent to `email = lower(btrim(email))` with a unique index on `lower(email)`. Add a role check limited to `ROLE_USER` and `ROLE_ADMIN`; no role table is created. `carteiras.usuario_id` is a non-null foreign key and unique, enforcing at most one wallet per user. `logs_auditoria.usuario_id` is a nullable foreign key so anonymous failures can be recorded.

The database can declaratively enforce `0..1` wallets but cannot enforce “every ROLE_USER has one and ROLE_ADMIN has none” across two tables with ordinary row checks. The supported creation flows therefore enforce that semantic invariant transactionally, while the unique foreign key enforces the structural maximum. A cross-table trigger was rejected because it would couple insert ordering, complicate the atomic registration transaction and obstruct future migrations.

### 3. Register through one transactional use case

The registration use case canonicalizes and validates input, checks for a friendly duplicate error, hashes the password with a BCrypt encoder configured at strength 12, creates an active `ROLE_USER`, creates its `Carteira Principal` with zero balance, records the successful registration audit event and commits them together. Its public response contains only `id`, `nome`, `email`, `role` and `ativo`; it never generates a JWT. The PostgreSQL unique index remains the final authority under concurrency; its violation maps to `409 Conflict`.

Token generation is not part of registration. Any failure before commit rolls back user, wallet and success-audit writes. This explicit orchestration was chosen over JPA cascade-only creation because the transaction, role assignment and audit boundary need to be visible and testable.

### 4. Use Spring Security resource-server JWT support with persisted-principal resolution

Add the Spring Security OAuth2 Resource Server/Jose support using versions managed by Spring Boot. Configure a stateless filter chain, disable CSRF for the stateless API, and keep form login, HTTP Basic and logout disabled. Use Nimbus-backed JWT encoding/decoding for HS256 instead of custom cryptographic parsing.

`JWT_SECRET_KEY` is interpreted as UTF-8 key material and must contain at least 32 bytes; startup fails otherwise. `JWT_EXPIRATION_HOURS` is a positive duration with default 24. `JWT_ISSUER=carteira-investimento-backend` and `JWT_AUDIENCE=carteira-investimento-api` are non-secret configuration, documented in `application.yml`, `.env.example` and Compose when implemented. Issuer, audience, signature, `exp`, `iat`, `jti`, `sub` UUID and the role claim are validated before authentication.

After cryptographic validation, a dedicated authentication converter/provider loads `sub` from `usuarios` for every protected request. It rejects missing users, inactive users and a token role that no longer matches the persisted role with `401`; authorities are built from the current persisted role, not trusted solely from the claim. This database lookup is an intentional consistency cost required to invalidate previously issued tokens immediately when a user becomes inactive.

A fully custom JWT filter was rejected because it would duplicate parsing, algorithm restrictions and standard Bearer error handling. A token-only principal without a database lookup was rejected because it violates immediate inactive-user enforcement.

### 5. Restore deliberate user authentication configuration

Remove the explicit `UserDetailsServiceAutoConfiguration` exclusion and provide the application's own repository-backed authentication components. Defining the required beans prevents Spring Boot from creating a generated default user while allowing the standard authentication manager and BCrypt provider to be composed explicitly.

Keeping the exclusion was considered, but it obscures why auto-configuration is disabled after real identity exists and makes future security diagnostics harder.

### 6. Define an explicit public-route allowlist and deny by default

The security chain permits only:

- `POST /api/v1/auth/register`;
- `POST /api/v1/auth/login`;
- `/actuator/health`;
- OpenAPI JSON endpoints used by Springdoc;
- `/swagger-ui.html` and Swagger UI assets.

Every other request requires authentication. Method/route role rules can be layered as capabilities add protected resources; this change tests at least the admin/user distinction without inventing future business endpoints, using security test probes where necessary. `/api/v1/auth/me` requires any authenticated role.

The former `anyRequest().permitAll()` is removed. Broad path prefixes such as all actuator endpoints are rejected so only the existing health contract remains public.

### 7. Share one ProblemDetail factory across MVC and security handlers

Extract the construction of sanitized `ProblemDetail` into a presentation error component used by `GlobalExceptionHandler`, a custom `AuthenticationEntryPoint` and a custom `AccessDeniedHandler`. Security handlers write `application/problem+json` directly because failures may happen before controller advice.

Messages remain generic: authentication failures do not distinguish unknown e-mail, bad password, missing user, inactive user or token-validation detail. The HTTP status differentiates authentication (`401`) from authorization (`403`), while internal logs/audit use typed reason codes without secrets.

### 8. Issue tokens only after credential and active-state checks

The login use case canonicalizes the e-mail, authenticates with the repository-backed BCrypt provider, verifies `ativo`, persists the successful event and only then issues a JWT. Unknown e-mail and bad password share the same public response. Inactive attempts return the same generic `401` but receive their distinct audit event when the persisted user is known.

The login response is exactly `accessToken`, literal `tokenType` `Bearer` and `expiresIn`, the access-token remaining duration in seconds. No credential, hash, refresh token or user persistence model is returned.

### 9. Provision the initial administrator with validated configuration and fail-fast semantics

Bind mandatory `ADMIN_NAME`, `ADMIN_EMAIL` and `ADMIN_PASSWORD` to validated configuration and run provisioning through `CommandLineRunner` in a transaction. Missing, partial or invalid configuration fails startup before any partial admin is created. The password follows the same policy and BCrypt strength as registration.

If the canonical e-mail is absent, create an active `ROLE_ADMIN`, no wallet, and its audit event. If it belongs to an admin, do nothing and never re-hash or overwrite credentials. If it belongs to `ROLE_USER`, fail startup explicitly. Fail-fast was selected over silently skipping bootstrap because an operator could otherwise believe administrative access was provisioned when it was not.

### 10. Make audit writes typed, correlated and sanitized

Expose an internal audit service that accepts a fixed event type, result, severity, optional known user UUID, nullable endpoint and correlation ID—not arbitrary objects, headers or request bodies. A servlet filter accepts `X-Correlation-ID` only when it is a bounded canonical UUID, otherwise generates a UUID, stores it as a request attribute/MDC value and makes it available to the audit service. System events without HTTP origin, including initial-admin creation, use a generated correlation UUID and a null endpoint.

Successful registration and initial-admin events participate in their creation transactions. Failed login, inactive-user attempts and access-denied events are written in their own short transaction because the originating operation does not commit. A failed audit write for a denied request is reported only through sanitized application logging and does not replace the required `401`/`403`; a successful registration or bootstrap does not commit without its required success event.

Audit tables contain no free-form request payload column in this change. This structural omission, DTO design and tests scanning persisted/logged values jointly enforce the prohibited-data list.

### 11. Preserve and extend the foundation verification strategy

Keep `ApplicationFoundationIT`, `FlywayValidationIT`, `PostgreSqlContainerSupport`, `MissingDatabaseConfigurationTest` and `GlobalExceptionHandlerTest`, adapting only their setup and assertions for mandatory security/admin configuration. Public health/OpenAPI/Swagger and database readiness remain asserted.

Add unit tests for canonicalization, password policy, JWT validation and sanitization, plus PostgreSQL/Testcontainers integration tests for database constraints, transactional rollback, duplicate-email races, principal-wallet cardinality, admin idempotency/conflict, inactive tokens and audit persistence. Security MVC tests cover public routes, missing/invalid/expired tokens, `401` versus `403`, `/me` identity sourcing and safe response fields.

## Risks / Trade-offs

- [Every protected request performs a database lookup] -> Keep the lookup narrowly indexed by UUID and accept the latency as the cost of immediate inactive-user enforcement; do not add a cache that could extend access.
- [A unique pre-check still races under concurrent registration] -> Treat the PostgreSQL unique index as authoritative and translate its constraint violation to the same `409` contract.
- [The database cannot declaratively guarantee the cross-table role/wallet rule with simple constraints] -> Restrict supported creation to transactional services, enforce the structural maximum in PostgreSQL and cover both role paths with integration tests.
- [Fail-fast `ADMIN_*` validation can prevent startup in environments not yet configured] -> Document all three variables, provide safe examples and wire them through Compose before deployment.
- [Security filter failures bypass controller advice] -> Reuse the same ProblemDetail factory and contract tests for MVC, entry-point and access-denied paths.
- [Audit persistence during rejected requests can fail independently] -> Use a short isolated transaction, never include sensitive fallback context, and preserve the original security response while emitting a sanitized operational error.
- [A raw environment secret can be misread as character count] -> Validate UTF-8 byte length at startup and document the minimum as 32 bytes/256 bits.

## Migration Plan

1. Configure valid `JWT_SECRET_KEY`, `JWT_EXPIRATION_HOURS` and the complete `ADMIN_*` set in each target environment; pass them to the backend service in Compose.
2. Deploy the additive Flyway migrations. They create only new tables, indexes and constraints because the foundation currently has no identity data to backfill.
3. Deploy the application with the definitive security chain. Startup runs Flyway, Hibernate validation and idempotent admin provisioning before health becomes `UP`.
4. Verify public health/OpenAPI/Swagger, admin idempotency, register/login/me, protected-route rejection and sanitized audit rows.

Rollback the application without editing or deleting applied Flyway migrations. The previous foundation can tolerate the additive unused tables; retain them for the next corrected forward deployment. If database removal is ever required, handle it as an explicit reviewed forward migration with a backup, not as an automatic rollback in this change.
