# Graph Report - projetoJeff  (2026-09-05)

## Corpus Check
- 108 files · ~46,600 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 985 nodes · 2066 edges · 74 communities (39 shown, 35 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 92 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Security Foundation
- Audit Ports
- Authentication Errors
- Authentication Flow
- Frontend Dependencies
- Roles and Wallets
- JWT Configuration
- Bearer Auth Specification
- Archived Bearer Auth
- Identity Wallet Specification
- Access Denied Tests
- Archived Identity Wallet
- TypeScript Configuration
- Token Issuing Services
- Audit Persistence Adapter
- Identity Domain Tests
- Security Application Ports
- OpenSpec Tooling
- Registration Workflow
- Security Auditing Specification
- Archived Security Auditing
- Frontend Components
- Identity Access Design
- OpenAPI Configuration
- Foundation Spec Changes
- Maven Wrapper
- Current Principal Service
- Initial Admin Bootstrap
- Implementation Task Plan
- Identity Database Tests
- Foundation Change Documents
- Application Foundation Tests
- Registration Transaction Tests
- Change Proposal
- OpenSpec Workflows
- Database Migrations
- Flyway Validation Tests
- PostgreSQL Test Support
- Exception Handler Tests
- Spring Boot Application
- Initial Admin Tests
- Registration Concurrency Tests
- Frontend Layout
- Graph Query Memory
- Project Governance
- Frontend Home Page
- JWT Configuration Spec
- Incompatible Migration Fixture
- Initial Migration Fixture
- Next.js Configuration
- Next Environment Types
- Frontend Utilities
- Identity Access Rationale
- Audit Rationale
- Docker Application Stack
- Impact Analysis Record
- Archived Foundation Spec
- Archived Bearer Spec
- Archived Wallet Spec
- Archived Audit Spec
- Archived Identity Tasks
- Archived Access Denied Spec
- Archived Audit Tasks
- Healthcheck Readiness
- Archived Identity Change
- Archived Audit Change
- Foundation Specification
- Identity Wallet Specification
- Maven Coordinates
- Investment Platform Concept
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
9. `AccessDeniedAuditingService` - 21 edges
10. `PersistedUserJwtAuthenticationConverter` - 21 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `RegistrationTransactionIntegrationTest` --references--> `CarteiraInvestimentoApplication`  [EXTRACTED]
  backend/src/test/java/com/carteira/carteiraInvestimento/integration/RegistrationTransactionIntegrationTest.java → backend/src/main/java/com/carteira/carteiraInvestimento/CarteiraInvestimentoApplication.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (74 total, 35 thin omitted)

### Community 0 - "Security Foundation"
Cohesion: 0.06
Nodes (41): FoundationSecurityConfiguration, BcryptPasswordHasher, Override, PersistedUserJwtAuthenticationConverter, FoundationSecurityConfigurationTest, SecurityTestConfiguration, PersistedUserJwtAuthenticationConverterTest, SecurityTestConfiguration (+33 more)

### Community 1 - "Audit Ports"
Cohesion: 0.06
Nodes (29): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+21 more)

### Community 2 - "Authentication Errors"
Cohesion: 0.08
Nodes (27): AuthenticationFailedException, DuplicateEmailException, AccessDeniedAuditingService, Override, SecurityProblemDetailHandler, Override, GlobalExceptionHandler, ProblemDetailFactory (+19 more)

### Community 3 - "Authentication Flow"
Cohesion: 0.06
Nodes (22): Authentication, IssuedAccessToken, CurrentPrincipalController, CurrentUserResponse, LoginController, LoginRequest, LoginResponse, RegisteredUserResponse (+14 more)

### Community 4 - "Frontend Dependencies"
Cohesion: 0.04
Nodes (47): class-variance-authority, clsx, eslint, eslint-config-next, dependencies, class-variance-authority, clsx, lucide-react (+39 more)

### Community 5 - "Roles and Wallets"
Cohesion: 0.07
Nodes (18): Role, ROLE_ADMIN, ROLE_USER, CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, LogAuditoriaJpaRepository (+10 more)

### Community 6 - "JWT Configuration"
Cohesion: 0.12
Nodes (15): AdminProperties, JwtConfiguration, JwtProperties, Override, JwtAccessTokenIssuer, JwtAccessTokenIssuerTest, jakarta.validation.constraints.AssertTrue, javax.crypto.spec.SecretKeySpec (+7 more)

### Community 7 - "Bearer Auth Specification"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 8 - "Archived Bearer Auth"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 9 - "Identity Wallet Specification"
Cohesion: 0.07
Nodes (29): Identity And Primary Wallet Specification, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+21 more)

