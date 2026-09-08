# Graph Report - projetoJeff  (2026-09-08)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1608 nodes · 3803 edges · 114 communities (65 shown, 39 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 212 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5d11d740`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- types.ts
- org.springframework.web.bind.annotation.GetMapping
- auth.ts
- jakarta.servlet.http.HttpServletRequest
- JwtProperties
- org.junit.jupiter.api.Test
- UsuarioJpaEntity
- AuditoriaCommand
- Ativo
- Requirements
- ADDED Requirements
- Requirement: Cadastro publico transacional
- Cotacao
- ADDED Requirements
- compilerOptions
- org.springframework.context.annotation.Bean
- AtivoPage
- AtivoPersistenceAdapter
- TipoAtivo
- AtivoControllerTest.java
- MarketQuoteApplicationService
- org.springframework.boot.test.context.SpringBootTest
- IdentityApplicationConfiguration.java
- RegistrationService
- PersistedUserJwtAuthenticationConverter
- AuditSecurityEventsIntegrationTest
- Fakes
- QuoteProvider
- PersistedUserJwtAuthenticationConverterTest
- HistoricoCotacaoJpaEntity
- org.junit.jupiter.api.BeforeEach
- .create
- PersistedUserJwtAuthenticationConverterTest.java
- package.json
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- UsuarioPort
- ADDED Requirements
- .record
- components.json
- Decisions
- InitialAdminBootstrapService.java
- AssetCatalogPersistenceIT
- AtivoController
- MarketQuoteApplicationServiceTest.java
- PostgreSqlContainerSupport
- Usuario
- .login
- .register
- IdentityPersistenceAdapter
- RegistrationTransactionIntegrationTest
- MarketQuotePersistenceIT
- MarketQuoteSecurityIT
- Requirement: Documentacao HTTP e erros padronizados
- PasswordHasher
- ApplicationFoundationIT
- mvnw
- tasks.md
- IdentitySchemaIntegrationTest
- scripts
- .novo
- Project Foundation Proposal
- proposal.md
- OpenSpec Apply Change Workflow
- usuarios
- app/layout.tsx
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- NomeAtivoCanonicalizer
- CotacaoDomainTest
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
- PostgreSqlContainerSupport
- Investment Management Platform
- Project Documentation

## God Nodes (most connected - your core abstractions)
1. `Usuario` - 54 edges
2. `Ativo` - 38 edges
3. `Cotacao` - 36 edges
4. `AuditoriaCommand` - 35 edges
5. `AuditSecurityEventsIntegrationTest` - 34 edges
6. `QuoteProvider` - 29 edges
7. `UsuarioPort` - 29 edges
8. `TipoAtivo` - 27 edges
9. `PersistedUserJwtAuthenticationConverterTest` - 27 edges
10. `Role` - 25 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `AlphaVantageQuoteAdapter` --implements--> `CotacaoProviderPort`  [EXTRACTED]
  backend/src/main/java/com/carteira/carteiraInvestimento/infrastructure/provider/AlphaVantageQuoteAdapter.java → backend/src/main/java/com/carteira/carteiraInvestimento/application/port/CotacaoProviderPort.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (114 total, 39 thin omitted)

### Community 0 - "types.ts"
Cohesion: 0.07
Nodes (48): POST(), { loginMock, originMock }, POST(), fetchMock, GET(), { resolveMock }, POST(), { registerMock, originMock } (+40 more)

### Community 1 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.06
Nodes (28): Authentication, QuoteIntegrationException, QuoteNotFoundException, MarketQuoteProperties, Provider, AlphaBar, AlphaMeta, AlphaResponse (+20 more)

### Community 2 - "auth.ts"
Cohesion: 0.06
Nodes (40): AccessDeniedPage(), fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), LoginPage(), AdminPage(), InicioPage() (+32 more)

### Community 3 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.09
Nodes (26): InactiveAssetRefreshException, Override, Override, GlobalExceptionHandler, MockHttpServletRequest, SecurityProblemDetailHandlerTest, com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException, com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException (+18 more)

