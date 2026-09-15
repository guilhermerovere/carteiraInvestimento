# Graph Report - projetoJeff  (2026-09-14)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 3722 nodes · 10470 edges · 209 communities (134 shown, 60 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 667 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `2bf7b179`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- financeBackendRequest
- jakarta.servlet.http.HttpServletRequest
- org.junit.jupiter.api.Test
- java.sql.Connection
- ObservacaoCambio
- AuditoriaCommand
- CambioApplicationService
- Role
- org.junit.jupiter.api.BeforeEach
- InvestmentConcurrencyIT
- contracts.ts
- com.carteira.carteiraInvestimento.domain.quote.Cotacao
- summary-panel.tsx
- .execute
- .edit
- types.ts
- management-api.ts
- Usuario
- CorretoraJpaEntity
- operation-center.tsx
- org.springframework.context.annotation.Bean
- MovimentacaoCaixa
- BrokerCatalogPersistenceIT
- PortfolioValuationIT
- AtivoApplicationService
- BrokerUseCase
- org.springframework.web.bind.annotation.RestController
- InvestmentPersistenceAdapter
- PersistedUserJwtAuthenticationConverterTest
- cn
- .criar
- CashMovementApplicationService
- .get
- Mercado.java
- Corretora
- org.springframework.security.oauth2.jwt.JwtEncoder
- org.springframework.stereotype.Component
- auth.ts
- PositionView
- Requirements
- org.springframework.web.bind.annotation.GetMapping
- ADDED Requirements
- Requirement: Cadastro publico transacional
- AuditSecurityEventsIntegrationTest.java
- Mercado
- values.tsx
- ADDED Requirements
- IdentityPersistenceAdapter
- InvestmentDomainTest
- compilerOptions
- AtivoJpaEntity
- TransactionView
- .fingerprint
- TwelveDataQuoteAdapter
- com.fasterxml.jackson.annotation.JsonAnySetter
- AtivoUseCase
- org.springframework.transaction.annotation.Transactional
- .register
- BrokerRegistrationRegressionIT.java
- org.springframework.security.oauth2.jwt.Jwt
- PostgreSqlContainerSupport
- asset-selector.tsx
- AtivoController
- TwelveDataQuoteAdapter.java
- Ativo
- com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties
- BrasilApiCnpjAdapter.java
- AccountSettingsUseCase
- AlphaVantageQuoteAdapter.java
- dependencies
- PortfolioValuationApplicationService
- AuditSecurityEventsIntegrationTest
- InvestmentApplicationServiceTest.java
- jakarta.persistence.Entity
- BrapiQuoteAdapter
- CorrelationIdFilter
- devDependencies
- login-form.tsx
- settings-page.tsx
- com.carteira.carteiraInvestimento.domain.asset.Moeda
- Cotacao
- PortfolioValuationApplicationServiceTest
- CashMovementRollbackIT
- package.json
- CashPersistenceAdapter
- Requirement: Eventos minimos de seguranca
- CashMovementConcurrencyIT
- HistoricoCotacaoJpaEntity
- CambioProviderAdaptersTest
- BrokerProviderAdaptersIT
- MarketQuoteSecurityIT
- CashMovementApiIT
- ADDED Requirements
- MarketQuoteUseCase
- components.json
- Decisions
- .login
- org.springframework.boot.context.properties.ConfigurationProperties
- InvestmentTransactionApiIT
- InvestmentRollbackIT
- return-to.ts
- Fakes
- LogoProvider
- org.slf4j.Logger
- AccessDeniedAuditingService
- PortfolioValuationPort
- org.springframework.stereotype.Repository
- BrapiQuoteAdapter.java
- BrokerApplicationService
- AssetCatalogPersistenceIT
- current-user.ts
- guard.ts
- MarketQuoteControllerTest.java
- TwelveDataCambioAdapter
- org.springframework.cloud.openfeign.FeignClient
- theme-toggle.tsx
- Requirement: Documentacao HTTP e erros padronizados
- AccountSettingsIT
- PortfolioValuationResult
- AssetCatalogSecurityIT
- SnapshotCompatibilityIT
- mvnw
- .correlationId
- tasks.md
- CnpjCanonicalizer
- MarketQuotePersistenceIT
- scripts
- auth-pages.test.tsx
- Project Foundation Proposal
- OpenApiConfiguration.java
- proposal.md
- OpenSpec Apply Change Workflow
- .canonicalize
- usuarios
- FakePort
- mock-backend.mjs
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- frontend/package.json
- AtivoNotFoundException
- BrokerAuditException
- CashConflictException
- DuplicateBrokerException
- InvestmentConflictException
- InvestmentNotFoundException
- PortfolioValuationConflictException
- PortfolioValuationUpstreamException
- PrimaryWalletMissingException
- FinancialStateException
- BrokerNotFoundException.java
- JWT Application Configuration
- incompatible/V1__foundation_probe.sql
- initial/V1__foundation_probe.sql
- eslint-config-next
- next.config.ts
- next-env.d.ts
- react
- tailwindcss
- @tailwindcss/postcss
- @testing-library/react
- @types/node
- postcss.config.mjs
- Identity and Access Design
- Access-Denied Auditing Design
- AfterAll
- AuditoriaCommand
- Reservation
- Reservation
- CashMovementApplicationService
- CashMovementPage
- com.carteira.carteiraInvestimento.application.port.CashWalletPort
- com.carteira.carteiraInvestimento.application.service.CashMovementApplicationService
- Docker Application Stack
- ExistingReservation
- Existing Impact Analysis
- LocalExchangeRate
- LoginService
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
- Page
- com.carteira:backend
- Investment Management Platform
- ProtectedAsset
- ProtectedBroker
- Project Documentation

