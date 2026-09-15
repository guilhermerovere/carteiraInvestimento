## Purpose

Define the authenticated Valore experience for preparing, reviewing, confirming, and recovering private cash and investment operations while preserving exact financial values and backend authority.

## ADDED Requirements

### Requirement: Authenticated operation surface
The frontend SHALL expose deposit, withdraw, and BUY through an `Operar` launcher in the `ROLE_USER` portfolio shell. SELL SHALL start only from an open position. `ROLE_ADMIN` SHALL retain the administrative area without a personal wallet or financial operation launcher. The browser SHALL call same-origin `/api/finance/*` routes only; JWT, `Authorization`, `BACKEND_API_URL`, financial provider keys, and provider secret credentials MUST remain server-side. A Logo.dev publishable key MAY be browser-visible only when used in an image request to the official `img.logo.dev` CDN.

#### Scenario: User opens the operation launcher
- **WHEN** a confirmed `ROLE_USER` opens `Operar`
- **THEN** the user can start Comprar, Depositar, or Sacar, while SELL remains contextual to an open position

#### Scenario: Administrator opens the application
- **WHEN** a confirmed `ROLE_ADMIN` navigates the application
- **THEN** the administrator remains outside personal portfolio operations and sees no wallet mutation controls

#### Scenario: Browser financial request
- **WHEN** the browser loads a catalog, quote, FX observation, or operation
- **THEN** it calls only a same-origin finance route and receives no backend bearer token, backend URL, financial/provider secret, or Logo.dev Search secret; a publishable Logo.dev key is used only by official CDN image delivery

### Requirement: Short operation container and required review
Every financial operation SHALL follow editable form → review → explicit confirmation → POST. Review SHALL display the fields, relevant estimate, and a clear Edit action. No mutation SHALL occur on the first form action. Desktop SHALL use a compact dialog or sheet; mobile SHALL use a high, near-full-screen sheet that supports scrolling, safe areas, keyboard visibility, back/cancel, review, and confirmation without hiding required content.

#### Scenario: Review before submission
- **WHEN** a user completes an operation form
- **THEN** the frontend presents a review with an Edit action and does not send a POST until the user confirms

#### Scenario: Mobile review with keyboard open
- **WHEN** a user reviews an operation on a mobile viewport with the software keyboard open
- **THEN** required fields, review content, and confirmation/cancel actions remain reachable without obscuring the active input

### Requirement: Asset selector and controlled catalog registration
The BUY asset selector SHALL remotely search the paginated active catalog using the supported `page`, `size`, `q`, `tipo`, `ativo`, `sort`, and `direction` parameters. Results SHALL identify ticker, name, market, currency, and an `EntityLogo`. Registration availability SHALL use an exact active canonical ticker lookup through `GET /api/v1/acoes/ticker/{ticker}` (via the BFF), not the presence or absence of fuzzy/partial `q` results. If that exact lookup returns no active ticker, the selector MAY offer controlled registration after local syntax validation; similar results do not suppress the offer. The frontend MUST NOT request or submit user-entered name, type, currency, price, provider, UUID, active status, logo, `logoUrl`, or arbitrary metadata. On successful registration it SHALL update only the asset catalog cache, select the returned canonical asset, and continue BUY without creating a position or transaction. If trusted Brapi validation resolves a historical B3 ticker to a current canonical ticker, the flow SHALL select only that canonical asset, explain the rename using requested and returned tickers, and never create an alias or second asset.

#### Scenario: Existing asset is selected
- **WHEN** a user searches for an existing active asset
- **THEN** the selector shows its identifying metadata and logo/fallback and selecting it continues BUY

#### Scenario: Exact active ticker takes precedence over similar results
- **WHEN** exact canonical ticker lookup finds an active ticker while fuzzy search also returns related assets
- **THEN** the exact canonical asset is selected/available as the match and registration is not offered for that ticker

#### Scenario: Similar results do not suppress registration
- **WHEN** fuzzy search returns related assets but exact canonical ticker lookup returns no active exact match
- **THEN** a syntactically valid ticker may still be offered for controlled registration

#### Scenario: Missing asset is registered in the BUY flow
- **WHEN** a user searches for an unlisted ticker and submits the compact ticker/market form
- **THEN** the system validates and enriches the asset, returns the canonical catalog record, selects it automatically, and resumes BUY preparation without creating a position, transaction, or cash movement