### Community 4 - "JwtProperties"
Cohesion: 0.07
Nodes (24): AccessTokenIssuer, IssuedAccessToken, AdminProperties, JwtConfiguration, JwtProperties, OpenApiConfiguration, Override, JwtAccessTokenIssuer (+16 more)

### Community 5 - "org.junit.jupiter.api.Test"
Cohesion: 0.07
Nodes (10): MarketQuoteApplicationServiceTest, IdentityDomainTest, MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, MissingDatabaseConfigurationTest, GlobalExceptionHandlerTest (+2 more)

### Community 6 - "UsuarioJpaEntity"
Cohesion: 0.07
Nodes (15): CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, LogAuditoriaJpaRepository, Override, RepositoryUserDetailsService, Entity (+7 more)

### Community 7 - "AuditoriaCommand"
Cohesion: 0.12
Nodes (20): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+12 more)

### Community 8 - "Ativo"
Cohesion: 0.16
Nodes (7): AtivoPort, AtivoApplicationService, Override, Ativo, TickerCanonicalizer, AtivoApplicationServiceTest, org.springframework.transaction.annotation.Transactional

### Community 9 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 10 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 11 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 12 - "Cotacao"
Cohesion: 0.15
Nodes (10): HistoricoCotacaoPage, MarketQuoteUseCase, Cotacao, CotacaoResponse, HistoricoCotacaoResponse, MarketQuoteController, MarketQuoteControllerTest, org.springframework.security.authentication.TestingAuthenticationToken (+2 more)

### Community 13 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 14 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 15 - "org.springframework.context.annotation.Bean"
Cohesion: 0.15
Nodes (10): AuditoriaIsoladaPort, AuditoriaPort, LoginService, AuditoriaPersistenceAdapter, Override, FaultInjectionConfiguration, FaultSwitches, ch.qos.logback.classic.Logger (+2 more)

### Community 16 - "AtivoPage"
Cohesion: 0.12
Nodes (6): AtivoPage, AtivoNotFoundException, AtivoUseCase, AtivoListResponse, AtivoControllerTest, TestingAuthenticationToken

### Community 17 - "AtivoPersistenceAdapter"
Cohesion: 0.12
Nodes (8): DuplicateTickerException, AtivoJpaRepository, AtivoPersistenceAdapter, Override, AtivoPersistenceAdapterTest, DataIntegrityViolationException, org.springframework.dao.DataIntegrityViolationException, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 18 - "TipoAtivo"
Cohesion: 0.16
Nodes (12): Mercado, B3, US, Moeda, BRL, USD, TipoAtivo, ACAO (+4 more)

### Community 19 - "AtivoControllerTest.java"
Cohesion: 0.15
Nodes (13): AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME, TICKER (+5 more)

### Community 20 - "MarketQuoteApplicationService"
Cohesion: 0.18
Nodes (7): HistoricoCotacaoPort, Override, MarketQuoteApplicationService, MarketQuoteConfiguration, com.carteira.carteiraInvestimento.application.port.AtivoPort, com.github.benmanes.caffeine.cache.Cache, org.springframework.cloud.openfeign.EnableFeignClients

### Community 21 - "org.springframework.boot.test.context.SpringBootTest"
Cohesion: 0.21
Nodes (11): AssetCatalogSecurityIT, com.carteira.carteiraInvestimento.application.service.AtivoUseCase, com.carteira.carteiraInvestimento.domain.asset.Mercado, com.carteira.carteiraInvestimento.domain.asset.TipoAtivo, com.carteira.carteiraInvestimento.domain.identity.Role, com.carteira.carteiraInvestimento.domain.identity.Usuario, javax.sql.DataSource, org.springframework.beans.factory.annotation.Autowired (+3 more)

### Community 22 - "IdentityApplicationConfiguration.java"
Cohesion: 0.16
Nodes (17): AdminProperties, IdentityApplicationConfiguration, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort, com.carteira.carteiraInvestimento.application.port.AuditoriaPort, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher, com.carteira.carteiraInvestimento.application.port.UsuarioPort (+9 more)