## God Nodes (most connected - your core abstractions)
1. `Usuario` - 90 edges
2. `Mercado` - 69 edges
3. `financeBackendRequest()` - 62 edges
4. `safeCorrelationId()` - 58 edges
5. `financeErrorResponse()` - 55 edges
6. `Corretora` - 54 edges
7. `FinanceValidationError` - 53 edges
8. `Ativo` - 51 edges
9. `financeJson()` - 51 edges
10. `PortfolioValuationIT` - 42 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `SettingsLayout()` --calls--> `requireCurrentUser()`  [EXTRACTED]
  frontend/src/app/(protected)/configuracoes/layout.tsx → frontend/src/server/auth/guard.ts
- `register()` --calls--> `ptBrError()`  [EXTRACTED]
  frontend/src/components/finance/asset-selector.tsx → frontend/src/client/presentation-errors.ts
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (209 total, 60 thin omitted)

### Community 0 - "financeBackendRequest"
Cohesion: 0.07
Nodes (104): { backendRequest, validOrigin }, POST(), POST(), PATCH(), { backendRequest, validOrigin }, context, PATCH(), PATCH() (+96 more)

### Community 1 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.06
Nodes (43): AccountClosureConflictException, Reason, CASH_NOT_ZERO, OPEN_POSITIONS, FxExpiredException, IncorrectCurrentPasswordException, Override, GlobalExceptionHandler (+35 more)

### Community 2 - "org.junit.jupiter.api.Test"
Cohesion: 0.05
Nodes (17): Cotacao, MarketQuoteApplicationServiceTest, ObservacaoCambioTest, IdentityDomainTest, CotacaoDomainTest, MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest (+9 more)

### Community 3 - "java.sql.Connection"
Cohesion: 0.08
Nodes (12): Fixture, CashMovementSchemaIT, Fixture, InvestmentSchemaIT, Column, InvestmentV9MetadataIT, PortfolioValuationSchemaIT, Entry (+4 more)

### Community 4 - "ObservacaoCambio"
Cohesion: 0.07
Nodes (18): CambioProviderPort, HistoricoCambioPort, CambioProviderException, CambioUnavailableException, CambioUseCase, CambioExterno, CambioProvider, ALPHA_VANTAGE (+10 more)

### Community 5 - "AuditoriaCommand"
Cohesion: 0.07
Nodes (35): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+27 more)

### Community 6 - "CambioApplicationService"
Cohesion: 0.08
Nodes (23): CambioApplicationService, Override, CambioConfiguration, CambioApplicationServiceTest, FakeAudit, FakeHistory, FakeProvider, Fixture (+15 more)

### Community 7 - "Role"
Cohesion: 0.05
Nodes (22): Role, ROLE_ADMIN, ROLE_USER, CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, HistoricoCambioJpaRepository (+14 more)

### Community 8 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.12
Nodes (26): PortfolioValuationConfiguration, BrokerLiveRegistrationQaIT, InitialAdminBootstrapIntegrationTest, com.carteira.carteiraInvestimento.application.service.AtivoUseCase, com.carteira.carteiraInvestimento.application.service.CambioUseCase, com.carteira.carteiraInvestimento.application.service.CashMovementUseCase, com.carteira.carteiraInvestimento.application.service.InvestmentTransactionCommand, com.carteira.carteiraInvestimento.application.service.InvestmentUseCase (+18 more)

### Community 9 - "InvestmentConcurrencyIT"
Cohesion: 0.10
Nodes (20): InvestmentTransactionCommand, TipoTransacao, BUY, SELL, InvestmentTransactionRequest, Action, CashInvestmentRace, DEPOSIT_BUY (+12 more)

### Community 10 - "contracts.ts"
Cohesion: 0.08
Nodes (39): FinanceApiError, request(), safeProblem(), fetchMock, hooks, QueryState, RefreshState, AssetDiscovery (+31 more)

### Community 11 - "com.carteira.carteiraInvestimento.domain.quote.Cotacao"
Cohesion: 0.10
Nodes (19): Override, MarketQuoteApplicationService, FakeHistory, FakeProvider, Fixture, CotacaoExterna, HistoricoCotacaoPage, FakeQuotes (+11 more)

