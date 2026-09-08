# Graph Report - projetoJeff  (2026-09-08)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1384 nodes · 3095 edges · 106 communities (61 shown, 35 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 165 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9eb74e2b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- types.ts
- org.junit.jupiter.api.Test
- auth.ts
- IdentityPersistenceAdapter
- jakarta.servlet.http.HttpServletRequest
- JwtProperties
- AtivoControllerTest.java
- IdentityApplicationConfiguration.java
- Ativo
- Requirements
- RegistrationTransactionIntegrationTest
- ADDED Requirements
- Requirement: Cadastro publico transacional
- ADDED Requirements
- compilerOptions
- TipoAtivo
- PersistedUserJwtAuthenticationConverterTest.java
- TipoEvento
- org.springframework.web.bind.annotation.GetMapping
- AtivoUseCase
- .create
- package.json
- AuditSecurityEventsIntegrationTest
- .register
- org.junit.jupiter.api.BeforeEach
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- LoginService
- AuditoriaCommand
- UsuarioPort
- ADDED Requirements
- SeveridadeAuditoria
- AuditSecurityEventsIntegrationTest.java
- components.json
- Decisions
- org.springframework.context.annotation.Bean
- org.springframework.stereotype.Component
- AtivoPersistenceAdapter
- PersistedUserJwtAuthenticationConverterTest
- Fakes
- AuditoriaPort
- PasswordHasher
- .provision
- AtivoControllerTest
- DuplicateTickerException
- Requirement: Documentacao HTTP e erros padronizados
- Usuario
- PersistedUserJwtAuthenticationConverter
- mvnw
- SecurityProblemDetailHandlerTest
- tasks.md
- .register
- scripts
- LoginControllerTest.java
- Project Foundation Proposal
- AuditoriaPersistenceAdapter
- proposal.md
- OpenSpec Apply Change Workflow
- usuarios
- app/layout.tsx
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- NomeAtivoCanonicalizer
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
2. `Ativo` - 38 edges
3. `AuditoriaCommand` - 35 edges
4. `AuditSecurityEventsIntegrationTest` - 34 edges
5. `TipoAtivo` - 30 edges
6. `UsuarioPort` - 29 edges
7. `PersistedUserJwtAuthenticationConverterTest` - 27 edges
8. `Mercado` - 26 edges
9. `Role` - 25 edges
10. `AuditoriaPort` - 24 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `IdentitySchemaIntegrationTest` --inherits--> `PostgreSqlContainerSupport`  [EXTRACTED]
  backend/src/test/java/com/carteira/carteiraInvestimento/integration/IdentitySchemaIntegrationTest.java → backend/src/test/java/com/carteira/carteiraInvestimento/integration/PostgreSqlContainerSupport.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (106 total, 35 thin omitted)

### Community 0 - "types.ts"
Cohesion: 0.07
Nodes (48): POST(), { loginMock, originMock }, POST(), fetchMock, GET(), { resolveMock }, POST(), { registerMock, originMock } (+40 more)

### Community 1 - "org.junit.jupiter.api.Test"
Cohesion: 0.05
Nodes (16): AtivoDomainTest, IdentityDomainTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, AssetCatalogPersistenceIT, SqlAction, FlywayValidationIT (+8 more)

### Community 2 - "auth.ts"
Cohesion: 0.06
Nodes (40): AccessDeniedPage(), fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), LoginPage(), AdminPage(), InicioPage() (+32 more)

### Community 3 - "IdentityPersistenceAdapter"
Cohesion: 0.06
Nodes (18): CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, IdentityPersistenceAdapter, Override, Override, RepositoryUserDetailsService (+10 more)

### Community 4 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.11
Nodes (24): AtivoNotFoundException, Override, Override, GlobalExceptionHandler, com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException, com.carteira.carteiraInvestimento.application.service.DuplicateEmailException, com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService, jakarta.servlet.FilterChain (+16 more)

### Community 5 - "JwtProperties"
Cohesion: 0.09
Nodes (22): AdminProperties, JwtConfiguration, JwtProperties, OpenApiConfiguration, Override, JwtAccessTokenIssuer, OpenApiConfigurationTest, PropertiesConfiguration (+14 more)

### Community 6 - "AtivoControllerTest.java"
Cohesion: 0.11
Nodes (15): AtivoPage, AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME (+7 more)

### Community 7 - "IdentityApplicationConfiguration.java"
Cohesion: 0.11
Nodes (21): AdminProperties, IdentityApplicationConfiguration, AssetCatalogSecurityIT, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort, com.carteira.carteiraInvestimento.application.port.AuditoriaPort, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher (+13 more)

### Community 8 - "Ativo"
Cohesion: 0.17
Nodes (7): AtivoPort, AtivoApplicationService, Override, Ativo, TickerCanonicalizer, AtivoApplicationServiceTest, org.springframework.transaction.annotation.Transactional

