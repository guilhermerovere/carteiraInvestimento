## MODIFIED Requirements

### Requirement: Controlled canonical user registration and automatic metadata
ROLE_USER registration SHALL add an asset to the shared global catalog, not create a Position, Transaction, BUY, cash mutation, or user-owned copy. The registration input SHALL consist only of ticker and market. The backend SHALL apply the existing domain normalization (trim and uppercase), validate the market-specific ticker pattern, validate existence through the existing market provider chain, derive market currency and asset type, and use provider company/security metadata for the canonical name when available. For B3, `/api/v2/tickers` SHALL be the identity and classification authority and `/api/v2/stocks/quote` SHALL be the quotation authority; a missing `type` in the quote payload MUST NOT trigger ticker-pattern classification. The only supported types are the existing `TipoAtivo` enum values `ACAO`, `FII`, and `ETF`; Brapi subtypes `unit` and `stock` SHALL map to `ACAO`, `fii` SHALL map to `FII`, and `etf` SHALL map to `ETF`. Exact symbol metadata from the existing US provider chain MUST yield a supported Equity or ETF type, mapped to `ACAO` or `ETF`; mutual funds and all unmapped provider types SHALL be rejected without changing the PRD-defined US provider selection in this change. A provider-recognized instrument that cannot be mapped to a supported type SHALL NOT be persisted and SHALL return 400 under current conventions. The current global `uk_acoes_ticker` constraint SHALL remain the final identity authority; market MUST also match the provider's result. B3 identity and quote results MUST agree on the canonical ticker and B3/BRL context before persistence; disagreement, missing required identity data, or missing required quote data SHALL fail safely and persist nothing. If a provider does not supply a usable name, the stored display name SHALL fall back to the canonical ticker rather than invented company information. The registration path MUST NOT reactivate an inactive canonical ticker. External provider validation/enrichment SHALL happen before the short persistence transaction; canonical normalization and database uniqueness remain the final race-safe checks.

#### Scenario: New B3 asset
- **WHEN** a USER submits an unregistered valid B3 ticker and Brapi identity and quote responses agree on its canonical B3/BRL identity
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
- **WHEN** Brapi identity metadata recognizes a security but supplies a subtype other than `unit`, `stock`, `fii`, or `etf`
- **THEN** the system rejects registration with 400, persists nothing, and does not infer a type from the ticker or quote payload

#### Scenario: Brapi subtype mapping
- **WHEN** Brapi identity metadata identifies a B3 subtype as `unit`, `stock`, `fii`, or `etf`
- **THEN** the backend maps it only to the corresponding supported `ACAO`, `ACAO`, `FII`, or `ETF` domain type

#### Scenario: Real B3 quote payload omits type
- **WHEN** quote payloads without `type` are returned for SANB11, HGLG11, BOVA11, and PETR4 while identity metadata classifies them respectively as `unit`, `fii`, `etf`, and `stock`
- **THEN** the system classifies them respectively as `ACAO`, `FII`, `ETF`, and `ACAO` and does not use the absent quote field or ticker suffix as a substitute

#### Scenario: Current Brapi identity envelope
- **WHEN** `/api/v2/tickers` returns the current `results[]` envelope with `symbol`, `subType`, B3, and BRL metadata for SANB11
- **THEN** the backend normalizes it to the same canonical identity contract as the supported legacy envelope and classifies SANB11 as `ACAO` before requesting its quote

#### Scenario: B3 identity and quote diverge
- **WHEN** the identity result and quote result disagree on canonical ticker or the quote does not establish BRL quotation for the B3 instrument
- **THEN** validation fails safely, no catalog record is inserted or changed, and no preview can be confirmed

#### Scenario: US symbol type mapping
- **WHEN** exact symbol metadata from the existing US provider chain identifies a supported US Equity or ETF
- **THEN** the backend maps it to `ACAO` or `ETF`; mutual fund or unknown type is rejected without persistence

#### Scenario: Provider resolves renamed B3 ticker
- **WHEN** Brapi resolves a requested historical ticker to a different current ticker and both B3 identity and quote results agree on that current ticker
- **THEN** the backend normalizes and validates only the current symbol, checks global uniqueness for that symbol, and creates or reuses only the canonical record without an alias

#### Scenario: US provider explicitly resolves a renamed ticker
- **WHEN** an authoritative US financial metadata provider explicitly reports a different current symbol for the requested ticker
- **THEN** the backend applies the same canonicalization, uniqueness, reuse, and no-alias rules; it does not infer a rename when the provider supplies none

#### Scenario: Renamed ticker races with canonical registration
- **WHEN** another request creates the provider-returned current symbol during registration
- **THEN** the database unique constraint prevents a duplicate and the service reloads/reuses the current active canonical asset or reports a recoverable inactive/duplicate outcome

### Requirement: Provider-backed user asset discovery and controlled registration
Every `ROLE_USER` SHALL discover supported B3 and US instruments by ticker or issuer name and register a valid canonical asset using only `ticker` and `mercado`. Before registration, discovery SHALL provide a non-persistent preview containing canonical `ticker`, provider-derived `nome`, supported `tipo`, `mercado`, `moeda`, optional system-owned `logoProvider` and `logoReference`, and `cotacaoAtual`. For B3 previews, identity and classification SHALL come from `/api/v2/tickers`, while current price and quote currency SHALL come from `/api/v2/stocks/quote`. The backend discovery contract SHALL return only normalized preview fields and SHALL NOT persist or mutate a catalog record. The BFF SHALL validate and forward only those allowlisted fields without deriving or rewriting identity/classification. The frontend SHALL display the preview before an explicit registration confirmation; confirmation SHALL send only the canonical preview `ticker` and `mercado` to the existing controlled registration operation. Registration SHALL NOT create a position, transaction or cash movement. Discovery, identity validation, quotation, and optional branding failures SHALL remain independently classified; required identity or quotation failure SHALL block confirmation, while missing optional branding SHALL use the existing visual fallback.

#### Scenario: User finds and adds a B3 asset
- **WHEN** a user searches for an uncatalogued supported B3 instrument and identity and quote sources agree
- **THEN** the UI presents canonical ticker, name, type, B3, BRL, optional logo, and current price without persisting the asset
- **AND** the registration action is available only after that preview
- **AND** confirming it sends only canonical ticker and market and persists validated canonical metadata

#### Scenario: Preview does not mutate the catalog
- **WHEN** a user loads or abandons an asset preview
- **THEN** no asset, position, transaction, quote history, or cash movement is created or changed

#### Scenario: Required preview sources fail or diverge
- **WHEN** B3 identity or quotation is unavailable, malformed, unsupported, or inconsistent with the other required source
- **THEN** the preview exposes a recoverable sanitized failure, does not offer confirmation, and persists nothing

#### Scenario: BFF preserves the preview trust boundary
- **WHEN** the backend returns a valid enriched preview
- **THEN** the BFF exposes only canonical ticker, name, supported type, market, currency, optional system-owned branding, and a lossless current-price value
- **AND** provider credentials, raw payload fields, and client-supplied metadata are not exposed or accepted

#### Scenario: Optional B3 branding is absent
- **WHEN** identity and quotation are valid but the B3 preview has no trusted logo
- **THEN** the preview remains confirmable and the frontend displays the existing monogram fallback
