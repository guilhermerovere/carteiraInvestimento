# asset-catalog Specification

## Purpose

Define o catálogo global de instrumentos financeiros com identidade canônica, lifecycle preservável e contratos backend estáveis para os módulos financeiros futuros.

## Requirements

### Requirement: Ativo global canônico e protegido
O sistema SHALL manter `Ativo` global sem usuário, carteira, quantidade, saldo, preço, cotação, corretora ou patrimônio. Cada ativo SHALL expor id UUID, ticker, nome, tipo, mercado, moeda, ativo, criadoEm e atualizadoEm. A aplicação SHALL executar trim, uppercase e validação do ticker antes de busca ou persistência. PostgreSQL SHALL rejeitar ticker diferente de `UPPER(TRIM(ticker))`, formato inválido para seu mercado, nome blank e violação da constraint única nomeada `uk_acoes_ticker`.

#### Scenario: Ticker canônico criado
- **WHEN** um administrador cria um ativo com ticker válido em minúsculas ou com espaços externos
- **THEN** o ativo é persistido exatamente com ticker trimado e em uppercase

#### Scenario: Escrita direta não canônica
- **WHEN** uma escrita direta tenta persistir ticker minúsculo ou com espaços externos
- **THEN** PostgreSQL rejeita a escrita

#### Scenario: Duplicidade concorrente
- **WHEN** duas operações concorrentes criam o mesmo ticker canônico
- **THEN** somente um ativo é persistido e a violação de `uk_acoes_ticker` é traduzida para `409 Conflict`

### Requirement: Valores controlados, formato e nome
O sistema SHALL admitir somente tipo `ACAO`, `FII` ou `ETF`, mercado `B3` ou `US`, e moeda `BRL` ou `USD`. B3 SHALL derivar BRL e ticker `^[A-Z]{4}[0-9]{1,2}$`; US SHALL derivar USD e ticker `^[A-Z]{1,5}$`. Os formatos são deliberadamente limitados a esta capability. Nome SHALL ser trimado, não blank e ter de 1 a 160 caracteres após normalização.

#### Scenario: Mercado determina moeda
- **WHEN** um administrador cria ativo B3 ou US válido
- **THEN** o ativo recebe respectivamente BRL ou USD sem moeda fornecida pelo cliente

#### Scenario: Formato ou nome inválido
- **WHEN** ticker não satisfaz o padrão do mercado ou nome é vazio, apenas espaços ou maior que 160 após trim
- **THEN** a operação é rejeitada com `400 Bad Request` sem persistência

#### Scenario: Formato inválido direto no banco
- **WHEN** escrita direta combina mercado B3 ou US com ticker fora de seu padrão
- **THEN** PostgreSQL rejeita a escrita

### Requirement: Requests estritos, identidade imutável e lifecycle
POST `/api/v1/acoes` SHALL document and accept exactly one role-specific body using explicit OpenAPI `oneOf` schemas: ADMIN sends `ticker`, `nome`, `tipo`, `mercado` under the existing contract; USER sends only `ticker`, `mercado` and receives name/type/currency/branding from server-side enrichment. The USER body MUST NOT accept ADMIN fields even though the ADMIN schema exists on the same endpoint. Neither body may submit `moeda`, `ativo`, `logo`, `logoUrl`, `logoProvider`, `logoReference`, provider, UUID, or arbitrary metadata. PATCH `/api/v1/acoes/{id}` SHALL accept exactly `nome` and remain ADMIN-only. PATCH `/api/v1/acoes/{id}/ativo` SHALL accept exactly `{ "ativo": true|false }`, define the requested state rather than toggle, be idempotent, and remain ADMIN-only. Ticker, type, market, and currency SHALL remain immutable; physical DELETE MUST NOT exist. Unknown or role-inappropriate fields SHALL return sanitized `400 Bad Request`.

#### Scenario: Existing administrative create contract
- **WHEN** an ADMIN creates an asset using the existing ticker/name/type/market request
- **THEN** the system preserves the established administrative behavior and returns the canonical asset

#### Scenario: Controlled user create request
- **WHEN** a USER creates an asset using ticker and market only
- **THEN** the system obtains all other asset metadata from trusted server-side enrichment and does not trust client metadata

#### Scenario: User supplies visual or administrative metadata
- **WHEN** a USER request includes name, type, currency, active status, logo, provider, or an unknown field
- **THEN** the request is rejected with sanitized 400 and no asset is persisted