#### Scenario: Existing or concurrent duplicate
- **WHEN** registration conflicts with an asset already created by another user or request
- **THEN** the client performs an exact catalog lookup, selects the existing active canonical asset when available, and otherwise presents a recoverable unavailable state without retrying creation blindly

#### Scenario: Asset is inactive
- **WHEN** the canonical ticker exists but is inactive
- **THEN** the user cannot reactivate it or use it for BUY, and the flow does not create a duplicate

#### Scenario: Provider resolves a renamed ticker
- **WHEN** Brapi confirms a requested ticker now resolves to a different canonical symbol
- **THEN** only the normalized provider-returned symbol is created or reused, the UI explains the old-to-current ticker mapping, and no historical alias asset is created

### Requirement: Broker selector and unavailable-broker state
BUY and SELL SHALL provide a paginated selector for active brokers using only backend-supported `page` and `size`. Results SHALL show legal/trade name and automatic logo/fallback; local filtering SHALL apply only to loaded pages and MUST NOT imply global search. `ROLE_USER` MUST NOT receive broker creation or administration controls. If the active catalog is empty, asset registration SHALL remain available, but BUY/SELL confirmation SHALL be disabled with a clear no-active-broker message.

#### Scenario: User selects a broker
- **WHEN** active brokers are returned
- **THEN** the user selects one by recognizable name and logo, while the UUID remains a payload identifier rather than the primary label

#### Scenario: No active brokers exist
- **WHEN** the broker catalog contains no active broker
- **THEN** the user can still search/register an asset, cannot confirm BUY/SELL, and is told that no broker is available for operations

### Requirement: Cash operation forms and payloads
Deposit and withdrawal SHALL use the existing contracts `POST /api/v1/carteira/caixa/deposito` and `/api/v1/carteira/caixa/saque`, proxied through same-origin `/api/finance/cash/deposit` and `/api/finance/cash/withdraw`. Each body SHALL contain only positive BRL `valor` and optional `descricao`; caixa scale SHALL be at most two and fit `NUMERIC(18,2)` without rounding. `Idempotency-Key` SHALL be mandatory. The form and review SHALL show known cash balance and projected balance as an estimate. A locally detected withdrawal above known cash MAY warn but MUST leave final rejection to the backend.

#### Scenario: Deposit succeeds
- **WHEN** a user confirms a valid deposit
- **THEN** the BFF forwards the exact allowlisted body and key, and success displays the authoritative movement and `saldoResultante`

#### Scenario: Withdrawal exceeds known balance
- **WHEN** a user enters more than the last loaded balance
- **THEN** the UI labels the projected balance as an estimate and may warn, while still treating backend response as authoritative

#### Scenario: Cash amount has too many significant decimal places
- **WHEN** a cash amount exceeds two significant fractional digits or `NUMERIC(18,2)` capacity
- **THEN** the form/BFF rejects it without silent rounding or POST

### Requirement: BUY preparation and review
BUY SHALL let the user select an active asset, request its current quote as a suggestion, select an active broker, enter quantity, editable unit price, BRL fees, and local trade date/time, and review the result. For US assets, it SHALL load and display a valid USD/BRL observation and use only its `id` as `exchangeRateId`; B3 SHALL omit/null that identifier according to the backend contract. Review SHALL identify provider and freshness, currency, fees, estimated BRL total, current average cost when available, and estimated new average cost. Estimates MUST be labeled as estimates and MUST NOT be presented as accounting results.

#### Scenario: BUY B3
- **WHEN** a user prepares a B3 purchase
- **THEN** the review uses BRL quote context, editable unit price and fees, and submits no `exchangeRateId` or a null value as allowed by the existing contract

#### Scenario: BUY US
- **WHEN** a user prepares a US purchase
- **THEN** the review shows USD price and the current FX provider/freshness, estimates BRL cost, and the transaction body contains the persisted FX observation id but no numeric FX rate

#### Scenario: Quote is unavailable
- **WHEN** quote lookup returns 404 or 502
- **THEN** the form remains usable for a manual price when the backend permits it, and the UI explains that the quote is unavailable without claiming a provider refresh occurred

