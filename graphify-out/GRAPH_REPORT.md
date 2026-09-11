# Graph Report - projetoJeff  (2026-09-11)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2692 nodes · 7252 edges · 177 communities (112 shown, 55 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 534 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c245c1ce`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ObservacaoCambio
- AuditoriaCommand
- jakarta.servlet.http.HttpServletRequest
- org.junit.jupiter.api.BeforeEach
- java.sql.Connection
- AlphaVantageCambioAdapter
- PersistedUserJwtAuthenticationConverterTest.java
- org.springframework.context.annotation.Bean
- Usuario
- InvestmentConcurrencyIT
- UsuarioPort
- Cotacao
- org.junit.jupiter.api.Test
- QuoteProvider
- org.springframework.stereotype.Component
- Corretora
- InvestmentPersistenceAdapter
- .create
- org.springframework.security.oauth2.jwt.Jwt
- auth.ts
- types.ts
- Requirements
- Ativo
- ADDED Requirements
- Requirement: Cadastro publico transacional
- com.carteira.carteiraInvestimento.application.port.AuditoriaPort
- .execute
- PersistedUserJwtAuthenticationConverterTest
- com.fasterxml.jackson.annotation.JsonAnySetter
- ADDED Requirements
- TipoAtivo
- Posicao
- org.springframework.transaction.annotation.Transactional
- compilerOptions
- org.springframework.web.bind.annotation.GetMapping
- MarketQuoteSecurityIT
- BrokerCatalogPersistenceIT
- components.json
- InvestmentRollbackIT
- TipoMovimentacaoCaixa
- CashMovementConcurrencyIT
- Fixture
- CorretoraReadScope
- AtivoController
- login-form.tsx
- BrokerProviderAdaptersIT
- AtivoQuery
- BrokerApplicationService
- HistoricoCotacaoJpaEntity
- IdentityApplicationConfiguration.java
- CorrelationIdFilter
- .criar
- jakarta.persistence.Entity
- package.json
- login/route.ts
- AuditSecurityEventsIntegrationTest
- BrokerUpstreamException
- Mercado
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- AlphaVantageQuoteAdapter.java
- PostgreSqlContainerSupport
- CashMovementApiIT
- cookie.ts
- ADDED Requirements
- InvestmentPersistencePort
- MarketQuoteProperties
- CorretoraJpaEntity
- AssetCatalogPersistenceIT
- CashMovementRollbackIT
- Decisions
- AtivoUseCase
- org.springframework.web.bind.annotation.RestController
- CanonicalFingerprint
- AtivoPersistenceAdapter
- org.springframework.cloud.openfeign.FeignClient
- .register
- CashPersistenceAdapter
- CorretoraPersistenceAdapter
- UsuarioJpaEntity
- .execute
- ApplicationFoundationIT
- IdentityPersistenceAdapter
- CashIdempotencyPort
- MarketQuoteControllerTest.java
- .nova
- org.springframework.data.jpa.repository.JpaRepository
- TipoTransacao
- InvestmentOperationResult
- PersistedUserJwtAuthenticationConverter
- org.springframework.security.access.prepost.PreAuthorize
- BrokerCatalogSecurityIT
- .b3BuyReplaySellListsOwnershipSnapshotsAndAuditAreCoherent
- RegistrationTransactionIntegrationTest
- Requirement: Documentacao HTTP e erros padronizados
- LocalSnapshotComposer
- MarketQuoteApplicationServiceTest
- SnapshotCompatibilityIT
- guard.ts
- mvnw
- CvmPort
- CarteiraJpaEntity
- .edit
- auth-pages.test.tsx
- tasks.md
- CnpjCanonicalizer
- CambioConfiguration
- HistoricoCambioJpaEntity
- CambioAuditIT
- IdentitySchemaIntegrationTest
- MarketQuotePersistenceIT
- scripts
- DuplicateEmailPersistenceIntegrationTest
- org.springframework.boot.context.properties.ConfigurationProperties
- UsuarioJpaRepository
- .get
- Project Foundation Proposal
- proposal.md
- OpenSpec Apply Change Workflow
- usuarios
- CarteiraInvestimentoApplication
- CorrelationIdFilterTest
- app/layout.tsx
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- NomeAtivoCanonicalizer
- CotacaoDomainTest
- .valor
- frontend/package.json
- BrokerAuditException
- CashConflictException
- DuplicateBrokerException
- PrimaryWalletMissingException
- FinancialStateException
- app/page.tsx
- BrokerNotFoundException.java
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
2. `Corretora` - 50 edges
3. `InvestmentPersistencePort` - 42 edges
4. `ObservacaoCambio` - 41 edges
5. `Ativo` - 38 edges
6. `AuditoriaCommand` - 36 edges
7. `Cotacao` - 36 edges
8. `GlobalExceptionHandler` - 34 edges
9. `AuditSecurityEventsIntegrationTest` - 34 edges
10. `CambioProvider` - 32 edges

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

