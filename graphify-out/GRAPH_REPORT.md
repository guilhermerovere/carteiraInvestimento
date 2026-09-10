# Graph Report - projetoJeff  (2026-09-09)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1808 nodes · 4412 edges · 135 communities (80 shown, 45 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 265 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `94521c7c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- jakarta.servlet.http.HttpServletRequest
- IdentityApplicationConfiguration.java
- AuditoriaCommand
- org.springframework.context.annotation.Bean
- CashMovementApiIT
- org.springframework.security.oauth2.jwt.JwtEncoder
- MarketQuoteSecurityIT
- Cotacao
- HistoricoCotacaoJpaEntity
- AtivoControllerTest.java
- org.junit.jupiter.api.Test
- auth.ts
- types.ts
- Requirements
- ADDED Requirements
- Requirement: Cadastro publico transacional
- Ativo
- ADDED Requirements
- Usuario
- compilerOptions
- AuditSecurityEventsIntegrationTest.java
- MarketQuoteProperties
- AtivoJpaEntity
- .updateLifecycle
- org.junit.jupiter.api.BeforeEach
- AuditSecurityEventsIntegrationTest
- MarketQuoteControllerTest.java
- CashMovementRollbackIT.java
- login-form.tsx
- org.springframework.stereotype.Component
- TipoAtivo
- AlphaVantageQuoteAdapter.java
- org.springframework.web.bind.annotation.GetMapping
- CashMovementApplicationService
- package.json
- login/route.ts
- QuoteProvider
- org.springframework.web.bind.annotation.RestController
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- .execute
- AtivoUseCase
- org.springframework.security.oauth2.jwt.Jwt
- cookie.ts
- ADDED Requirements
- .register
- UsuarioJpaEntity
- AssetCatalogPersistenceIT
- Decisions
- TipoMovimentacaoCaixa
- org.springframework.security.access.prepost.PreAuthorize
- Role
- MarketQuoteApplicationServiceTest.java
- org.springframework.data.jpa.repository.JpaRepository
- CashPersistenceAdapter
- PersistedUserJwtAuthenticationConverterTest
- CashMovementSchemaIT
- RegistrationTransactionIntegrationTest
- Wallet
- .novo
- .provision
- AtivoController
- Requirement: Documentacao HTTP e erros padronizados
- .login
- CashMovementPage
- MarketQuoteApplicationServiceTest
- guard.ts
- mvnw
- CarteiraJpaEntity
- auth-pages.test.tsx
- tasks.md
- PersistedUserJwtAuthenticationConverter.java
- ApplicationFoundationIT
- scripts
- DuplicateEmailPersistenceIntegrationTest
- IdentitySchemaIntegrationTest
- Project Foundation Proposal
- CotacaoDomainTest.java
- BcryptPasswordHasher
- IdentityPersistenceAdapter
- proposal.md
- OpenSpec Apply Change Workflow
- usuarios
- IdentityApplicationServiceTest
- CarteiraInvestimentoApplication
- app/layout.tsx
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- NomeAtivoCanonicalizer
- .correlationId
- .me
- frontend/package.json
- ProtectedProbeController
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
3. `AuditoriaCommand` - 36 edges
4. `Cotacao` - 36 edges
5. `AuditSecurityEventsIntegrationTest` - 34 edges
6. `UsuarioPort` - 29 edges
7. `QuoteProvider` - 29 edges
8. `TipoAtivo` - 27 edges
9. `PersistedUserJwtAuthenticationConverterTest` - 27 edges
10. `GlobalExceptionHandler` - 25 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `SecurityProblemDetailHandlerTest` --references--> `AuditoriaCommand`  [EXTRACTED]
  backend/src/test/java/com/carteira/carteiraInvestimento/infrastructure/security/SecurityProblemDetailHandlerTest.java → backend/src/main/java/com/carteira/carteiraInvestimento/application/service/AuditoriaCommand.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (135 total, 45 thin omitted)

### Community 0 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.08
Nodes (32): CashConflictException, PrimaryWalletMissingException, Override, Override, GlobalExceptionHandler, MockHttpServletRequest, SecurityProblemDetailHandlerTest, com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException (+24 more)

