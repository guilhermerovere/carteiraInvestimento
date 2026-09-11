# Graph Report - projetoJeff  (2026-09-10)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2326 nodes · 5958 edges · 165 communities (100 shown, 55 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 426 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `fa2eeb46`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ObservacaoCambio
- org.junit.jupiter.api.BeforeEach
- jakarta.servlet.http.HttpServletRequest
- org.junit.jupiter.api.Test
- org.springframework.context.annotation.Bean
- .register
- Usuario
- AuditoriaCommand
- org.springframework.data.jpa.repository.JpaRepository
- auth.ts
- com.fasterxml.jackson.annotation.JsonAnySetter
- AlphaVantageCambioAdapter
- types.ts
- Requirements
- Cotacao
- org.springframework.security.oauth2.jwt.JwtEncoder
- ADDED Requirements
- Requirement: Cadastro publico transacional
- MarketQuoteControllerTest.java
- org.springframework.security.access.prepost.PreAuthorize
- Role
- ADDED Requirements
- AtivoControllerTest.java
- org.springframework.security.oauth2.jwt.Jwt
- compilerOptions
- PasswordHasher
- AtivoPort
- TipoAtivo
- AuditSecurityEventsIntegrationTest.java
- BrokerCatalogPersistenceIT
- .criar
- QuoteProvider
- org.springframework.stereotype.Component
- BrokerUseCase
- Fixture
- org.springframework.web.bind.annotation.GetMapping
- BrokerCatalogSecurityIT
- org.springframework.web.bind.annotation.RestController
- CambioProviderAdaptersTest
- CashMovementRollbackIT.java
- login-form.tsx
- AuditSecurityEventsIntegrationTest
- org.springframework.transaction.annotation.Transactional
- IdentityApplicationConfiguration.java
- CashMovementApplicationService
- Corretora
- Ativo
- HistoricoCotacaoJpaEntity
- package.json
- login/route.ts
- .login
- .execute
- CashMovementConcurrencyIT
- CorretoraPersistenceAdapter
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- AtivoUseCase
- MarketQuoteProperties
- AlphaVantageQuoteAdapter.java
- CashMovementApiIT
- cookie.ts
- ADDED Requirements
- BrokerProviderAdaptersIT
- RegistrationTransactionIntegrationTest
- CorretoraJpaEntity
- components.json
- Decisions
- AssetCatalogPersistenceIT
- LogAuditoriaJpaEntity
- TipoMovimentacaoCaixa
- org.springframework.context.annotation.Configuration
- BrapiQuoteAdapter.java
- .create
- PersistedUserJwtAuthenticationConverterTest
- PostgreSqlContainerSupport
- CashMovementSchemaIT
- Wallet
- ReceitaFederalPort
- .novo
- CashPersistenceAdapter
- AccessDeniedAuditingService
- .edit
- MarketQuoteSecurityIT
- Requirement: Documentacao HTTP e erros padronizados
- org.springframework.cloud.openfeign.FeignClient
- AtivoPersistenceAdapterTest.java
- org.springframework.boot.context.properties.ConfigurationProperties
- PersistedUserJwtAuthenticationConverter
- MarketQuoteApplicationServiceTest.java
- MarketQuoteApplicationServiceTest
- guard.ts
- mvnw
- CvmPort
- auth-pages.test.tsx
- tasks.md
- CambioConfiguration
- CambioAuditIT
- MarketQuotePersistenceIT
- scripts
- CashMovementPage
- CnpjCanonicalizer
- AtivoController
- IdentitySchemaIntegrationTest
- Project Foundation Proposal
- org.springframework.jdbc.core.JdbcTemplate
- CambioSchemaIT
- proposal.md
- OpenSpec Apply Change Workflow
- usuarios
- Fakes
- app/layout.tsx
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- .reject
- CotacaoDomainTest
- .valor
- .setup
- frontend/package.json
- BrokerAuditException
- PrimaryWalletMissingException
- .setup
- app/page.tsx
- BrokerNotFoundException.java
- JWT Application Configuration
- .setup
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
2. `Corretora` - 50 edges
3. `ObservacaoCambio` - 41 edges
4. `Ativo` - 38 edges
5. `Cotacao` - 36 edges
6. `AuditoriaCommand` - 36 edges
7. `AuditSecurityEventsIntegrationTest` - 34 edges
8. `CambioProvider` - 32 edges
9. `GlobalExceptionHandler` - 32 edges
10. `QuoteProvider` - 29 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `AlphaVantageCambioAdapter` --implements--> `CambioProviderPort`  [EXTRACTED]
  backend/src/main/java/com/carteira/carteiraInvestimento/infrastructure/provider/AlphaVantageCambioAdapter.java → backend/src/main/java/com/carteira/carteiraInvestimento/application/port/CambioProviderPort.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (165 total, 55 thin omitted)