### Community 23 - "RegistrationService"
Cohesion: 0.13
Nodes (10): RegistrationService, LoginController, LoginRequest, LoginResponse, RegisteredUserResponse, RegisterRequest, RegistrationController, org.springframework.http.ResponseEntity (+2 more)

### Community 24 - "PersistedUserJwtAuthenticationConverter"
Cohesion: 0.17
Nodes (12): FoundationSecurityConfiguration, Override, PersistedUserJwtAuthenticationConverter, InvalidBearerTokenException, org.springframework.core.convert.converter.Converter, org.springframework.security.authentication.AbstractAuthenticationToken, org.springframework.security.authentication.AuthenticationManager, org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity (+4 more)

### Community 25 - "AuditSecurityEventsIntegrationTest"
Cohesion: 0.18
Nodes (3): AuditSecurityEventsIntegrationTest.AccessDeniedProbeConfiguration, AuditSecurityEventsIntegrationTest, org.springframework.transaction.support.TransactionTemplate

### Community 26 - "Fakes"
Cohesion: 0.14
Nodes (4): CarteiraPort, Carteira, Fakes, IdentityApplicationServiceTest

### Community 27 - "QuoteProvider"
Cohesion: 0.16
Nodes (8): CotacaoProviderPort, CotacaoExterna, QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA, FakeProvider, com.carteira.carteiraInvestimento.domain.asset.Moeda

### Community 28 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.16
Nodes (10): Role, ROLE_ADMIN, ROLE_USER, AdminProbeController, PersistedUserJwtAuthenticationConverterTest, ProtectedProbeController, org.springframework.web.bind.annotation.RestController, PersistedUserJwtAuthenticationConverterTest.AdminProbeController (+2 more)

### Community 29 - "HistoricoCotacaoJpaEntity"
Cohesion: 0.18
Nodes (6): HistoricoCotacaoJpaEntity, HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable

### Community 30 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.20
Nodes (11): AccessDeniedAuditingService, SecurityProblemDetailHandler, CorrelationIdFilter, ProblemDetailFactory, RegistrationControllerTest, org.junit.jupiter.api.BeforeEach, org.springframework.core.annotation.Order, org.springframework.security.web.access.AccessDeniedHandler (+3 more)

### Community 31 - ".create"
Cohesion: 0.15
Nodes (5): CreateAtivoRequest, UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator

### Community 32 - "PersistedUserJwtAuthenticationConverterTest.java"
Cohesion: 0.25
Nodes (15): FoundationSecurityConfigurationTest, SecurityTestConfiguration, SecurityTestConfiguration, CurrentPrincipalControllerTest, SecurityTestConfiguration, CurrentPrincipalControllerTest.SecurityTestConfiguration, FoundationSecurityConfigurationTest.SecurityProbeController, FoundationSecurityConfigurationTest.SecurityTestConfiguration (+7 more)

### Community 33 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 34 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 35 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 36 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 37 - "UsuarioPort"
Cohesion: 0.17
Nodes (5): UsuarioPort, CurrentPrincipalService, CurrentPrincipalController, CurrentUserResponse, io.swagger.v3.oas.annotations.security.SecurityRequirement

### Community 38 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 39 - ".record"
Cohesion: 0.18
Nodes (5): AccessDeniedAuditingServiceTest, MockHttpServletRequest, CorrelationIdFilterTest, CorrelationProbeController, org.springframework.test.web.servlet.MvcResult

### Community 40 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 41 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 42 - "InitialAdminBootstrapService.java"
Cohesion: 0.20
Nodes (4): EmailCanonicalizer, PasswordPolicy, DuplicateEmailPersistenceIntegrationTest, DataSource

### Community 43 - "AssetCatalogPersistenceIT"
Cohesion: 0.23
Nodes (4): AssetCatalogPersistenceIT, AtivoQuery, SqlAction, FunctionalInterface

