# Graph Report - projetoJeff  (2026-09-12)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2874 nodes · 7776 edges · 184 communities (122 shown, 52 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 576 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `6b537e71`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AuditoriaCommand
- jakarta.servlet.http.HttpServletRequest
- java.sql.Connection
- InvestmentConcurrencyIT
- PortfolioValuationIT.java
- org.springframework.context.annotation.Bean
- org.junit.jupiter.api.BeforeEach
- Usuario
- UsuarioJpaEntity
- .register
- com.carteira.carteiraInvestimento.domain.quote.Cotacao
- AuditSecurityEventsIntegrationTest.java
- org.springframework.security.access.prepost.PreAuthorize
- InvestmentPersistenceAdapter
- .execute
- org.junit.jupiter.api.Test
- auth.ts
- AtivoControllerTest.java
- com.fasterxml.jackson.annotation.JsonAnySetter
- types.ts
- Requirements
- org.springframework.web.bind.annotation.GetMapping
- Ativo
- TransactionView
- ADDED Requirements
- Requirement: Cadastro publico transacional
- ADDED Requirements
- compilerOptions
- CorretoraReadScope
- org.springframework.boot.test.context.SpringBootTest
- JwtConfiguration
- org.springframework.web.bind.annotation.RestController
- BrokerCatalogPersistenceIT
- CambioApplicationService
- Cotacao
- PortfolioValuationPort
- PortfolioValuationIT
- CambioProvider
- components.json
- .get
- com.carteira.carteiraInvestimento.application.port.AuditoriaPort
- .execute
- TipoTransacao
- CambioProviderAdaptersTest
- Fixture
- AuditSecurityEventsIntegrationTest
- .execute
- Corretora
- AlphaVantageQuoteAdapter.java
- org.springframework.security.oauth2.jwt.Jwt
- login-form.tsx
- Posicao
- MarketQuoteUseCase
- TipoAtivo
- AtivoJpaEntity
- InvestmentApplicationService.java
- Role
- package.json
- login/route.ts
- MovimentacaoCaixa
- SecurityProblemDetailHandler
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- CorretoraPersistenceAdapter
- PortfolioValuationApplicationServiceTest
- com.carteira.carteiraInvestimento.domain.asset.Moeda
- jakarta.persistence.Entity
- CashPersistenceAdapter
- CashMovementApiIT
- cookie.ts
- ADDED Requirements
- AtivoUseCase
- .criar
- com.carteira.carteiraInvestimento.domain.quote.QuoteProvider
- CorretoraJpaEntity
- AssetCatalogPersistenceIT
- Decisions
- CashIdempotencyPort
- org.springframework.transaction.annotation.Transactional
- CambioAuditIT.java
- CanonicalFingerprint
- AtivoController
- InvestmentTransactionApiIT
- ObservacaoCambio
- org.springframework.stereotype.Repository
- BrapiQuoteAdapter.java
- PersistedUserJwtAuthenticationConverterTest
- FakeQuotes
- .consultar
- CvmPort
- CambioSchemaIT
- Mercado.java
- RegistrationTransactionIntegrationTest
- MarketQuoteApplicationServiceTest
- CotacaoExterna
- MarketQuoteController
- org.springframework.jdbc.core.JdbcTemplate
- ApplicationFoundationIT
- Requirement: Documentacao HTTP e erros padronizados
- BrokerProviderAdaptersIT
- org.springframework.cloud.openfeign.FeignClient
- .fingerprint
- org.springframework.boot.context.properties.ConfigurationProperties
- HistoricoCotacaoPersistenceAdapter
- TwelveDataCambioAdapter
- TwelveDataQuoteAdapter
- guard.ts
- mvnw
- ViaCepPort
- BrokerControllerTest
- CambioConfiguration.java
- AlphaVantageCambioAdapter
- UpdateBrokerRequest
- auth-pages.test.tsx
- tasks.md
- CnpjCanonicalizer
- .properties
- CambioAuditIT
- MarketQuotePersistenceIT
- scripts
- BrokerProviderAdaptersIT.java
- IdentitySchemaIntegrationTest
- Project Foundation Proposal
- ObservacaoCambioTest
- proposal.md
- OpenSpec Apply Change Workflow
- AtivoApplicationService.java
- usuarios
- Fakes
- .validate
- CorrelationIdFilterTest
- app/layout.tsx
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- NomeAtivoCanonicalizer
- PropertiesConfiguration
- frontend/package.json
- BrokerAuditException
- CashConflictException
- PrimaryWalletMissingException
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
- CashMovementPage
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
3. `PortfolioValuationIT` - 42 edges
4. `ObservacaoCambio` - 41 edges
5. `InvestmentPersistencePort` - 38 edges
6. `Ativo` - 38 edges
7. `AuditoriaCommand` - 36 edges
8. `GlobalExceptionHandler` - 36 edges
9. `AuditSecurityEventsIntegrationTest` - 34 edges
10. `TipoEvento` - 32 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `Fakes` --references--> `AuditoriaCommand`  [EXTRACTED]
  backend/src/test/java/com/carteira/carteiraInvestimento/application/service/IdentityApplicationServiceTest.java → backend/src/main/java/com/carteira/carteiraInvestimento/application/service/AuditoriaCommand.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (184 total, 52 thin omitted)