### Community 0 - "ObservacaoCambio"
Cohesion: 0.10
Nodes (18): CambioProviderPort, HistoricoCambioPort, CambioApplicationService, Override, CambioUseCase, CambioExterno, CambioProvider, ALPHA_VANTAGE (+10 more)

### Community 1 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.10
Nodes (24): ApplicationFoundationIT, AssetCatalogSecurityIT, BrokerTransactionBoundaryIT, BeforeEach, CambioSecurityIT, FixedCambio, BrokerTransactionBoundaryIT.Fakes, CambioSecurityIT.FixedCambio (+16 more)

### Community 2 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.13
Nodes (26): GlobalExceptionHandler, com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException, com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException, com.carteira.carteiraInvestimento.application.service.BrokerAuditException, com.carteira.carteiraInvestimento.application.service.BrokerComplianceException, com.carteira.carteiraInvestimento.application.service.BrokerNotFoundException, com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException, com.carteira.carteiraInvestimento.application.service.CashConflictException (+18 more)

### Community 3 - "org.junit.jupiter.api.Test"
Cohesion: 0.07
Nodes (10): ObservacaoCambioTest, IdentityDomainTest, MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, FlywayValidationIT, MissingDatabaseConfigurationTest (+2 more)

### Community 4 - "org.springframework.context.annotation.Bean"
Cohesion: 0.12
Nodes (23): AuditoriaIsoladaPort, FoundationSecurityConfiguration, FoundationSecurityConfigurationTest, SecurityTestConfiguration, SecurityTestConfiguration, AccessDeniedProbeConfiguration, CurrentPrincipalControllerTest, SecurityTestConfiguration (+15 more)

### Community 5 - ".register"
Cohesion: 0.09
Nodes (10): CarteiraPort, DuplicateEmailException, RegistrationService, Carteira, IdentityPersistenceAdapter, Override, RegistrationController, DuplicateEmailPersistenceIntegrationTest (+2 more)

### Community 6 - "Usuario"
Cohesion: 0.10
Nodes (9): UsuarioPort, CurrentPrincipalService, Usuario, Fakes, IdentityApplicationServiceTest, Override, TestUsuarios, Override (+1 more)

### Community 7 - "AuditoriaCommand"
Cohesion: 0.12
Nodes (25): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+17 more)

### Community 8 - "org.springframework.data.jpa.repository.JpaRepository"
Cohesion: 0.07
Nodes (16): AuditoriaPersistenceAdapter, Override, CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, HistoricoCambioJpaRepository, HistoricoCambioPersistenceAdapter (+8 more)

### Community 9 - "auth.ts"
Cohesion: 0.14
Nodes (24): messageFor(), RegisterForm(), authMeKey, clearAuthState(), confirmCurrentUser(), asProblem(), AuthFormError, AuthProblem (+16 more)

### Community 10 - "com.fasterxml.jackson.annotation.JsonAnySetter"
Cohesion: 0.11
Nodes (6): CreateAtivoRequest, UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, UpdateBrokerLifecycleRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator

### Community 11 - "AlphaVantageCambioAdapter"
Cohesion: 0.13
Nodes (9): CambioProviderException, AlphaVantageCambioAdapter, AlphaVantageCambioClient, Override, Override, TwelveDataCambioAdapter, TwelveDataCambioClient, com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties (+1 more)

### Community 12 - "types.ts"
Cohesion: 0.15
Nodes (19): { loginMock, originMock }, { resolveMock }, CurrentUser, LoginInput, RegisterInput, Role, request(), fetchMock (+11 more)

### Community 13 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 14 - "Cotacao"
Cohesion: 0.18
Nodes (11): CotacaoProviderPort, HistoricoCotacaoPort, Override, MarketQuoteApplicationService, Cotacao, MarketQuoteConfiguration, FakeHistory, Fixture (+3 more)

### Community 15 - "org.springframework.security.oauth2.jwt.JwtEncoder"
Cohesion: 0.15
Nodes (11): JwtConfiguration, JwtProperties, Override, JwtAccessTokenIssuer, JwtAccessTokenIssuerTest, javax.crypto.spec.SecretKeySpec, org.springframework.security.oauth2.core.OAuth2TokenValidator, org.springframework.security.oauth2.core.OAuth2TokenValidatorResult (+3 more)

