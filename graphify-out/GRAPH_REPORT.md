# Graph Report - projetoJeff  (2026-09-12)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 3102 nodes · 8498 edges · 166 communities (106 shown, 49 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 579 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `bc903f1e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- types.ts
- QuoteProvider
- AuditoriaCommand
- org.springframework.context.annotation.Bean
- auth-form-api.ts
- jakarta.servlet.http.HttpServletRequest
- InvestmentConcurrencyIT
- ObservacaoCambio
- org.junit.jupiter.api.Test
- Fakes
- org.junit.jupiter.api.BeforeEach
- AlphaVantageCambioAdapter
- InvestmentPersistenceAdapter
- positions-panel.tsx
- org.springframework.stereotype.Repository
- Usuario
- finance-panels.test.tsx
- transport.ts
- org.springframework.cloud.openfeign.FeignClient
- .edit
- InvestmentApplicationService
- com.carteira.carteiraInvestimento.domain.quote.Cotacao
- CorretoraReadScope
- AuditSecurityEventsIntegrationTest.java
- org.springframework.web.bind.annotation.GetMapping
- PortfolioValuationIT
- CorretoraJpaEntity
- Cotacao
- org.springframework.transaction.annotation.Transactional
- com.fasterxml.jackson.annotation.JsonAnySetter
- Requirements
- .criar
- Posicao
- PortfolioValuationPort
- org.springframework.security.oauth2.jwt.Jwt
- ADDED Requirements
- Requirement: Cadastro publico transacional
- org.springframework.security.oauth2.jwt.JwtEncoder
- ADDED Requirements
- compilerOptions
- Ativo
- org.springframework.web.bind.annotation.RestController
- AtivoPort
- InvestmentPersistencePort
- CashMovementRollbackIT.java
- Corretora
- TipoAtivo
- PostgreSqlContainerSupport
- dependencies
- Fixture
- MovimentacaoCaixa
- PortfolioValuationApplicationServiceTest
- UsuarioJpaEntity
- AtivoController
- java.sql.Connection
- portfolio-queries.ts
- TipoTransacao
- devDependencies
- IdentityApplicationConfiguration.java
- AuditSecurityEventsIntegrationTest
- BrokerApplicationService
- com.carteira.carteiraInvestimento.domain.asset.Moeda
- AtivoUseCase
- HistoricoCotacaoJpaEntity
- .get
- package.json
- Requirement: Eventos minimos de seguranca
- CashMovementApplicationService
- jakarta.persistence.Entity
- CashPersistenceAdapter
- CashMovementApiIT
- CashMovementRollbackIT
- ADDED Requirements
- AtivoPersistenceAdapter
- SecurityProblemDetailHandler
- org.springframework.security.access.prepost.PreAuthorize
- AssetCatalogPersistenceIT
- CashMovementSchemaIT
- PostgreSqlContainerSupport
- components.json
- mappers.ts
- Decisions
- .execute
- InvestmentTransactionApiIT
- .login
- .execute
- CashIdempotencyPort
- .novo
- CambioUseCase
- InvestmentLifecycleConcurrencyIT
- MarketQuoteSecurityIT
- finance-core.test.ts
- Requirement: Documentacao HTTP e erros padronizados
- CashMovementConfiguration.java
- InvestmentApplicationService.java
- Mercado.java
- IdentitySchemaIntegrationTest
- PortfolioValuationSchemaIT
- mvnw
- CanonicalFingerprint
- tasks.md
- CnpjCanonicalizer
- CambioConfiguration
- CambioAuditIT
- MarketQuotePersistenceIT
- scripts
- BrokerProviderAdaptersIT.java
- FakeQuotes
- Project Foundation Proposal
- BcryptPasswordHasher
- CambioSchemaIT
- proposal.md
- OpenSpec Apply Change Workflow
- usuarios
- RegistrationConcurrencyIntegrationTest
- mock-backend.mjs
- AppShell
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- CotacaoDomainTest
- frontend/package.json
- BrokerAuditException
- CashConflictException
- PrimaryWalletMissingException
- BrokerNotFoundException.java
- JWT Application Configuration
- incompatible/V1__foundation_probe.sql
- initial/V1__foundation_probe.sql
- next.config.ts
- next-env.d.ts
- jsdom
- tailwindcss
- @testing-library/user-event
- typescript
- vitest
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
5. `Ativo` - 38 edges
6. `InvestmentPersistencePort` - 38 edges
7. `AuditoriaCommand` - 36 edges
8. `GlobalExceptionHandler` - 36 edges
9. `AuditSecurityEventsIntegrationTest` - 34 edges
10. `TipoEvento` - 32 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `GET()` --indirect_call--> `mapCashMovement()`  [INFERRED]
  frontend/src/app/api/finance/cash/movements/route.ts → frontend/src/server/finance/mappers.ts