### Community 0 - "AuditoriaCommand"
Cohesion: 0.06
Nodes (40): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+32 more)

### Community 1 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.09
Nodes (32): PortfolioValuationConflictException, PortfolioValuationUpstreamException, GlobalExceptionHandler, com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException, com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException, com.carteira.carteiraInvestimento.application.service.BrokerAuditException, com.carteira.carteiraInvestimento.application.service.BrokerComplianceException, com.carteira.carteiraInvestimento.application.service.BrokerNotFoundException (+24 more)

### Community 2 - "java.sql.Connection"
Cohesion: 0.08
Nodes (10): CashMovementSchemaIT, Fixture, InvestmentSchemaIT, Column, InvestmentV9MetadataIT, PortfolioValuationSchemaIT, Entry, java.sql.Connection (+2 more)

### Community 3 - "InvestmentConcurrencyIT"
Cohesion: 0.09
Nodes (18): CashMovementConcurrencyIT, Fixture, FunctionalInterface, Outcomes, ThrowingSupplier, Action, CashInvestmentRace, DEPOSIT_BUY (+10 more)

### Community 4 - "PortfolioValuationIT.java"
Cohesion: 0.08
Nodes (30): CashMovementRollbackIT, FailureStage, AUDIT, MOVEMENT, RESULT, SNAPSHOT, TransactionTemplate, Fixture (+22 more)

### Community 5 - "org.springframework.context.annotation.Bean"
Cohesion: 0.10
Nodes (30): FoundationSecurityConfiguration, JwtProperties, Override, PersistedUserJwtAuthenticationConverter, FoundationSecurityConfigurationTest, SecurityTestConfiguration, SecurityTestConfiguration, AccessDeniedProbeConfiguration (+22 more)

### Community 6 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.10
Nodes (16): AssetCatalogSecurityIT, CambioSecurityIT, MarketQuoteSecurityIT, CambioSecurityIT.FixedCambio, com.carteira.carteiraInvestimento.application.port.UsuarioPort, com.carteira.carteiraInvestimento.application.service.AtivoUseCase, com.carteira.carteiraInvestimento.domain.asset.Mercado, com.carteira.carteiraInvestimento.domain.asset.TipoAtivo (+8 more)

### Community 7 - "Usuario"
Cohesion: 0.08
Nodes (9): UsuarioPort, CurrentPrincipalService, Usuario, Fakes, IdentityApplicationServiceTest, Override, TestUsuarios, Override (+1 more)

### Community 8 - "UsuarioJpaEntity"
Cohesion: 0.07
Nodes (16): AtivoJpaRepository, CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, LogAuditoriaJpaRepository, Override, RepositoryUserDetailsService (+8 more)

