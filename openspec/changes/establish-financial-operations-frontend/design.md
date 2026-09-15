## Context

Valore already has authenticated, same-origin read experiences for a user's portfolio. This change adds reviewed, idempotent operations while retaining server-side JWT handling, lossless monetary values, existing financial rules, and per-user portfolio isolation. The OpenAPI/backend contracts and current frontend conventions were inspected before planning.

Branding delivery is resolved using Logo.dev's documented key separation: its secret key is server-only and used for Search API; its publishable key is explicitly designed for browser use and works only with `img.logo.dev`. The browser may receive the publishable key only as a token on that official image CDN. No image proxy or image-byte storage is planned. Brapi's public issuer `logourl` is used for B3 when available.

The current canonical asset table has a database-wide unique ticker constraint (`uk_acoes_ticker`), and the domain canonicalizes ticker with trim plus uppercase. This is stricter than `(market, normalized ticker)` uniqueness. Preserve the existing global uniqueness for this change unless a separately authorized schema decision changes it; do not silently weaken the constraint.

## Goals / Non-Goals

**Goals:** provide a compact authenticated launcher and reviewed deposit, withdrawal, BUY and contextual SELL flows; let ROLE_USER register a financially validated asset in the global catalog; preserve exact decimal JSON, backend authorization and idempotency; provide resilient asset/broker selectors, quotes, FX, recovery, accessible responsive UX, functional administration of existing catalogs, self-service account settings, and focused verification.

**Non-Goals:** dashboard analytics, charts, administrative CRUD for users, user-managed brokers, manual logos, logo uploads or a media library, dividends/tax/fixed income/watchlists, changing average-cost accounting, changing existing cash or transaction request shapes, physical deletion of financial/audit history, global JWT revocation, exposing credentials, or broad backend work beyond the explicit extensions in the request.

## Decisions

### Identity, permissions, and catalog

- Keep the current Next.js same-origin BFF and HttpOnly `auth_session` session. Spring remains the authority for ownership and authorization. JWT, Authorization, backend URLs, financial provider keys, and all provider secret credentials remain server-side. A provider-documented publishable image-CDN key may be browser-visible only for the provider's official image delivery endpoint.
- ROLE_USER may search active assets and register a valid new global asset using only ticker and market. The backend derives name, type, currency and available metadata from its existing financial provider. Asset registration creates no position, transaction, or cash movement.
- Provider type mapping is constrained by the real `TipoAtivo` enum: `ACAO`, `FII`, and `ETF`. B3 metadata maps only supported Brapi subtypes (`stock`→ACAO, `fii`→FII, `etf`→ETF); unit, BDR, indices and other unsupported categories are rejected. US Alpha Vantage symbol metadata accepts only an exact Equity→ACAO or ETF→ETF match; mutual funds and all unmapped types are rejected. Provider recognition alone does not grant unsupported domain behavior.
- Keep the canonical global asset identity and current database unique-ticker constraint. Normalize through the existing trim/uppercase domain canonicalizer. An existing active asset is returned/reused where the API can do so; otherwise duplicate/conflict is recoverable and the selector reloads/selects the canonical asset. Database uniqueness remains the concurrency authority.
- A matching inactive asset is never reactivated by ROLE_USER and is not BUY-selectable. Existing positions remain sellable according to backend rules using metadata carried in the Position response and an editable manual price if no quote is available.
- Assets and brokers have no direct relationship. Broker selection is attached to each transaction. Asset registration remains possible before any broker is active. ROLE_USER only reads active brokers; all broker administration stays ADMIN-only. No active broker disables BUY confirmation with a clear message and never offers broker creation.

### Enrichment and branding