- `GET()` --indirect_call--> `mapPosition()`  [INFERRED]
  frontend/src/app/api/finance/positions/route.ts → frontend/src/server/finance/mappers.ts
- `GET()` --indirect_call--> `mapTransaction()`  [INFERRED]
  frontend/src/app/api/finance/transactions/route.ts → frontend/src/server/finance/mappers.ts

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (166 total, 49 thin omitted)

### Community 0 - "types.ts"
Cohesion: 0.05
Nodes (61): POST(), { loginMock, originMock }, POST(), fetchMock, GET(), { resolveMock }, POST(), { registerMock, originMock } (+53 more)

### Community 1 - "QuoteProvider"
Cohesion: 0.06
Nodes (33): CotacaoProviderPort, QuoteIntegrationException, QuoteNotFoundException, CotacaoExterna, QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA (+25 more)

### Community 2 - "AuditoriaCommand"
Cohesion: 0.05
Nodes (42): AuditoriaCommand, ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO (+34 more)

### Community 3 - "org.springframework.context.annotation.Bean"
Cohesion: 0.07
Nodes (39): FoundationSecurityConfiguration, OpenApiConfiguration, PortfolioValuationConfiguration, Override, PersistedUserJwtAuthenticationConverter, FoundationSecurityConfigurationTest, SecurityTestConfiguration, OpenApiConfigurationTest (+31 more)

### Community 4 - "auth-form-api.ts"
Cohesion: 0.06
Nodes (43): AccessDeniedPage(), fetchMock, { replaceMock, searchMock }, { redirect }, metadata, LoginForm(), messageFor(), LoginPage() (+35 more)

### Community 5 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.10
Nodes (30): PortfolioValuationConflictException, PortfolioValuationUpstreamException, GlobalExceptionHandler, com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException, com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException, com.carteira.carteiraInvestimento.application.service.BrokerAuditException, com.carteira.carteiraInvestimento.application.service.BrokerComplianceException, com.carteira.carteiraInvestimento.application.service.BrokerNotFoundException (+22 more)

### Community 6 - "InvestmentConcurrencyIT"
Cohesion: 0.09
Nodes (18): CashMovementConcurrencyIT, Fixture, FunctionalInterface, Outcomes, ThrowingSupplier, Action, CashInvestmentRace, DEPOSIT_BUY (+10 more)

### Community 7 - "ObservacaoCambio"
Cohesion: 0.09
Nodes (17): CambioProviderPort, HistoricoCambioPort, CambioApplicationService, Override, CambioExterno, CambioProvider, ALPHA_VANTAGE, TWELVE_DATA (+9 more)

### Community 8 - "org.junit.jupiter.api.Test"
Cohesion: 0.06
Nodes (13): Cotacao, MarketQuoteApplicationServiceTest, ObservacaoCambioTest, MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, CorrelationIdFilterTest (+5 more)

### Community 9 - "Fakes"
Cohesion: 0.06
Nodes (11): CarteiraPort, DuplicateEmailException, Carteira, IdentityPersistenceAdapter, Override, Fakes, IdentityApplicationServiceTest, IdentityDomainTest (+3 more)

### Community 10 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.11
Nodes (23): AssetCatalogSecurityIT, CambioSecurityIT, InitialAdminBootstrapIntegrationTest, CambioSecurityIT.FixedCambio, com.carteira.carteiraInvestimento.application.port.UsuarioPort, com.carteira.carteiraInvestimento.application.service.AtivoUseCase, com.carteira.carteiraInvestimento.application.service.CashMovementUseCase, com.carteira.carteiraInvestimento.domain.asset.TipoAtivo (+15 more)