## Communities (177 total, 55 thin omitted)

### Community 0 - "ObservacaoCambio"
Cohesion: 0.09
Nodes (22): CambioProviderPort, HistoricoCambioPort, CambioApplicationService, Override, CambioProviderException, CambioUnavailableException, CambioUseCase, CambioExterno (+14 more)

### Community 1 - "AuditoriaCommand"
Cohesion: 0.07
Nodes (32): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+24 more)

### Community 2 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.12
Nodes (27): GlobalExceptionHandler, com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException, com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException, com.carteira.carteiraInvestimento.application.service.BrokerAuditException, com.carteira.carteiraInvestimento.application.service.BrokerComplianceException, com.carteira.carteiraInvestimento.application.service.BrokerNotFoundException, com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException, com.carteira.carteiraInvestimento.application.service.CambioUnavailableException (+19 more)

### Community 3 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.12
Nodes (25): AssetCatalogSecurityIT, BrokerTransactionBoundaryIT, BeforeEach, InitialAdminBootstrapIntegrationTest, InvestmentTransactionApiIT, BrokerTransactionBoundaryIT.Fakes, com.carteira.carteiraInvestimento.application.port.UsuarioPort, com.carteira.carteiraInvestimento.application.service.AtivoUseCase (+17 more)

### Community 4 - "java.sql.Connection"
Cohesion: 0.10
Nodes (9): CashMovementSchemaIT, Fixture, InvestmentSchemaIT, Column, InvestmentV9MetadataIT, Entry, java.sql.Connection, org.flywaydb.core.Flyway (+1 more)

### Community 5 - "AlphaVantageCambioAdapter"
Cohesion: 0.10
Nodes (13): AlphaCambioRate, AlphaCambioResponse, AlphaVantageCambioAdapter, AlphaVantageCambioClient, Override, Override, TwelveCambioResponse, TwelveDataCambioAdapter (+5 more)

### Community 6 - "PersistedUserJwtAuthenticationConverterTest.java"
Cohesion: 0.12
Nodes (24): JwtConfiguration, JwtProperties, JwtAccessTokenIssuer, FoundationSecurityConfigurationTest, SecurityTestConfiguration, JwtAccessTokenIssuerTest, SecurityTestConfiguration, CurrentPrincipalControllerTest (+16 more)

### Community 7 - "org.springframework.context.annotation.Bean"
Cohesion: 0.08
Nodes (19): AuditoriaIsoladaPort, FoundationSecurityConfiguration, InvestmentConfiguration, BcryptPasswordHasher, PropertiesConfiguration, AccessDeniedProbeConfiguration, ch.qos.logback.classic.Logger, LocalValidatorFactoryBean (+11 more)

### Community 8 - "Usuario"
Cohesion: 0.08
Nodes (9): CurrentPrincipalService, Usuario, Override, Fakes, IdentityApplicationServiceTest, Override, TestUsuarios, Override (+1 more)

### Community 9 - "InvestmentConcurrencyIT"
Cohesion: 0.15
Nodes (12): Action, CashInvestmentRace, DEPOSIT_BUY, DEPOSIT_SELL, WITHDRAW_BUY, WITHDRAW_SELL, Fixture, InvestmentConcurrencyIT (+4 more)

### Community 10 - "UsuarioPort"
Cohesion: 0.10
Nodes (12): AccessTokenIssuer, IssuedAccessToken, AuditoriaPort, PasswordHasher, UsuarioPort, InitialAdminBootstrapService, LoginService, EmailCanonicalizer (+4 more)