### Community 12 - "summary-panel.tsx"
Cohesion: 0.11
Nodes (22): financeApi, portfolioKeys, useCashBalance(), useCashMovements(), usePortfolioSummary(), usePositions(), useRefreshPortfolioSummary(), useTransactions() (+14 more)

### Community 13 - ".execute"
Cohesion: 0.16
Nodes (12): ExistingReservation, InvestmentPersistencePort, LocalExchangeRate, NewReservation, ProtectedAsset, ProtectedBroker, Reservation, Wallet (+4 more)

### Community 14 - ".edit"
Cohesion: 0.11
Nodes (16): BrokerAdminResponse, BrokerController, GetMapping, BrokerErrors, BrokerSelectionResponse, CreateBrokerRequest, UpdateBrokerRequest, com.fasterxml.jackson.annotation.JsonSetter (+8 more)

### Community 15 - "types.ts"
Cohesion: 0.13
Nodes (26): POST(), { loginMock, originMock }, POST(), { registerMock, originMock }, CurrentUser, LoginInput, RegisterInput, Role (+18 more)

### Community 16 - "management-api.ts"
Cohesion: 0.09
Nodes (28): accountApi, adminApi, AdminAsset, AdminBroker, request(), safeProblem(), { api }, asset (+20 more)

### Community 17 - "Usuario"
Cohesion: 0.08
Nodes (8): Role, Usuario, Fakes, IdentityApplicationServiceTest, Override, TestUsuarios, Override, TestUsuarios

### Community 18 - "CorretoraJpaEntity"
Cohesion: 0.10
Nodes (8): CorretoraJpaEntity, CorretoraJpaRepository, CorretoraPersistenceAdapter, CorretoraPage, Override, com.carteira.carteiraInvestimento.application.port.CorretoraPage, com.carteira.carteiraInvestimento.application.port.CorretoraReadScope, com.carteira.carteiraInvestimento.application.service.DuplicateBrokerException

### Community 19 - "operation-center.tsx"
Cohesion: 0.11
Nodes (31): clearAmbiguous(), freezeIntent(), loadAmbiguous(), RECOVERY_KEY, saveAmbiguous(), nowLocal(), OperationDialog(), review() (+23 more)

### Community 20 - "org.springframework.context.annotation.Bean"
Cohesion: 0.12
Nodes (21): AdminProperties, AccountSettingsPersistencePort, IdentityApplicationConfiguration, LoginService, FaultInjectionConfiguration, FaultSwitches, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.AuditoriaPort (+13 more)

### Community 21 - "MovimentacaoCaixa"
Cohesion: 0.11
Nodes (15): CashIdempotencyPort, ExistingReservation, NewReservation, Reservation, CashLedgerPort, CashMovementPage, Wallet, CashOperationResult (+7 more)

### Community 22 - "BrokerCatalogPersistenceIT"
Cohesion: 0.12
Nodes (10): CorretoraPort, CorretoraPage, CorretoraReadScope, BrokerLocalTransactionService, AuditoriaCommand, BrokerPatch, BrokerCatalogPersistenceIT, BeforeEach (+2 more)

### Community 23 - "PortfolioValuationIT"
Cohesion: 0.13
Nodes (9): FakeExchange, Fakes, Fixture, Cotacao, InvestmentTransactionCommand, PortfolioValuationIT, com.carteira.carteiraInvestimento.application.service.PortfolioValuationUseCase, org.springframework.context.annotation.Primary (+1 more)

### Community 24 - "AtivoApplicationService"
Cohesion: 0.13
Nodes (7): AssetDiscoveryProviderPort, AssetMetadata, AssetMetadataProviderPort, AtivoPort, AtivoApplicationService, Override, AtivoApplicationServiceTest

### Community 25 - "BrokerUseCase"
Cohesion: 0.09
Nodes (14): CorretoraPage, CorretoraReadScope, ACTIVE_ONLY, ALL, BrokerPatch, BrokerUseCase, BrokerListResponse, Test (+6 more)

### Community 26 - "org.springframework.web.bind.annotation.RestController"
Cohesion: 0.16
Nodes (15): PortfolioValuationUseCase, CurrentPrincipalController, LoginController, RegistrationController, CambioController, MarketQuoteController, PortfolioValuationController, AdminProbeController (+7 more)

### Community 27 - "InvestmentPersistenceAdapter"
Cohesion: 0.14
Nodes (9): InvestmentPersistenceAdapter, LocalSnapshotComposer, Override, Page, Posicao, Transacao, Wallet, com.carteira.carteiraInvestimento.domain.investment.Posicao (+1 more)

### Community 28 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.13
Nodes (19): ProblemDetailFactory, FoundationSecurityConfigurationTest, SecurityTestConfiguration, PersistedUserJwtAuthenticationConverterTest, SecurityTestConfiguration, CurrentPrincipalControllerTest, SecurityTestConfiguration, CurrentPrincipalControllerTest.SecurityTestConfiguration (+11 more)