#### Scenario: Campo estrutural em PATCH de nome
- **WHEN** PATCH de nome inclui `ticker` ou qualquer campo não permitido
- **THEN** o sistema responde 400 e não altera o ativo

#### Scenario: Lifecycle idempotente
- **WHEN** administrador envia o mesmo valor `ativo` já persistido
- **THEN** o sistema responde 200 com a representação atual, mantendo o estado

### Requirement: Respostas de mutação
ADMIN asset POST and both ADMIN PATCH routes SHALL preserve their existing statuses (POST 201, PATCH 200) and ADMIN POST response contract. USER asset POST SHALL return 201 for a newly created canonical asset, 200 with the canonical active `AtivoResponse` when trusted provider resolution maps a historical requested ticker to an already-existing current ticker, and a recoverable 409 for an exact canonical duplicate/race or inactive canonical asset. Responses SHALL contain the existing `AtivoResponse` fields and optional system-owned `logoProvider` and `logoReference`; these fields MUST NOT contain credentials or keys and MUST NOT be accepted in a request. A USER duplicate conflict SHALL be safely resolvable through exact active catalog lookup without creating a second asset. OpenAPI SHALL document both request schemas, role restrictions, forbidden cross-shape fields, statuses, and response fields.

#### Scenario: Mutação bem-sucedida
- **WHEN** administrador cria, altera nome ou define lifecycle válido
- **THEN** a resposta tem o status previsto e somente os campos de `AtivoResponse`

#### Scenario: Mutation success by role
- **WHEN** ADMIN creates/updates an authorized field or USER creates a valid new asset
- **THEN** the response uses the role-appropriate status and canonical asset metadata

#### Scenario: Duplicate registration is recoverable
- **WHEN** a USER registration conflicts with an already-existing canonical ticker
- **THEN** it returns a recoverable conflict and the client can query and select the existing active record

#### Scenario: Renamed ticker resolves to existing canonical asset
- **WHEN** provider resolution maps the requested historical ticker to an already-existing active canonical ticker
- **THEN** POST returns that canonical asset without creating an alias or duplicate and the UI explains the rename by comparing requested and returned ticker

### Requirement: Listagem paginada controlada
GET `/api/v1/acoes` SHALL retornar envelope estável `{items, page, size, totalElements, totalPages}`. Defaults são page 0 e size 20; `page >= 0` e `1 <= size <= 100`. Aceita somente `q`, `tipo`, `ativo` administrativo, `sort` ticker/nome e `direction` asc/desc, com ticker asc padrão. `q` SHALL ser trimado, case-insensitive e contains em ticker ou nome; vazio após trim equivale a ausência de filtro.

#### Scenario: Busca simples por q
- **WHEN** lista recebe `q` parcial, com caixa ou espaços externos
- **THEN** localiza ticker ou nome correspondentes por contains case-insensitive

#### Scenario: Paginação ou ordenação inválida
- **WHEN** page é negativo, size é 0 ou maior que 100, ou sort/direction não é permitido
- **THEN** o sistema responde 400 com ProblemDetail sanitizado

### Requirement: Visibilidade e consulta por ticker
GET lista e GET ticker SHALL permitir ROLE_USER e ROLE_ADMIN. User SHALL receber somente ativos ativos na lista, ignorando tentativa de `ativo=false`, e 404 para ticker inativo. Admin sem filtro ativo SHALL ver ativos e inativos; com `ativo=true|false` SHALL receber o filtro; e poderá consultar ticker inativo. A visibilidade SHALL ser decidida antes da persistência sem introduzir dependência de Spring Security no domínio.

#### Scenario: User não amplia visibilidade
- **WHEN** ROLE_USER lista com `ativo=false` ou busca ticker inativo
- **THEN** a lista contém somente ativos e a busca responde 404

#### Scenario: Admin consulta catálogo completo
- **WHEN** ROLE_ADMIN lista sem filtro ou consulta ticker inativo
- **THEN** obtém ativos ativos e inativos conforme o contrato

### Requirement: Autorização, erros e persistência
Asset list/get SHALL allow ROLE_USER and ROLE_ADMIN while preserving active-only visibility for USER and administrative visibility for ADMIN. POST `/api/v1/acoes` SHALL allow USER only for the strict ticker/market registration path and ADMIN for the existing administrative create path. Name edits, lifecycle changes, and all general administrative CRUD SHALL remain ADMIN-only; USER MUST NOT edit, delete, activate, deactivate, or change metadata on an existing asset. Anonymous calls SHALL receive 401 and unauthorized mutations 403. Nonexistent records SHALL return 404; invalid input 400; only `uk_acoes_ticker` duplication 409. A USER create MUST validate the asset financially before persistence, but branding lookup failure alone MUST NOT fail creation. All errors SHALL use sanitized ProblemDetail and `X-Correlation-ID`. The `acoes` table SHALL remain Flyway-managed and ORM-validated; non-ticker constraints MUST NOT be translated as duplicate conflicts. Authorized catalog CRUD SHALL NOT add operational audit events, preserving existing denied-access audit behavior.