### Community 9 - ".register"
Cohesion: 0.08
Nodes (11): CarteiraPort, DuplicateEmailException, RegistrationService, Carteira, IdentityPersistenceAdapter, Override, DuplicateEmailPersistenceIntegrationTest, DataSource (+3 more)

### Community 10 - "com.carteira.carteiraInvestimento.domain.quote.Cotacao"
Cohesion: 0.13
Nodes (18): Override, MarketQuoteApplicationService, FakeAssets, FakeHistory, Fixture, CotacaoExterna, HistoricoCotacaoPage, Override (+10 more)

### Community 11 - "AuditSecurityEventsIntegrationTest.java"
Cohesion: 0.09
Nodes (13): AccessTokenIssuer, IssuedAccessToken, AuditoriaIsoladaPort, AuditoriaPort, PasswordHasher, AuthenticationFailedException, InitialAdminBootstrapService, LoginService (+5 more)

### Community 12 - "org.springframework.security.access.prepost.PreAuthorize"
Cohesion: 0.14
Nodes (17): BrokerUseCase, BrokerAdminResponse, BrokerController, GetMapping, BrokerErrors, BrokerListResponse, BrokerSelectionResponse, Test (+9 more)

### Community 13 - "InvestmentPersistenceAdapter"
Cohesion: 0.12
Nodes (15): InvestmentPersistenceAdapter, Override, Reservation, Wallet, com.carteira.carteiraInvestimento.domain.investment.Posicao, com.carteira.carteiraInvestimento.domain.investment.Transacao, ExistingReservation, LocalExchangeRate (+7 more)

### Community 14 - ".execute"
Cohesion: 0.20
Nodes (9): ExistingReservation, InvestmentPersistencePort, LocalExchangeRate, NewReservation, ProtectedAsset, ProtectedBroker, Reservation, Wallet (+1 more)

### Community 15 - "org.junit.jupiter.api.Test"
Cohesion: 0.09
Nodes (10): MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, FlywayValidationIT, MissingDatabaseConfigurationTest, InvestmentTransactionContractTest, CashMovementContractTest (+2 more)

### Community 16 - "auth.ts"
Cohesion: 0.14
Nodes (24): messageFor(), RegisterForm(), authMeKey, clearAuthState(), confirmCurrentUser(), asProblem(), AuthFormError, AuthProblem (+16 more)

### Community 17 - "AtivoControllerTest.java"
Cohesion: 0.14
Nodes (12): AtivoPage, AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME (+4 more)

### Community 18 - "com.fasterxml.jackson.annotation.JsonAnySetter"
Cohesion: 0.09
Nodes (7): UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, CreateBrokerRequest, UpdateBrokerLifecycleRequest, CashMovementRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator

### Community 19 - "types.ts"
Cohesion: 0.15
Nodes (19): { loginMock, originMock }, { resolveMock }, CurrentUser, LoginInput, RegisterInput, Role, request(), fetchMock (+11 more)

### Community 20 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 21 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.09
Nodes (9): Authentication, InvestmentConflictException, InvestmentNotFoundException, SecurityProbeController, AdminProbeController, ProtectedProbeController, AccessDeniedProbeController, FailureProbeController (+1 more)

### Community 22 - "Ativo"
Cohesion: 0.17
Nodes (6): AtivoPort, AtivoApplicationService, Override, Ativo, AtivoApplicationServiceTest, AtivoDomainTest

### Community 23 - "TransactionView"
Cohesion: 0.14
Nodes (11): Page, PositionView, TransactionView, InvestmentOperationResult, InvestmentUseCase, InvestmentOperationResponse, PositionPageResponse, PositionResponse (+3 more)

### Community 24 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 25 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 26 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 27 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 28 - "CorretoraReadScope"
Cohesion: 0.11
Nodes (8): CorretoraPage, CorretoraPort, CorretoraReadScope, ACTIVE_ONLY, ALL, Override, DataIntegrityViolationException, org.springframework.stereotype.Service

