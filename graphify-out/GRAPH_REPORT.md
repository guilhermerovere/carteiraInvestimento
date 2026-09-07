# Graph Report - projetoJeff  (2026-09-07)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1150 nodes · 2433 edges · 87 communities (38 shown, 39 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 95 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f9ca1fe1`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Usuario
- types.ts
- org.springframework.context.annotation.Bean
- jakarta.servlet.http.HttpServletRequest
- auth.ts
- Role
- AuditoriaCommand
- JwtProperties
- org.springframework.web.bind.annotation.GetMapping
- Requirements
- ADDED Requirements
- Requirement: Cadastro publico transacional
- ADDED Requirements
- compilerOptions
- RegistrationTransactionIntegrationTest
- AuditSecurityEventsIntegrationTest
- org.junit.jupiter.api.Test
- package.json
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- ADDED Requirements
- PersistedUserJwtAuthenticationConverterTest
- components.json
- Decisions
- Requirement: Documentacao HTTP e erros padronizados
- mvnw
- tasks.md
- PostgreSqlContainerSupport
- scripts
- SecurityConfigurationPropertiesTest.java
- IdentitySchemaIntegrationTest
- Project Foundation Proposal
- ApplicationFoundationIT
- proposal.md
- OpenSpec Apply Change Workflow
- usuarios
- CorrelationIdFilterTest
- FlywayValidationIT
- InitialAdminBootstrapIntegrationTest
- RegistrationConcurrencyIntegrationTest
- app/layout.tsx
- Frontend Layout
- Spec-Driven Development Policy
- IdentityDomainTest
- frontend/package.json
- app/page.tsx
- JWT Application Configuration
- incompatible/V1__foundation_probe.sql
- initial/V1__foundation_probe.sql
- eslint-config-next
- next.config.ts
- next-env.d.ts
- tailwindcss
- @testing-library/jest-dom
- @testing-library/user-event
- @types/react
- @types/react-dom
- postcss.config.mjs
- Identity and Access Design
- Access-Denied Auditing Design
- Docker Application Stack
- Existing Impact Analysis
- Archived Backend Foundation Specification
- Archived Bearer Authentication Specification
- Archived Identity and Primary Wallet Specification
- Archived Security Event Auditing Specification
- Identity and Access Tasks
- Archived Access-Denied Auditing Specification
- Access-Denied Auditing Tasks
- Healthcheck Readiness Chain
- Archived Identity and Access Change
- Archived Access-Denied Auditing Change
- Backend Foundation Specification
- com.carteira:backend
- Investment Management Platform
- Project Documentation

## God Nodes (most connected - your core abstractions)
1. `Usuario` - 54 edges
2. `AuditoriaCommand` - 35 edges
3. `UsuarioPort` - 34 edges
4. `AuditSecurityEventsIntegrationTest` - 34 edges
5. `AuditoriaPort` - 28 edges
6. `PersistedUserJwtAuthenticationConverterTest` - 27 edges
7. `Role` - 25 edges
8. `AuditoriaIsoladaPort` - 22 edges
9. `PersistedUserJwtAuthenticationConverter` - 21 edges
10. `AccessDeniedAuditingService` - 21 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `IdentityPersistenceAdapter` --implements--> `CarteiraPort`  [EXTRACTED]
  backend/src/main/java/com/carteira/carteiraInvestimento/infrastructure/persistence/IdentityPersistenceAdapter.java → backend/src/main/java/com/carteira/carteiraInvestimento/application/port/CarteiraPort.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (87 total, 39 thin omitted)

### Community 0 - "Usuario"
Cohesion: 0.05
Nodes (27): AccessTokenIssuer, IssuedAccessToken, AuditoriaPort, CarteiraPort, PasswordHasher, UsuarioPort, CurrentPrincipalService, InitialAdminBootstrapService (+19 more)

### Community 1 - "types.ts"
Cohesion: 0.07
Nodes (48): POST(), { loginMock, originMock }, POST(), fetchMock, GET(), { resolveMock }, POST(), { registerMock, originMock } (+40 more)

### Community 2 - "org.springframework.context.annotation.Bean"
Cohesion: 0.07
Nodes (38): AuditoriaIsoladaPort, FoundationSecurityConfiguration, BcryptPasswordHasher, Override, PersistedUserJwtAuthenticationConverter, FoundationSecurityConfigurationTest, SecurityTestConfiguration, SecurityTestConfiguration (+30 more)

### Community 3 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.07
Nodes (31): AuthenticationFailedException, DuplicateEmailException, AccessDeniedAuditingService, Override, SecurityProblemDetailHandler, CorrelationIdFilter, Override, GlobalExceptionHandler (+23 more)