### Community 11 - "Cotacao"
Cohesion: 0.13
Nodes (13): CotacaoProviderPort, HistoricoCotacaoPage, HistoricoCotacaoPort, Override, MarketQuoteApplicationService, MarketQuoteUseCase, Cotacao, MarketQuoteConfiguration (+5 more)

### Community 12 - "org.junit.jupiter.api.Test"
Cohesion: 0.08
Nodes (10): ObservacaoCambioTest, IdentityDomainTest, MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, FlywayValidationIT, MissingDatabaseConfigurationTest (+2 more)

### Community 13 - "QuoteProvider"
Cohesion: 0.10
Nodes (14): CotacaoExterna, QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA, FakeAssets, FakeHistory, FakeProvider (+6 more)

### Community 14 - "org.springframework.stereotype.Component"
Cohesion: 0.12
Nodes (15): AuthenticationFailedException, AccessDeniedAuditingService, Override, SecurityProblemDetailHandler, ProblemDetailFactory, MockHttpServletRequest, SecurityProblemDetailHandlerTest, LoginControllerTest (+7 more)

### Community 15 - "Corretora"
Cohesion: 0.11
Nodes (8): BrokerPatch, BrokerUseCase, Corretora, Test, BrokerControllerTest, MockMvc, Test, org.springframework.security.authentication.TestingAuthenticationToken

### Community 16 - "InvestmentPersistenceAdapter"
Cohesion: 0.13
Nodes (5): TransactionView, InvestmentConflictException, InvestmentPersistenceAdapter, Override, java.sql.ResultSet

### Community 17 - ".create"
Cohesion: 0.14
Nodes (15): BrokerAdminResponse, BrokerController, GetMapping, BrokerErrors, BrokerListResponse, BrokerSelectionResponse, CreateBrokerRequest, io.swagger.v3.oas.annotations.responses.ApiResponse (+7 more)

### Community 18 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.18
Nodes (11): InvestmentController, CashBalanceResponse, CashMovementController, CashMovementResponse, io.swagger.v3.oas.annotations.Operation, io.swagger.v3.oas.annotations.responses.ApiResponses, io.swagger.v3.oas.annotations.tags.Tag, org.springframework.http.ResponseEntity (+3 more)

### Community 19 - "auth.ts"
Cohesion: 0.14
Nodes (24): messageFor(), RegisterForm(), authMeKey, clearAuthState(), confirmCurrentUser(), asProblem(), AuthFormError, AuthProblem (+16 more)

### Community 20 - "types.ts"
Cohesion: 0.15
Nodes (19): { loginMock, originMock }, { resolveMock }, CurrentUser, LoginInput, RegisterInput, Role, request(), fetchMock (+11 more)

### Community 21 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 22 - "Ativo"
Cohesion: 0.17
Nodes (6): AtivoPort, AtivoApplicationService, Override, Ativo, AtivoApplicationServiceTest, AtivoDomainTest

### Community 23 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 24 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 25 - "com.carteira.carteiraInvestimento.application.port.AuditoriaPort"
Cohesion: 0.14
Nodes (14): CashSnapshotPort, CashWalletPort, CashMovementApplicationService, Override, CashMovementConfiguration, CashMovementUseCase, com.carteira.carteiraInvestimento.application.port.AuditoriaPort, com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort (+6 more)

### Community 26 - ".execute"
Cohesion: 0.23
Nodes (4): ProtectedAsset, ProtectedBroker, Wallet, InvestmentApplicationServiceTest

### Community 27 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.11
Nodes (11): Role, ROLE_ADMIN, ROLE_USER, CurrentUserResponse, RegisteredUserResponse, RegisterRequest, AdminProbeController, PersistedUserJwtAuthenticationConverterTest (+3 more)

### Community 28 - "com.fasterxml.jackson.annotation.JsonAnySetter"
Cohesion: 0.10
Nodes (6): UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, UpdateBrokerLifecycleRequest, CashMovementRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator

### Community 29 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 30 - "TipoAtivo"
Cohesion: 0.12
Nodes (12): AtivoSort, NOME, TICKER, SortDirection, ASC, DESC, DuplicateTickerException, TipoAtivo (+4 more)