### Community 29 - "cn"
Cohesion: 0.10
Nodes (18): AdminShell(), links, AppShell(), isActive(), navigation, NavigationLink(), { pathname, replace, logout }, user (+10 more)

### Community 30 - ".criar"
Cohesion: 0.15
Nodes (13): CadastroCnpj, CvmPort, EnderecoPostal, RegistroCvm, BrokerApplicationServiceTest, AuditoriaIsoladaPort, CvmPort, ReceitaFederalPort (+5 more)

### Community 31 - "CashMovementApplicationService"
Cohesion: 0.12
Nodes (15): CashSnapshotPort, CashWalletPort, CashMovementApplicationService, Override, Wallet, CashMovementConfiguration, CashMovementUseCase, CashOperationResult (+7 more)

### Community 32 - ".get"
Cohesion: 0.11
Nodes (8): ApplicationFoundationIT, BrokerCatalogSecurityIT, Test, GlobalExceptionHandlerTest, com.fasterxml.jackson.databind.JsonNode, JwtEncoder, org.springframework.web.context.WebApplicationContext, Usuario

### Community 33 - "Mercado.java"
Cohesion: 0.07
Nodes (19): AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME, TICKER (+11 more)

### Community 34 - "Corretora"
Cohesion: 0.11
Nodes (7): BrokerPatch, CorretoraPage, CorretoraReadScope, Override, Corretora, BrokerDomainTest, BeforeEach

### Community 35 - "org.springframework.security.oauth2.jwt.JwtEncoder"
Cohesion: 0.13
Nodes (13): JwtConfiguration, JwtProperties, Override, JwtAccessTokenIssuer, PropertiesConfiguration, JwtAccessTokenIssuerTest, javax.crypto.spec.SecretKeySpec, LocalValidatorFactoryBean (+5 more)

### Community 36 - "org.springframework.stereotype.Component"
Cohesion: 0.12
Nodes (16): FoundationSecurityConfiguration, BcryptPasswordHasher, Override, PersistedUserJwtAuthenticationConverter, SecurityProblemDetailHandler, InvalidBearerTokenException, org.springframework.core.convert.converter.Converter, org.springframework.security.authentication.AbstractAuthenticationToken (+8 more)

### Community 37 - "auth.ts"
Cohesion: 0.12
Nodes (23): SettingsLayout(), authMeKey, clearAuthState(), confirmCurrentUser(), asProblem(), AuthFormError, AuthProblem, getCurrentUser() (+15 more)

### Community 38 - "PositionView"
Cohesion: 0.12
Nodes (9): Page, PositionView, InvestmentApplicationService, Override, Wallet, InvestmentUseCase, InvestmentConfiguration, PositionPageResponse (+1 more)

### Community 39 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 40 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.11
Nodes (6): Authentication, SecurityProbeController, ProtectedProbeController, AccessDeniedProbeController, FailureProbeController, org.springframework.web.bind.annotation.GetMapping

### Community 41 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 42 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 43 - "AuditSecurityEventsIntegrationTest.java"
Cohesion: 0.13
Nodes (9): AccessTokenIssuer, AuditoriaIsoladaPort, AuditoriaPort, PasswordHasher, UsuarioPort, CurrentPrincipalService, InitialAdminBootstrapService, LoginService (+1 more)

### Community 44 - "Mercado"
Cohesion: 0.12
Nodes (13): Moeda, Mercado, B3, US, AtivoPage, AssetDiscoveryResponse, CreateAtivoRequest, RegisterAtivoRequest (+5 more)

### Community 45 - "values.tsx"
Cohesion: 0.15
Nodes (22): DecimalInput(), MoneyInput(), PriceInput(), Props, QuantityInput(), AveragePrice(), DateTime(), Percentage() (+14 more)

### Community 46 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 47 - "IdentityPersistenceAdapter"
Cohesion: 0.12
Nodes (7): DuplicateEmailException, EmailCanonicalizer, PasswordPolicy, IdentityPersistenceAdapter, Override, DuplicateEmailPersistenceIntegrationTest, DataSource

### Community 48 - "InvestmentDomainTest"
Cohesion: 0.14
Nodes (6): Posicao, Sale, KnownValuation, LocalSnapshotComposer, Totals, InvestmentDomainTest

### Community 49 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 50 - "AtivoJpaEntity"
Cohesion: 0.14
Nodes (6): AtivoJpaRepository, AtivoJpaEntity, AtivoJpaRepository, AtivoPersistenceAdapter, Override, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 51 - "TransactionView"
Cohesion: 0.12
Nodes (10): TransactionView, InvestmentOperationResult, InvestmentOperationResponse, PositionResponse, TransactionPageResponse, LogoProvider, TransactionResponse, com.carteira.carteiraInvestimento.domain.investment.TipoTransacao (+2 more)

