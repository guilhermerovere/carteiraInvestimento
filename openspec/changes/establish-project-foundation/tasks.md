## 1. Repository and version baseline

- [ ] 1.1 Confirm implementation is on `feature/fundacao-do-projeto`, inventory the current `carteiraInvestimento/` files and record a clean baseline by running the existing Maven tests before structural edits.
- [ ] 1.2 Move `carteiraInvestimento/` to `backend/` while preserving every source and Maven Wrapper file, update references to the old path and verify `carteiraInvestimento/` no longer exists and `backend/pom.xml` is present.
- [ ] 1.3 Preserve the root `package.json` and `package-lock.json` as repository/OpenSpec tooling, confirm root and nested `node_modules` remain ignored by Git and verify the diff contains no Next.js dependency in the root manifests.
- [ ] 1.4 Apply and document the approved matrix Spring Boot 4.1.1, Java 21, Spring Cloud 2025.1.2, Springdoc 3.1.0 and Boot-managed Spring Framework 7.0.9, Spring Security 7.1.1, Testcontainers 2.0.5, Flyway 12.4.0 and PostgreSQL JDBC 42.7.13; verify the Maven effective model and dependency tree contain no redundant manual override or compatibility conflict.

## 2. Backend build and configuration

- [ ] 2.1 Update `backend/pom.xml` and Maven Wrapper for Java 21/Spring Boot 4.1.1 with Web MVC, Validation, Data JPA, Boot-managed Spring Security, Actuator, Cloud-BOM-managed OpenFeign, Caffeine, Flyway PostgreSQL, PostgreSQL driver, Springdoc 3.1.0, logging and test dependencies; verify `./mvnw dependency:tree` succeeds, matches the approved matrix and contains no H2 artifact.
- [ ] 2.2 Replace `application.properties` with environment-driven `application.yml` for application port, PostgreSQL datasource, Flyway `validate-on-migrate`, Hibernate `ddl-auto: validate` and safe Actuator health exposure; verify tests demonstrate that missing required database configuration cannot fall back to an in-memory database.
- [ ] 2.3 Create `backend/src/main/resources/db/migration/` without domain DDL and verify inspection finds no tables or fields for identity, portfolio, brokerage, asset, transaction, cash movement, snapshot or audit entities.
- [ ] 2.4 Establish the base source areas `domain`, `infrastructure` and `presentation` using only legitimate technical scaffolding, and verify compilation plus a source-tree review confirm that no JPA domain entity, financial rule, external API client or business endpoint was added.
- [ ] 2.5 Configure the Boot-managed Spring Security in an explicitly temporary permissive mode without generated login, users, JWT or definitive RBAC, mark it for replacement by the identity/authentication change and verify Swagger plus `/actuator/health` are reachable without credentials while no authentication endpoint exists.
- [ ] 2.6 Configure OpenAPI metadata and the Swagger UI route `/swagger-ui.html`, including a preparatory Bearer scheme only in documentation, and verify an automated HTTP test loads the OpenAPI document and resolves the Swagger UI route.
- [ ] 2.7 Implement the global HTTP error foundation with `RestControllerAdvice` and sanitized `ProblemDetail` responses for generic supported failures, and verify focused tests assert `application/problem+json`, correct status and absence of secrets/internal stack details.
- [ ] 2.8 Configure Spring Boot Actuator to expose `GET /actuator/health` on the main backend port without sensitive details, and verify an integration test receives HTTP success with status `UP` only after context, Flyway, Hibernate and PostgreSQL initialization complete.

## 3. PostgreSQL integration tests

- [ ] 3.1 Add a reusable PostgreSQL Testcontainers 2.0.5 integration-test setup using the version managed by Spring Boot 4.1.1, supply dynamic datasource properties to Spring and verify the test observes a PostgreSQL JDBC connection rather than H2.
- [ ] 3.2 Add integration coverage for application-context startup, Flyway migrate/validate on a clean PostgreSQL database and Hibernate validation, and verify the Maven test suite passes with Docker available.
- [ ] 3.3 Add a negative Flyway validation case using an incompatible migration history and verify startup or validation fails for the expected Flyway reason.