### Community 31 - "Posicao"
Cohesion: 0.14
Nodes (4): InvestmentNumbers, Posicao, Sale, InvestmentDomainTest

### Community 32 - "org.springframework.transaction.annotation.Transactional"
Cohesion: 0.14
Nodes (8): Page, PositionView, InvestmentApplicationService, Override, InvestmentUseCase, PositionPageResponse, TransactionPageResponse, org.springframework.transaction.annotation.Transactional

### Community 33 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 34 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.11
Nodes (7): Authentication, InvestmentNotFoundException, SecurityProbeController, ProtectedProbeController, AccessDeniedProbeController, FailureProbeController, org.springframework.web.bind.annotation.GetMapping

### Community 35 - "MarketQuoteSecurityIT"
Cohesion: 0.14
Nodes (6): CambioSecurityIT, FixedCambio, MarketQuoteSecurityIT, CambioSecurityIT.FixedCambio, com.carteira.carteiraInvestimento.domain.identity.Role, com.carteira.carteiraInvestimento.domain.identity.Usuario

### Community 36 - "BrokerCatalogPersistenceIT"
Cohesion: 0.21
Nodes (4): BrokerLocalTransactionService, BrokerCatalogPersistenceIT, BeforeEach, Test

### Community 37 - "components.json"
Cohesion: 0.09
Nodes (20): OpenApiConfiguration, OpenApiConfigurationTest, aliases, components, hooks, lib, ui, utils (+12 more)

### Community 38 - "InvestmentRollbackIT"
Cohesion: 0.13
Nodes (12): AuditoriaPersistenceAdapter, Override, Fixture, InvestmentRollbackIT, Stage, AUDIT, COMPLETE, POSITION (+4 more)

### Community 39 - "TipoMovimentacaoCaixa"
Cohesion: 0.14
Nodes (9): CashLedgerPort, CashMovementPage, CashOperationResult, MovimentacaoCaixa, TipoMovimentacaoCaixa, DEPOSITO, SAQUE, CashMovementHistoryResponse (+1 more)

### Community 40 - "CashMovementConcurrencyIT"
Cohesion: 0.22
Nodes (6): CashMovementUseCase, CashMovementConcurrencyIT, Fixture, FunctionalInterface, Outcomes, ThrowingSupplier

### Community 41 - "Fixture"
Cohesion: 0.16
Nodes (7): AuditoriaCommand, CambioApplicationServiceTest, FakeAudit, FakeHistory, Fixture, Override, MutableClock

### Community 42 - "CorretoraReadScope"
Cohesion: 0.14
Nodes (6): CorretoraPage, CorretoraPort, CorretoraReadScope, ACTIVE_ONLY, ALL, Override

### Community 43 - "AtivoController"
Cohesion: 0.18
Nodes (5): AtivoController, AtivoListResponse, AtivoResponse, CreateAtivoRequest, org.springframework.web.bind.annotation.PatchMapping

### Community 44 - "login-form.tsx"
Cohesion: 0.15
Nodes (11): fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), AUTH_SESSION_COOKIE, protectedRoutes, DEFAULT_RETURN_TO, isSafeInternalPath() (+3 more)

### Community 45 - "BrokerProviderAdaptersIT"
Cohesion: 0.16
Nodes (10): AfterAll, BrokerProviderProperties, Provider, BrokerProviderAdaptersIT, Test, MockResponse, org.springframework.core.env.Environment, org.springframework.test.context.DynamicPropertyRegistry (+2 more)

### Community 46 - "AtivoQuery"
Cohesion: 0.17
Nodes (8): AtivoPage, AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, TickerCanonicalizer, org.springframework.dao.DataIntegrityViolationException

### Community 47 - "BrokerApplicationService"
Cohesion: 0.13
Nodes (9): ReceitaFederalPort, ViaCepPort, BrokerApplicationService, AuditoriaIsoladaPort, BrokerComplianceException, BeforeEach, Fakes, Bean (+1 more)

### Community 48 - "HistoricoCotacaoJpaEntity"
Cohesion: 0.18
Nodes (6): HistoricoCotacaoJpaEntity, HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable

### Community 49 - "IdentityApplicationConfiguration.java"
Cohesion: 0.16
Nodes (14): AdminProperties, IdentityApplicationConfiguration, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher, com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService, com.carteira.carteiraInvestimento.application.service.InitialAdminBootstrapService, com.carteira.carteiraInvestimento.application.service.LoginService (+6 more)

### Community 50 - "CorrelationIdFilter"
Cohesion: 0.14
Nodes (6): CorrelationIdFilter, Override, AccessDeniedAuditingServiceTest, MockHttpServletRequest, CorrelationProbeController, jakarta.servlet.FilterChain

### Community 51 - ".criar"
Cohesion: 0.29
Nodes (6): CadastroCnpj, EnderecoPostal, RegistroCvm, BrokerApplicationServiceTest, AuditoriaIsoladaPort, Test

### Community 52 - "jakarta.persistence.Entity"
Cohesion: 0.20
Nodes (8): CarteiraSnapshotJpaEntity, PosicaoJpaEntity, TransacaoIdempotenciaJpaEntity, TransacaoJpaEntity, jakarta.persistence.Column, jakarta.persistence.Entity, jakarta.persistence.Table, org.hibernate.annotations.Immutable

### Community 53 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 54 - "login/route.ts"
Cohesion: 0.18
Nodes (12): POST(), POST(), { registerMock, originMock }, backendClient, AuthConfig, AuthEnv, loadAuthConfig(), hasValidOrigin() (+4 more)

### Community 56 - "BrokerUpstreamException"
Cohesion: 0.16
Nodes (9): BrokerUpstreamException, BrasilApiCnpjAdapter, BrasilApiCnpjClient, CnpjProviderResponse, Override, Override, ViaCepAdapter, ViaCepClient (+1 more)

### Community 57 - "Mercado"
Cohesion: 0.19
Nodes (7): Mercado, B3, US, Moeda, BRL, USD, AtivoJpaEntity

### Community 58 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 59 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 60 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 61 - "AlphaVantageQuoteAdapter.java"
Cohesion: 0.24
Nodes (7): AlphaBar, AlphaMeta, AlphaResponse, AlphaVantageClient, AlphaVantageQuoteAdapter, Override, QuoteProviderAdaptersTest

### Community 62 - "PostgreSqlContainerSupport"
Cohesion: 0.17
Nodes (4): CambioSchemaIT, CambioTransactionBoundaryIT, PostgreSqlContainerSupport, RegistrationConcurrencyIntegrationTest

### Community 63 - "CashMovementApiIT"
Cohesion: 0.24
Nodes (6): CashMovementApiIT, HttpCall, HttpResults, FunctionalInterface, User, org.springframework.test.web.servlet.MvcResult

### Community 64 - "cookie.ts"
Cohesion: 0.23
Nodes (10): POST(), fetchMock, GET(), AUTH_SESSION_COOKIE, authCookie(), authCookieBase(), expiredAuthCookie(), resolveCurrentUser() (+2 more)

### Community 65 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 66 - "InvestmentPersistencePort"
Cohesion: 0.28
Nodes (7): ExistingReservation, InvestmentPersistencePort, LocalExchangeRate, NewReservation, Reservation, Transacao, com.carteira.carteiraInvestimento.domain.fx.MoedaCambio

### Community 67 - "MarketQuoteProperties"
Cohesion: 0.23
Nodes (7): MarketQuoteProperties, Provider, Override, TwelveDataClient, TwelveDataQuoteAdapter, TwelveResponse, org.slf4j.Logger

### Community 69 - "AssetCatalogPersistenceIT"
Cohesion: 0.21
Nodes (4): AssetCatalogPersistenceIT, AtivoQuery, SqlAction, FunctionalInterface

### Community 70 - "CashMovementRollbackIT"
Cohesion: 0.19
Nodes (8): CashMovementRollbackIT, FailureStage, AUDIT, MOVEMENT, RESULT, SNAPSHOT, com.carteira.carteiraInvestimento.infrastructure.persistence.AuditoriaPersistenceAdapter, org.springframework.transaction.support.TransactionTemplate

### Community 71 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 72 - "AtivoUseCase"
Cohesion: 0.19
Nodes (3): AtivoNotFoundException, AtivoUseCase, AtivoControllerTest