### Community 52 - ".fingerprint"
Cohesion: 0.11
Nodes (8): InvestmentFingerprint, CanonicalFingerprint, Field, CashMovementNormalizer, TipoMovimentacaoCaixa, CashMovementDomainTest, DataOutputStream, java.io.DataOutputStream

### Community 53 - "TwelveDataQuoteAdapter"
Cohesion: 0.19
Nodes (5): CotacaoExterna, Override, QuoteIntegrationException, TwelveDataQuoteAdapter, com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException

### Community 54 - "com.fasterxml.jackson.annotation.JsonAnySetter"
Cohesion: 0.11
Nodes (6): UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, UpdateBrokerLifecycleRequest, CashMovementRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator

### Community 55 - "AtivoUseCase"
Cohesion: 0.14
Nodes (4): AtivoRegistrationResult, AtivoUseCase, AtivoQuery, AtivoControllerTest

### Community 56 - "org.springframework.transaction.annotation.Transactional"
Cohesion: 0.19
Nodes (5): AccountSettingsApplicationService, AuditoriaCommand, Override, AccountSettingsApplicationServiceTest, org.springframework.transaction.annotation.Transactional

### Community 57 - ".register"
Cohesion: 0.13
Nodes (8): CarteiraPort, RegistrationService, CarteiraInvestimentoApplication, Carteira, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 58 - "BrokerRegistrationRegressionIT.java"
Cohesion: 0.12
Nodes (11): BrokerComplianceException, BrokerRegistrationRegressionIT, MockResponse, MockWebServer, okhttp3.mockwebserver.MockResponse, okhttp3.mockwebserver.MockWebServer, org.junit.jupiter.api.AfterAll, org.springframework.test.annotation.DirtiesContext (+3 more)

### Community 59 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.21
Nodes (8): CashMovementUseCase, InvestmentController, CashBalanceResponse, CashMovementController, CashMovementResponse, io.swagger.v3.oas.annotations.Operation, org.springframework.http.ResponseEntity, org.springframework.security.oauth2.jwt.Jwt

### Community 60 - "PostgreSqlContainerSupport"
Cohesion: 0.11
Nodes (8): BrokerTransactionBoundaryIT, BeforeEach, CambioSchemaIT, PostgreSqlContainerSupport, RegistrationConcurrencyIntegrationTest, TransactionHistoryPlaywrightIT, BrokerTransactionBoundaryIT.Fakes, Import

### Community 61 - "asset-selector.tsx"
Cohesion: 0.14
Nodes (16): AssetDiscoveryPage(), AssetSelector(), precheck(), register(), selectDiscovery(), catalogKeys, ExactState, BrokerSelector() (+8 more)

### Community 62 - "AtivoController"
Cohesion: 0.15
Nodes (8): AtivoListResponse, AtivoController, AtivoResponse, com.carteira.carteiraInvestimento.application.port.AtivoSort, com.carteira.carteiraInvestimento.application.port.SortDirection, org.springframework.web.bind.annotation.PatchMapping, UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest

### Community 63 - "TwelveDataQuoteAdapter.java"
Cohesion: 0.17
Nodes (9): AssetValidationUnavailableException, TwelveDataClient, TwelveResponse, TwelveStockItem, TwelveStocksResponse, MarketQuoteProperties, QuoteProviderAdaptersTest, com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException (+1 more)

### Community 64 - "Ativo"
Cohesion: 0.15
Nodes (7): Ativo, Moeda, TipoAtivo, NomeAtivoCanonicalizer, FakeAssets, Override, AtivoDomainTest

### Community 65 - "com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties"
Cohesion: 0.16
Nodes (9): AlphaCambioRate, AlphaCambioResponse, AlphaVantageCambioAdapter, AlphaVantageCambioClient, CambioExterno, Override, CambioProviderException, com.carteira.carteiraInvestimento.application.service.CambioProviderException (+1 more)

### Community 66 - "BrasilApiCnpjAdapter.java"
Cohesion: 0.13
Nodes (11): ReceitaFederalPort, ViaCepPort, BrokerUpstreamException, BrasilApiCnpjAdapter, BrasilApiCnpjClient, CnpjProviderResponse, Override, Override (+3 more)

### Community 67 - "AccountSettingsUseCase"
Cohesion: 0.15
Nodes (8): AccountSettingsUseCase, AccountSettingsController, ChangePasswordRequest, CloseAccountRequest, UpdateProfileRequest, AccountSettingsControllerTest, Jwt, MockHttpServletRequest

### Community 68 - "AlphaVantageQuoteAdapter.java"
Cohesion: 0.18
Nodes (10): AlphaBar, AlphaMeta, AlphaResponse, AlphaSearchMatch, AlphaSearchResponse, AlphaVantageClient, AlphaVantageQuoteAdapter, CotacaoExterna (+2 more)

### Community 69 - "dependencies"
Cohesion: 0.09
Nodes (23): class-variance-authority, clsx, decimal.js-light, dependencies, class-variance-authority, clsx, decimal.js-light, lossless-json (+15 more)