### Community 11 - "AlphaVantageCambioAdapter"
Cohesion: 0.09
Nodes (16): CambioProviderException, AlphaCambioRate, AlphaCambioResponse, AlphaVantageCambioAdapter, AlphaVantageCambioClient, Override, Override, TwelveCambioResponse (+8 more)

### Community 12 - "InvestmentPersistenceAdapter"
Cohesion: 0.08
Nodes (22): InvestmentPersistenceAdapter, Override, Reservation, Wallet, KnownValuation, LocalSnapshotComposer, Totals, com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort (+14 more)

### Community 13 - "positions-panel.tsx"
Cohesion: 0.11
Nodes (31): isActive(), navigation, NavigationLink(), titles, MarketFreshness(), FinancialPageIntro(), Pagination(), EmptyState() (+23 more)

### Community 14 - "org.springframework.stereotype.Repository"
Cohesion: 0.06
Nodes (20): CarteiraInvestimentoApplication, AuditoriaPersistenceAdapter, Override, CarteiraJpaEntity, Entity, Table, CarteiraJpaRepository, HistoricoCambioJpaRepository (+12 more)

### Community 15 - "Usuario"
Cohesion: 0.07
Nodes (15): CurrentPrincipalService, Role, ROLE_ADMIN, ROLE_USER, Usuario, CurrentUserResponse, RegisteredUserResponse, Override (+7 more)

### Community 16 - "finance-panels.test.tsx"
Cohesion: 0.09
Nodes (35): financeApi, FinanceApiError, request(), safeProblem(), fetchMock, hooks, QueryState, RefreshState (+27 more)

### Community 17 - "transport.ts"
Cohesion: 0.20
Nodes (26): GET(), GET(), { backendRequest }, POST(), GET(), GET(), GET(), GET() (+18 more)

### Community 18 - "org.springframework.cloud.openfeign.FeignClient"
Cohesion: 0.09
Nodes (20): CvmPort, ReceitaFederalPort, ViaCepPort, BrokerUpstreamException, BrasilApiCnpjAdapter, BrasilApiCnpjClient, CnpjProviderResponse, Override (+12 more)

### Community 19 - ".edit"
Cohesion: 0.13
Nodes (17): BrokerAdminResponse, BrokerController, GetMapping, BrokerErrors, BrokerSelectionResponse, UpdateBrokerRequest, com.fasterxml.jackson.annotation.JsonSetter, io.swagger.v3.oas.annotations.responses.ApiResponse (+9 more)

### Community 20 - "InvestmentApplicationService"
Cohesion: 0.12
Nodes (11): Page, PositionView, TransactionView, InvestmentApplicationService, Override, InvestmentOperationResult, InvestmentUseCase, InvestmentConfiguration (+3 more)

### Community 21 - "com.carteira.carteiraInvestimento.domain.quote.Cotacao"
Cohesion: 0.14
Nodes (18): Override, MarketQuoteApplicationService, FakeAssets, FakeHistory, Fixture, CotacaoExterna, HistoricoCotacaoPage, Override (+10 more)

### Community 22 - "CorretoraReadScope"
Cohesion: 0.08
Nodes (13): CorretoraPage, CorretoraReadScope, ACTIVE_ONLY, ALL, BrokerUseCase, DuplicateBrokerException, BrokerListResponse, Test (+5 more)

### Community 23 - "AuditSecurityEventsIntegrationTest.java"
Cohesion: 0.13
Nodes (12): AccessTokenIssuer, AuditoriaIsoladaPort, AuditoriaPort, PasswordHasher, UsuarioPort, InitialAdminBootstrapService, LoginService, RegistrationService (+4 more)

### Community 24 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.08
Nodes (9): Authentication, InvestmentConflictException, InvestmentNotFoundException, SecurityProbeController, ProtectedProbeController, CorrelationProbeController, AccessDeniedProbeController, FailureProbeController (+1 more)