### Community 16 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 17 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 18 - "MarketQuoteControllerTest.java"
Cohesion: 0.11
Nodes (7): HistoricoCotacaoPage, InactiveAssetRefreshException, MarketQuoteUseCase, QuoteIntegrationException, QuoteNotFoundException, MarketQuoteControllerTest, com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory

### Community 19 - "org.springframework.security.access.prepost.PreAuthorize"
Cohesion: 0.16
Nodes (12): BrokerAdminResponse, BrokerController, GetMapping, BrokerListResponse, BrokerSelectionResponse, io.swagger.v3.oas.annotations.Operation, io.swagger.v3.oas.annotations.responses.ApiResponse, io.swagger.v3.oas.annotations.tags.Tag (+4 more)

### Community 20 - "Role"
Cohesion: 0.11
Nodes (10): Role, ROLE_ADMIN, ROLE_USER, Override, Entity, Table, UsuarioJpaEntity, CurrentUserResponse (+2 more)

### Community 21 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 22 - "AtivoControllerTest.java"
Cohesion: 0.14
Nodes (14): AtivoPage, AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME (+6 more)

### Community 23 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.14
Nodes (9): CashMovementUseCase, CashBalanceResponse, CashMovementController, CashMovementHistoryResponse, CashMovementRequest, CashMovementResponse, Movement, org.springframework.http.ResponseEntity (+1 more)

### Community 24 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 25 - "PasswordHasher"
Cohesion: 0.12
Nodes (7): AccessTokenIssuer, PasswordHasher, InitialAdminBootstrapService, EmailCanonicalizer, PasswordPolicy, BcryptPasswordHasher, org.springframework.security.crypto.password.PasswordEncoder

### Community 26 - "AtivoPort"
Cohesion: 0.18
Nodes (5): AtivoPort, AtivoApplicationService, Override, TickerCanonicalizer, AtivoApplicationServiceTest

### Community 27 - "TipoAtivo"
Cohesion: 0.16
Nodes (12): Mercado, B3, US, Moeda, BRL, USD, TipoAtivo, ACAO (+4 more)

### Community 28 - "AuditSecurityEventsIntegrationTest.java"
Cohesion: 0.13
Nodes (9): AuditoriaPort, CorrelationIdFilter, Override, CorrelationIdFilterTest, CorrelationProbeController, ch.qos.logback.classic.Logger, jakarta.servlet.FilterChain, org.springframework.core.annotation.Order (+1 more)

### Community 29 - "BrokerCatalogPersistenceIT"
Cohesion: 0.20
Nodes (4): BrokerPatch, BrokerCatalogPersistenceIT, BeforeEach, Test

### Community 30 - ".criar"
Cohesion: 0.22
Nodes (6): CadastroCnpj, EnderecoPostal, RegistroCvm, BrokerApplicationServiceTest, AuditoriaIsoladaPort, Test

### Community 31 - "QuoteProvider"
Cohesion: 0.13
Nodes (8): CotacaoExterna, QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA, CotacaoResponse, FakeProvider, com.carteira.carteiraInvestimento.domain.asset.Moeda

### Community 32 - "org.springframework.stereotype.Component"
Cohesion: 0.18
Nodes (12): Override, SecurityProblemDetailHandler, ProblemDetailFactory, MockHttpServletRequest, SecurityProblemDetailHandlerTest, jakarta.servlet.http.HttpServletResponse, org.springframework.http.HttpStatus, org.springframework.security.access.AccessDeniedException (+4 more)

### Community 33 - "BrokerUseCase"
Cohesion: 0.13
Nodes (7): CorretoraPage, BrokerUseCase, Test, BrokerControllerTest, MockMvc, Test, org.springframework.security.authentication.TestingAuthenticationToken

### Community 34 - "Fixture"
Cohesion: 0.16
Nodes (7): AuditoriaCommand, CambioApplicationServiceTest, FakeAudit, FakeHistory, Fixture, Override, MutableClock

### Community 35 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.13
Nodes (8): Authentication, SecurityProbeController, AdminProbeController, ProtectedProbeController, AccessDeniedProbeController, FailureProbeController, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.PostMapping

### Community 36 - "BrokerCatalogSecurityIT"
Cohesion: 0.16
Nodes (8): CambioUnavailableException, BrokerCatalogSecurityIT, Test, CambioControllerTest, com.fasterxml.jackson.databind.ObjectMapper, JwtEncoder, Role, Usuario