### Community 70 - "PortfolioValuationApplicationService"
Cohesion: 0.16
Nodes (12): CambioUseCase, MarketQuoteUseCase, OpenPosition, ValuedPosition, PortfolioValuationApplicationService, com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort, com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort.LocalState, com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort.MaterializedTotals (+4 more)

### Community 71 - "AuditSecurityEventsIntegrationTest"
Cohesion: 0.17
Nodes (3): AuditSecurityEventsIntegrationTest.AccessDeniedProbeConfiguration, AccessDeniedProbeConfiguration, AuditSecurityEventsIntegrationTest

### Community 72 - "InvestmentApplicationServiceTest.java"
Cohesion: 0.16
Nodes (9): Moeda, BRL, USD, InvestmentNumbers, com.carteira.carteiraInvestimento.domain.fx.MoedaCambio, Posicao, PositionView, Transacao (+1 more)

### Community 73 - "jakarta.persistence.Entity"
Cohesion: 0.19
Nodes (8): CarteiraSnapshotJpaEntity, PosicaoJpaEntity, TransacaoIdempotenciaJpaEntity, TransacaoJpaEntity, jakarta.persistence.Column, jakarta.persistence.Entity, jakarta.persistence.Table, org.hibernate.annotations.Immutable

### Community 74 - "BrapiQuoteAdapter"
Cohesion: 0.22
Nodes (4): BrapiQuoteAdapter, CotacaoExterna, Override, QuoteIntegrationException

### Community 75 - "CorrelationIdFilter"
Cohesion: 0.12
Nodes (7): CorrelationIdFilter, Override, CorrelationIdFilterTest, RegistrationControllerTest, jakarta.servlet.FilterChain, org.springframework.core.annotation.Order, org.springframework.web.filter.OncePerRequestFilter

### Community 76 - "devDependencies"
Cohesion: 0.10
Nodes (21): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @testing-library/jest-dom, @testing-library/user-event (+13 more)

### Community 77 - "login-form.tsx"
Cohesion: 0.16
Nodes (11): fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), messageFor(), RegisterForm(), authErrorMessage(), useRegister() (+3 more)

### Community 78 - "settings-page.tsx"
Cohesion: 0.15
Nodes (11): ptBrError(), confirm(), consultQuote(), SettingsPage(), changePassword(), closeAccount(), updateProfile(), { router, currentUser, updateName, updateEmail, changePassword, close, cash, positions } (+3 more)

### Community 79 - "com.carteira.carteiraInvestimento.domain.asset.Moeda"
Cohesion: 0.14
Nodes (9): CotacaoProviderPort, Transacao, CotacaoExterna, QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA, com.carteira.carteiraInvestimento.domain.asset.Ativo (+1 more)

### Community 80 - "Cotacao"
Cohesion: 0.17
Nodes (6): HistoricoCotacaoPage, HistoricoCotacaoPort, Cotacao, MarketQuoteConfiguration, HistoricoCotacaoResponse, org.springframework.cloud.openfeign.EnableFeignClients

### Community 81 - "PortfolioValuationApplicationServiceTest"
Cohesion: 0.23
Nodes (7): Override, CambioUseCase, Cotacao, MarketQuoteUseCase, ObservacaoCambio, OpenPosition, PortfolioValuationApplicationServiceTest

### Community 82 - "CashMovementRollbackIT"
Cohesion: 0.16
Nodes (8): CambioTransactionBoundaryIT, CashMovementRollbackIT, FailureStage, AUDIT, MOVEMENT, RESULT, SNAPSHOT, TransactionTemplate

### Community 83 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 84 - "CashPersistenceAdapter"
Cohesion: 0.18
Nodes (7): CashPersistenceAdapter, CashMovementPage, LocalSnapshotComposer, Override, Wallet, com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa, MovimentacaoCaixa

### Community 85 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 86 - "CashMovementConcurrencyIT"
Cohesion: 0.31
Nodes (4): CashMovementConcurrencyIT, FunctionalInterface, Outcomes, ThrowingSupplier

### Community 87 - "HistoricoCotacaoJpaEntity"
Cohesion: 0.20
Nodes (6): HistoricoCotacaoJpaEntity, HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable

### Community 88 - "CambioProviderAdaptersTest"
Cohesion: 0.27
Nodes (5): TwelveCambioResponse, CambioProviderAdaptersTest, MarketQuoteProperties, MockResponse, MockWebServer

### Community 89 - "BrokerProviderAdaptersIT"
Cohesion: 0.16
Nodes (11): BrokerProviderAdaptersIT, AfterAll, BeforeEach, CvmPort, MockResponse, MockWebServer, ReceitaFederalPort, Test (+3 more)

### Community 90 - "MarketQuoteSecurityIT"
Cohesion: 0.20
Nodes (5): CambioSecurityIT, FixedCambio, MarketQuoteSecurityIT, CambioSecurityIT.FixedCambio, com.carteira.carteiraInvestimento.domain.identity.Usuario