### Community 25 - "PortfolioValuationIT"
Cohesion: 0.16
Nodes (6): PortfolioValuationUseCase, Fixture, Cotacao, InvestmentTransactionCommand, PortfolioValuationIT, PortfolioValuationIT.Fakes

### Community 26 - "CorretoraJpaEntity"
Cohesion: 0.12
Nodes (4): CorretoraJpaEntity, CorretoraJpaRepository, CorretoraPersistenceAdapter, Override

### Community 27 - "Cotacao"
Cohesion: 0.11
Nodes (10): HistoricoCotacaoPage, HistoricoCotacaoPort, InactiveAssetRefreshException, MarketQuoteUseCase, Cotacao, MarketQuoteConfiguration, MarketQuoteControllerTest, com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage (+2 more)

### Community 28 - "org.springframework.transaction.annotation.Transactional"
Cohesion: 0.15
Nodes (6): Override, BrokerPatch, BrokerCatalogPersistenceIT, BeforeEach, Test, org.springframework.transaction.annotation.Transactional

### Community 29 - "com.fasterxml.jackson.annotation.JsonAnySetter"
Cohesion: 0.10
Nodes (7): UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, CreateBrokerRequest, UpdateBrokerLifecycleRequest, CashMovementRequest, com.fasterxml.jackson.annotation.JsonAnySetter, com.fasterxml.jackson.annotation.JsonCreator

### Community 30 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 31 - ".criar"
Cohesion: 0.19
Nodes (10): AfterAll, CadastroCnpj, EnderecoPostal, RegistroCvm, BrokerApplicationServiceTest, AuditoriaIsoladaPort, Test, BrokerProviderAdaptersIT (+2 more)

### Community 32 - "Posicao"
Cohesion: 0.13
Nodes (4): InvestmentNumbers, Posicao, Sale, InvestmentDomainTest

### Community 33 - "PortfolioValuationPort"
Cohesion: 0.13
Nodes (13): LocalState, MaterializedTotals, PortfolioValuationPort, CambioUseCase, PortfolioValuationApplicationService, Override, PortfolioValuationPersistenceAdapter, WalletRow (+5 more)

### Community 34 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.17
Nodes (8): CashMovementUseCase, InvestmentController, PortfolioValuationController, CashBalanceResponse, CashMovementController, CashMovementResponse, io.swagger.v3.oas.annotations.Operation, org.springframework.security.oauth2.jwt.Jwt

### Community 35 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 36 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 37 - "org.springframework.security.oauth2.jwt.JwtEncoder"
Cohesion: 0.16
Nodes (11): JwtConfiguration, JwtProperties, Override, JwtAccessTokenIssuer, JwtAccessTokenIssuerTest, javax.crypto.spec.SecretKeySpec, org.springframework.security.oauth2.core.OAuth2TokenValidator, org.springframework.security.oauth2.core.OAuth2TokenValidatorResult (+3 more)

### Community 38 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 39 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 40 - "Ativo"
Cohesion: 0.17
Nodes (13): AtivoPage, AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME (+5 more)

### Community 41 - "org.springframework.web.bind.annotation.RestController"
Cohesion: 0.20
Nodes (11): CurrentPrincipalController, LoginController, LoginRequest, RegisterRequest, RegistrationController, CambioController, io.swagger.v3.oas.annotations.security.SecurityRequirement, org.springframework.http.ResponseEntity (+3 more)

### Community 42 - "AtivoPort"
Cohesion: 0.18
Nodes (5): AtivoPort, AtivoApplicationService, Override, TickerCanonicalizer, AtivoApplicationServiceTest

### Community 43 - "InvestmentPersistencePort"
Cohesion: 0.24
Nodes (10): ExistingReservation, InvestmentPersistencePort, LocalExchangeRate, NewReservation, ProtectedAsset, ProtectedBroker, Reservation, Wallet (+2 more)

### Community 44 - "CashMovementRollbackIT.java"
Cohesion: 0.14
Nodes (16): Fixture, InvestmentRollbackIT, InvestmentTransactionCommand, Stage, AUDIT, COMPLETE, POSITION, SNAPSHOT (+8 more)