- Financial validation and visual branding are separate. Financial provider validation is required to create an asset; logo enrichment is best-effort and cannot block a valid asset or compliant broker.
- B3: use Brapi as the priority for canonical company metadata, supported subtype, and `logourl`. The quote response may also identify renames through `requestedSymbol`, `symbol`, and `changed`. When they differ, normalize and persist/reuse only the returned current symbol; never create an alias. If that active canonical record already exists, return/reuse it and let the frontend explain the mapping; an inactive canonical record remains unavailable and cannot be reactivated by USER. Extend the Brapi adapter to retain documented metadata and use the Brapi ticker catalog when subtype is needed. Persist `logoProvider=BRAPI` plus `logoReference` only when it is an HTTPS URL on the explicitly trusted `icons.brapi.dev` host (or another exact host confirmed by provider contract). Brapi credentials remain server-side. A missing logo yields no reference; no lookup occurs during list rendering.
- US: Alpha Vantage remains part of financial validation/quote fallback, not the visual-logo delivery mechanism. Its server-side symbol search must yield an exact canonical symbol/type/market; Equity maps to ACAO and ETF maps to ETF, while mutual funds/unsupported results are rejected. If an authoritative US provider explicitly resolves a requested ticker to a current canonical symbol, apply the same rename/no-alias rule as B3; otherwise do not infer a rename. Use Logo.dev's ticker CDN when its supported exchange identifier is available. Persist `logoProvider=LOGO_DEV` and a normalized provider reference such as `ticker/AAPL`; the browser constructs the official image URL using the publishable key. Branding availability never determines whether an instrument is financially valid.
- Brokers: after existing authoritative broker validation, attempt Logo.dev Search API server-side using `strategy=match` and only actual canonical `razaoSocial`, `nomeFantasia`, or an official domain if the current model supplies one. Never invent a domain. Accept only one sufficiently strong exact normalized identity match resolving to one canonical domain. Multiple plausible matches, no match, malformed results, missing key, timeout, or provider outage leave branding absent and never roll back an otherwise valid broker. Persist only `logoProvider=LOGO_DEV` and the canonical domain in `logoReference`.
- Broker regulatory validation uses the established provider-backed CVM lookup by the exact canonical 14-digit CNPJ. The provider result and Receita identity must agree with the submitted CNPJ; an active result is compliant, a definitive absence is rejected, and provider timeouts/5xx/malformed responses return a temporary validation error. No runtime snapshot, local baseline, root fallback, or manually maintained CNPJ list may decide a rejection.
- Keep `LOGO_DEV_SECRET_KEY` server-only for Search API; never return/log it. `NEXT_PUBLIC_LOGO_DEV_PUBLISHABLE_KEY` (or the existing equivalent convention) may be exposed in frontend code solely for `img.logo.dev` image URLs; it is not used for Search API or any other endpoint. Restrict it to the production/development origins supported by Logo.dev. Document placeholders in `.env.example`, Compose, backend server configuration, and frontend config; never persist actual key values, provider payloads, credential-bearing Search URLs, or image bytes.
- Catalog and Position reads reuse persisted references and never trigger per-row provider calls. The single provider-discriminated model is: BRAPI stores a validated public image URL; LOGO_DEV stores `ticker/<ticker>[.<exchange>]` for a US security or the resolved canonical domain for a broker. Neither reference contains a publishable or secret key. EntityLogo validates the Brapi host, builds Logo.dev CDN URLs and uses an exact `img.logo.dev` host. Use direct `<img>` delivery unless Next Image supports these exact hosts without weakening restrictions; do not proxy or self-host. A load failure or absent reference uses a deterministic monogram fallback.
- EntityLogo is one reusable component for selectors, positions and review. It handles loading/error, accessible name, deterministic ticker/name monogram, Light/Dark and high-contrast appearance without a broken-image state.

### Position, transaction and exchange-rate contracts

- Extend the existing Position response only with real joined asset metadata required for a contextual SELL independent of the active catalog: canonical name, market, currency, active status and optional branding reference. Do not add a second endpoint or alter ownership/valuation rules.
- Preserve exact transaction POST fields and cash endpoint contracts. Cash is `NUMERIC(18,2)` (scale at most 2). Transaction quantity, unit price, fees and FX use their existing field-specific `NUMERIC(18,8)` contract. Quote is `NUMERIC(15,4)`, normalized by the quote domain to scale 4 using HALF_UP; an editable manual transaction price still follows transaction scale 8. Materialized BRL values use scale 2 and average cost scale 8. B3 submits `exchangeRateId` absent/null per the existing contract; US submits the persisted FX `id`. FX validity is the existing five-minute window from `registradoEm`. Add a structured `FX_EXPIRED` code to that existing 409 path; other unstructured 409 responses remain generic recoverable conflicts.
- Keep the audited average-price formula unchanged. Add only the requested total-SELL residual coverage proving full remaining `totalInvestidoBrl` is removed even where quantity × rounded average differs by one cent.
- Quote is a suggestion, never an order price. Quote 404/502 preserves manual price entry. The backend response is authoritative for all committed balances and positions.