### Community 10 - "Access Denied Tests"
Cohesion: 0.13
Nodes (6): AuditSecurityEventsIntegrationTest.AccessDeniedProbeConfiguration, AuditSecurityEventsIntegrationTest, DuplicateEmailPersistenceIntegrationTest, DataSource, org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc, org.springframework.transaction.support.TransactionTemplate

### Community 11 - "Archived Identity Wallet"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 12 - "TypeScript Configuration"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 13 - "Token Issuing Services"
Cohesion: 0.16
Nodes (7): Usuario, Fakes, IdentityApplicationServiceTest, Override, TestUsuarios, Override, TestUsuarios

### Community 14 - "Audit Persistence Adapter"
Cohesion: 0.17
Nodes (9): CarteiraPort, AuditoriaPersistenceAdapter, Override, IdentityPersistenceAdapter, Override, FaultInjectionConfiguration, FaultSwitches, org.springframework.context.annotation.Primary (+1 more)

### Community 15 - "Identity Domain Tests"
Cohesion: 0.13
Nodes (7): IdentityDomainTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, MissingDatabaseConfigurationTest, org.junit.jupiter.api.Test, org.springframework.boot.test.context.runner.ApplicationContextRunner

### Community 16 - "Security Application Ports"
Cohesion: 0.27
Nodes (9): AccessTokenIssuer, AuditoriaIsoladaPort, AuditoriaPort, PasswordHasher, InitialAdminBootstrapService, LoginService, RegistrationService, IdentityApplicationConfiguration (+1 more)

### Community 17 - "OpenSpec Tooling"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 18 - "Registration Workflow"
Cohesion: 0.14
Nodes (3): Carteira, RegistrationControllerTest, org.springframework.transaction.annotation.Transactional

### Community 19 - "Security Auditing Specification"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 20 - "Archived Security Auditing"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 21 - "Frontend Components"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 22 - "Identity Access Design"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 23 - "OpenAPI Configuration"
Cohesion: 0.20
Nodes (8): OpenApiConfiguration, OpenApiConfigurationTest, PropertiesConfiguration, io.swagger.v3.oas.models.OpenAPI, LocalValidatorFactoryBean, OpenAPI, org.springframework.context.annotation.Configuration, org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

### Community 24 - "Foundation Spec Changes"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 25 - "Maven Wrapper"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 28 - "Implementation Task Plan"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 30 - "Foundation Change Documents"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 33 - "Change Proposal"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 34 - "OpenSpec Workflows"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 35 - "Database Migrations"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 37 - "PostgreSQL Test Support"
Cohesion: 0.53
Nodes (4): PostgreSqlContainerSupport, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer

### Community 39 - "Spring Boot Application"
Cohesion: 0.60
Nodes (3): CarteiraInvestimentoApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 40 - "Initial Admin Tests"
Cohesion: 0.60
Nodes (3): InitialAdminBootstrapIntegrationTest, org.springframework.boot.test.context.SpringBootTest, org.springframework.jdbc.core.JdbcTemplate

### Community 43 - "Graph Query Memory"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 44 - "Project Governance"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

## Knowledge Gaps
- **270 isolated node(s):** `metadata`, `Purpose`, `Scenario: Acesso a rota publica`, `Scenario: Acesso anonimo a rota privada`, `Scenario: Acesso negado apos autenticacao` (+265 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Usuario` connect `Token Issuing Services` to `Security Foundation`, `Authentication Errors`, `Authentication Flow`, `Roles and Wallets`, `JWT Configuration`, `Access Denied Tests`, `Audit Persistence Adapter`, `Security Application Ports`, `Registration Workflow`, `Current Principal Service`, `Initial Admin Bootstrap`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Why does `IdentityPersistenceAdapter` connect `Audit Persistence Adapter` to `Access Denied Tests`, `Current Principal Service`, `Initial Admin Bootstrap`, `Roles and Wallets`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Why does `Role` connect `Roles and Wallets` to `Security Foundation`, `Authentication Errors`, `Authentication Flow`, `JWT Configuration`, `Access Denied Tests`, `Token Issuing Services`, `Initial Admin Bootstrap`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `AuditoriaCommand` (e.g. with `.provision()` and `.login()`) actually correct?**
  _`AuditoriaCommand` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `metadata`, `Purpose`, `Scenario: Acesso a rota publica` to the rest of the system?**
  _270 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Security Foundation` be split into smaller, more focused modules?**
  _Cohesion score 0.05965324713488099 - nodes in this community are weakly interconnected._
- **Should `Audit Ports` be split into smaller, more focused modules?**
  _Cohesion score 0.057297297297297295 - nodes in this community are weakly interconnected._