### Community 45 - "Corretora"
Cohesion: 0.14
Nodes (3): Corretora, BrokerDomainTest, BeforeEach

### Community 46 - "TipoAtivo"
Cohesion: 0.16
Nodes (11): Mercado, B3, US, Moeda, BRL, USD, TipoAtivo, ACAO (+3 more)

### Community 47 - "PostgreSqlContainerSupport"
Cohesion: 0.14
Nodes (11): BrokerCatalogSecurityIT, Test, BrokerTransactionBoundaryIT, BeforeEach, CambioTransactionBoundaryIT, PostgreSqlContainerSupport, BrokerTransactionBoundaryIT.Fakes, Import (+3 more)

### Community 48 - "dependencies"
Cohesion: 0.09
Nodes (23): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lossless-json, lucide-react, next (+15 more)

### Community 49 - "Fixture"
Cohesion: 0.16
Nodes (7): AuditoriaCommand, CambioApplicationServiceTest, FakeAudit, FakeHistory, Fixture, Override, MutableClock

### Community 50 - "MovimentacaoCaixa"
Cohesion: 0.15
Nodes (9): CashLedgerPort, CashMovementPage, CashOperationResult, MovimentacaoCaixa, TipoMovimentacaoCaixa, DEPOSITO, SAQUE, CashMovementHistoryResponse (+1 more)

### Community 51 - "PortfolioValuationApplicationServiceTest"
Cohesion: 0.19
Nodes (7): OpenPosition, Override, CambioUseCase, Cotacao, ObservacaoCambio, PortfolioValuationApplicationServiceTest, SuppressWarnings

### Community 52 - "UsuarioJpaEntity"
Cohesion: 0.16
Nodes (8): Override, RepositoryUserDetailsService, Entity, Table, UsuarioJpaEntity, UsuarioJpaRepository, UserDetails, UserDetailsService

### Community 53 - "AtivoController"
Cohesion: 0.16
Nodes (5): AtivoController, AtivoListResponse, AtivoResponse, CreateAtivoRequest, org.springframework.web.bind.annotation.PatchMapping

### Community 54 - "java.sql.Connection"
Cohesion: 0.28
Nodes (3): Fixture, InvestmentSchemaIT, java.sql.Connection

### Community 55 - "portfolio-queries.ts"
Cohesion: 0.10
Nodes (12): portfolioKeys, useCashBalance(), useCashMovements(), usePortfolioSummary(), usePositions(), useRefreshPortfolioSummary(), useTransactions(), CashPanel() (+4 more)

### Community 56 - "TipoTransacao"
Cohesion: 0.17
Nodes (9): InvestmentTransactionCommand, TipoTransacao, BUY, SELL, InvestmentOperationResponse, InvestmentTransactionRequest, PositionResponse, TransactionResponse (+1 more)

### Community 57 - "devDependencies"
Cohesion: 0.10
Nodes (21): eslint, eslint-config-next, devDependencies, eslint, eslint-config-next, @playwright/test, shadcn, @tailwindcss/postcss (+13 more)

### Community 58 - "IdentityApplicationConfiguration.java"
Cohesion: 0.16
Nodes (14): AdminProperties, IdentityApplicationConfiguration, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher, com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService, com.carteira.carteiraInvestimento.application.service.InitialAdminBootstrapService, com.carteira.carteiraInvestimento.application.service.LoginService (+6 more)

### Community 60 - "BrokerApplicationService"
Cohesion: 0.15
Nodes (7): CorretoraPort, BrokerApplicationService, AuditoriaIsoladaPort, BrokerComplianceException, BrokerLocalTransactionService, BeforeEach, org.springframework.stereotype.Service

### Community 61 - "com.carteira.carteiraInvestimento.domain.asset.Moeda"
Cohesion: 0.19
Nodes (10): CurrentExchangeRate, PortfolioValuationResult, ValuedPosition, CambioAtualResponse, PortfolioValuationResponse, ValuedPositionResponse, FakeProvider, com.carteira.carteiraInvestimento.domain.asset.Moeda (+2 more)

### Community 62 - "AtivoUseCase"
Cohesion: 0.16
Nodes (3): AtivoNotFoundException, AtivoUseCase, AtivoControllerTest