### Community 29 - "org.springframework.boot.test.context.SpringBootTest"
Cohesion: 0.15
Nodes (12): AuditPersistenceIntegrationTest, DataSource, BrokerTransactionBoundaryIT, BeforeEach, InitialAdminBootstrapIntegrationTest, PostgreSqlContainerSupport, RegistrationConcurrencyIntegrationTest, BrokerTransactionBoundaryIT.Fakes (+4 more)

### Community 30 - "JwtConfiguration"
Cohesion: 0.13
Nodes (9): JwtConfiguration, Override, JwtAccessTokenIssuer, JwtAccessTokenIssuerTest, javax.crypto.spec.SecretKeySpec, org.springframework.security.oauth2.core.OAuth2TokenValidator, org.springframework.security.oauth2.core.OAuth2TokenValidatorResult, org.springframework.security.oauth2.jwt.JwtDecoder (+1 more)

### Community 31 - "org.springframework.web.bind.annotation.RestController"
Cohesion: 0.18
Nodes (12): CurrentPrincipalController, LoginController, LoginRequest, RegisterRequest, RegistrationController, CambioController, CorrelationProbeController, io.swagger.v3.oas.annotations.security.SecurityRequirement (+4 more)

### Community 32 - "BrokerCatalogPersistenceIT"
Cohesion: 0.20
Nodes (4): BrokerLocalTransactionService, BrokerCatalogPersistenceIT, BeforeEach, Test

### Community 33 - "CambioApplicationService"
Cohesion: 0.18
Nodes (6): CambioProviderPort, HistoricoCambioPort, CambioApplicationService, Override, CambioExterno, com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort

### Community 34 - "Cotacao"
Cohesion: 0.13
Nodes (6): HistoricoCotacaoPage, HistoricoCotacaoPort, InactiveAssetRefreshException, Cotacao, MarketQuoteControllerTest, com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory

### Community 35 - "PortfolioValuationPort"
Cohesion: 0.15
Nodes (10): LocalState, MaterializedTotals, OpenPosition, PortfolioValuationPort, Override, PortfolioValuationPersistenceAdapter, WalletRow, FakePort (+2 more)

### Community 36 - "PortfolioValuationIT"
Cohesion: 0.24
Nodes (4): Fixture, Cotacao, PortfolioValuationIT, PortfolioValuationIT.Fakes

### Community 37 - "CambioProvider"
Cohesion: 0.13
Nodes (10): CambioProvider, ALPHA_VANTAGE, TWELVE_DATA, MoedaCambio, BRL, USD, HistoricoCambioJpaEntity, CambioResponse (+2 more)

### Community 38 - "components.json"
Cohesion: 0.09
Nodes (20): OpenApiConfiguration, OpenApiConfigurationTest, aliases, components, hooks, lib, ui, utils (+12 more)

### Community 39 - ".get"
Cohesion: 0.15
Nodes (7): BrokerCatalogSecurityIT, BeforeEach, Test, GlobalExceptionHandlerTest, JwtEncoder, Role, Usuario

### Community 40 - "com.carteira.carteiraInvestimento.application.port.AuditoriaPort"
Cohesion: 0.14
Nodes (15): AdminProperties, IdentityApplicationConfiguration, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.AuditoriaPort, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher, com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService, com.carteira.carteiraInvestimento.application.service.InitialAdminBootstrapService (+7 more)

### Community 41 - ".execute"
Cohesion: 0.15
Nodes (7): CashMovementPage, CashMovementUseCase, CashBalanceResponse, CashMovementController, CashMovementHistoryResponse, CashMovementResponse, Movement

### Community 42 - "TipoTransacao"
Cohesion: 0.18
Nodes (9): InvestmentTransactionCommand, TipoTransacao, BUY, SELL, InvestmentTransactionRequest, Fixture, Fixture, HeldOperation (+1 more)

### Community 43 - "CambioProviderAdaptersTest"
Cohesion: 0.21
Nodes (8): AlphaCambioRate, AlphaCambioResponse, TwelveCambioResponse, CambioProviderAdaptersTest, MarketQuoteProperties, MockWebServer, okhttp3.mockwebserver.MockResponse, okhttp3.mockwebserver.MockWebServer