#### Scenario: FX expires during confirmation
- **WHEN** the backend returns structured code `FX_EXPIRED`
- **THEN** the form values remain, the UI requests a new FX observation, recalculates estimates, returns to review, and requires confirmation under a new idempotency key

### Requirement: Contextual SELL and inactive held assets
SELL SHALL start only from an open position and SHALL lock the selected asset identity in the operation form. It SHALL use the position response's asset name, ticker, market, currency, lifecycle status, and available branding reference rather than requiring the public active catalog. It SHALL show available quantity and average cost, request an optional quote, permit manual price when quote is unavailable, and require an active broker, quantity, fees, and local trade date/time. The UI MAY warn on an oversell against known quantity. Partial SELL SHALL show unchanged remaining average cost; total SELL estimates SHALL use the persisted total invested amount as cost basis semantics. Result and proceeds SHALL be labeled estimates.

#### Scenario: Sell from an open position
- **WHEN** a user starts SELL from a position card or row
- **THEN** the asset is preselected and cannot be changed, and the form shows held quantity and average cost

#### Scenario: Sell a held inactive asset
- **WHEN** a position refers to an inactive asset missing from the public active catalog or quote endpoint
- **THEN** the user can still prepare a SELL using the position metadata and manual price, subject to backend rules

#### Scenario: Partial sale review
- **WHEN** the review is for a partial sale
- **THEN** it labels realized result as an estimate and does not present average cost as recalculated from the sale price

### Requirement: Exact decimal state and localized inputs
Canonical browser state for monetary amounts, quantities, prices, fees, FX, quotes, and derived estimates SHALL be decimal strings. Financial calculations MUST NOT use JavaScript `Number` as canonical state. Inputs SHALL accept a deterministic pt-BR decimal comma, preserve incomplete text while editing, use `inputMode="decimal"`, reject ambiguous grouping and invalid precision, and MUST NOT silently round input. Field-specific contracts SHALL apply: cash is `NUMERIC(18,2)` with scale at most 2; transaction quantity, unit price, fees and FX are `NUMERIC(18,8)`; quote is `NUMERIC(15,4)` normalized by backend to scale 4 using HALF_UP; materialized BRL values use scale 2; average cost uses scale 8. Manual transaction price keeps transaction scale 8 even when a scale-4 quote suggested it. Final derived BRL estimates use 2 places with HALF_EVEN. Estimates SHALL use high intermediate precision and remain subordinate to backend results.

#### Scenario: Exact decimal examples survive the browser
- **WHEN** the user enters `0,10000001`, `0,00000001`, or `123456789,12345678`
- **THEN** the canonical values remain exactly `0.10000001`, `0.00000001`, and `123456789.12345678` through review and the BFF boundary

#### Scenario: Value exceeds database precision
- **WHEN** an otherwise syntactically valid value exceeds the applicable database precision
- **THEN** the input/BFF rejects it and does not round it down to fit

#### Scenario: Quote uses its own precision contract
- **WHEN** a quote is displayed or checked before it is copied into an editable transaction price
- **THEN** quote precision is scale 4 (`NUMERIC(15,4)`) and transaction price validation remains scale 8 (`NUMERIC(18,8)`), without a universal scale-8 validator

#### Scenario: Date and time include local offset
- **WHEN** the user enters a local negotiation date/time
- **THEN** the review identifies the device timezone and the BFF accepts only ISO-8601 date-time with an explicit offset, including deterministic rejection of invalid local/DST input

### Requirement: Lossless BFF mutation boundary
The BFF SHALL authenticate through the existing HttpOnly `auth_session` server-side, validate same-origin `Origin`, use `no-store`, validate exact field allowlists, UUIDs, decimal strings, supported enum values, offset timestamps, and the required `Idempotency-Key`, and forward Bearer only server-side. Decimal request strings SHALL be converted to `LosslessNumber` and serialized as exact JSON numeric tokens without passing through `Number`. It SHALL preserve safe ProblemDetail `status`, `title`, `detail`, `instance`, allowlisted structured `code`, and correlation ID. It MUST NOT log keys, financial payloads, JWTs, Authorization, or provider secrets.

#### Scenario: Financial request serializes exact decimals
- **WHEN** a valid canonical decimal string is forwarded to Spring
- **THEN** the JSON body contains the same decimal as a numeric token and the request contains server-side Bearer plus the unchanged idempotency key