### Community 44 - "AtivoController"
Cohesion: 0.25
Nodes (3): AtivoController, org.springframework.security.access.prepost.PreAuthorize, org.springframework.web.bind.annotation.PatchMapping

### Community 45 - "MarketQuoteApplicationServiceTest.java"
Cohesion: 0.24
Nodes (8): FakeAssets, FakeHistory, Fixture, Override, com.carteira.carteiraInvestimento.application.port.AtivoPage, com.carteira.carteiraInvestimento.application.port.AtivoQuery, com.carteira.carteiraInvestimento.application.port.AtivoReadScope, com.carteira.carteiraInvestimento.domain.asset.Ativo

### Community 46 - "PostgreSqlContainerSupport"
Cohesion: 0.19
Nodes (6): FlywayValidationIT, PostgreSqlContainerSupport, RegistrationConcurrencyIntegrationTest, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer

### Community 47 - "Usuario"
Cohesion: 0.25
Nodes (5): Usuario, Override, TestUsuarios, Override, TestUsuarios

### Community 48 - ".login"
Cohesion: 0.17
Nodes (4): AuthenticationFailedException, AuditPersistenceIntegrationTest, DataSource, LoginControllerTest

### Community 49 - ".register"
Cohesion: 0.22
Nodes (3): InitialAdminBootstrapService, InitialAdminBootstrapIntegrationTest, org.springframework.jdbc.core.JdbcTemplate

### Community 50 - "IdentityPersistenceAdapter"
Cohesion: 0.23
Nodes (4): DuplicateEmailException, IdentityPersistenceAdapter, Override, org.springframework.stereotype.Repository

### Community 51 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.24
Nodes (5): CarteiraInvestimentoApplication, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 54 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 55 - "PasswordHasher"
Cohesion: 0.24
Nodes (3): PasswordHasher, BcryptPasswordHasher, org.springframework.security.crypto.password.PasswordEncoder

### Community 56 - "ApplicationFoundationIT"
Cohesion: 0.24
Nodes (4): ApplicationFoundationIT, org.flywaydb.core.Flyway, org.springframework.security.core.userdetails.UserDetailsService, org.springframework.web.context.WebApplicationContext

### Community 57 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 58 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 60 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 62 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 63 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 64 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 65 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 67 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 68 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 71 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **313 isolated node(s):** `AuthConfig`, `AuthEnv`, `BackendErrorKind`, `AuthProblem`, `{ loginMock, originMock }` (+308 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 436 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **39 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Usuario` connect `Usuario` to `PersistedUserJwtAuthenticationConverterTest.java`, `JwtProperties`, `UsuarioPort`, `UsuarioJpaEntity`, `InitialAdminBootstrapService.java`, `org.springframework.context.annotation.Bean`, `.register`, `IdentityPersistenceAdapter`, `RegistrationService`, `PersistedUserJwtAuthenticationConverter`, `AuditSecurityEventsIntegrationTest`, `Fakes`, `PersistedUserJwtAuthenticationConverterTest`, `org.junit.jupiter.api.BeforeEach`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Why does `QuoteProvider` connect `QuoteProvider` to `org.springframework.web.bind.annotation.GetMapping`, `AuditoriaCommand`, `Cotacao`, `MarketQuoteApplicationServiceTest.java`, `MarketQuoteApplicationService`, `org.springframework.boot.test.context.SpringBootTest`, `HistoricoCotacaoJpaEntity`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Ativo` (e.g. with `.updatesOnlyNameAndLifecycle()` and `.requiresControlledTypeAndConsistentCurrency()`) actually correct?**
  _`Ativo` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `AuthConfig`, `AuthEnv`, `BackendErrorKind` to the rest of the system?**
  _313 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `types.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.06772151898734177 - nodes in this community are weakly interconnected._
- **Should `org.springframework.web.bind.annotation.GetMapping` be split into smaller, more focused modules?**
  _Cohesion score 0.05639097744360902 - nodes in this community are weakly interconnected._
- **Should `auth.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.06349206349206349 - nodes in this community are weakly interconnected._