### Community 44 - "Fixture"
Cohesion: 0.16
Nodes (7): AuditoriaCommand, CambioApplicationServiceTest, FakeAudit, FakeHistory, Fixture, Override, MutableClock

### Community 46 - ".execute"
Cohesion: 0.19
Nodes (10): CashMovementApplicationService, Override, CashMovementUseCase, CashOperationResult, com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort, com.carteira.carteiraInvestimento.application.port.CashLedgerPort, com.carteira.carteiraInvestimento.application.port.CashMovementPage, com.carteira.carteiraInvestimento.application.port.CashSnapshotPort (+2 more)

### Community 47 - "Corretora"
Cohesion: 0.15
Nodes (3): Corretora, BrokerDomainTest, BeforeEach

### Community 48 - "AlphaVantageQuoteAdapter.java"
Cohesion: 0.18
Nodes (8): QuoteIntegrationException, QuoteNotFoundException, MarketQuoteProperties, Provider, AlphaVantageClient, AlphaVantageQuoteAdapter, Override, org.slf4j.Logger

### Community 49 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.27
Nodes (7): InvestmentController, PortfolioValuationController, PortfolioValuationResponse, io.swagger.v3.oas.annotations.Operation, io.swagger.v3.oas.annotations.responses.ApiResponses, io.swagger.v3.oas.annotations.tags.Tag, org.springframework.security.oauth2.jwt.Jwt

### Community 50 - "login-form.tsx"
Cohesion: 0.15
Nodes (11): fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), AUTH_SESSION_COOKIE, protectedRoutes, DEFAULT_RETURN_TO, isSafeInternalPath() (+3 more)

### Community 51 - "Posicao"
Cohesion: 0.19
Nodes (3): Posicao, Sale, InvestmentDomainTest

### Community 52 - "MarketQuoteUseCase"
Cohesion: 0.16
Nodes (11): MarketQuoteUseCase, CambioUseCase, PortfolioValuationApplicationService, PortfolioValuationUseCase, MarketQuoteConfiguration, PortfolioValuationConfiguration, com.carteira.carteiraInvestimento.application.service.CambioUseCase, org.springframework.cloud.openfeign.EnableFeignClients (+3 more)

### Community 53 - "TipoAtivo"
Cohesion: 0.17
Nodes (11): Mercado, B3, US, Moeda, BRL, USD, TipoAtivo, ACAO (+3 more)

### Community 54 - "AtivoJpaEntity"
Cohesion: 0.18
Nodes (3): AtivoJpaEntity, AtivoPersistenceAdapter, Override

### Community 55 - "InvestmentApplicationService.java"
Cohesion: 0.16
Nodes (4): FinancialStateException, InvestmentNumbers, Transacao, com.carteira.carteiraInvestimento.domain.fx.MoedaCambio

### Community 56 - "Role"
Cohesion: 0.14
Nodes (7): EmailCanonicalizer, PasswordPolicy, Role, ROLE_ADMIN, ROLE_USER, CurrentUserResponse, RegisteredUserResponse

### Community 57 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 58 - "login/route.ts"
Cohesion: 0.18
Nodes (12): POST(), POST(), { registerMock, originMock }, backendClient, AuthConfig, AuthEnv, loadAuthConfig(), hasValidOrigin() (+4 more)

### Community 59 - "MovimentacaoCaixa"
Cohesion: 0.17
Nodes (8): CashLedgerPort, CashSnapshotPort, CashOperationResult, MovimentacaoCaixa, TipoMovimentacaoCaixa, DEPOSITO, SAQUE, CashMovementConfiguration

### Community 60 - "SecurityProblemDetailHandler"
Cohesion: 0.22
Nodes (9): Override, SecurityProblemDetailHandler, MockHttpServletRequest, SecurityProblemDetailHandlerTest, jakarta.servlet.http.HttpServletResponse, org.springframework.security.access.AccessDeniedException, org.springframework.security.core.AuthenticationException, org.springframework.security.web.access.AccessDeniedHandler (+1 more)