### Community 9 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 10 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.11
Nodes (13): CarteiraInvestimentoApplication, PostgreSqlContainerSupport, RegistrationConcurrencyIntegrationTest, FaultInjectionConfiguration, FaultSwitches, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan (+5 more)

### Community 11 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 12 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 13 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 14 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 15 - "TipoAtivo"
Cohesion: 0.15
Nodes (12): Mercado, B3, US, Moeda, BRL, USD, TipoAtivo, ACAO (+4 more)

### Community 16 - "PersistedUserJwtAuthenticationConverterTest.java"
Cohesion: 0.22
Nodes (17): AuditoriaIsoladaPort, AccessDeniedAuditingService, SecurityProblemDetailHandler, ProblemDetailFactory, FoundationSecurityConfigurationTest, SecurityTestConfiguration, CurrentPrincipalControllerTest, CurrentPrincipalControllerTest.SecurityTestConfiguration (+9 more)

### Community 17 - "TipoEvento"
Cohesion: 0.12
Nodes (14): ResultadoAuditoria, FALHA, NEGADO, SUCESSO, TipoEvento, ACESSO_NEGADO, ADMIN_INICIAL_CRIADO, CADASTRO_USUARIO (+6 more)

### Community 18 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.14
Nodes (8): Authentication, SecurityProbeController, AdminProbeController, ProtectedProbeController, AccessDeniedProbeController, FailureProbeController, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.RestController

### Community 19 - "AtivoUseCase"
Cohesion: 0.20
Nodes (5): AtivoUseCase, AtivoController, org.springframework.security.access.prepost.PreAuthorize, org.springframework.security.core.Authentication, org.springframework.web.bind.annotation.PatchMapping

### Community 20 - ".create"
Cohesion: 0.16
Nodes (5): CreateAtivoRequest, UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator

### Community 21 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 22 - "AuditSecurityEventsIntegrationTest"
Cohesion: 0.20
Nodes (3): AuditSecurityEventsIntegrationTest.AccessDeniedProbeConfiguration, AuditSecurityEventsIntegrationTest, org.springframework.transaction.support.TransactionTemplate

### Community 23 - ".register"
Cohesion: 0.17
Nodes (6): CarteiraPort, RegistrationService, Carteira, RegistrationController, RegistrationControllerTest, org.springframework.stereotype.Repository

### Community 24 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.17
Nodes (7): ApplicationFoundationIT, javax.sql.DataSource, org.flywaydb.core.Flyway, org.junit.jupiter.api.BeforeEach, org.springframework.beans.factory.annotation.Autowired, org.springframework.boot.test.context.SpringBootTest, org.springframework.web.context.WebApplicationContext

### Community 25 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 26 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 27 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 28 - "LoginService"
Cohesion: 0.20
Nodes (7): AccessTokenIssuer, IssuedAccessToken, LoginService, LoginController, LoginRequest, LoginResponse, org.springframework.web.bind.annotation.RequestMapping

### Community 29 - "AuditoriaCommand"
Cohesion: 0.19
Nodes (3): AuditoriaCommand, AccessDeniedAuditingServiceTest, MockHttpServletRequest

### Community 30 - "UsuarioPort"
Cohesion: 0.18
Nodes (6): UsuarioPort, CurrentPrincipalService, CurrentPrincipalController, CurrentUserResponse, io.swagger.v3.oas.annotations.security.SecurityRequirement, org.springframework.http.ResponseEntity

### Community 31 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 32 - "SeveridadeAuditoria"
Cohesion: 0.26
Nodes (6): SeveridadeAuditoria, ALERTA, AVISO, INFO, org.slf4j.Logger, org.springframework.mock.web.MockHttpServletRequest

### Community 33 - "AuditSecurityEventsIntegrationTest.java"
Cohesion: 0.19
Nodes (8): DuplicateEmailException, Role, ROLE_ADMIN, ROLE_USER, CorrelationIdFilter, ch.qos.logback.classic.Logger, org.springframework.core.annotation.Order, org.springframework.web.filter.OncePerRequestFilter

### Community 34 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 35 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 36 - "org.springframework.context.annotation.Bean"
Cohesion: 0.22
Nodes (6): SecurityTestConfiguration, AccessDeniedProbeConfiguration, SecurityTestConfiguration, org.springframework.context.annotation.Bean, org.springframework.security.config.annotation.web.builders.HttpSecurity, org.springframework.security.web.SecurityFilterChain

### Community 37 - "org.springframework.stereotype.Component"
Cohesion: 0.21
Nodes (7): FoundationSecurityConfiguration, BcryptPasswordHasher, org.springframework.security.authentication.AuthenticationManager, org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity, org.springframework.security.core.userdetails.UserDetailsService, org.springframework.security.crypto.password.PasswordEncoder, org.springframework.stereotype.Component