### Community 73 - "org.springframework.web.bind.annotation.RestController"
Cohesion: 0.28
Nodes (8): RegistrationService, CurrentPrincipalController, RegistrationController, CambioController, MarketQuoteController, io.swagger.v3.oas.annotations.security.SecurityRequirement, org.springframework.web.bind.annotation.RequestMapping, org.springframework.web.bind.annotation.RestController

### Community 74 - "CanonicalFingerprint"
Cohesion: 0.18
Nodes (5): InvestmentFingerprint, CanonicalFingerprint, Field, DataOutputStream, java.io.DataOutputStream

### Community 75 - "AtivoPersistenceAdapter"
Cohesion: 0.18
Nodes (4): AtivoPersistenceAdapter, Override, AtivoPersistenceAdapterTest, DataIntegrityViolationException

### Community 76 - "org.springframework.cloud.openfeign.FeignClient"
Cohesion: 0.28
Nodes (6): BrapiClient, BrapiQuoteAdapter, BrapiResponse, BrapiResult, Override, org.springframework.cloud.openfeign.FeignClient

### Community 77 - ".register"
Cohesion: 0.16
Nodes (3): DuplicateEmailException, Carteira, RegistrationControllerTest

### Community 78 - "CashPersistenceAdapter"
Cohesion: 0.23
Nodes (5): CashPersistenceAdapter, CashMovementPage, Override, com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa, MovimentacaoCaixa

### Community 79 - "CorretoraPersistenceAdapter"
Cohesion: 0.24
Nodes (3): CorretoraJpaRepository, CorretoraPersistenceAdapter, Override

### Community 80 - "UsuarioJpaEntity"
Cohesion: 0.25
Nodes (4): Override, Entity, Table, UsuarioJpaEntity

### Community 81 - ".execute"
Cohesion: 0.22
Nodes (4): CashMovementNormalizer, TipoMovimentacaoCaixa, CashMovementDomainTest, CashOperationResult

### Community 82 - "ApplicationFoundationIT"
Cohesion: 0.23
Nodes (4): ApplicationFoundationIT, com.fasterxml.jackson.databind.JsonNode, com.fasterxml.jackson.databind.ObjectMapper, org.springframework.web.context.WebApplicationContext

### Community 83 - "IdentityPersistenceAdapter"
Cohesion: 0.24
Nodes (5): CarteiraPort, CarteiraJpaRepository, IdentityPersistenceAdapter, Override, org.springframework.stereotype.Repository

### Community 84 - "CashIdempotencyPort"
Cohesion: 0.35
Nodes (6): CashIdempotencyPort, ExistingReservation, NewReservation, Reservation, Wallet, CashMovementApplicationServiceTest

### Community 85 - "MarketQuoteControllerTest.java"
Cohesion: 0.22
Nodes (3): InactiveAssetRefreshException, QuoteIntegrationException, QuoteNotFoundException

### Community 86 - ".nova"
Cohesion: 0.18
Nodes (4): BrokerDomainTest, BeforeEach, BeforeEach, TestingAuthenticationToken

### Community 87 - "org.springframework.data.jpa.repository.JpaRepository"
Cohesion: 0.23
Nodes (7): AtivoJpaRepository, HistoricoCambioJpaRepository, HistoricoCambioPersistenceAdapter, Override, LogAuditoriaJpaRepository, org.springframework.data.jpa.repository.JpaRepository, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 88 - "TipoTransacao"
Cohesion: 0.24
Nodes (5): InvestmentTransactionCommand, TipoTransacao, BUY, SELL, InvestmentTransactionRequest

### Community 89 - "InvestmentOperationResult"
Cohesion: 0.29
Nodes (5): InvestmentOperationResult, InvestmentOperationResponse, PositionResponse, TransactionResponse, com.fasterxml.jackson.annotation.JsonPropertyOrder

### Community 90 - "PersistedUserJwtAuthenticationConverter"
Cohesion: 0.27
Nodes (6): Override, PersistedUserJwtAuthenticationConverter, InvalidBearerTokenException, org.springframework.core.convert.converter.Converter, org.springframework.security.authentication.AbstractAuthenticationToken, org.springframework.security.oauth2.server.resource.InvalidBearerTokenException