### Community 61 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 62 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 63 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 64 - "CorretoraPersistenceAdapter"
Cohesion: 0.18
Nodes (4): DuplicateBrokerException, CorretoraJpaRepository, CorretoraPersistenceAdapter, Override

### Community 65 - "PortfolioValuationApplicationServiceTest"
Cohesion: 0.25
Nodes (5): Override, CambioUseCase, Cotacao, ObservacaoCambio, PortfolioValuationApplicationServiceTest

### Community 66 - "com.carteira.carteiraInvestimento.domain.asset.Moeda"
Cohesion: 0.22
Nodes (7): QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA, HistoricoCotacaoJpaEntity, CotacaoResponse, com.carteira.carteiraInvestimento.domain.asset.Moeda

### Community 67 - "jakarta.persistence.Entity"
Cohesion: 0.22
Nodes (8): CarteiraSnapshotJpaEntity, PosicaoJpaEntity, TransacaoIdempotenciaJpaEntity, TransacaoJpaEntity, jakarta.persistence.Column, jakarta.persistence.Entity, jakarta.persistence.Table, org.hibernate.annotations.Immutable

### Community 68 - "CashPersistenceAdapter"
Cohesion: 0.19
Nodes (8): CashPersistenceAdapter, Override, Reservation, Wallet, CashMovementPage, com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa, java.sql.ResultSet, MovimentacaoCaixa

### Community 69 - "CashMovementApiIT"
Cohesion: 0.24
Nodes (6): CashMovementApiIT, HttpCall, HttpResults, FunctionalInterface, User, org.springframework.test.web.servlet.MvcResult

### Community 70 - "cookie.ts"
Cohesion: 0.23
Nodes (10): POST(), fetchMock, GET(), AUTH_SESSION_COOKIE, authCookie(), authCookieBase(), expiredAuthCookie(), resolveCurrentUser() (+2 more)

### Community 71 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 72 - "AtivoUseCase"
Cohesion: 0.18
Nodes (3): AtivoNotFoundException, AtivoUseCase, AtivoControllerTest

### Community 73 - ".criar"
Cohesion: 0.18
Nodes (6): BrokerApplicationService, AuditoriaIsoladaPort, BrokerComplianceException, BrokerApplicationServiceTest, AuditoriaIsoladaPort, BeforeEach

### Community 74 - "com.carteira.carteiraInvestimento.domain.quote.QuoteProvider"
Cohesion: 0.19
Nodes (8): CurrentExchangeRate, PortfolioValuationResult, ValuedPosition, CambioAtualResponse, ValuedPositionResponse, FakeProvider, com.carteira.carteiraInvestimento.domain.fx.CambioProvider, com.carteira.carteiraInvestimento.domain.quote.QuoteProvider

### Community 76 - "AssetCatalogPersistenceIT"
Cohesion: 0.21
Nodes (5): AssetCatalogPersistenceIT, AtivoQuery, SqlAction, com.carteira.carteiraInvestimento.application.port.AtivoQuery, FunctionalInterface

### Community 77 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 78 - "CashIdempotencyPort"
Cohesion: 0.27
Nodes (7): CashIdempotencyPort, ExistingReservation, NewReservation, Reservation, CashWalletPort, Wallet, CashMovementApplicationServiceTest

### Community 79 - "org.springframework.transaction.annotation.Transactional"
Cohesion: 0.23
Nodes (4): InvestmentApplicationService, Override, InvestmentConfiguration, org.springframework.transaction.annotation.Transactional

### Community 80 - "CambioAuditIT.java"
Cohesion: 0.23
Nodes (3): CambioProviderException, AlphaVantageCambioClient, com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties

### Community 81 - "CanonicalFingerprint"
Cohesion: 0.18
Nodes (5): InvestmentFingerprint, CanonicalFingerprint, Field, DataOutputStream, java.io.DataOutputStream

