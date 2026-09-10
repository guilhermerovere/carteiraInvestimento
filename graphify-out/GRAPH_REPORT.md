# Graph Report - projetoJeff  (2026-09-10)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 2122 nodes · 5289 edges · 145 communities (93 shown, 42 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 396 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `68e0676e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- types.ts
- org.springframework.security.access.prepost.PreAuthorize
- auth.ts
- jakarta.servlet.http.HttpServletRequest
- org.junit.jupiter.api.Test
- Corretora
- Cotacao
- UsuarioPort
- org.springframework.context.annotation.Bean
- org.springframework.boot.test.context.SpringBootTest
- Role
- CorretoraJpaEntity
- Requirements
- ADDED Requirements
- Requirement: Cadastro publico transacional
- ADDED Requirements
- BrokerCatalogPersistenceIT
- CashMovementApplicationService
- compilerOptions
- AtivoControllerTest.java
- .criar
- HistoricoCotacaoPage
- TipoAtivo
- AtivoJpaEntity
- org.springframework.test.web.servlet.MockMvc
- MarketQuoteControllerTest.java
- org.junit.jupiter.api.BeforeEach
- CashMovementRollbackIT.java
- ResultadoAuditoria
- AlphaVantageQuoteAdapter.java
- BrokerUpstreamException
- Usuario
- HistoricoCotacaoJpaEntity
- Ativo
- IdentityApplicationConfiguration.java
- AuditSecurityEventsIntegrationTest
- BrokerApplicationService
- org.springframework.security.oauth2.jwt.Jwt
- package.json
- AtivoPort
- .execute
- CashMovementConcurrencyIT
- org.springframework.web.bind.annotation.GetMapping
- PostgreSqlContainerSupport
- dependencies
- devDependencies
- Requirement: Eventos minimos de seguranca
- QuoteProvider
- .register
- ADDED Requirements
- BrokerCatalogSecurityIT
- CashMovementApiIT
- components.json
- Decisions
- TipoMovimentacaoCaixa
- UsuarioJpaEntity
- CashPersistenceAdapter
- AssetCatalogPersistenceIT
- AtivoPersistenceAdapter.java
- TipoEvento
- .nova
- Fakes
- BrapiQuoteAdapter.java
- AtivoController
- PersistedUserJwtAuthenticationConverterTest
- CashMovementSchemaIT
- org.springframework.web.bind.annotation.RestController
- Wallet
- LogAuditoriaJpaEntity
- SecurityProblemDetailHandler
- BrokerProviderAdaptersIT
- AuditSecurityEventsIntegrationTest.java
- MarketQuoteSecurityIT
- Requirement: Documentacao HTTP e erros padronizados
- AuditoriaCommand
- org.springframework.boot.context.properties.ConfigurationProperties
- IdentityPersistenceAdapter
- PersistedUserJwtAuthenticationConverter
- mvnw
- CurrentPrincipalService
- CarteiraJpaEntity
- .register
- tasks.md
- Carteira
- org.springframework.cloud.openfeign.FeignClient
- org.springframework.web.bind.annotation.PostMapping
- MarketQuotePersistenceIT
- scripts
- .novo
- Project Foundation Proposal
- AuditoriaPersistenceAdapter
- UsuarioJpaRepository
- SecurityProblemDetailHandlerTest
- proposal.md
- OpenSpec Apply Change Workflow
- AtivoApplicationService.java
- usuarios
- Fakes
- .failingAuditoriaPort
- app/layout.tsx
- Q: Localizar impactos existentes para estabelecer identidade e controle de acesso
- Spec-Driven Development Policy
- frontend/package.json
- PrimaryWalletMissingException
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
2. `Corretora` - 50 edges
3. `Ativo` - 38 edges
4. `Cotacao` - 36 edges
5. `AuditoriaCommand` - 36 edges
6. `AuditSecurityEventsIntegrationTest` - 34 edges
7. `GlobalExceptionHandler` - 30 edges
8. `QuoteProvider` - 29 edges
9. `UsuarioPort` - 29 edges
10. `TipoEvento` - 28 edges