### Community 91 - "CashMovementApiIT"
Cohesion: 0.24
Nodes (6): CashMovementApiIT, HttpCall, HttpResults, FunctionalInterface, User, org.springframework.test.web.servlet.MvcResult

### Community 92 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 93 - "MarketQuoteUseCase"
Cohesion: 0.17
Nodes (4): MarketQuoteUseCase, CotacaoResponse, MarketQuoteControllerTest, org.springframework.web.bind.annotation.PutMapping

### Community 94 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 95 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 96 - ".login"
Cohesion: 0.17
Nodes (4): IssuedAccessToken, AuthenticationFailedException, LoginResponse, LoginControllerTest

### Community 97 - "org.springframework.boot.context.properties.ConfigurationProperties"
Cohesion: 0.17
Nodes (8): AdminProperties, BrokerProviderProperties, Provider, MarketQuoteProperties, Provider, jakarta.validation.constraints.AssertTrue, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.validation.annotation.Validated

### Community 98 - "InvestmentTransactionApiIT"
Cohesion: 0.38
Nodes (3): Fixture, Fixture, InvestmentTransactionApiIT

### Community 99 - "InvestmentRollbackIT"
Cohesion: 0.18
Nodes (10): Fixture, InvestmentRollbackIT, InvestmentTransactionCommand, Stage, AUDIT, COMPLETE, POSITION, SNAPSHOT (+2 more)

### Community 100 - "return-to.ts"
Cohesion: 0.25
Nodes (10): AUTH_SESSION_COOKIE, isProtectedRoute(), DEFAULT_RETURN_TO, isSafeInternalPath(), matchesRoute(), naturalRouteForRole(), safeReturnTo(), safeReturnToForRole() (+2 more)

### Community 101 - "Fakes"
Cohesion: 0.24
Nodes (4): WalletState, Fakes, AuditoriaCommand, Override

### Community 102 - "LogoProvider"
Cohesion: 0.17
Nodes (5): AtivoPage, LogoProvider, BRAPI, LOGO_DEV, AtivoListResponse

### Community 103 - "org.slf4j.Logger"
Cohesion: 0.26
Nodes (6): LogoDevProperties, Override, LogoDevBrokerBrandingAdapter, LogoDevSearchClient, LogoDevSearchResult, org.slf4j.Logger

### Community 104 - "AccessDeniedAuditingService"
Cohesion: 0.22
Nodes (4): BrokerComplianceException, AccessDeniedAuditingService, AccessDeniedAuditingServiceTest, MockHttpServletRequest

### Community 105 - "PortfolioValuationPort"
Cohesion: 0.24
Nodes (8): LocalState, MaterializedTotals, OpenPosition, PortfolioValuationPort, Override, PortfolioValuationPersistenceAdapter, WalletRow, com.carteira.carteiraInvestimento.application.service.PrimaryWalletMissingException

### Community 106 - "org.springframework.stereotype.Repository"
Cohesion: 0.21
Nodes (6): AccountSettingsPersistenceAdapter, Override, AuditoriaPersistenceAdapter, Override, LogAuditoriaJpaRepository, org.springframework.stereotype.Repository

### Community 107 - "BrapiQuoteAdapter.java"
Cohesion: 0.29
Nodes (7): BrapiAvailableResponse, BrapiClient, BrapiQuoteData, BrapiResponse, BrapiResult, CatalogBrandingAdaptersTest, MarketQuoteProperties

### Community 108 - "BrokerApplicationService"
Cohesion: 0.22
Nodes (8): BrokerBrandingPort, BrokerApplicationService, AuditoriaIsoladaPort, CvmPort, ReceitaFederalPort, ViaCepPort, BeforeEach, BrokerUseCase

### Community 110 - "current-user.ts"
Cohesion: 0.27
Nodes (7): GET(), { resolveMock }, Home(), { redirectMock, resolveMock }, resolveCurrentUser(), resolveCurrentUserFromSessionCookie(), { meMock, cookiesMock }

### Community 111 - "guard.ts"
Cohesion: 0.28
Nodes (8): AdminLayout(), InicioLayout(), PortfolioLayout(), redirectToLogin(), requireCurrentUser(), requireRole(), { redirectMock, resolveMock }, user

### Community 112 - "MarketQuoteControllerTest.java"
Cohesion: 0.20
Nodes (3): InactiveAssetRefreshException, QuoteIntegrationException, QuoteNotFoundException

### Community 113 - "TwelveDataCambioAdapter"
Cohesion: 0.33
Nodes (3): Override, TwelveDataCambioAdapter, TwelveDataCambioClient

### Community 114 - "org.springframework.cloud.openfeign.FeignClient"
Cohesion: 0.27
Nodes (8): BrasilApiCvmAdapter, BrasilApiCvmClient, CvmProviderResponse, Override, com.carteira.carteiraInvestimento.application.port.CvmPort, com.carteira.carteiraInvestimento.application.port.RegistroCvm, org.springframework.cloud.openfeign.FeignClient, RegistroCvm