### Community 4 - "auth.ts"
Cohesion: 0.06
Nodes (40): AccessDeniedPage(), fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), LoginPage(), AdminPage(), InicioPage() (+32 more)

### Community 5 - "Role"
Cohesion: 0.06
Nodes (22): Role, ROLE_ADMIN, ROLE_USER, CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, IdentityPersistenceAdapter (+14 more)

### Community 6 - "AuditoriaCommand"
Cohesion: 0.08
Nodes (23): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+15 more)

### Community 7 - "JwtProperties"
Cohesion: 0.09
Nodes (22): AdminProperties, JwtConfiguration, JwtProperties, OpenApiConfiguration, Override, JwtAccessTokenIssuer, OpenApiConfigurationTest, PropertiesConfiguration (+14 more)

### Community 8 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.10
Nodes (14): CurrentPrincipalController, CurrentUserResponse, LoginController, RegistrationController, SecurityProbeController, AdminProbeController, AccessDeniedProbeController, FailureProbeController (+6 more)

### Community 9 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 10 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 11 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 12 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 13 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 14 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.12
Nodes (11): CarteiraInvestimentoApplication, AuditoriaPersistenceAdapter, Override, LogAuditoriaJpaRepository, FaultInjectionConfiguration, FaultSwitches, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication (+3 more)

### Community 15 - "AuditSecurityEventsIntegrationTest"
Cohesion: 0.19
Nodes (4): AuditSecurityEventsIntegrationTest.AccessDeniedProbeConfiguration, AuditSecurityEventsIntegrationTest, org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc, org.springframework.transaction.support.TransactionTemplate

### Community 16 - "org.junit.jupiter.api.Test"
Cohesion: 0.14
Nodes (6): PasswordSecurityConfigurationTest, AuditoriaPersistenceAdapterTest, MissingDatabaseConfigurationTest, RegistrationControllerTest, GlobalExceptionHandlerTest, org.junit.jupiter.api.Test

### Community 17 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 18 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 19 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 20 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 21 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 22 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.21
Nodes (6): Authentication, PersistedUserJwtAuthenticationConverterTest, ProtectedProbeController, PersistedUserJwtAuthenticationConverterTest.AdminProbeController, PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController, PersistedUserJwtAuthenticationConverterTest.SecurityTestConfiguration

### Community 23 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 24 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 25 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 26 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 27 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 28 - "PostgreSqlContainerSupport"
Cohesion: 0.31
Nodes (6): AuditPersistenceIntegrationTest, DataSource, PostgreSqlContainerSupport, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer

### Community 29 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 30 - "SecurityConfigurationPropertiesTest.java"
Cohesion: 0.29
Nodes (3): SecurityConfigurationPropertiesTest, org.springframework.boot.test.context.runner.ApplicationContextRunner, org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

### Community 32 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 34 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 35 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 36 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 42 - "Frontend Layout"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 43 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 45 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **296 isolated node(s):** `AuthConfig`, `AuthEnv`, `BackendErrorKind`, `AuthProblem`, `{ loginMock, originMock }` (+291 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 392 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **39 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Usuario` connect `Usuario` to `org.springframework.context.annotation.Bean`, `jakarta.servlet.http.HttpServletRequest`, `Role`, `JwtProperties`, `org.springframework.web.bind.annotation.GetMapping`, `AuditSecurityEventsIntegrationTest`, `org.junit.jupiter.api.Test`, `PersistedUserJwtAuthenticationConverterTest`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Why does `AuditSecurityEventsIntegrationTest` connect `AuditSecurityEventsIntegrationTest` to `Usuario`, `org.springframework.context.annotation.Bean`, `jakarta.servlet.http.HttpServletRequest`, `AuditoriaCommand`, `JwtProperties`, `org.springframework.web.bind.annotation.GetMapping`, `PostgreSqlContainerSupport`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **Why does `AuditoriaCommand` connect `AuditoriaCommand` to `Usuario`, `org.springframework.context.annotation.Bean`, `jakarta.servlet.http.HttpServletRequest`, `RegistrationTransactionIntegrationTest`, `AuditSecurityEventsIntegrationTest`, `org.junit.jupiter.api.Test`, `PostgreSqlContainerSupport`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `AuditoriaCommand` (e.g. with `.provision()` and `.login()`) actually correct?**
  _`AuditoriaCommand` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `AuthConfig`, `AuthEnv`, `BackendErrorKind` to the rest of the system?**
  _296 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Usuario` be split into smaller, more focused modules?**
  _Cohesion score 0.05078929306794784 - nodes in this community are weakly interconnected._
- **Should `types.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.06772151898734177 - nodes in this community are weakly interconnected._