### Community 1 - "IdentityApplicationConfiguration.java"
Cohesion: 0.06
Nodes (37): AdminProperties, IdentityApplicationConfiguration, OpenApiConfiguration, OpenApiConfigurationTest, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher (+29 more)

### Community 2 - "AuditoriaCommand"
Cohesion: 0.10
Nodes (24): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+16 more)

### Community 3 - "org.springframework.context.annotation.Bean"
Cohesion: 0.13
Nodes (23): FoundationSecurityConfiguration, PersistedUserJwtAuthenticationConverter, FoundationSecurityConfigurationTest, SecurityTestConfiguration, SecurityTestConfiguration, AccessDeniedProbeConfiguration, CurrentPrincipalControllerTest, SecurityTestConfiguration (+15 more)

### Community 4 - "CashMovementApiIT"
Cohesion: 0.12
Nodes (11): CashMovementApiIT, HttpCall, HttpResults, FunctionalInterface, User, CashMovementConcurrencyIT, Fixture, FunctionalInterface (+3 more)

### Community 5 - "org.springframework.security.oauth2.jwt.JwtEncoder"
Cohesion: 0.10
Nodes (17): AdminProperties, JwtConfiguration, JwtProperties, Override, JwtAccessTokenIssuer, PropertiesConfiguration, JwtAccessTokenIssuerTest, jakarta.validation.constraints.AssertTrue (+9 more)

### Community 6 - "MarketQuoteSecurityIT"
Cohesion: 0.09
Nodes (9): AssetCatalogSecurityIT, MarketQuotePersistenceIT, MarketQuoteSecurityIT, PostgreSqlContainerSupport, RegistrationConcurrencyIntegrationTest, com.carteira.carteiraInvestimento.domain.identity.Usuario, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource (+1 more)

### Community 7 - "Cotacao"
Cohesion: 0.14
Nodes (12): HistoricoCotacaoPage, HistoricoCotacaoPort, Override, MarketQuoteApplicationService, MarketQuoteUseCase, Cotacao, MarketQuoteConfiguration, FakeHistory (+4 more)

### Community 8 - "HistoricoCotacaoJpaEntity"
Cohesion: 0.10
Nodes (10): HistoricoCotacaoJpaEntity, HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, LogAuditoriaJpaEntity, jakarta.persistence.Entity, jakarta.persistence.Table, org.springframework.data.domain.Page (+2 more)

### Community 9 - "AtivoControllerTest.java"
Cohesion: 0.11
Nodes (15): AtivoPage, AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME (+7 more)

### Community 10 - "org.junit.jupiter.api.Test"
Cohesion: 0.09
Nodes (11): MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, FlywayValidationIT, MissingDatabaseConfigurationTest, CashMovementContractTest, org.junit.jupiter.api.Test (+3 more)

### Community 11 - "auth.ts"
Cohesion: 0.14
Nodes (24): messageFor(), RegisterForm(), authMeKey, clearAuthState(), confirmCurrentUser(), asProblem(), AuthFormError, AuthProblem (+16 more)

### Community 12 - "types.ts"
Cohesion: 0.15
Nodes (19): { loginMock, originMock }, { resolveMock }, CurrentUser, LoginInput, RegisterInput, Role, request(), fetchMock (+11 more)

### Community 13 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 14 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 15 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 16 - "Ativo"
Cohesion: 0.18
Nodes (7): AtivoPort, AtivoApplicationService, Override, Ativo, TickerCanonicalizer, AtivoApplicationServiceTest, org.springframework.transaction.annotation.Transactional

### Community 17 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 18 - "Usuario"
Cohesion: 0.11
Nodes (7): CurrentPrincipalService, Usuario, Fakes, Override, TestUsuarios, Override, TestUsuarios

### Community 19 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 20 - "AuditSecurityEventsIntegrationTest.java"
Cohesion: 0.18
Nodes (11): AccessTokenIssuer, AuditoriaIsoladaPort, AuditoriaPort, PasswordHasher, UsuarioPort, InitialAdminBootstrapService, LoginService, RegistrationService (+3 more)