### Community 63 - "HistoricoCotacaoJpaEntity"
Cohesion: 0.19
Nodes (6): HistoricoCotacaoJpaEntity, HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable

### Community 64 - ".get"
Cohesion: 0.16
Nodes (4): ApplicationFoundationIT, GlobalExceptionHandlerTest, com.fasterxml.jackson.databind.JsonNode, org.springframework.web.context.WebApplicationContext

### Community 65 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 66 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 67 - "CashMovementApplicationService"
Cohesion: 0.23
Nodes (10): CashMovementApplicationService, Override, CashMovementUseCase, com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort, com.carteira.carteiraInvestimento.application.port.CashLedgerPort, com.carteira.carteiraInvestimento.application.port.CashMovementPage, com.carteira.carteiraInvestimento.application.port.CashSnapshotPort, com.carteira.carteiraInvestimento.application.port.CashWalletPort (+2 more)

### Community 68 - "jakarta.persistence.Entity"
Cohesion: 0.22
Nodes (8): CarteiraSnapshotJpaEntity, PosicaoJpaEntity, TransacaoIdempotenciaJpaEntity, TransacaoJpaEntity, jakarta.persistence.Column, jakarta.persistence.Entity, jakarta.persistence.Table, org.hibernate.annotations.Immutable

### Community 69 - "CashPersistenceAdapter"
Cohesion: 0.19
Nodes (7): CashPersistenceAdapter, Override, Reservation, Wallet, CashMovementPage, com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa, MovimentacaoCaixa

### Community 70 - "CashMovementApiIT"
Cohesion: 0.24
Nodes (6): CashMovementApiIT, HttpCall, HttpResults, FunctionalInterface, User, org.springframework.test.web.servlet.MvcResult

### Community 71 - "CashMovementRollbackIT"
Cohesion: 0.21
Nodes (9): CashMovementRollbackIT, FailureStage, AUDIT, MOVEMENT, RESULT, SNAPSHOT, TransactionTemplate, CashMovementApplicationService (+1 more)

### Community 72 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 73 - "AtivoPersistenceAdapter"
Cohesion: 0.18
Nodes (5): DuplicateTickerException, AtivoJpaRepository, AtivoPersistenceAdapter, Override, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 74 - "SecurityProblemDetailHandler"
Cohesion: 0.21
Nodes (8): Override, SecurityProblemDetailHandler, MockHttpServletRequest, SecurityProblemDetailHandlerTest, jakarta.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException, org.springframework.security.web.access.AccessDeniedHandler, org.springframework.security.web.AuthenticationEntryPoint

### Community 75 - "org.springframework.security.access.prepost.PreAuthorize"
Cohesion: 0.19
Nodes (6): CotacaoResponse, HistoricoCotacaoResponse, MarketQuoteController, AdminProbeController, org.springframework.security.access.prepost.PreAuthorize, org.springframework.web.bind.annotation.PutMapping

### Community 76 - "AssetCatalogPersistenceIT"
Cohesion: 0.21
Nodes (4): AssetCatalogPersistenceIT, AtivoQuery, SqlAction, FunctionalInterface

### Community 78 - "PostgreSqlContainerSupport"
Cohesion: 0.20
Nodes (5): FlywayValidationIT, Column, InvestmentV9MetadataIT, Entry, PostgreSqlContainerSupport

### Community 79 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 80 - "mappers.ts"
Cohesion: 0.35
Nodes (16): array(), Data, decimal(), mapCashBalance(), mapCashMovement(), mapPage(), mapPosition(), mapSummary() (+8 more)

### Community 81 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 84 - ".login"
Cohesion: 0.19
Nodes (4): IssuedAccessToken, AuthenticationFailedException, LoginResponse, LoginControllerTest

### Community 85 - ".execute"
Cohesion: 0.22
Nodes (4): CashMovementNormalizer, TipoMovimentacaoCaixa, CashMovementDomainTest, CashOperationResult

### Community 86 - "CashIdempotencyPort"
Cohesion: 0.35
Nodes (6): CashIdempotencyPort, ExistingReservation, NewReservation, Reservation, Wallet, CashMovementApplicationServiceTest