## Surprising Connections (you probably didn't know these)
- `Spec-Driven Development Policy` --conceptually_related_to--> `OpenSpec Spec-Driven Schema Configuration`  [INFERRED]
  AGENTS.md → openspec/config.yaml
- `JWT Application Configuration` --implements--> `Bearer Authentication Specification`  [INFERRED]
  backend/src/main/resources/application.yml → openspec/specs/bearer-authentication-and-authorization/spec.md
- `Containerized Local Environment Delta Specification` --semantically_similar_to--> `Containerized Local Environment Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/containerized-local-environment/spec.md → openspec/specs/containerized-local-environment/spec.md
- `Frontend Foundation Delta Specification` --semantically_similar_to--> `Frontend Foundation Specification`  [INFERRED] [semantically similar]
  openspec/changes/archive/2026-08-31-establish-project-foundation/specs/frontend-foundation/spec.md → openspec/specs/frontend-foundation/spec.md
- `BrokerController` --references--> `BrokerUseCase`  [EXTRACTED]
  backend/src/main/java/com/carteira/carteiraInvestimento/presentation/broker/BrokerController.java → backend/src/main/java/com/carteira/carteiraInvestimento/application/service/BrokerUseCase.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **OpenSpec Change Lifecycle** — _agents_skills_openspec_explore_skill_openspec_explore, _agents_skills_openspec_propose_skill_openspec_propose, _agents_skills_openspec_apply_change_skill_openspec_apply_change, _agents_skills_openspec_archive_change_skill_openspec_archive_change [EXTRACTED 1.00]
- **Project Foundation Planning Artifacts** — openspec_changes_archive_2026_08_31_establish_project_foundation_proposal_project_foundation_proposal, openspec_changes_archive_2026_08_31_establish_project_foundation_design_project_foundation_design, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_backend_foundation_spec_backend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_containerized_local_environment_spec_containerized_environment_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_specs_frontend_foundation_spec_frontend_foundation_delta, openspec_changes_archive_2026_08_31_establish_project_foundation_tasks_project_foundation_tasks [EXTRACTED 1.00]
- **Integrated Runtime Readiness** — openspec_changes_archive_2026_08_31_establish_project_foundation_design_healthcheck_readiness_chain, openspec_specs_containerized_local_environment_spec_containerized_local_environment [INFERRED 0.95]

## Communities (145 total, 42 thin omitted)

### Community 0 - "types.ts"
Cohesion: 0.07
Nodes (48): POST(), { loginMock, originMock }, POST(), fetchMock, GET(), { resolveMock }, POST(), { registerMock, originMock } (+40 more)

### Community 1 - "org.springframework.security.access.prepost.PreAuthorize"
Cohesion: 0.06
Nodes (27): UpdateAtivoLifecycleRequest, UpdateAtivoNameRequest, BrokerAdminResponse, BrokerController, GetMapping, BrokerErrors, BrokerListResponse, BrokerSelectionResponse (+19 more)

### Community 2 - "auth.ts"
Cohesion: 0.06
Nodes (40): AccessDeniedPage(), fetchMock, { replaceMock, searchMock }, LoginForm(), messageFor(), LoginPage(), AdminPage(), InicioPage() (+32 more)

### Community 3 - "jakarta.servlet.http.HttpServletRequest"
Cohesion: 0.10
Nodes (26): BrokerAuditException, BrokerComplianceException, BrokerNotFoundException, DuplicateBrokerException, GlobalExceptionHandler, com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException, com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException, com.carteira.carteiraInvestimento.application.service.CashConflictException (+18 more)

### Community 4 - "org.junit.jupiter.api.Test"
Cohesion: 0.06
Nodes (13): MarketQuoteApplicationServiceTest, CotacaoDomainTest, MarketQuoteConfigurationTest, PasswordSecurityConfigurationTest, SecurityConfigurationPropertiesTest, AuditoriaPersistenceAdapterTest, CorrelationIdFilterTest, FlywayValidationIT (+5 more)

### Community 5 - "Corretora"
Cohesion: 0.10
Nodes (13): CorretoraPage, CorretoraPort, CorretoraReadScope, ACTIVE_ONLY, ALL, Override, BrokerUseCase, Corretora (+5 more)

### Community 6 - "Cotacao"
Cohesion: 0.13
Nodes (17): CotacaoProviderPort, HistoricoCotacaoPort, Override, MarketQuoteApplicationService, Cotacao, MarketQuoteConfiguration, FakeAssets, FakeHistory (+9 more)

### Community 7 - "UsuarioPort"
Cohesion: 0.10
Nodes (12): AccessTokenIssuer, IssuedAccessToken, AuditoriaPort, PasswordHasher, UsuarioPort, AuthenticationFailedException, InitialAdminBootstrapService, LoginService (+4 more)

### Community 8 - "org.springframework.context.annotation.Bean"
Cohesion: 0.09
Nodes (19): FoundationSecurityConfiguration, OpenApiConfiguration, BcryptPasswordHasher, PropertiesConfiguration, AccessDeniedProbeConfiguration, SecurityTestConfiguration, io.swagger.v3.oas.models.OpenAPI, LocalValidatorFactoryBean (+11 more)

### Community 9 - "org.springframework.boot.test.context.SpringBootTest"
Cohesion: 0.14
Nodes (17): AssetCatalogSecurityIT, BrokerTransactionBoundaryIT, BeforeEach, Test, BrokerTransactionBoundaryIT.Fakes, com.carteira.carteiraInvestimento.application.port.UsuarioPort, com.carteira.carteiraInvestimento.application.service.AtivoUseCase, com.carteira.carteiraInvestimento.domain.asset.Mercado (+9 more)

### Community 10 - "Role"
Cohesion: 0.13
Nodes (14): Role, ROLE_ADMIN, ROLE_USER, JwtConfiguration, JwtProperties, Override, JwtAccessTokenIssuer, JwtAccessTokenIssuerTest (+6 more)

### Community 11 - "CorretoraJpaEntity"
Cohesion: 0.12
Nodes (4): CorretoraJpaEntity, CorretoraJpaRepository, CorretoraPersistenceAdapter, Override

### Community 12 - "Requirements"
Cohesion: 0.06
Nodes (30): Bearer Authentication And Authorization Specification, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+22 more)

### Community 13 - "ADDED Requirements"
Cohesion: 0.07
Nodes (29): ADDED Requirements, Purpose, Requirement: Access token JWT verificavel, Requirement: Autenticacao stateless limitada ao access token, Requirement: Estado atual validado em toda requisicao protegida, Requirement: Fronteira de rotas publicas e privadas, Requirement: Login por credenciais, Requirement: Semantica uniforme de erros de seguranca (+21 more)

### Community 14 - "Requirement: Cadastro publico transacional"
Cohesion: 0.07
Nodes (29): Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido, Requirements (+21 more)

### Community 15 - "ADDED Requirements"
Cohesion: 0.07
Nodes (28): ADDED Requirements, Purpose, Requirement: Cadastro publico transacional, Requirement: Carteira principal minima por usuario comum, Requirement: E-mail canonico e unico, Requirement: Provisionamento do administrador inicial, Requirement: Role unica e restrita, Requirement: Usuario persistido e protegido (+20 more)

### Community 16 - "BrokerCatalogPersistenceIT"
Cohesion: 0.17
Nodes (6): AuditoriaCommand, BrokerLocalTransactionService, BrokerPatch, BrokerCatalogPersistenceIT, BeforeEach, Test

### Community 17 - "CashMovementApplicationService"
Cohesion: 0.17
Nodes (11): CashIdempotencyPort, CashLedgerPort, CashMovementPage, CashSnapshotPort, CashWalletPort, CashMovementApplicationService, Override, CashMovementUseCase (+3 more)

### Community 18 - "compilerOptions"
Cohesion: 0.07
Nodes (27): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+19 more)

### Community 19 - "AtivoControllerTest.java"
Cohesion: 0.15
Nodes (13): AtivoPage, AtivoQuery, AtivoReadScope, ACTIVE_ONLY, ALL, INACTIVE_ONLY, AtivoSort, NOME (+5 more)

### Community 20 - ".criar"
Cohesion: 0.21
Nodes (8): CadastroCnpj, EnderecoPostal, RegistroCvm, BrokerApplicationServiceTest, AuditoriaIsoladaPort, Test, Test, MockResponse

### Community 21 - "HistoricoCotacaoPage"
Cohesion: 0.13
Nodes (7): HistoricoCotacaoPage, MarketQuoteUseCase, CotacaoResponse, HistoricoCotacaoResponse, MarketQuoteController, MarketQuoteControllerTest, org.springframework.web.bind.annotation.PutMapping

### Community 22 - "TipoAtivo"
Cohesion: 0.13
Nodes (11): Mercado, B3, US, Moeda, BRL, USD, TipoAtivo, ACAO (+3 more)

### Community 23 - "AtivoJpaEntity"
Cohesion: 0.15
Nodes (6): AtivoJpaEntity, AtivoJpaRepository, AtivoPersistenceAdapter, Override, org.springframework.data.jpa.repository.JpaRepository, org.springframework.data.jpa.repository.JpaSpecificationExecutor

### Community 24 - "org.springframework.test.web.servlet.MockMvc"
Cohesion: 0.20
Nodes (17): AuditoriaIsoladaPort, AccessDeniedAuditingService, ProblemDetailFactory, FoundationSecurityConfigurationTest, SecurityTestConfiguration, SecurityTestConfiguration, CurrentPrincipalControllerTest, CurrentPrincipalControllerTest.SecurityTestConfiguration (+9 more)

### Community 25 - "MarketQuoteControllerTest.java"
Cohesion: 0.14
Nodes (8): InactiveAssetRefreshException, QuoteIntegrationException, QuoteNotFoundException, Override, TwelveDataClient, TwelveDataQuoteAdapter, TwelveResponse, com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory

### Community 26 - "org.junit.jupiter.api.BeforeEach"
Cohesion: 0.11
Nodes (6): ApplicationFoundationIT, IdentitySchemaIntegrationTest, org.flywaydb.core.Flyway, org.junit.jupiter.api.BeforeEach, org.springframework.web.context.WebApplicationContext, TestingAuthenticationToken

### Community 27 - "CashMovementRollbackIT.java"
Cohesion: 0.14
Nodes (13): CashMovementRollbackIT, FailureStage, AUDIT, MOVEMENT, RESULT, SNAPSHOT, com.carteira.carteiraInvestimento.infrastructure.persistence.AuditoriaPersistenceAdapter, org.junit.jupiter.params.ParameterizedTest (+5 more)

### Community 28 - "ResultadoAuditoria"
Cohesion: 0.18
Nodes (11): ResultadoAuditoria, FALHA, NEGADO, SUCESSO, SeveridadeAuditoria, ALERTA, AVISO, INFO (+3 more)

### Community 29 - "AlphaVantageQuoteAdapter.java"
Cohesion: 0.18
Nodes (10): MarketQuoteProperties, Provider, AlphaBar, AlphaMeta, AlphaResponse, AlphaVantageClient, AlphaVantageQuoteAdapter, Override (+2 more)

### Community 30 - "BrokerUpstreamException"
Cohesion: 0.13
Nodes (11): CvmPort, ViaCepPort, BrokerUpstreamException, BrasilApiCvmAdapter, BrasilApiCvmClient, CvmProviderResponse, Override, Override (+3 more)

### Community 31 - "Usuario"
Cohesion: 0.14
Nodes (7): Usuario, CurrentUserResponse, Override, TestUsuarios, Override, TestUsuarios, RegistrationControllerTest

### Community 32 - "HistoricoCotacaoJpaEntity"
Cohesion: 0.16
Nodes (7): HistoricoCotacaoJpaEntity, HistoricoCotacaoJpaRepository, HistoricoCotacaoPersistenceAdapter, Override, org.springframework.data.domain.Page, org.springframework.data.domain.Pageable, org.springframework.stereotype.Repository

### Community 33 - "Ativo"
Cohesion: 0.15
Nodes (4): AtivoUseCase, Ativo, NomeAtivoCanonicalizer, AtivoControllerTest

### Community 34 - "IdentityApplicationConfiguration.java"
Cohesion: 0.15
Nodes (15): AdminProperties, IdentityApplicationConfiguration, com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer, com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort, com.carteira.carteiraInvestimento.application.port.CarteiraPort, com.carteira.carteiraInvestimento.application.port.PasswordHasher, com.carteira.carteiraInvestimento.application.service.CurrentPrincipalService, com.carteira.carteiraInvestimento.application.service.InitialAdminBootstrapService (+7 more)

### Community 36 - "BrokerApplicationService"
Cohesion: 0.13
Nodes (6): ReceitaFederalPort, BrokerApplicationService, AuditoriaIsoladaPort, CnpjCanonicalizer, BeforeEach, org.springframework.stereotype.Service

### Community 37 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.19
Nodes (7): CashBalanceResponse, CashMovementController, CashMovementHistoryResponse, CashMovementResponse, Movement, org.springframework.http.ResponseEntity, org.springframework.security.oauth2.jwt.Jwt

### Community 38 - "package.json"
Cohesion: 0.10
Nodes (19): @fission-ai/openspec, author, bugs, url, description, devDependencies, @fission-ai/openspec, homepage (+11 more)

### Community 39 - "AtivoPort"
Cohesion: 0.25
Nodes (4): AtivoPort, AtivoApplicationService, Override, AtivoApplicationServiceTest

### Community 40 - ".execute"
Cohesion: 0.16
Nodes (4): CashMovementNormalizer, CashMovementDomainTest, DataOutputStream, java.io.DataOutputStream

### Community 41 - "CashMovementConcurrencyIT"
Cohesion: 0.29
Nodes (5): CashMovementConcurrencyIT, Fixture, FunctionalInterface, Outcomes, ThrowingSupplier

### Community 42 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.15
Nodes (5): DuplicateEmailException, SecurityProbeController, AccessDeniedProbeController, FailureProbeController, org.springframework.web.bind.annotation.GetMapping

### Community 43 - "PostgreSqlContainerSupport"
Cohesion: 0.15
Nodes (5): IdentityDomainTest, DuplicateEmailPersistenceIntegrationTest, DataSource, InitialAdminBootstrapIntegrationTest, PostgreSqlContainerSupport

### Community 44 - "dependencies"
Cohesion: 0.11
Nodes (19): class-variance-authority, clsx, dependencies, class-variance-authority, clsx, lucide-react, next, react (+11 more)

### Community 45 - "devDependencies"
Cohesion: 0.11
Nodes (19): eslint, devDependencies, eslint, jsdom, @playwright/test, shadcn, @tailwindcss/postcss, @testing-library/react (+11 more)

### Community 46 - "Requirement: Eventos minimos de seguranca"
Cohesion: 0.11
Nodes (18): Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Requirements, Scenario: Acesso negado, Scenario: Administrador inicial criado (+10 more)

### Community 47 - "QuoteProvider"
Cohesion: 0.17
Nodes (7): CotacaoExterna, QuoteProvider, ALPHA_VANTAGE, BRAPI, TWELVE_DATA, FakeProvider, com.carteira.carteiraInvestimento.domain.asset.Moeda

### Community 48 - ".register"
Cohesion: 0.18
Nodes (6): CarteiraInvestimentoApplication, RegistrationConcurrencyIntegrationTest, RegistrationTransactionIntegrationTest, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.ConfigurationPropertiesScan, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration

### Community 49 - "ADDED Requirements"
Cohesion: 0.11
Nodes (17): ADDED Requirements, Purpose, Requirement: Ausencia de consulta de auditoria nesta etapa, Requirement: Eventos minimos de seguranca, Requirement: Registro persistido de auditoria de seguranca, Requirement: Sanitizacao obrigatoria, Scenario: Acesso negado, Scenario: Administrador inicial criado (+9 more)

### Community 50 - "BrokerCatalogSecurityIT"
Cohesion: 0.23
Nodes (7): BrokerCatalogSecurityIT, BeforeEach, Test, com.fasterxml.jackson.databind.ObjectMapper, JwtEncoder, Role, Usuario

### Community 51 - "CashMovementApiIT"
Cohesion: 0.26
Nodes (6): CashMovementApiIT, HttpCall, HttpResults, FunctionalInterface, User, org.springframework.test.web.servlet.MvcResult

### Community 52 - "components.json"
Cohesion: 0.12
Nodes (16): aliases, components, hooks, lib, ui, utils, iconLibrary, rsc (+8 more)

### Community 53 - "Decisions"
Cohesion: 0.12
Nodes (16): 10. Make audit writes typed, correlated and sanitized, 11. Preserve and extend the foundation verification strategy, 1. Separate domain, application, infrastructure and presentation, 2. Add forward-only Flyway migrations with PostgreSQL-native integrity, 3. Register through one transactional use case, 4. Use Spring Security resource-server JWT support with persisted-principal resolution, 5. Restore deliberate user authentication configuration, 6. Define an explicit public-route allowlist and deny by default (+8 more)

### Community 54 - "TipoMovimentacaoCaixa"
Cohesion: 0.21
Nodes (6): CashConflictException, CashOperationResult, MovimentacaoCaixa, TipoMovimentacaoCaixa, DEPOSITO, SAQUE

### Community 55 - "UsuarioJpaEntity"
Cohesion: 0.22
Nodes (4): Override, Entity, Table, UsuarioJpaEntity

### Community 56 - "CashPersistenceAdapter"
Cohesion: 0.23
Nodes (4): CashPersistenceAdapter, Override, java.sql.ResultSet, org.springframework.jdbc.core.JdbcTemplate

### Community 57 - "AssetCatalogPersistenceIT"
Cohesion: 0.25
Nodes (4): AssetCatalogPersistenceIT, AtivoQuery, SqlAction, FunctionalInterface

### Community 58 - "AtivoPersistenceAdapter.java"
Cohesion: 0.16
Nodes (5): AtivoNotFoundException, DuplicateTickerException, AtivoPersistenceAdapterTest, DataIntegrityViolationException, org.springframework.dao.DataIntegrityViolationException

### Community 59 - "TipoEvento"
Cohesion: 0.14
Nodes (14): TipoEvento, ACESSO_NEGADO, ADMIN_INICIAL_CRIADO, CADASTRO_USUARIO, CORRETORA_ATIVADA, CORRETORA_COMPLIANCE_REJEITADA, CORRETORA_CRIADA, CORRETORA_DESATIVADA (+6 more)

### Community 62 - "BrapiQuoteAdapter.java"
Cohesion: 0.32
Nodes (5): BrapiClient, BrapiQuoteAdapter, BrapiResponse, BrapiResult, Override

### Community 63 - "AtivoController"
Cohesion: 0.27
Nodes (3): AtivoController, AtivoResponse, org.springframework.web.bind.annotation.PatchMapping

### Community 64 - "PersistedUserJwtAuthenticationConverterTest"
Cohesion: 0.27
Nodes (4): PersistedUserJwtAuthenticationConverterTest, PersistedUserJwtAuthenticationConverterTest.AdminProbeController, PersistedUserJwtAuthenticationConverterTest.ProtectedProbeController, PersistedUserJwtAuthenticationConverterTest.SecurityTestConfiguration

### Community 65 - "CashMovementSchemaIT"
Cohesion: 0.35
Nodes (3): CashMovementSchemaIT, java.sql.Connection, org.junit.jupiter.api.BeforeAll

### Community 66 - "org.springframework.web.bind.annotation.RestController"
Cohesion: 0.22
Nodes (7): Authentication, LoginController, RegistrationController, AdminProbeController, ProtectedProbeController, org.springframework.web.bind.annotation.RequestMapping, org.springframework.web.bind.annotation.RestController

### Community 67 - "Wallet"
Cohesion: 0.35
Nodes (5): ExistingReservation, NewReservation, Reservation, Wallet, CashMovementApplicationServiceTest

### Community 68 - "LogAuditoriaJpaEntity"
Cohesion: 0.21
Nodes (3): LogAuditoriaJpaEntity, jakarta.persistence.Entity, jakarta.persistence.Table

### Community 69 - "SecurityProblemDetailHandler"
Cohesion: 0.29
Nodes (7): Override, SecurityProblemDetailHandler, jakarta.servlet.http.HttpServletResponse, org.springframework.security.access.AccessDeniedException, org.springframework.security.core.AuthenticationException, org.springframework.security.web.access.AccessDeniedHandler, org.springframework.security.web.AuthenticationEntryPoint

### Community 70 - "BrokerProviderAdaptersIT"
Cohesion: 0.26
Nodes (7): AfterAll, BrokerProviderAdaptersIT, MockWebServer, org.springframework.core.env.Environment, org.springframework.test.context.DynamicPropertyRegistry, org.springframework.test.context.DynamicPropertySource, org.testcontainers.containers.PostgreSQLContainer

### Community 71 - "AuditSecurityEventsIntegrationTest.java"
Cohesion: 0.24
Nodes (6): CorrelationIdFilter, Override, ch.qos.logback.classic.Logger, jakarta.servlet.FilterChain, org.springframework.core.annotation.Order, org.springframework.web.filter.OncePerRequestFilter

### Community 73 - "Requirement: Documentacao HTTP e erros padronizados"
Cohesion: 0.17
Nodes (11): MODIFIED Requirements, Requirement: Documentacao HTTP e erros padronizados, Requirement: Seguranca temporariamente permissiva, Scenario: Acesso anonimo fora da lista publica, Scenario: Acesso tecnico apos identidade, Scenario: Consulta do Swagger, Scenario: Erro tratado pela aplicacao, Scenario: Falha de autenticacao (+3 more)

### Community 74 - "AuditoriaCommand"
Cohesion: 0.33
Nodes (3): AuditoriaCommand, AccessDeniedAuditingServiceTest, MockHttpServletRequest

### Community 75 - "org.springframework.boot.context.properties.ConfigurationProperties"
Cohesion: 0.25
Nodes (6): AdminProperties, BrokerProviderProperties, Provider, jakarta.validation.constraints.AssertTrue, org.springframework.boot.context.properties.ConfigurationProperties, org.springframework.validation.annotation.Validated

### Community 76 - "IdentityPersistenceAdapter"
Cohesion: 0.27
Nodes (3): CarteiraJpaRepository, IdentityPersistenceAdapter, Override

### Community 77 - "PersistedUserJwtAuthenticationConverter"
Cohesion: 0.31
Nodes (6): Override, PersistedUserJwtAuthenticationConverter, InvalidBearerTokenException, org.springframework.core.convert.converter.Converter, org.springframework.security.authentication.AbstractAuthenticationToken, org.springframework.security.oauth2.server.resource.InvalidBearerTokenException

### Community 78 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 79 - "CurrentPrincipalService"
Cohesion: 0.29
Nodes (4): CurrentPrincipalService, CurrentPrincipalController, OpenApiConfigurationTest, io.swagger.v3.oas.annotations.security.SecurityRequirement

### Community 80 - "CarteiraJpaEntity"
Cohesion: 0.20
Nodes (3): CarteiraJpaEntity, Entity, Table

### Community 81 - ".register"
Cohesion: 0.24
Nodes (3): RegisteredUserResponse, RegisterRequest, CorrelationProbeController

### Community 82 - "tasks.md"
Cohesion: 0.20
Nodes (9): 1. Dependencias e configuracao segura, 2. Schema PostgreSQL governado por Flyway, 3. Dominio, application e adapters de persistencia, 4. Auditoria tecnica sanitizada, 5. Cadastro e carteira principal, 6. Login, JWT e principal atual, 7. Autorizacao e contratos de erro, 8. Administrador inicial (+1 more)

### Community 84 - "org.springframework.cloud.openfeign.FeignClient"
Cohesion: 0.36
Nodes (5): BrasilApiCnpjAdapter, BrasilApiCnpjClient, CnpjProviderResponse, Override, org.springframework.cloud.openfeign.FeignClient

### Community 85 - "org.springframework.web.bind.annotation.PostMapping"
Cohesion: 0.25
Nodes (3): LoginRequest, LoginResponse, org.springframework.web.bind.annotation.PostMapping

### Community 87 - "scripts"
Cohesion: 0.22
Nodes (9): scripts, build, dev, lint, start, test, test:e2e, test:watch (+1 more)

### Community 89 - "Project Foundation Proposal"
Cohesion: 0.36
Nodes (8): Project Foundation Design, Project Foundation Proposal, Backend Foundation Delta Specification, Containerized Local Environment Delta Specification, Frontend Foundation Delta Specification, Project Foundation Implementation Tasks, Containerized Local Environment Specification, Frontend Foundation Specification

### Community 90 - "AuditoriaPersistenceAdapter"
Cohesion: 0.43
Nodes (3): AuditoriaPersistenceAdapter, Override, LogAuditoriaJpaRepository

### Community 91 - "UsuarioJpaRepository"
Cohesion: 0.33
Nodes (4): RepositoryUserDetailsService, UsuarioJpaRepository, UserDetails, UserDetailsService

### Community 93 - "proposal.md"
Cohesion: 0.29
Nodes (6): Capabilities, Impact, Modified Capabilities, New Capabilities, What Changes, Why

### Community 94 - "OpenSpec Apply Change Workflow"
Cohesion: 0.40
Nodes (6): OpenSpec Apply Change Workflow, OpenSpec Archive Change Workflow, OpenSpec Explore Mode, OpenSpec Propose Change Workflow, OpenSpec Sync Specs Workflow, OpenSpec Update Change Workflow

### Community 96 - "usuarios"
Cohesion: 0.33
Nodes (3): usuarios, carteiras, logs_auditoria

### Community 97 - "Fakes"
Cohesion: 0.60
Nodes (3): Fakes, Bean, Primary

### Community 98 - ".failingAuditoriaPort"
Cohesion: 0.53
Nodes (3): FaultInjectionConfiguration, FaultSwitches, org.springframework.context.annotation.Primary

### Community 100 - "Q: Localizar impactos existentes para estabelecer identidade e controle de acesso"
Cohesion: 0.40
Nodes (4): Answer, Outcome, Q: Localizar impactos existentes para estabelecer identidade e controle de acesso, Source Nodes

### Community 101 - "Spec-Driven Development Policy"
Cohesion: 0.50
Nodes (4): Codex Project Instructions, Spec-Driven Development Policy, Archived Foundation Change Metadata, OpenSpec Spec-Driven Schema Configuration

### Community 102 - "frontend/package.json"
Cohesion: 0.50
Nodes (3): name, private, version

## Knowledge Gaps
- **329 isolated node(s):** `AuthConfig`, `AuthEnv`, `BackendErrorKind`, `AuthProblem`, `Provider` (+324 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 499 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **42 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Corretora` connect `Corretora` to `org.springframework.security.access.prepost.PreAuthorize`, `BrokerApplicationService`, `org.springframework.boot.test.context.SpringBootTest`, `CorretoraJpaEntity`, `BrokerCatalogPersistenceIT`, `BrokerCatalogSecurityIT`, `.criar`, `.nova`?**
  _High betweenness centrality (0.028) - this node is a cross-community bridge._