#### Scenario: USER can register but not administer
- **WHEN** a USER creates a valid new asset and then attempts to edit its name or lifecycle
- **THEN** creation is allowed and administrative edits remain forbidden

#### Scenario: ADMIN catalog authority is preserved
- **WHEN** an ADMIN performs general catalog maintenance
- **THEN** existing ADMIN catalog capabilities remain available and are not granted to USER

#### Scenario: Duplicate constraint is concurrency-safe
- **WHEN** two users concurrently register the same canonical ticker
- **THEN** the database unique constraint allows one row only and the losing request receives a recoverable duplicate result

#### Scenario: Outra constraint não vira conflito
- **WHEN** persistência falha por CHECK ou constraint diferente de `uk_acoes_ticker`
- **THEN** a falha não é traduzida como 409 de ticker duplicado

### Requirement: Controlled canonical user registration and automatic metadata
ROLE_USER registration SHALL add an asset to the shared global catalog, not create a Position, Transaction, BUY, cash mutation, or user-owned copy. The registration input SHALL consist only of ticker and market. The backend SHALL apply the existing domain normalization (trim and uppercase), validate the market-specific ticker pattern, validate existence through the existing market provider chain, derive market currency and asset type, and use provider company/security metadata for the canonical name when available. The only supported types are the existing `TipoAtivo` enum values `ACAO`, `FII`, and `ETF`; Brapi subtypes `stock`, `fii`, and `etf` map respectively to those values. US Alpha Vantage symbol metadata MUST yield an exact symbol and a supported Equity or ETF type, mapped to `ACAO` or `ETF`; mutual funds and all unmapped provider types SHALL be rejected. A provider-recognized instrument that cannot be mapped to a supported type SHALL NOT be persisted and SHALL return 400 under current conventions. The current global `uk_acoes_ticker` constraint SHALL remain the final identity authority; market MUST also match the provider's result. If a provider does not supply a usable name, the stored display name SHALL fall back to the canonical ticker rather than invented company information. The registration path MUST NOT reactivate an inactive canonical ticker. External provider validation/enrichment SHALL happen before the short persistence transaction; canonical normalization and database uniqueness remain the final race-safe checks.

#### Scenario: New B3 asset
- **WHEN** a USER submits an unregistered valid B3 ticker and Brapi confirms it
- **THEN** the system persists one global asset with B3/BRL and provider-derived metadata when available

#### Scenario: New US asset
- **WHEN** a USER submits an unregistered valid US ticker and the existing US provider chain confirms it
- **THEN** the system persists one global asset with US/USD and provider-derived metadata when available

#### Scenario: Market or ticker is invalid or unknown
- **WHEN** ticker syntax is invalid, the provider confirms no matching instrument, or returned market/currency metadata conflicts with the request
- **THEN** the system returns the status already established by the asset/provider architecture and persists nothing

#### Scenario: Asset is shared across users
- **WHEN** a second USER searches for a ticker created by another USER
- **THEN** the second user finds the same canonical UUID and no user-specific duplicate exists

#### Scenario: Asset exists but is inactive
- **WHEN** a USER attempts to register an inactive canonical ticker
- **THEN** no duplicate is created and the USER cannot reactivate or select it for BUY

#### Scenario: Provider recognizes unsupported instrument type
- **WHEN** a provider recognizes a security but its type is not one of `ACAO`, `FII`, or `ETF`
- **THEN** the system rejects registration with 400, persists nothing, and does not imply support for that instrument

#### Scenario: Brapi subtype mapping
- **WHEN** Brapi metadata identifies a B3 subtype `stock`, `fii`, or `etf`
- **THEN** the backend maps it only to the corresponding supported `ACAO`, `FII`, or `ETF` domain type

#### Scenario: US symbol type mapping
- **WHEN** Alpha Vantage exact symbol metadata identifies a supported US Equity or ETF
- **THEN** the backend maps it to `ACAO` or `ETF`; mutual fund or unknown type is rejected without persistence