### Browser decimals and dates

- Financial canonical state and BFF validation use decimal strings. Add `decimal.js-light` only for estimates, configured for about 50 significant digits; final BRL estimates use HALF_EVEN at two places. Never coerce financial input or response into JS Number and never silently round inputs to fit a contract.
- BFF validates allowlisted fields, UUIDs, field-specific decimal scales/precision, offset ISO timestamps, Origin, and required Idempotency-Key; it converts decimal strings to `LosslessNumber` and serializes exact JSON numeric tokens. Backend remains `BigDecimal` authority. Do not create one overly broad scale validator.
- Inputs preserve intermediate pt-BR text and parse deterministically (decimal comma, no ambiguous grouping), use `inputMode="decimal"`, and share only necessary DecimalInput/MoneyInput/QuantityInput primitives.
- Negotiation date/time is entered locally and serialized as ISO-8601 with the device's explicit offset. Test invalid values, offsets and DST transitions with native APIs; add no date library unless implementation proves native handling insufficient.

### Operation lifecycle and recovery

- A compact desktop Dialog and near-full-screen mobile Sheet host short forms and a distinct review step. Users can edit before confirm. SELL starts only from an open position and locks the asset.
- On the first confirmation of the final canonical payload, create one `crypto.randomUUID()` Idempotency-Key and freeze the pair before POST. Guard pending/double clicks and disable the submit action. Financial TanStack mutations set `retry: false`.
- A definitive success removes recovery state, announces the result, then selectively invalidates only the documented query keys. A later refetch error does not turn success into mutation failure. No financial optimistic state is committed as truth.
- A network failure/timeout after POST is ambiguous. Persist only operation kind, frozen canonical payload, key and minimal recovery state in sessionStorage. On shell/operation infrastructure mount, show an accessible persistent pending-operation indicator with a `Revisar operação` action. Reopening shows operation type, safe identifying financial fields, submission context/time when available, ambiguous status, and a manual retry action using identical payload and key; no edit or cancellation action is offered. Closing UI does not cancel or clear the intent, and the launcher/shell keeps it discoverable through the sessionStorage lifecycle. Clear only on definitive success/rejection or abandonment before any POST. A newly fetched FX changes payload and therefore starts a new intent and key only after explicit review/confirmation.
- Cash invalidates summary, cash balance, and cash-movement prefix. BUY/SELL invalidates summary, positions, transactions and exact position(assetId) when cached. Asset registration invalidates only catalog.assets. Never invalidate all queries.

### Presentation, errors, and test strategy

- Use existing Shadcn primitives (Dialog/Sheet, input, select/combobox, alert-dialog) and current shell/provider conventions. Operation launcher exposes Comprar, Depositar and Sacar only to ROLE_USER; SELL is contextual.
- Every form has labels, descriptions, inline accessible errors, keyboard-complete selectors, focus containment/restore, Escape before POST, touch targets, safe-area and keyboard handling, reduced-motion support, contrast, and polite aria-live success/error/process states. Review identifies every browser-derived amount as an estimate.
- Preserve the established 400/401/403/404/409/502/network semantics. `FX_EXPIRED` is detected by structured code, never by detail text. A 401 uses the existing session flow; a 403 preserves the session. Unknown 409 is generic; POST network failure is ambiguous.
- Add focused backend integration/unit coverage, Vitest/RTL for precision/BFF/state/components/errors, and a small Playwright smoke set (cash deposit/withdrawal, BUY B3/US, contextual SELL, integrated asset registration, and one FX-expired or ambiguous-retry path). Build/typecheck/lint and responsive Light/Dark visual QA span desktop/tablet/mobile; do not create a giant E2E matrix.

### Manual-QA refinement and operation presentation