### Community 21 - "MarketQuoteProperties"
Cohesion: 0.18
Nodes (11): MarketQuoteProperties, Provider, BrapiClient, BrapiQuoteAdapter, BrapiResponse, BrapiResult, Override, TwelveDataClient (+3 more)

### Community 22 - "AtivoJpaEntity"
Cohesion: 0.16
Nodes (5): AtivoJpaEntity, AtivoJpaRepository, AtivoPersistenceAdapter, Override, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 23 - ".updateLifecycle"
Cohesion: 0.13
Nodes (5): UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator, org.springframework.web.bind.annotation.PatchMapping

### Community 24 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.20
Nodes (10): CorrelationIdFilterTest, com.carteira.carteiraInvestimento.application.service.AtivoUseCase, com.carteira.carteiraInvestimento.domain.asset.TipoAtivo, com.carteira.carteiraInvestimento.domain.identity.Role, javax.sql.DataSource, org.junit.jupiter.api.BeforeEach, org.springframework.beans.factory.annotation.Autowired, org.springframework.boot.test.context.SpringBootTest (+2 more)

### Community 26 - "MarketQuoteControllerTest.java"
Cohesion: 0.14
Nodes (6): InactiveAssetRefreshException, QuoteIntegrationException, QuoteNotFoundException, Override, MarketQuoteControllerTest, org.springframework.security.authentication.TestingAuthenticationToken

### Community 27 - "CashMovementRollbackIT.java"
Cohesion: 0.16
Nodes (12): CashMovementRollbackIT, FailureStage, AUDIT, MOVEMENT, RESULT, SNAPSHOT, com.carteira.carteiraInvestimento.infrastructure.persistence.AuditoriaPersistenceAdapter, org.junit.jupiter.params.ParameterizedTest (+4 more)

### Community 28 - "login-form.tsx"
Cohesion: 0.15
Nodes (11): fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), AUTH_SESSION_COOKIE, protectedRoutes, DEFAULT_RETURN_TO, isSafeInternalPath() (+3 more)

### Community 29 - "org.springframework.stereotype.Component"
Cohesion: 0.17
Nodes (9): AuthenticationFailedException, AccessDeniedAuditingService, SecurityProblemDetailHandler, CorrelationIdFilter, ProblemDetailFactory, LoginControllerTest, org.springframework.core.annotation.Order, org.springframework.stereotype.Component (+1 more)

### Community 30 - "TipoAtivo"
Cohesion: 0.17
Nodes (12): Mercado, B3, US, Moeda, BRL, USD, TipoAtivo, ACAO (+4 more)

### Community 31 - "AlphaVantageQuoteAdapter.java"
Cohesion: 0.20
Nodes (8): AlphaBar, AlphaMeta, AlphaResponse, AlphaVantageClient, AlphaVantageQuoteAdapter, Override, TwelveResponse, QuoteProviderAdaptersTest

### Community 32 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.13
Nodes (5): SecurityProbeController, AccessDeniedProbeController, FailureProbeController, GlobalExceptionHandlerTest, org.springframework.web.bind.annotation.GetMapping

### Community 33 - "CashMovementApplicationService"
Cohesion: 0.26
Nodes (8): CashIdempotencyPort, CashLedgerPort, CashSnapshotPort, CashWalletPort, CashMovementApplicationService, CashMovementConfiguration, com.carteira.carteiraInvestimento.application.port.AuditoriaPort, java.util.regex.Pattern

### Community 34 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 35 - "login/route.ts"
Cohesion: 0.18
Nodes (12): POST(), POST(), { registerMock, originMock }, backendClient, AuthConfig, AuthEnv, loadAuthConfig(), hasValidOrigin() (+4 more)

### Community 36 - "QuoteProvider"
Cohesion: 0.18
Nodes (8): CotacaoProviderPort, CotacaoExterna, QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA, FakeProvider, com.carteira.carteiraInvestimento.domain.asset.Moeda

### Community 37 - "org.springframework.web.bind.annotation.RestController"
Cohesion: 0.23
Nodes (9): CurrentPrincipalController, LoginController, RegisterRequest, RegistrationController, io.swagger.v3.oas.annotations.security.SecurityRequirement, org.springframework.http.ResponseEntity, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.bind.annotation.RequestMapping (+1 more)