#### Scenario: Invalid body or origin
- **WHEN** a financial POST contains an unknown field, malformed decimal/UUID/date, missing key, or invalid/missing Origin
- **THEN** the BFF rejects it before calling Spring and exposes no authentication material

#### Scenario: Structured FX expiry code
- **WHEN** Spring returns `code=FX_EXPIRED`
- **THEN** the BFF preserves that allowlisted code for specialized recovery without interpreting `detail` text

### Requirement: Frozen idempotent intent and ambiguous POST recovery
Before the first POST, the user MAY edit the operation. At confirmation, the frontend SHALL freeze the canonical payload and create one `crypto.randomUUID()` key for that exact logical intent. Pending state, a local duplicate-submit guard, a disabled submit button, and backend idempotency SHALL prevent double-click duplication. Financial mutations SHALL use `retry: false`. A definite success or rejection SHALL clear the pending intent; a timeout, connection reset, or uncertain response after POST SHALL preserve the exact payload and key plus minimal operation context/time in `sessionStorage`. On shell/operation infrastructure mount, an accessible persistent indicator SHALL announce “Existe uma operação com resultado pendente” and offer `Revisar operação`. Reopening SHALL show operation type, safe relevant payload fields, available submission context/time and ambiguous status, with a manual retry action; it SHALL NOT offer edit or cancellation. The launcher/shell SHALL keep the intent discoverable throughout the sessionStorage lifecycle, including after closing the original Dialog/Sheet. Manual retry MUST reuse the same key and byte-equivalent canonical payload. Closing UI MUST NOT claim an ambiguous POST was cancelled. A changed payload, including a newly fetched FX id, is a new intent with a new key after review and confirmation.

#### Scenario: Double click
- **WHEN** a user activates confirmation repeatedly while a mutation is pending
- **THEN** only one request is sent for the frozen key and payload

#### Scenario: Ambiguous POST retry
- **WHEN** the browser loses the response after submitting a POST
- **THEN** the UI exposes recovery state and a manual retry sends the exact same key and payload without automatic mutation retry

#### Scenario: User changes the operation after an ambiguous result
- **WHEN** a POST outcome is ambiguous
- **THEN** editing is unavailable for that intent until it is resolved or safely abandoned before any POST; abandoning after a POST cannot be represented as cancellation

#### Scenario: New FX observation
- **WHEN** an expired FX id is replaced with a newly loaded id
- **THEN** the changed canonical payload requires a new review and new key

#### Scenario: Session storage contents
- **WHEN** an ambiguous intent is persisted for recovery
- **THEN** storage contains only operation type, key, canonical allowlisted payload, and minimal recovery status, with no JWT, Authorization, password, or provider secret

#### Scenario: Ambiguous intent remains discoverable
- **WHEN** the shell mounts while an ambiguous intent remains in sessionStorage
- **THEN** an accessible persistent recovery indicator opens a read-only review with safe operation details and retry; it provides no edit/cancel action and closing it preserves the intent

### Requirement: Authoritative completion and selective cache invalidation
A `201 Created` response SHALL be the authoritative operation result. After success, the UI SHALL announce the result through `aria-live`, clear pending recovery state, and close/reset only after displaying or preserving the result. A later refetch failure MUST NOT turn a committed operation into a failure. Deposit/withdraw SHALL invalidate only portfolio summary, cash balance, and the cash movements prefix. BUY/SELL SHALL invalidate only portfolio summary, positions prefix, transactions prefix, and exact cached position for the affected asset. Asset registration SHALL invalidate only the asset catalog and MUST NOT optimistically change cash, positions, average cost, net worth, or realized profit. No financial ledger value SHALL be treated as truth before the backend response.

#### Scenario: Operation succeeds but refetch fails
- **WHEN** an operation returns 201 and a subsequent query refresh fails
- **THEN** success remains visible and is not replaced by a mutation error

#### Scenario: Cash operation invalidation
- **WHEN** deposit or withdrawal succeeds
- **THEN** only summary, cash balance, and cash movement queries are invalidated

#### Scenario: Trade operation invalidation
- **WHEN** BUY or SELL succeeds
- **THEN** only summary, positions, transactions, and the affected exact position query are invalidated

#### Scenario: Asset registration invalidation
- **WHEN** a canonical asset is registered
- **THEN** only asset catalog queries are invalidated and the new asset is selected without creating financial effects