### Community 82 - "AtivoController"
Cohesion: 0.24
Nodes (4): AtivoController, AtivoListResponse, AtivoResponse, org.springframework.web.bind.annotation.PatchMapping

### Community 83 - "InvestmentTransactionApiIT"
Cohesion: 0.36
Nodes (3): Fixture, InvestmentTransactionApiIT, PostgreSqlContainerSupport

### Community 84 - "ObservacaoCambio"
Cohesion: 0.22
Nodes (5): CambioUnavailableException, CambioUseCase, ObservacaoCambio, CambioControllerTest, org.springframework.security.authentication.TestingAuthenticationToken

### Community 85 - "org.springframework.stereotype.Repository"
Cohesion: 0.19
Nodes (7): AuditoriaPersistenceAdapter, Override, HistoricoCambioJpaRepository, HistoricoCambioPersistenceAdapter, Override, LogAuditoriaJpaRepository, org.springframework.stereotype.Repository

### Community 86 - "BrapiQuoteAdapter.java"
Cohesion: 0.30
Nodes (5): BrapiClient, BrapiQuoteAdapter, BrapiResponse, BrapiResult, Override

### Community 87 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.27
Nodes (4): PersistedUserJwtAuthenticationConverterTest, PersistedUserJwtAuthenticationConverterTest.AdminProbeController, PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController, PersistedUserJwtAuthenticationConverterTest.SecurityTestConfiguration

### Community 88 - "FakeQuotes"
Cohesion: 0.18
Nodes (6): FakeExchange, FakeQuotes, Fakes, ObservacaoCambio, Override, com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio

### Community 89 - ".consultar"
Cohesion: 0.35
Nodes (4): CadastroCnpj, EnderecoPostal, RegistroCvm, Test

### Community 90 - "CvmPort"
Cohesion: 0.23
Nodes (6): CvmPort, BrokerUpstreamException, BrasilApiCvmAdapter, BrasilApiCvmClient, CvmProviderResponse, Override

### Community 92 - "Mercado.java"
Cohesion: 0.19
Nodes (4): DuplicateTickerException, AtivoPersistenceAdapterTest, java.util.regex.Pattern, org.springframework.dao.DataIntegrityViolationException

### Community 93 - "RegistrationTransactionIntegrationTest"
Cohesion: 0.24
Nodes (5): CarteiraInvestimentoApplication, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 95 - "CotacaoExterna"
Cohesion: 0.21
Nodes (3): CotacaoProviderPort, CotacaoExterna, CotacaoDomainTest

### Community 96 - "MarketQuoteController"
Cohesion: 0.23
Nodes (3): HistoricoCotacaoResponse, MarketQuoteController, org.springframework.web.bind.annotation.PutMapping

### Community 97 - "org.springframework.jdbc.core.JdbcTemplate"
Cohesion: 0.32
Nodes (4): KnownValuation, LocalSnapshotComposer, Totals, org.springframework.jdbc.core.JdbcTemplate

### Community 98 - "ApplicationFoundationIT"
Cohesion: 0.29
Nodes (4): ApplicationFoundationIT, com.fasterxml.jackson.databind.JsonNode, com.fasterxml.jackson.databind.ObjectMapper, org.springframework.web.context.WebApplicationContext

### Community 99 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 100 - "BrokerProviderAdaptersIT"
Cohesion: 0.38
Nodes (4): AfterAll, BrokerProviderAdaptersIT, Test, MockResponse

### Community 101 - "org.springframework.cloud.openfeign.FeignClient"
Cohesion: 0.29
Nodes (6): ReceitaFederalPort, BrasilApiCnpjAdapter, BrasilApiCnpjClient, CnpjProviderResponse, Override, org.springframework.cloud.openfeign.FeignClient

### Community 102 - ".fingerprint"
Cohesion: 0.27
Nodes (3): CashMovementNormalizer, TipoMovimentacaoCaixa, CashMovementDomainTest

### Community 103 - "org.springframework.boot.context.properties.ConfigurationProperties"
Cohesion: 0.25
Nodes (6): AdminProperties, BrokerProviderProperties, Provider, jakarta.validation.constraints.AssertTrue, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.validation.annotation.Validated