### Community 88 - "CambioUseCase"
Cohesion: 0.20
Nodes (4): CambioUnavailableException, CambioUseCase, FixedCambio, CambioControllerTest

### Community 89 - "InvestmentLifecycleConcurrencyIT"
Cohesion: 0.41
Nodes (4): Fixture, Fixture, HeldOperation, InvestmentLifecycleConcurrencyIT

### Community 91 - "finance-core.test.ts"
Cohesion: 0.35
Nodes (10): DATE_TIME, decimalSign(), formatDateTime(), formatDecimal(), formatMoney(), formatPercentage(), formatQuantity(), group() (+2 more)

### Community 92 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 93 - "CashMovementConfiguration.java"
Cohesion: 0.24
Nodes (3): CashSnapshotPort, CashWalletPort, CashMovementConfiguration

### Community 94 - "InvestmentApplicationService.java"
Cohesion: 0.22
Nodes (3): FinancialStateException, InvestmentFingerprint, Transacao

### Community 95 - "Mercado.java"
Cohesion: 0.24
Nodes (3): AtivoPersistenceAdapterTest, DataIntegrityViolationException, org.springframework.dao.DataIntegrityViolationException

### Community 98 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 99 - "CanonicalFingerprint"
Cohesion: 0.29
Nodes (4): CanonicalFingerprint, Field, DataOutputStream, java.io.DataOutputStream

### Community 100 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 102 - "CambioConfiguration"
Cohesion: 0.33
Nodes (4): CambioConfiguration, CambioConfigurationTest, MutableTicker, com.github.benmanes.caffeine.cache.Ticker

### Community 105 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 106 - "BrokerProviderAdaptersIT.java"
Cohesion: 0.36
Nodes (4): org.springframework.core.env.Environment, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer

### Community 107 - "FakeQuotes"
Cohesion: 0.36
Nodes (3): FakeQuotes, ObservacaoCambio, Override

### Community 108 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 111 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 112 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 113 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 115 - "mock-backend.mjs"
Cohesion: 0.70
Nodes (4): bodyOf(), json(), server, summary()

### Community 117 - "AppShell"
Cohesion: 0.40
Nodes (3): AppShell(), { pathname, replace, logout }, user

### Community 118 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 119 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 121 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **376 isolated node(s):** `AuthConfig`, `AuthEnv`, `BackendErrorKind`, `Provider`, `BrokerNotFoundException` (+371 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 627 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **49 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CashPersistenceAdapter` connect `CashPersistenceAdapter` to `CashMovementApplicationService`, `CashMovementRollbackIT`, `org.junit.jupiter.api.BeforeEach`, `InvestmentPersistenceAdapter`, `CashMovementRollbackIT.java`, `org.springframework.stereotype.Repository`?**
  _High betweenness centrality (0.020) - this node is a cross-community bridge._
- **Why does `Corretora` connect `Corretora` to `CnpjCanonicalizer`, `org.junit.jupiter.api.BeforeEach`, `org.springframework.transaction.annotation.Transactional`, `PostgreSqlContainerSupport`, `.edit`, `CorretoraReadScope`, `CorretoraJpaEntity`, `BrokerApplicationService`, `.criar`?**
  _High betweenness centrality (0.015) - this node is a cross-community bridge._
- **Why does `CashMovementApiIT` connect `CashMovementApiIT` to `org.junit.jupiter.api.BeforeEach`, `org.springframework.security.oauth2.jwt.JwtEncoder`, `PostgreSqlContainerSupport`?**
  _High betweenness centrality (0.015) - this node is a cross-community bridge._
- **What connects `AuthConfig`, `AuthEnv`, `BackendErrorKind` to the rest of the system?**
  _376 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `types.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.051446321102698506 - nodes in this community are weakly interconnected._
- **Should `QuoteProvider` be split into smaller, more focused modules?**
  _Cohesion score 0.05524537173082574 - nodes in this community are weakly interconnected._
- **Should `AuditoriaCommand` be split into smaller, more focused modules?**
  _Cohesion score 0.05328005328005328 - nodes in this community are weakly interconnected._