### Requirement: Persisted transaction history remains independent of market data
`/carteira/transacoes` SHALL read its paginated history exclusively from the persisted transaction ledger route. Quote, FX, valuation, provider refresh, and other complementary market work MUST NOT be a prerequisite for rendering the page or its loaded rows. If such complementary work fails, the history table/list SHALL remain visible and the UI MAY show only a compact secondary notice. A failed history request itself SHALL retain any previously loaded page while it retries and otherwise show a recoverable local error without presenting it as a market-data failure.

#### Scenario: Provider fails after transaction history is available
- **WHEN** a ROLE_USER has loaded persisted transaction rows and a quote, FX, valuation, or provider refresh fails
- **THEN** the Compra/Venda history remains visible with its friendly formatting
- **AND** no full-page market error replaces the table

### Requirement: Automatic logos and safe visual fallback
Asset and broker catalog capabilities SHALL remain normative for provider resolution, confidence rules, and persistence of `logoProvider` plus `logoReference`. The operation UI SHALL consume those system-owned references without searching during render. B3 references are validated public Brapi URLs; US ticker and broker domain references are rendered through Logo.dev's official image CDN. Alpha Vantage remains financial-only for this experience. `LOGO_DEV_PUBLISHABLE_KEY` MAY be client-visible solely as a token on `https://img.logo.dev`; `LOGO_DEV_SECRET_KEY`, Brapi and Alpha Vantage keys MUST remain server-side. Broker/asset requests SHALL reject manual logo URL, image, file, provider, reference or keys. Missing/ambiguous references, branding provider failure, or CDN failure MUST NOT invalidate a financially valid asset, broker registration, BUY, SELL, deposit, or withdrawal. Lists SHALL reuse persisted references and MUST NOT search once per rendered row. `EntityLogo` SHALL validate Brapi references, build explicit Logo.dev domain/ticker CDN paths, handle loading/errors/accessibility, and use deterministic ticker/name monogram fallback in Light/Dark and high contrast. Remote hosts SHALL be exact allowlist entries, never wildcard; use direct official CDN delivery rather than proxy/storage.

#### Scenario: Logo is absent or fails to load
- **WHEN** a catalog record has no logo or its image request fails
- **THEN** a deterministic readable monogram appears and no broken-image icon is shown

#### Scenario: Branding provider is unavailable
- **WHEN** a branding lookup fails after financial asset validation or broker compliance validation
- **THEN** the primary record remains valid with an absent logo reference and can still be used operationally

#### Scenario: Logo.dev publishable key delivery
- **WHEN** a Logo.dev US-ticker or broker-domain image is rendered
- **THEN** only the Logo.dev publishable key is included in a request to the official `img.logo.dev` CDN; the Search API secret and financial provider keys remain server-side

#### Scenario: Catalog list with many records
- **WHEN** a page contains multiple assets or brokers
- **THEN** rendering reuses resolved references and does not issue a separate branding search/lookup for each row

### Requirement: Accessible responsive operation feedback
All operation controls SHALL have visible labels, descriptions, field-level errors, `aria-invalid`/`aria-describedby` where applicable, keyboard-complete asset/broker selection, visible focus, sufficient touch targets, text-plus-color status, reduced-motion support, and focus trap/restore for dialogs and sheets. Escape/back SHALL close an operation only before POST begins or after its result is definitive; an ambiguous POST SHALL remain recoverable. Desktop, tablet, and mobile SHALL support light and dark themes, long financial values, selector loading/empty/error states, quote/FX states, review, success, and errors.

#### Scenario: Keyboard selection and confirmation
- **WHEN** a user navigates selectors and reviews with a keyboard or screen reader
- **THEN** every choice, error, status, review value, and confirmation action has a usable accessible name and focus order

#### Scenario: Escape during a request
- **WHEN** the user presses Escape before POST, after definitive completion, or while POST is ambiguous
- **THEN** Escape may cancel before a request or close after a definitive result, but it preserves and communicates recovery for an ambiguous request

#### Scenario: Visual and automated coverage
- **WHEN** the operational experience is validated
- **THEN** Vitest/RTL cover numeric parsing, payload, idempotency, recovery, selectors, forms, errors, and accessibility, while a focused Playwright set covers deposit, withdrawal, BUY B3, BUY US, contextual SELL, asset registration continuing BUY, and one recovery path; visual QA covers Light/Dark at desktop, tablet, and mobile