## 4. Frontend foundation

- [ ] 4.1 Scaffold an independent `frontend/` project with its own `package.json` and `package-lock.json`, Next.js 15+, App Router, TypeScript and Tailwind CSS using the selected Node version; verify clean installation, lint/type checks and production build succeed while root manifests remain unchanged.
- [ ] 4.2 Add the minimal root page and shared styling needed to prove the foundation, and verify the running app renders the page without login, dashboard or other functional business screens.
- [ ] 4.3 Install or initialize Shadcn UI, Recharts and TanStack Query only in `frontend/`, adding the minimum technical provider/configuration scaffolding required, and verify the frontend manifest, build and smoke test succeed without dashboard or functional business components.
- [ ] 4.4 Read the public backend URL exclusively from `NEXT_PUBLIC_API_URL` and verify a build with an alternate value reflects the supplied non-secret configuration without a hard-coded production endpoint.

## 5. Environment and container images

- [ ] 5.1 Create root `.env.example` with the complete PRD variable contract and a local academic `.env` with demonstration values only; update ignore rules as needed and verify both files exist locally while no real secret or token is committed.
- [ ] 5.2 Create a multi-stage `backend/Dockerfile` using Java 21 build/runtime images and the Maven Wrapper, and verify the image builds and starts the backend with valid PostgreSQL environment variables.
- [ ] 5.3 Create a multi-stage `frontend/Dockerfile` using the lockfile and a compatible pinned Node image, and verify the image builds and serves the minimal page on the configured port.
- [ ] 5.4 Add appropriate `.dockerignore` files for both projects and verify build contexts exclude generated outputs, local dependencies, IDE metadata and secret-bearing environment files.

## 6. Docker Compose integration

- [ ] 6.1 Create root `docker-compose.yml` with PostgreSQL, backend and frontend services, configurable port mappings and a persistent PostgreSQL volume; verify `docker compose config` succeeds.
- [ ] 6.2 Pin the official PostgreSQL image to an explicit available `16.x` patch tag and add a `pg_isready` healthcheck using configured credentials; verify the rendered Compose config uses neither `latest` nor the floating `16` tag and reports the database healthy when ready.
- [ ] 6.3 Configure backend dependency on PostgreSQL `service_healthy`, internal JDBC hostname resolution and startup-time Flyway/Hibernate validation; verify backend startup is withheld while `pg_isready` is unhealthy and can begin only after PostgreSQL becomes healthy.
- [ ] 6.4 Add the backend Compose healthcheck against `GET /actuator/health` and verify a merely running process remains unhealthy until the endpoint returns HTTP success with state `UP`.
- [ ] 6.5 Configure frontend dependency on backend `service_healthy` and `NEXT_PUBLIC_API_URL` for browser access, and verify the frontend remains withheld until the backend is healthy and then becomes reachable through its configured host port.

## 7. Documentation and final verification

- [ ] 7.1 Update root documentation for the independent root/frontend Node manifests, `backend/` and `frontend/` layout, approved Spring matrix, temporary security, Actuator healthcheck, environment setup, Maven/frontend commands, Testcontainers requirement and `docker compose up --build`; verify every documented command and path matches the repository.
- [ ] 7.2 Run the complete backend test suite and package build from `backend/`, and verify all unit/integration tests pass against PostgreSQL Testcontainers with no H2 dependency or generated domain schema.
- [ ] 7.3 Run clean frontend installation, lint/type checks and production build from `frontend/`, and verify all commands complete successfully.
- [ ] 7.4 Run `docker compose config` followed by `docker compose up --build`, and verify the observed order is PostgreSQL healthy via `pg_isready`, backend healthy via `/actuator/health`, then frontend startup; also verify Swagger, health and the frontend root page respond on their documented ports.
- [ ] 7.5 Review the final diff against the explicit non-goals and verify it contains no Usuario, authentication/JWT, definitive RBAC, Carteira, Corretora, Acao, Posicao, Transacao, MovimentacaoCaixa, CarteiraSnapshot, LogAuditoria, JPA domain entity, financial rule, real external integration, complete domain schema or functional dashboard/business screen.