### Community 37 - "org.springframework.web.bind.annotation.RestController"
Cohesion: 0.18
Nodes (10): LoginService, CurrentPrincipalController, LoginController, CambioController, HistoricoCotacaoResponse, MarketQuoteController, io.swagger.v3.oas.annotations.security.SecurityRequirement, org.springframework.web.bind.annotation.PutMapping (+2 more)

### Community 38 - "CambioProviderAdaptersTest"
Cohesion: 0.23
Nodes (7): AlphaCambioRate, AlphaCambioResponse, TwelveCambioResponse, CambioProviderAdaptersTest, MarketQuoteProperties, MockWebServer, okhttp3.mockwebserver.MockWebServer

### Community 39 - "CashMovementRollbackIT.java"
Cohesion: 0.16
Nodes (12): CashMovementRollbackIT, FailureStage, AUDIT, MOVEMENT, RESULT, SNAPSHOT, com.carteira.carteiraInvestimento.infrastructure.persistence.AuditoriaPersistenceAdapter, org.junit.jupiter.params.ParameterizedTest (+4 more)

### Community 40 - "login-form.tsx"
Cohesion: 0.15
Nodes (11): fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), AUTH_SESSION_COOKIE, protectedRoutes, DEFAULT_RETURN_TO, isSafeInternalPath() (+3 more)

### Community 42 - "org.springframework.transaction.annotation.Transactional"
Cohesion: 0.21
Nodes (10): CorretoraPort, CorretoraReadScope, ACTIVE_ONLY, ALL, BrokerApplicationService, AuditoriaIsoladaPort, Override, BrokerLocalTransactionService (+2 more)

### Community 43 - "IdentityApplicationConfiguration.java"
Cohesion: 0.16
Nodes (14): AdminProperties, IdentityApplicationConfiguration, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher, com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService, com.carteira.carteiraInvestimento.application.service.InitialAdminBootstrapService, com.carteira.carteiraInvestimento.application.service.LoginService (+6 more)

### Community 44 - "CashMovementApplicationService"
Cohesion: 0.26
Nodes (8): CashIdempotencyPort, CashLedgerPort, CashSnapshotPort, CashWalletPort, CashMovementApplicationService, CashMovementConfiguration, com.carteira.carteiraInvestimento.application.port.AuditoriaPort, java.util.regex.Pattern

### Community 46 - "Ativo"
Cohesion: 0.17
Nodes (6): DuplicateTickerException, Ativo, AtivoJpaRepository, AtivoPersistenceAdapter, Override, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 47 - "HistoricoCotacaoJpaEntity"
Cohesion: 0.19
Nodes (6): HistoricoCotacaoJpaEntity, HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable

### Community 48 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 49 - "login/route.ts"
Cohesion: 0.18
Nodes (12): POST(), POST(), { registerMock, originMock }, backendClient, AuthConfig, AuthEnv, loadAuthConfig(), hasValidOrigin() (+4 more)

### Community 50 - ".login"
Cohesion: 0.15
Nodes (5): IssuedAccessToken, AuthenticationFailedException, LoginRequest, LoginResponse, LoginControllerTest

### Community 51 - ".execute"
Cohesion: 0.16
Nodes (4): CashMovementNormalizer, CashMovementDomainTest, DataOutputStream, java.io.DataOutputStream

### Community 52 - "CashMovementConcurrencyIT"
Cohesion: 0.29
Nodes (5): CashMovementConcurrencyIT, Fixture, FunctionalInterface, Outcomes, ThrowingSupplier

### Community 53 - "CorretoraPersistenceAdapter"
Cohesion: 0.18
Nodes (4): DuplicateBrokerException, CorretoraJpaRepository, CorretoraPersistenceAdapter, Override

### Community 54 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 55 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 56 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 57 - "AtivoUseCase"
Cohesion: 0.18
Nodes (3): AtivoNotFoundException, AtivoUseCase, AtivoControllerTest

### Community 58 - "MarketQuoteProperties"
Cohesion: 0.20
Nodes (7): MarketQuoteProperties, Provider, Override, TwelveDataClient, TwelveDataQuoteAdapter, TwelveResponse, org.slf4j.Logger

### Community 59 - "AlphaVantageQuoteAdapter.java"
Cohesion: 0.24
Nodes (7): AlphaBar, AlphaMeta, AlphaResponse, AlphaVantageClient, AlphaVantageQuoteAdapter, Override, QuoteProviderAdaptersTest