### Requirement: Typed business results without raw technical payloads
The operation UI SHALL transform authoritative responses into typed presentation models for deposit, withdrawal, BUY and SELL. It MUST NOT render `JSON.stringify`, raw request/response bodies, arbitrary object entries, raw ProblemDetail fields, stack traces, backend DTO dumps, correlation identifiers as business content, or technical UUIDs without a business need. Deposit and withdrawal SHALL announce operation-specific pt-BR success and show amount, resulting cash balance and optional description. BUY and SELL SHALL announce operation-specific pt-BR success and show recognizable asset/broker and financial result fields; SELL SHALL include realized result and remaining position values when returned. Query-refetch failure after success MUST NOT replace the friendly result.

#### Scenario: Cash operation result
- **WHEN** deposit or withdrawal returns success
- **THEN** the UI announces the operation-specific pt-BR message and a labelled financial summary with no serialized object or raw movement

#### Scenario: Trade operation result
- **WHEN** BUY or SELL returns success
- **THEN** the UI announces Compra/Venda registrada com sucesso and presents typed ticker, quantity, price, total, broker/balance/position fields when available without DTO dumping

### Requirement: Safe pt-BR presentation error mapping
The BFF MAY preserve safe status, allowlisted structured code and correlation id for internal logic, but the business UI SHALL map them to natural pt-BR text and MUST NOT display backend `title`, `detail`, `instance` or raw ProblemDetail automatically. Required mappings SHALL cover 400 field review, 401 expired session, 403 permission, quote 404/manual price, generic 409 conflict, structured `FX_EXPIRED`, 502 temporary market service failure and ambiguous network outcome. Structured codes, never detail-text parsing, SHALL drive specialized behavior.

#### Scenario: Provider unavailable message
- **WHEN** asset validation or market provider returns a technical English detail
- **THEN** the UI displays a natural pt-BR temporary-unavailability message and no technical English detail

#### Scenario: Ambiguous network result
- **WHEN** a financial POST outcome cannot be confirmed
- **THEN** the UI asks the user to review the pending operation before trying again and preserves recovery semantics without exposing transport details

### Requirement: Asset-validation availability is distinct from confirmed absence
The selector SHALL offer controlled registration only after exact lookup completes as absent while the mandatory financial-validation capability is operational. If lookup or required validation is unavailable, it SHALL preserve the typed ticker, explain that the asset cannot be validated now, provide Tentar novamente when appropriate, and MUST NOT present registration as ready or accept manual metadata as a workaround.

#### Scenario: Exact absence with provider operational
- **WHEN** exact lookup confirms no ticker and validation is available
- **THEN** the ticker/market-only registration action may be offered

#### Scenario: Provider unavailable
- **WHEN** the mandatory provider or exact-validation path is unavailable
- **THEN** ticker remains visible, retry is offered, and registration is unavailable until validation can run

### Requirement: Strict lossless financial text inputs
All editable BigDecimal values SHALL use locale-aware text inputs with `inputMode=decimal`, dot-decimal string canonical state, field-specific precision/scale validation and a distinct display formatter. Inputs SHALL reject letters, scientific notation, signs unless the domain explicitly permits them, a second decimal separator and ambiguous paste. They SHALL NOT silently round or use JavaScript Number as canonical financial state.

#### Scenario: Exact locale parsing
- **WHEN** a user enters `0,00000001` or `123456789,12345678` in a permitted scale-eight field
- **THEN** outbound canonical values are exactly `0.00000001` and `123456789.12345678`
- **AND** invalid `1e10`, `abc`, a second separator or excess scale is rejected in pt-BR.

### Requirement: Automatic quote and average-price presentation
BUY discovery and asset registration SHALL automatically request a current quote after canonical selection, prefill editable unit price when available, and preserve the form when unavailable. Position, BUY and SELL surfaces SHALL display average cost with a string-aware minimum-two/maximum-four display formatter while preserving scale-eight values.

#### Scenario: Existing holding in BUY
- **WHEN** a user selects an already-held asset
- **THEN** the form and review show current average price and a clearly labeled estimated post-purchase average
- **AND** a quote, when available, is loaded automatically but remains editable.