### Community 38 - "AtivoPersistenceAdapter"
Cohesion: 0.24
Nodes (4): AtivoJpaRepository, AtivoPersistenceAdapter, Override, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 39 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.27
Nodes (4): PersistedUserJwtAuthenticationConverterTest, PersistedUserJwtAuthenticationConverterTest.AdminProbeController, PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController, PersistedUserJwtAuthenticationConverterTest.SecurityTestConfiguration

### Community 41 - "AuditoriaPort"
Cohesion: 0.21
Nodes (6): AuditoriaPort, CorrelationIdFilterTest, CorrelationProbeController, AuditPersistenceIntegrationTest, DataSource, org.springframework.test.web.servlet.MvcResult

### Community 42 - "PasswordHasher"
Cohesion: 0.23
Nodes (3): PasswordHasher, EmailCanonicalizer, PasswordPolicy

### Community 43 - ".provision"
Cohesion: 0.24
Nodes (3): InitialAdminBootstrapService, InitialAdminBootstrapIntegrationTest, org.springframework.jdbc.core.JdbcTemplate

### Community 44 - "AtivoControllerTest"
Cohesion: 0.23
Nodes (3): AtivoControllerTest, org.springframework.security.authentication.TestingAuthenticationToken, TestingAuthenticationToken

### Community 45 - "DuplicateTickerException"
Cohesion: 0.23
Nodes (4): DuplicateTickerException, AtivoPersistenceAdapterTest, DataIntegrityViolationException, org.springframework.dao.DataIntegrityViolationException

### Community 46 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 47 - "Usuario"
Cohesion: 0.36
Nodes (5): Usuario, Override, TestUsuarios, Override, TestUsuarios

### Community 48 - "PersistedUserJwtAuthenticationConverter"
Cohesion: 0.36
Nodes (7): Override, PersistedUserJwtAuthenticationConverter, InvalidBearerTokenException, org.springframework.core.convert.converter.Converter, org.springframework.security.authentication.AbstractAuthenticationToken, org.springframework.security.oauth2.jwt.Jwt, org.springframework.security.oauth2.server.resource.InvalidBearerTokenException

### Community 49 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 50 - "SecurityProblemDetailHandlerTest"
Cohesion: 0.27
Nodes (3): MockHttpServletRequest, SecurityProblemDetailHandlerTest, org.junit.jupiter.api.AfterEach

### Community 51 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 52 - ".register"
Cohesion: 0.25
Nodes (3): RegisteredUserResponse, RegisterRequest, org.springframework.web.bind.annotation.PostMapping

### Community 53 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 55 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 56 - "AuditoriaPersistenceAdapter"
Cohesion: 0.43
Nodes (3): AuditoriaPersistenceAdapter, Override, LogAuditoriaJpaRepository

### Community 57 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 58 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 59 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 61 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 62 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 64 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **310 isolated node(s):** `AuthConfig`, `AuthEnv`, `BackendErrorKind`, `AuthProblem`, `{ loginMock, originMock }` (+305 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 426 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Usuario` connect `Usuario` to `AuditSecurityEventsIntegrationTest.java`, `IdentityPersistenceAdapter`, `JwtProperties`, `PersistedUserJwtAuthenticationConverterTest`, `Fakes`, `PasswordHasher`, `.provision`, `PersistedUserJwtAuthenticationConverter`, `PersistedUserJwtAuthenticationConverterTest.java`, `.register`, `AuditSecurityEventsIntegrationTest`, `.register`, `org.junit.jupiter.api.BeforeEach`, `LoginService`, `UsuarioPort`?**
  _High betweenness centrality (0.031) - this node is a cross-community bridge._
- **Why does `TipoAtivo` connect `TipoAtivo` to `AtivoControllerTest.java`, `IdentityApplicationConfiguration.java`, `Ativo`, `AtivoControllerTest`, `DuplicateTickerException`, `AtivoUseCase`, `.create`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **Why does `Ativo` connect `Ativo` to `org.junit.jupiter.api.Test`, `AtivoControllerTest.java`, `AtivoPersistenceAdapter`, `AtivoControllerTest`, `DuplicateTickerException`, `TipoAtivo`, `AtivoUseCase`?**
  _High betweenness centrality (0.013) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Ativo` (e.g. with `.updatesOnlyNameAndLifecycle()` and `.requiresControlledTypeAndConsistentCurrency()`) actually correct?**
  _`Ativo` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `AuditoriaCommand` (e.g. with `.provision()` and `.login()`) actually correct?**
  _`AuditoriaCommand` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `AuthConfig`, `AuthEnv`, `BackendErrorKind` to the rest of the system?**
  _310 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `types.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.06772151898734177 - nodes in this community are weakly interconnected._