### Community 60 - "CashMovementApiIT"
Cohesion: 0.24
Nodes (6): CashMovementApiIT, HttpCall, HttpResults, FunctionalInterface, User, org.springframework.test.web.servlet.MvcResult

### Community 61 - "cookie.ts"
Cohesion: 0.23
Nodes (10): POST(), fetchMock, GET(), AUTH_SESSION_COOKIE, authCookie(), authCookieBase(), expiredAuthCookie(), resolveCurrentUser() (+2 more)

### Community 62 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 63 - "BrokerProviderAdaptersIT"
Cohesion: 0.20
Nodes (8): AfterAll, BrokerProviderAdaptersIT, Test, MockResponse, org.springframework.core.env.Environment, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer

### Community 64 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.18
Nodes (7): CarteiraInvestimentoApplication, FaultInjectionConfiguration, FaultSwitches, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 66 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 67 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 68 - "AssetCatalogPersistenceIT"
Cohesion: 0.23
Nodes (4): AssetCatalogPersistenceIT, AtivoQuery, SqlAction, FunctionalInterface

### Community 69 - "LogAuditoriaJpaEntity"
Cohesion: 0.18
Nodes (3): LogAuditoriaJpaEntity, jakarta.persistence.Entity, jakarta.persistence.Table

### Community 70 - "TipoMovimentacaoCaixa"
Cohesion: 0.22
Nodes (6): CashConflictException, CashOperationResult, MovimentacaoCaixa, TipoMovimentacaoCaixa, DEPOSITO, SAQUE

### Community 71 - "org.springframework.context.annotation.Configuration"
Cohesion: 0.18
Nodes (9): OpenApiConfiguration, OpenApiConfigurationTest, PropertiesConfiguration, io.swagger.v3.oas.models.OpenAPI, LocalValidatorFactoryBean, OpenAPI, org.springframework.boot.test.context.runner.ApplicationContextRunner, org.springframework.context.annotation.Configuration (+1 more)

### Community 72 - "BrapiQuoteAdapter.java"
Cohesion: 0.32
Nodes (5): BrapiClient, BrapiQuoteAdapter, BrapiResponse, BrapiResult, Override

### Community 73 - ".create"
Cohesion: 0.21
Nodes (6): BrokerErrors, CreateBrokerRequest, io.swagger.v3.oas.annotations.responses.ApiResponses, java.lang.annotation.Retention, java.lang.annotation.Target, PostMapping

### Community 74 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.27
Nodes (4): PersistedUserJwtAuthenticationConverterTest, PersistedUserJwtAuthenticationConverterTest.AdminProbeController, PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController, PersistedUserJwtAuthenticationConverterTest.SecurityTestConfiguration

### Community 75 - "PostgreSqlContainerSupport"
Cohesion: 0.20
Nodes (6): AuditPersistenceIntegrationTest, DataSource, CambioTransactionBoundaryIT, PostgreSqlContainerSupport, RegistrationConcurrencyIntegrationTest, org.springframework.transaction.PlatformTransactionManager

### Community 76 - "CashMovementSchemaIT"
Cohesion: 0.35
Nodes (3): CashMovementSchemaIT, java.sql.Connection, org.junit.jupiter.api.BeforeAll

### Community 77 - "Wallet"
Cohesion: 0.35
Nodes (5): ExistingReservation, NewReservation, Reservation, Wallet, CashMovementApplicationServiceTest

### Community 78 - "ReceitaFederalPort"
Cohesion: 0.23
Nodes (6): ReceitaFederalPort, BrokerUpstreamException, BrasilApiCnpjAdapter, BrasilApiCnpjClient, CnpjProviderResponse, Override

### Community 80 - "CashPersistenceAdapter"
Cohesion: 0.27
Nodes (3): CashPersistenceAdapter, Override, java.sql.ResultSet

### Community 81 - "AccessDeniedAuditingService"
Cohesion: 0.29
Nodes (4): AccessDeniedAuditingService, AccessDeniedAuditingServiceTest, MockHttpServletRequest, org.junit.jupiter.api.AfterEach

### Community 82 - ".edit"
Cohesion: 0.26
Nodes (3): UpdateBrokerRequest, com.fasterxml.jackson.annotation.JsonSetter, PatchMapping

### Community 84 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 85 - "org.springframework.cloud.openfeign.FeignClient"
Cohesion: 0.29
Nodes (6): ViaCepPort, Override, ViaCepAdapter, ViaCepClient, ViaCepProviderResponse, org.springframework.cloud.openfeign.FeignClient