### Community 104 - "HistoricoCotacaoPersistenceAdapter"
Cohesion: 0.29
Nodes (5): HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable

### Community 105 - "TwelveDataCambioAdapter"
Cohesion: 0.38
Nodes (3): Override, TwelveDataCambioAdapter, TwelveDataCambioClient

### Community 106 - "TwelveDataQuoteAdapter"
Cohesion: 0.35
Nodes (4): Override, TwelveDataClient, TwelveDataQuoteAdapter, TwelveResponse

### Community 107 - "guard.ts"
Cohesion: 0.33
Nodes (7): AdminLayout(), InicioLayout(), redirectToLogin(), requireCurrentUser(), requireRole(), { redirectMock, resolveMock }, user

### Community 108 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 109 - "ViaCepPort"
Cohesion: 0.31
Nodes (5): ViaCepPort, Override, ViaCepAdapter, ViaCepClient, ViaCepProviderResponse

### Community 110 - "BrokerControllerTest"
Cohesion: 0.31
Nodes (4): BrokerPatch, BrokerControllerTest, MockMvc, Test

### Community 111 - "CambioConfiguration.java"
Cohesion: 0.31
Nodes (4): CambioConfiguration, CambioConfigurationTest, MutableTicker, com.github.benmanes.caffeine.cache.Ticker

### Community 114 - "auth-pages.test.tsx"
Cohesion: 0.29
Nodes (5): AccessDeniedPage(), LoginPage(), AdminPage(), InicioPage(), RegisterPage()

### Community 115 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 117 - ".properties"
Cohesion: 0.44
Nodes (4): AlphaBar, AlphaMeta, AlphaResponse, QuoteProviderAdaptersTest

### Community 120 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 121 - "BrokerProviderAdaptersIT.java"
Cohesion: 0.36
Nodes (4): org.springframework.core.env.Environment, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer

### Community 123 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 125 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 126 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 128 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 129 - "Fakes"
Cohesion: 0.60
Nodes (3): Fakes, Bean, Primary

### Community 133 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 134 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 136 - "PropertiesConfiguration"
Cohesion: 0.50
Nodes (3): PropertiesConfiguration, LocalValidatorFactoryBean, org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

### Community 137 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **348 isolated node(s):** `Provider`, `BrokerNotFoundException`, `AuthProblem`, `BackendErrorKind`, `AuthConfig` (+343 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 580 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **52 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Corretora` connect `Corretora` to `BrokerCatalogPersistenceIT`, `CorretoraPersistenceAdapter`, `jakarta.servlet.http.HttpServletRequest`, `.get`, `.criar`, `CorretoraJpaEntity`, `org.springframework.security.access.prepost.PreAuthorize`, `BrokerControllerTest`, `CnpjCanonicalizer`, `CorretoraReadScope`, `org.springframework.boot.test.context.SpringBootTest`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Why does `TipoEvento` connect `AuditoriaCommand` to `BrokerCatalogPersistenceIT`, `AuditSecurityEventsIntegrationTest.java`, `jakarta.persistence.Entity`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Why does `InvestmentPersistenceAdapter` connect `InvestmentPersistenceAdapter` to `jakarta.servlet.http.HttpServletRequest`, `PortfolioValuationIT.java`, `org.springframework.stereotype.Repository`, `org.springframework.jdbc.core.JdbcTemplate`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **What connects `Provider`, `BrokerNotFoundException`, `AuthProblem` to the rest of the system?**
  _348 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `AuditoriaCommand` be split into smaller, more focused modules?**
  _Cohesion score 0.05765765765765766 - nodes in this community are weakly interconnected._
- **Should `jakarta.servlet.http.HttpServletRequest` be split into smaller, more focused modules?**
  _Cohesion score 0.08653026427962489 - nodes in this community are weakly interconnected._
- **Should `java.sql.Connection` be split into smaller, more focused modules?**
  _Cohesion score 0.08125 - nodes in this community are weakly interconnected._