# Graph Report - projetoJeff  (2026-09-03)

## Corpus Check
- 128 files · ~43,259 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 919 nodes · 1880 edges · 43 communities (32 shown, 8 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 83 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `38c0b0de`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- org.springframework.context.annotation.Bean
- Project Foundation Documentation
- package.json
- dependencies
- Usuario
- compilerOptions
- components.json
- JwtProperties
- jakarta.servlet.http.HttpServletRequest
- mvnw
- IdentityPersistenceAdapter
- TipoEvento
- org.junit.jupiter.api.Test
- OpenSpec Apply Change Workflow
- RegistrationTransactionIntegrationTest
- layout.tsx
- page.tsx
- Requirements
- ADDED Requirements
- next.config.ts
- next-env.d.ts
- postcss.config.mjs
- com.carteira:backend
- Requirement: Cadastro publico transacional
- ADDED Requirements
- UsuarioPort
- Fakes
- Requirement: Eventos minimos de seguranca
- ADDED Requirements
- AuditoriaCommand
- Decisions
- AuditSecurityEventsIntegrationTest
- RegistrationControllerTest.java
- RegistrationService
- Requirement: Documentacao HTTP e erros padronizados
- tasks.md
- DuplicateEmailPersistenceIntegrationTest
- AuditoriaPersistenceAdapter
- proposal.md
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso

## God Nodes (most connected - your core abstractions)
1. `Usuario` - 54 edges
2. `UsuarioPort` - 34 edges
3. `AuditoriaPort` - 28 edges
4. `AuditoriaCommand` - 27 edges
5. `PersistedUserJwtAuthenticationConverterTest` - 26 edges
6. `Role` - 24 edges
7. `PasswordHasher` - 20 edges
8. `Fakes` - 20 edges
9. `RegistrationService` - 19 edges
10. `JwtProperties` - 19 edges

## Surprising Connections (you probably didn't know these)
- `Project Foundation Documentation` --conceptually_related_to--> `Frontend Foundation Specification`  [INFERRED]
  README.md → openspec/specs/frontend-foundation/spec.md
- `Backend Application Configuration` --implements--> `Backend Foundation Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/backend-foundation/spec.md
- `Local Service Orchestration` --implements--> `Containerized Local Environment Specification`  [INFERRED]
  docker-compose.yml → openspec/specs/containerized-local-environment/spec.md
- `Healthcheck Readiness Chain` --rationale_for--> `Local Service Orchestration`  [INFERRED]
  openspec/changes/archive/2026-08-31-establish-project-foundation/design.md → docker-compose.yml
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — docker_compose_local_service_orchestration, backend_src_main_resources_application_application_configuration, openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_backend_foundation_spec_backend_foundation, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (43 total, 8 thin omitted)

### Community 0 - "org.springframework.context.annotation.Bean"
Cohesion: 0.06
Nodes (43): Authentication, FoundationSecurityConfiguration, Override, PersistedUserJwtAuthenticationConverter, FoundationSecurityConfigurationTest, SecurityProbeController, SecurityTestConfiguration, AdminProbeController (+35 more)

### Community 1 - "Project Foundation Documentation"
Cohesion: 0.14
Nodes (21): Codex Project Instructions, Spec-Driven Development Policy, Backend Application Configuration, Local Service Orchestration, Archived Foundation Change Metadata, Healthcheck Readiness Chain, Project Foundation Design, Project Foundation Proposal (+13 more)

### Community 2 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 3 - "dependencies"
Cohesion: 0.04
Nodes (47): class-variance-authority, clsx, eslint, eslint-config-next, dependencies, class-variance-authority, clsx, lucide-react (+39 more)

### Community 4 - "Usuario"
Cohesion: 0.07
Nodes (18): CurrentPrincipalService, Role, ROLE_ADMIN, ROLE_USER, Usuario, CurrentPrincipalController, CurrentUserResponse, RegisteredUserResponse (+10 more)

### Community 5 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 6 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 7 - "JwtProperties"
Cohesion: 0.08
Nodes (23): AdminProperties, JwtConfiguration, JwtProperties, OpenApiConfiguration, Override, JwtAccessTokenIssuer, OpenApiConfigurationTest, PropertiesConfiguration (+15 more)

### Community 8 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.07
Nodes (31): IssuedAccessToken, AuthenticationFailedException, BcryptPasswordHasher, Override, SecurityProblemDetailHandler, CorrelationIdFilter, Override, LoginController (+23 more)

### Community 9 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 10 - "IdentityPersistenceAdapter"
Cohesion: 0.07
Nodes (17): CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, IdentityPersistenceAdapter, Override, LogAuditoriaJpaRepository, Override (+9 more)

### Community 11 - "TipoEvento"
Cohesion: 0.08
Nodes (21): ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO, INFO (+13 more)

### Community 12 - "org.junit.jupiter.api.Test"
Cohesion: 0.05
Nodes (18): IdentityDomainTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, FlywayValidationIT, IdentitySchemaIntegrationTest, InitialAdminBootstrapIntegrationTest, PostgreSqlContainerSupport (+10 more)

### Community 13 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 14 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.17
Nodes (8): CarteiraInvestimentoApplication, FaultInjectionConfiguration, FaultSwitches, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, org.springframework.context.annotation.Primary, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 17 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 18 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 26 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Identity And Primary Wallet Specification, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+21 more)

### Community 27 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 28 - "UsuarioPort"
Cohesion: 0.27
Nodes (9): AccessTokenIssuer, AuditoriaIsoladaPort, AuditoriaPort, PasswordHasher, UsuarioPort, InitialAdminBootstrapService, LoginService, IdentityApplicationConfiguration (+1 more)

### Community 29 - "Fakes"
Cohesion: 0.15
Nodes (4): CarteiraPort, Carteira, Fakes, IdentityApplicationServiceTest

### Community 30 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 31 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 32 - "AuditoriaCommand"
Cohesion: 0.20
Nodes (4): AuditoriaCommand, AuditPersistenceIntegrationTest, DataSource, org.springframework.transaction.annotation.Transactional

### Community 33 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 35 - "RegistrationControllerTest.java"
Cohesion: 0.20
Nodes (3): DuplicateEmailException, EmailCanonicalizer, PasswordPolicy

### Community 36 - "RegistrationService"
Cohesion: 0.30
Nodes (5): RegistrationService, RegistrationController, RegistrationConcurrencyIntegrationTest, javax.sql.DataSource, org.springframework.boot.test.context.SpringBootTest

### Community 37 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 38 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 40 - "AuditoriaPersistenceAdapter"
Cohesion: 0.36
Nodes (3): AuditoriaPersistenceAdapter, Override, org.springframework.stereotype.Repository

### Community 41 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 42 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

## Knowledge Gaps
- **248 isolated node(s):** `com.carteira:backend`, `SUCESSO`, `FALHA`, `NEGADO`, `INFO` (+243 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 316 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **8 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Usuario` connect `Usuario` to `AuditoriaCommand`, `org.springframework.context.annotation.Bean`, `RegistrationControllerTest.java`, `JwtProperties`, `jakarta.servlet.http.HttpServletRequest`, `AuditoriaPersistenceAdapter`, `IdentityPersistenceAdapter`, `org.junit.jupiter.api.Test`, `UsuarioPort`, `Fakes`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Why does `UsuarioPort` connect `UsuarioPort` to `AuditoriaCommand`, `org.springframework.context.annotation.Bean`, `AuditSecurityEventsIntegrationTest`, `RegistrationControllerTest.java`, `Usuario`, `RegistrationService`, `AuditoriaPersistenceAdapter`, `IdentityPersistenceAdapter`, `Fakes`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Why does `IdentityPersistenceAdapter` connect `IdentityPersistenceAdapter` to `RegistrationControllerTest.java`, `RegistrationService`, `DuplicateEmailPersistenceIntegrationTest`, `AuditoriaPersistenceAdapter`, `RegistrationTransactionIntegrationTest`, `UsuarioPort`, `Fakes`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **What connects `com.carteira:backend`, `SUCESSO`, `FALHA` to the rest of the system?**
  _248 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `org.springframework.context.annotation.Bean` be split into smaller, more focused modules?**
  _Cohesion score 0.05600722673893405 - nodes in this community are weakly interconnected._
- **Should `Project Foundation Documentation` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._
- **Should `package.json` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._