### Community 38 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 39 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 40 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 41 - ".execute"
Cohesion: 0.17
Nodes (4): CashMovementNormalizer, CashMovementDomainTest, DataOutputStream, java.io.DataOutputStream

### Community 42 - "AtivoUseCase"
Cohesion: 0.16
Nodes (4): AtivoNotFoundException, AtivoUseCase, AtivoControllerTest, TestingAuthenticationToken

### Community 43 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.24
Nodes (6): CashMovementUseCase, CashBalanceResponse, CashMovementController, CashMovementRequest, io.swagger.v3.oas.annotations.Operation, org.springframework.security.oauth2.jwt.Jwt

### Community 44 - "cookie.ts"
Cohesion: 0.23
Nodes (10): POST(), fetchMock, GET(), AUTH_SESSION_COOKIE, authCookie(), authCookieBase(), expiredAuthCookie(), resolveCurrentUser() (+2 more)

### Community 45 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 46 - ".register"
Cohesion: 0.16
Nodes (4): CarteiraPort, DuplicateEmailException, Carteira, RegistrationControllerTest

### Community 47 - "UsuarioJpaEntity"
Cohesion: 0.21
Nodes (4): Override, Entity, Table, UsuarioJpaEntity

### Community 48 - "AssetCatalogPersistenceIT"
Cohesion: 0.21
Nodes (4): AssetCatalogPersistenceIT, AtivoQuery, SqlAction, FunctionalInterface

### Community 49 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 50 - "TipoMovimentacaoCaixa"
Cohesion: 0.22
Nodes (7): CashOperationResult, MovimentacaoCaixa, TipoMovimentacaoCaixa, DEPOSITO, SAQUE, CashMovementResponse, Movement

### Community 51 - "org.springframework.security.access.prepost.PreAuthorize"
Cohesion: 0.21
Nodes (6): CotacaoResponse, HistoricoCotacaoResponse, MarketQuoteController, AdminProbeController, org.springframework.security.access.prepost.PreAuthorize, org.springframework.web.bind.annotation.PutMapping

### Community 52 - "Role"
Cohesion: 0.20
Nodes (6): EmailCanonicalizer, PasswordPolicy, Role, ROLE_ADMIN, ROLE_USER, RegisteredUserResponse

### Community 53 - "MarketQuoteApplicationServiceTest.java"
Cohesion: 0.23
Nodes (7): FakeAssets, Override, com.carteira.carteiraInvestimento.application.port.AtivoPage, com.carteira.carteiraInvestimento.application.port.AtivoQuery, com.carteira.carteiraInvestimento.application.port.AtivoReadScope, com.carteira.carteiraInvestimento.domain.asset.Ativo, com.carteira.carteiraInvestimento.domain.asset.Mercado

### Community 54 - "org.springframework.data.jpa.repository.JpaRepository"
Cohesion: 0.19
Nodes (8): CarteiraJpaRepository, LogAuditoriaJpaRepository, RepositoryUserDetailsService, UsuarioJpaRepository, org.springframework.data.jpa.repository.JpaRepository, org.springframework.stereotype.Service, UserDetails, UserDetailsService

### Community 55 - "CashPersistenceAdapter"
Cohesion: 0.23
Nodes (4): CashPersistenceAdapter, Override, java.sql.ResultSet, org.springframework.jdbc.core.JdbcTemplate

### Community 56 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.27
Nodes (4): PersistedUserJwtAuthenticationConverterTest, PersistedUserJwtAuthenticationConverterTest.AdminProbeController, PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController, PersistedUserJwtAuthenticationConverterTest.SecurityTestConfiguration

### Community 57 - "CashMovementSchemaIT"
Cohesion: 0.35
Nodes (3): CashMovementSchemaIT, java.sql.Connection, org.junit.jupiter.api.BeforeAll

### Community 58 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.24
Nodes (5): FaultInjectionConfiguration, FaultSwitches, RegistrationTransactionIntegrationTest, org.springframework.context.annotation.Primary, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 59 - "Wallet"
Cohesion: 0.35
Nodes (5): ExistingReservation, NewReservation, Reservation, Wallet, CashMovementApplicationServiceTest