#### Scenario: Provider resolves renamed B3 ticker
- **WHEN** Brapi returns `changed=true` with a requested symbol and a different current `symbol`
- **THEN** the backend normalizes and validates only the current symbol, checks global uniqueness for that symbol, and creates or reuses only the canonical record without an alias

#### Scenario: US provider explicitly resolves a renamed ticker
- **WHEN** an authoritative US financial metadata provider explicitly reports a different current symbol for the requested ticker
- **THEN** the backend applies the same canonicalization, uniqueness, reuse, and no-alias rules; it does not infer a rename when the provider supplies none

#### Scenario: Renamed ticker races with canonical registration
- **WHEN** another request creates the provider-returned current symbol during registration
- **THEN** the database unique constraint prevents a duplicate and the service reloads/reuses the current active canonical asset or reports a recoverable inactive/duplicate outcome

### Requirement: Automatic optional asset branding
Asset branding SHALL be system-resolved and optional. Brapi SHALL be the priority for B3 company metadata and its public `logourl`; persist it as `logoProvider=BRAPI` plus `logoReference` only after HTTPS and exact trusted-host validation. For US equities, Alpha Vantage remains a financial validation/quote provider and MUST NOT be used as the main browser visual-logo delivery endpoint; Logo.dev Stock Ticker Logo CDN SHALL be used when its supported exchange identifier resolves, represented as `logoProvider=LOGO_DEV` and a normalized ticker reference `ticker/<symbol>[.<exchange>]`. `LOGO_DEV_PUBLISHABLE_KEY` MAY be client-visible only in official `img.logo.dev` image requests; Brapi and Alpha Vantage credentials remain server-side. Branding resolution SHALL be separate from financial validation and SHALL NOT block a financially valid registration if a logo is absent or branding fails. Persist only system-owned `logoProvider`/`logoReference`; catalog reads SHALL reuse it and MUST NOT call branding lookup once per rendered asset. Requests MUST reject branding metadata and credentials.

#### Scenario: Financial asset valid but logo missing
- **WHEN** the market provider confirms a valid asset but the branding response is empty, unavailable, or malformed
- **THEN** the asset is created with no logo reference and no financial validation failure

#### Scenario: Branding reference is persisted
- **WHEN** a trusted provider resolves a logo for an asset
- **THEN** the system persists the provider and provider-specific canonical reference for reuse, not a client-supplied URL, publishable key, or secret credential

#### Scenario: B3 logo is missing
- **WHEN** Brapi confirms a valid B3 security but provides no usable `logourl`
- **THEN** the asset is still created without a logo reference and the frontend uses its monogram fallback

#### Scenario: US ticker logo is requested
- **WHEN** a valid US asset has a Logo.dev-supported ticker/exchange identifier
- **THEN** EntityLogo uses the official Logo.dev ticker CDN and publishable key for image delivery while financial provider keys remain server-side

#### Scenario: Existing catalog is listed
- **WHEN** a page of assets is read
- **THEN** stored branding references are returned without external per-item lookup

### Requirement: Administrative asset presentation reuses canonical contracts
The ROLE_ADMIN frontend SHALL list assets and create them with exactly ticker, name, type and market; it SHALL edit only name and set lifecycle through the existing idempotent `ativo` request. Structural identity fields SHALL remain read-only after creation, physical DELETE and manual branding inputs MUST NOT appear, and EntityLogo SHALL consume only system-owned branding with fallback.

#### Scenario: Admin maintains an asset
- **WHEN** ROLE_ADMIN creates, renames, activates or deactivates an asset in the administrative UI
- **THEN** the UI submits only the existing role-appropriate DTO fields and presents a friendly pt-BR result without raw JSON

### Requirement: Provider-backed user asset discovery and controlled registration
Every `ROLE_USER` SHALL discover supported B3 and US instruments by ticker or issuer name and register a valid canonical asset using only `ticker` and `mercado`. Discovery responses SHALL be normalized, provider metadata SHALL remain server-derived, and registration SHALL NOT create a position, transaction or cash movement. Discovery, validation and quotation failures SHALL be independently classified so a missing optional Brapi token does not prevent a provider-supported public B3 search.

#### Scenario: User finds and adds a B3 asset
- **WHEN** a user searches for `Banco do Brasil` or `BBAS3`
- **THEN** the result provides canonical ticker, provider-derived name, supported type, market, currency, optional logo and current price
- **AND** adding it sends only ticker and market
- **AND** persisted catalog metadata is validated and canonical.