- On desktop, Operar belongs to the bottom sidebar group immediately above Recolher/Expandir, using the same grid and gutters as navigation. Collapsed mode keeps a centered icon, tooltip, accessible name and working menu. Mobile keeps the existing bottom navigation and exposes Operar through Mais; no header duplicate remains. SELL stays contextual.
- Use the native accessible dialog as a centered 520-640px desktop surface with safe max-height/internal scrolling. Mobile remains a near-full-screen sheet-like surface with safe-area spacing and reachable actions.
- Replace arbitrary response dumping with typed presentation models for deposit, withdrawal, BUY and SELL. No business surface renders JSON, raw ProblemDetail, request/response bodies, stack traces or technical identifiers without a business need.
- Centralize pt-BR presentation mapping by safe status/code. Preserve status, correlation id and allowlisted code internally; never display backend `title`, `detail`, `instance` automatically. `FX_EXPIRED` remains structured-code driven. Provider-unavailable exact lookup is distinct from confirmed absence and disables registration while preserving the ticker and offering retry.

### Administrative catalog experience

- ROLE_ADMIN receives a dedicated Valore shell with Visao geral, Corretoras and Ativos, theme/profile/logout and no wallet/Operar controls. The overview uses only simple existing catalog counts/links.
- Same-origin admin BFF routes proxy the existing Spring catalog contracts with server-side bearer and Origin validation for mutations. Broker forms use only `cnpj`, optional `numero` and `complemento`; editing changes only `numero`/`complemento`. Asset forms use only `ticker`, `nome`, `tipo`, `mercado`; editing changes only name. Lifecycle uses the existing idempotent `ativo` PATCH. No physical DELETE or manual logo field exists.
- Admin lists reuse `EntityLogo` and system-owned branding metadata. Missing branding degrades to fallback and never blocks a compliant catalog mutation. Server-side guards and Spring remain authoritative; client guards are complementary.

### Account settings, password and closure

- Add protected `/configuracoes` for the authenticated principal and a profile-menu entry before Sair. ROLE_USER and ROLE_ADMIN may manage only their own identity; the page never becomes system administration.
- Reuse `Usuario`, `EmailCanonicalizer`, `PasswordPolicy`, BCrypt strength 12, current `sub` identity and current-user response. Add minimal backend endpoints under `/api/v1/account`: PATCH profile, POST password, and POST closure. Same-origin BFF routes remain the browser boundary and validate Origin for all mutations.
- Profile PATCH trims name, enforces 1..255 characters, canonicalizes/validates email and preserves PostgreSQL case-insensitive uniqueness. The JWT identity authority is `sub`, not email, so name/email update keeps the token and immediately refreshes `['auth','me']`.
- Password change requires and verifies the current password, applies the existing password policy, stores a new BCrypt hash and never logs/returns password data. The BFF clears the current `auth_session` after success and the UI returns to login. Because there is no token version/blacklist, other already-issued JWTs are not represented as globally revoked; persisted-user active checks still apply normally.
- Physical deletion is unsafe: `carteiras`, `movimentacoes_caixa`, `transacoes`, idempotency records and `logs_auditoria` reference user/wallet under restrictive FKs. Closure therefore locks the principal wallet, revalidates `saldo_caixa_brl = 0` and absence of `posicoes.quantidade > 0` in the same transaction, then sets `ativo=false`, replaces name/email with deterministic non-personal unique values derived from UUID, and replaces the password hash with an unusable random BCrypt credential. Financial/audit rows and zero historical positions remain intact.
- Closure requires current password plus explicit confirmation. Cash or open positions produce safe structured conflicts; a known ambiguous financial intent also blocks the browser attempt, but sessionStorage is never ledger authority. Wallet locking serializes closure with cash/trade writes and prevents a stale zero-state race. Existing authentication checks reject both subsequent login and previously issued JWT use once `ativo=false`.
- Audit name/email/profile sensitivity conservatively: email change, password change and account closure are sanitized events containing no old/new email, password/hash, JWT, Authorization, request payload or financial values. Closure audit is committed in the same transaction before identity anonymization completes.

## Risks / Trade-offs