### Community 60 - ".novo"
Cohesion: 0.23
Nodes (3): AtivoDomainTest, AtivoPersistenceAdapterTest, DataIntegrityViolationException

### Community 62 - "AtivoController"
Cohesion: 0.30
Nodes (3): AtivoController, AtivoListResponse, org.springframework.security.core.Authentication

### Community 63 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 64 - ".login"
Cohesion: 0.27
Nodes (3): IssuedAccessToken, LoginRequest, LoginResponse

### Community 65 - "CashMovementPage"
Cohesion: 0.22
Nodes (3): CashMovementPage, Override, CashMovementHistoryResponse

### Community 67 - "guard.ts"
Cohesion: 0.33
Nodes (7): AdminLayout(), InicioLayout(), redirectToLogin(), requireCurrentUser(), requireRole(), { redirectMock, resolveMock }, user

### Community 68 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 69 - "CarteiraJpaEntity"
Cohesion: 0.20
Nodes (3): CarteiraJpaEntity, Entity, Table

### Community 70 - "auth-pages.test.tsx"
Cohesion: 0.29
Nodes (5): AccessDeniedPage(), LoginPage(), AdminPage(), InicioPage(), RegisterPage()

### Community 71 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 72 - "PersistedUserJwtAuthenticationConverter.java"
Cohesion: 0.28
Nodes (5): Override, InvalidBearerTokenException, org.springframework.core.convert.converter.Converter, org.springframework.security.authentication.AbstractAuthenticationToken, org.springframework.security.oauth2.server.resource.InvalidBearerTokenException

### Community 73 - "ApplicationFoundationIT"
Cohesion: 0.22
Nodes (3): ApplicationFoundationIT, org.flywaydb.core.Flyway, org.springframework.web.context.WebApplicationContext

### Community 74 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 77 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 81 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 82 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 83 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 85 - "CarteiraInvestimentoApplication"
Cohesion: 0.60
Nodes (3): CarteiraInvestimentoApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 87 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 88 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 92 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **321 isolated node(s):** `AuthProblem`, `BackendErrorKind`, `AuthConfig`, `AuthEnv`, `components` (+316 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 454 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **45 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Usuario` connect `Usuario` to `.login`, `org.springframework.context.annotation.Bean`, `org.springframework.security.oauth2.jwt.JwtEncoder`, `org.springframework.web.bind.annotation.RestController`, `PersistedUserJwtAuthenticationConverter.java`, `.register`, `UsuarioJpaEntity`, `IdentityPersistenceAdapter`, `org.springframework.stereotype.Component`, `AuditSecurityEventsIntegrationTest.java`, `Role`, `IdentityApplicationServiceTest`, `PersistedUserJwtAuthenticationConverterTest`, `AuditSecurityEventsIntegrationTest`, `.me`, `.provision`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Why does `QuoteProvider` connect `QuoteProvider` to `Cotacao`, `HistoricoCotacaoJpaEntity`, `org.springframework.security.access.prepost.PreAuthorize`, `MarketQuoteProperties`, `MarketQuoteApplicationServiceTest.java`, `MarketQuoteControllerTest.java`, `AlphaVantageQuoteAdapter.java`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **Why does `TipoAtivo` connect `TipoAtivo` to `org.springframework.web.bind.annotation.RestController`, `HistoricoCotacaoJpaEntity`, `AtivoControllerTest.java`, `AtivoUseCase`, `Ativo`, `AtivoJpaEntity`, `.updateLifecycle`, `.novo`, `AtivoController`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `Ativo` (e.g. with `.updatesOnlyNameAndLifecycle()` and `.requiresControlledTypeAndConsistentCurrency()`) actually correct?**
  _`Ativo` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `AuthProblem`, `BackendErrorKind`, `AuthConfig` to the rest of the system?**
  _321 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `jakarta.servlet.http.HttpServletRequest` be split into smaller, more focused modules?**
  _Cohesion score 0.07878787878787878 - nodes in this community are weakly interconnected._
- **Should `IdentityApplicationConfiguration.java` be split into smaller, more focused modules?**
  _Cohesion score 0.060129509713228495 - nodes in this community are weakly interconnected._