### Community 91 - "org.springframework.security.access.prepost.PreAuthorize"
Cohesion: 0.24
Nodes (4): CotacaoResponse, HistoricoCotacaoResponse, org.springframework.security.access.prepost.PreAuthorize, org.springframework.web.bind.annotation.PutMapping

### Community 92 - "BrokerCatalogSecurityIT"
Cohesion: 0.33
Nodes (5): BrokerCatalogSecurityIT, Test, JwtEncoder, Role, Usuario

### Community 94 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.27
Nodes (4): FaultInjectionConfiguration, FaultSwitches, RegistrationTransactionIntegrationTest, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 95 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 96 - "LocalSnapshotComposer"
Cohesion: 0.29
Nodes (3): KnownValuation, LocalSnapshotComposer, Totals

### Community 99 - "guard.ts"
Cohesion: 0.33
Nodes (7): AdminLayout(), InicioLayout(), redirectToLogin(), requireCurrentUser(), requireRole(), { redirectMock, resolveMock }, user

### Community 100 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 101 - "CvmPort"
Cohesion: 0.31
Nodes (5): CvmPort, BrasilApiCvmAdapter, BrasilApiCvmClient, CvmProviderResponse, Override

### Community 102 - "CarteiraJpaEntity"
Cohesion: 0.20
Nodes (3): CarteiraJpaEntity, Entity, Table

### Community 104 - "auth-pages.test.tsx"
Cohesion: 0.29
Nodes (5): AccessDeniedPage(), LoginPage(), AdminPage(), InicioPage(), RegisterPage()

### Community 105 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 107 - "CambioConfiguration"
Cohesion: 0.33
Nodes (4): CambioConfiguration, CambioConfigurationTest, MutableTicker, com.github.benmanes.caffeine.cache.Ticker

### Community 112 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 114 - "org.springframework.boot.context.properties.ConfigurationProperties"
Cohesion: 0.36
Nodes (4): AdminProperties, jakarta.validation.constraints.AssertTrue, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.validation.annotation.Validated

### Community 115 - "UsuarioJpaRepository"
Cohesion: 0.32
Nodes (5): RepositoryUserDetailsService, UsuarioJpaRepository, org.springframework.stereotype.Service, UserDetails, UserDetailsService

### Community 117 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 118 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 119 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 120 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 121 - "CarteiraInvestimentoApplication"
Cohesion: 0.60
Nodes (3): CarteiraInvestimentoApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan

### Community 124 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 125 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 129 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **348 isolated node(s):** `BrokerNotFoundException`, `AuthProblem`, `BackendErrorKind`, `Provider`, `AuthConfig` (+343 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 551 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **55 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `BrokerCatalogPersistenceIT` connect `BrokerCatalogPersistenceIT` to `CorretoraReadScope`, `org.junit.jupiter.api.BeforeEach`, `PostgreSqlContainerSupport`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Why does `TipoEvento` connect `AuditoriaCommand` to `BrokerCatalogPersistenceIT`, `jakarta.persistence.Entity`, `org.springframework.context.annotation.Bean`?**
  _High betweenness centrality (0.018) - this node is a cross-community bridge._
- **Why does `Corretora` connect `Corretora` to `org.junit.jupiter.api.BeforeEach`, `BrokerCatalogPersistenceIT`, `CorretoraJpaEntity`, `CorretoraReadScope`, `CnpjCanonicalizer`, `CorretoraPersistenceAdapter`, `.create`, `.criar`, `.nova`, `BrokerCatalogSecurityIT`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **What connects `BrokerNotFoundException`, `AuthProblem`, `BackendErrorKind` to the rest of the system?**
  _348 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ObservacaoCambio` be split into smaller, more focused modules?**
  _Cohesion score 0.08525149190110827 - nodes in this community are weakly interconnected._
- **Should `AuditoriaCommand` be split into smaller, more focused modules?**
  _Cohesion score 0.07477288609364081 - nodes in this community are weakly interconnected._
- **Should `jakarta.servlet.http.HttpServletRequest` be split into smaller, more focused modules?**
  _Cohesion score 0.12336719883889695 - nodes in this community are weakly interconnected._