- **External logo availability and coverage:** Logo.dev may not resolve every supported ticker or broker. High-confidence matching avoids wrong logos; deterministic fallback preserves recognition and financial flows.
- **Publishable key/origin configuration:** the publishable key is intended for browser CDN delivery but must be limited to approved origins and used only at `img.logo.dev`. A leaked secret Search key remains a credential incident; keep it server-only.
- **Brapi reference trust:** accept only HTTPS image references from the allowlisted host. A missing or changed provider URL degrades to fallback and must not invalidate the asset.
- **Broker matching:** `strategy=match` ranks exact/near-exact candidates, but the application must still compare actual normalized names and require one unambiguous domain; popularity rank alone is insufficient.
- **PRD alignment:** this change updates the PRD authorization and account-lifecycle language so the product source of truth matches the authorized implementation before validation.
- **Global ticker uniqueness:** current schema prevents same ticker in different markets. This preserves existing canonical identity and avoids a migration, but may reject a legitimate cross-market collision; do not change without reviewing existing data and defining canonical semantics.
- **Provider metadata varies:** financial providers may not return every issuer name/logo consistently. Visual enrichment remains optional and fallback is deterministic; validation must remain based on the financial provider, not logo availability.
- **Ambiguous POST storage:** sessionStorage is tab/session scoped and may be cleared by the browser. It improves recovery but does not replace backend idempotency or guarantee recovery after all client storage loss.

## Migration Plan

1. Configure Logo.dev secret and publishable keys under distinct server/public environment names, restrict the publishable key to intended origins, and document names/placeholders in `.env.example`, Compose and frontend/backend config. Never put real values in tracked files.
2. Add bounded backend asset registration, provider validation/enrichment, optional branding references, Position metadata, and structured FX error code. Resolve external provider data before the short persistence transaction and use uniqueness constraints for races. Preserve existing database data and financial formulas.
3. Add decimal foundation, exact BFF mutation routes, field-specific validation and discoverable idempotent intent recovery.
4. Add selectors, optional logo/fallback presentation, quotes/FX and reviewed operation flows behind authenticated ROLE_USER shell.
5. Add the existing-contract ADMIN catalog UI and self-service account settings/closure backend and BFF boundaries, without changing financial formulas or deleting history.
6. Run backend tests, Vitest/RTL, Playwright smoke, production build/typecheck/lint and visual/accessibility checks. Align all OpenSpec artifacts before declaring the change complete.

## Open Questions

None.

## Final discovery, decimal and visual refinement

- `/carteira/ativos` is a ROLE_USER route alongside Carteira, Posicoes, Transacoes and Movimentacoes. It presents provider-backed ticker/company discovery, market filters, provider-derived name/type/currency/logo/current price, and one small add action. The browser never submits provider metadata.
- Use an internal normalized discovery model (`ticker`, `nome`, `mercado`, `moeda`, `tipo`, optional `precoAtual`, `logoProvider`, `logoReference`, `cadastrado`, `ativo`). B3 public discovery and B3 validation/quote are independent. Missing configuration, 401/403, 429, timeout and 5xx are classified internally and rendered only as safe pt-BR states.
- Selection or registration immediately requests a quote and only suggests the editable `precoUnitario`. Persisted metadata is reused in lists; contextual administrative sync is bounded and cannot overwrite locally editable fields without an explicit rule.
- Decimal inputs are text plus `inputMode=decimal`. Canonical values are dot-decimal strings; edit values are ungrouped pt-BR decimals; blur display is separately formatted. The parser permits digits plus one separator, rejects letters, signs, scientific notation and ambiguous paste, validates precision/scale without rounding, and never uses `Number`, `parseFloat` or `toFixed` as financial truth.
- `formatAveragePrice` shows at least two and at most four places without changing scale-eight canonical values. Positions show average price; BUY shows current and clearly labeled estimated post-purchase average; SELL preserves and displays unchanged average cost.
- Collapsed desktop Operar uses a small anchored popover. Dialogs, discovery results and ADMIN catalog lists use compact rows, whitespace and contextual dialogs instead of persistent nested-card forms.
- The transaction history is a persisted-ledger read surface, independent of quote, FX and portfolio valuation refreshes. A failed external market request may produce only a compact secondary notice; it MUST NOT replace a loaded transaction table or hide persisted entries. A true history-query failure still has a recoverable local error state. The collapsed Operar control anchors its compact action popover to the sidebar control, keeps icon and tooltip geometry separate while open, and supports keyboard activation without clipping or overflow in either theme.


### Persisted transaction history after BUY

The transaction ledger is persisted independently of valuation. After a committed BUY/SELL, invalidate the transaction query prefix and refetch the paginated BFF ledger. Its backend query, DTO mapper, and rendering must tolerate nullable exchange-rate/result/branding fields and must not call quote, FX, valuation, or other market providers. Regression coverage must exercise the complete UI-to-BFF-to-real-backend persistence-to-history-GET path and verify that the just-committed transaction is returned and displayed even when market-provider work fails.