### Community 86 - "AtivoPersistenceAdapterTest.java"
Cohesion: 0.24
Nodes (3): AtivoPersistenceAdapterTest, DataIntegrityViolationException, org.springframework.dao.DataIntegrityViolationException

### Community 87 - "org.springframework.boot.context.properties.ConfigurationProperties"
Cohesion: 0.25
Nodes (6): AdminProperties, BrokerProviderProperties, Provider, jakarta.validation.constraints.AssertTrue, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.validation.annotation.Validated

### Community 88 - "PersistedUserJwtAuthenticationConverter"
Cohesion: 0.31
Nodes (6): Override, PersistedUserJwtAuthenticationConverter, InvalidBearerTokenException, org.springframework.core.convert.converter.Converter, org.springframework.security.authentication.AbstractAuthenticationToken, org.springframework.security.oauth2.server.resource.InvalidBearerTokenException

### Community 89 - "MarketQuoteApplicationServiceTest.java"
Cohesion: 0.35
Nodes (6): FakeAssets, Override, com.carteira.carteiraInvestimento.application.port.AtivoPage, com.carteira.carteiraInvestimento.application.port.AtivoQuery, com.carteira.carteiraInvestimento.application.port.AtivoReadScope, com.carteira.carteiraInvestimento.domain.asset.Ativo

### Community 91 - "guard.ts"
Cohesion: 0.33
Nodes (7): AdminLayout(), InicioLayout(), redirectToLogin(), requireCurrentUser(), requireRole(), { redirectMock, resolveMock }, user

### Community 92 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 93 - "CvmPort"
Cohesion: 0.31
Nodes (5): CvmPort, BrasilApiCvmAdapter, BrasilApiCvmClient, CvmProviderResponse, Override

### Community 94 - "auth-pages.test.tsx"
Cohesion: 0.29
Nodes (5): AccessDeniedPage(), LoginPage(), AdminPage(), InicioPage(), RegisterPage()

### Community 95 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 96 - "CambioConfiguration"
Cohesion: 0.33
Nodes (4): CambioConfiguration, CambioConfigurationTest, MutableTicker, com.github.benmanes.caffeine.cache.Ticker

### Community 99 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 104 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 107 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 108 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 109 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 110 - "Fakes"
Cohesion: 0.60
Nodes (3): Fakes, Bean, Primary

### Community 112 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 113 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 118 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **335 isolated node(s):** `BackendErrorKind`, `BrokerNotFoundException`, `AuthConfig`, `AuthEnv`, `Provider` (+330 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 519 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **55 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Usuario` connect `Usuario` to `org.springframework.stereotype.Component`, `org.springframework.context.annotation.Bean`, `.register`, `AuditSecurityEventsIntegrationTest`, `PersistedUserJwtAuthenticationConverterTest`, `org.springframework.security.oauth2.jwt.JwtEncoder`, `.login`, `Role`, `PersistedUserJwtAuthenticationConverter`, `PasswordHasher`, `AuditSecurityEventsIntegrationTest.java`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Why does `Corretora` connect `Corretora` to `BrokerUseCase`, `CorretoraJpaEntity`, `org.junit.jupiter.api.BeforeEach`, `BrokerCatalogSecurityIT`, `org.springframework.transaction.annotation.Transactional`, `org.springframework.security.access.prepost.PreAuthorize`, `CorretoraPersistenceAdapter`, `BrokerCatalogPersistenceIT`, `.criar`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **Why does `QuoteProvider` connect `QuoteProvider` to `org.junit.jupiter.api.BeforeEach`, `LogAuditoriaJpaEntity`, `BrapiQuoteAdapter.java`, `Cotacao`, `HistoricoCotacaoJpaEntity`, `MarketQuoteControllerTest.java`, `MarketQuoteApplicationServiceTest.java`, `MarketQuoteProperties`, `AlphaVantageQuoteAdapter.java`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **What connects `BackendErrorKind`, `BrokerNotFoundException`, `AuthConfig` to the rest of the system?**
  _335 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ObservacaoCambio` be split into smaller, more focused modules?**
  _Cohesion score 0.09562841530054644 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.BeforeEach` be split into smaller, more focused modules?**
  _Cohesion score 0.09954751131221719 - nodes in this community are weakly interconnected._
- **Should `jakarta.servlet.http.HttpServletRequest` be split into smaller, more focused modules?**
  _Cohesion score 0.12627450980392158 - nodes in this community are weakly interconnected._