### Community 115 - "theme-toggle.tsx"
Cohesion: 0.23
Nodes (6): metadata, options, { setTheme, providerProps, themeState }, ThemeToggle(), QueryProvider(), ThemeProvider()

### Community 116 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 117 - "AccountSettingsIT"
Cohesion: 0.31
Nodes (3): AccountSettingsIT, ThrowingAction, FunctionalInterface

### Community 118 - "PortfolioValuationResult"
Cohesion: 0.24
Nodes (7): CurrentExchangeRate, PortfolioValuationResult, ValuedPosition, CambioAtualResponse, ValuedPosition, PortfolioValuationResponse, ValuedPositionResponse

### Community 119 - "AssetCatalogSecurityIT"
Cohesion: 0.29
Nodes (3): AccountProfileResponse, AssetCatalogSecurityIT, com.carteira.carteiraInvestimento.domain.identity.Role

### Community 121 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 122 - ".correlationId"
Cohesion: 0.22
Nodes (3): LoginRequest, RegisterRequest, CorrelationProbeController

### Community 123 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 126 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 127 - "auth-pages.test.tsx"
Cohesion: 0.25
Nodes (3): { redirect }, AdminPage(), AdminOverview()

### Community 128 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 129 - "OpenApiConfiguration.java"
Cohesion: 0.33
Nodes (4): OpenApiConfiguration, OpenApiConfigurationTest, io.swagger.v3.oas.models.OpenAPI, OpenAPI

### Community 130 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 131 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 133 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 134 - "FakePort"
Cohesion: 0.47
Nodes (4): FakePort, LocalState, Override, MaterializedTotals

### Community 135 - "mock-backend.mjs"
Cohesion: 0.53
Nodes (5): bodyOf(), json(), server, summary(), userProfile

### Community 137 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 138 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 141 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **402 isolated node(s):** `AuthConfig`, `AuthEnv`, `Data`, `FinanceProblem`, `SafeProblemCode` (+397 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 767 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **60 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Mercado` connect `Mercado` to `jakarta.servlet.http.HttpServletRequest`, `org.junit.jupiter.api.Test`, `org.junit.jupiter.api.BeforeEach`, `com.carteira.carteiraInvestimento.domain.quote.Cotacao`, `.execute`, `AtivoApplicationService`, `CashMovementApplicationService`, `Mercado.java`, `PositionView`, `AtivoJpaEntity`, `TransactionView`, `TwelveDataQuoteAdapter`, `AtivoUseCase`, `AtivoController`, `TwelveDataQuoteAdapter.java`, `Ativo`, `AlphaVantageQuoteAdapter.java`, `PortfolioValuationApplicationService`, `InvestmentApplicationServiceTest.java`, `jakarta.persistence.Entity`, `BrapiQuoteAdapter`, `com.carteira.carteiraInvestimento.domain.asset.Moeda`, `PortfolioValuationApplicationServiceTest`, `LogoProvider`, `BrapiQuoteAdapter.java`, `PortfolioValuationResult`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Why does `LogoProvider` connect `LogoProvider` to `Ativo`, `Corretora`, `AlphaVantageQuoteAdapter.java`, `PositionView`, `InvestmentApplicationServiceTest.java`, `jakarta.persistence.Entity`, `BrapiQuoteAdapter.java`, `Mercado`, `.execute`, `.edit`, `AtivoJpaEntity`, `TransactionView`, `CorretoraJpaEntity`, `AtivoApplicationService`, `AtivoController`, `TwelveDataQuoteAdapter.java`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Why does `Usuario` connect `Usuario` to `.login`, `AccountSettingsUseCase`, `org.springframework.security.oauth2.jwt.JwtEncoder`, `org.springframework.stereotype.Component`, `Fakes`, `Role`, `org.junit.jupiter.api.BeforeEach`, `org.springframework.stereotype.Repository`, `AuditSecurityEventsIntegrationTest.java`, `CorrelationIdFilter`, `IdentityPersistenceAdapter`, `org.springframework.context.annotation.Bean`, `AssetCatalogSecurityIT`, `org.springframework.transaction.annotation.Transactional`, `.register`, `org.springframework.web.bind.annotation.RestController`, `PersistedUserJwtAuthenticationConverterTest`?**
  _High betweenness centrality (0.020) - this node is a cross-community bridge._
- **What connects `AuthConfig`, `AuthEnv`, `Data` to the rest of the system?**
  _402 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `financeBackendRequest` be split into smaller, more focused modules?**
  _Cohesion score 0.06804805289042974 - nodes in this community are weakly interconnected._
- **Should `jakarta.servlet.http.HttpServletRequest` be split into smaller, more focused modules?**
  _Cohesion score 0.06245710363761153 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.04680365296803653 - nodes in this community are weakly interconnected._