- **Why does `Usuario` connect `Usuario` to `PersistedUserJwtAuthenticationConverterTest`, `org.springframework.web.bind.annotation.RestController`, `AuditSecurityEventsIntegrationTest`, `UsuarioPort`, `AuditSecurityEventsIntegrationTest.java`, `Role`, `PostgreSqlContainerSupport`, `IdentityPersistenceAdapter`, `PersistedUserJwtAuthenticationConverter`, `CurrentPrincipalService`, `.register`, `.register`, `Carteira`, `UsuarioJpaEntity`, `org.springframework.test.web.servlet.MockMvc`, `Fakes`?**
  _High betweenness centrality (0.024) - this node is a cross-community bridge._
- **Why does `SeveridadeAuditoria` connect `ResultadoAuditoria` to `AuditoriaCommand`, `AuditSecurityEventsIntegrationTest`, `LogAuditoriaJpaEntity`, `AuditSecurityEventsIntegrationTest.java`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **What connects `AuthConfig`, `AuthEnv`, `BackendErrorKind` to the rest of the system?**
  _329 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `types.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.06772151898734177 - nodes in this community are weakly interconnected._
- **Should `org.springframework.security.access.prepost.PreAuthorize` be split into smaller, more focused modules?**
  _Cohesion score 0.05754385964912281 - nodes in this community are weakly interconnected._
- **Should `auth.ts` be split into smaller, more focused modules?**
  _Cohesion score 0.06349206349206349 - nodes in this community are weakly interconnected._