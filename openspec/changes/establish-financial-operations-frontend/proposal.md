## Why

Valore currently supports authenticated financial reading but has no operational frontend for deposits, withdrawals, BUY, or contextual SELL. The academic requirement also needs every `ROLE_USER` to add a valid instrument to the shared catalog, while preserving precise amounts, private server-side authentication, and safe retries for financial writes.

## What Changes

- Add one responsive operational experience for deposits, withdrawals, BUY, and SELL, with review, explicit confirmation, clear success and recoverable error states, and selective cache invalidation.
- Add remote asset and active-broker selectors; let `ROLE_USER` request controlled registration of an asset from ticker and market, then select it and continue BUY without creating a position or transaction.
- Enrich B3 issuer logos from Brapi, US stock logos from Logo.dev's ticker CDN, and broker brands from Logo.dev Search API plus its image CDN. Persist only system-resolved references and use a deterministic fallback.
- Add exact decimal input, estimation, and outbound BFF serialization; bind each confirmed payload to one `Idempotency-Key` and support recovery from an ambiguous POST without changing its payload.
- Extend the position response only with asset metadata needed to sell a held inactive asset, and add a structured `FX_EXPIRED` error code without changing existing pricing formulas or transaction payloads.
- Refine the manual-QA findings: move Operar to the lower desktop sidebar/mobile More menu, center the desktop dialog, present typed operation results, remove raw JSON, normalize visible errors to pt-BR, and block asset registration while mandatory provider validation is unavailable.
- Add a functional ROLE_ADMIN shell for the existing broker and asset catalog contracts, including list, create, permitted edits and lifecycle controls with automatic branding fallback.
- Add account settings for the authenticated principal: profile name/email updates, password change, and confirmed account closure only after backend verification of zero cash and no open positions.
- Keep analytics dashboards, charts, user-managed brokers, manual logo input, media storage, physical deletion of financial history, refresh tokens, and global JWT revocation out of scope.

## Capabilities

### New Capabilities

- `financial-operations-frontend`: same-origin operational UI and BFF flows for cash movements, BUY/SELL, controlled asset registration, selectors, precise values, idempotency, review, recovery, and responsive accessible feedback.
- `account-settings`: self-service profile, password and safe account-closure semantics for an authenticated principal.
- `administrative-catalog-ui`: responsive ROLE_ADMIN shell and same-origin catalog management UI over existing broker and asset contracts.

### Modified Capabilities

- `asset-catalog`: allow controlled `ROLE_USER` asset registration in the canonical catalog, with provider-derived metadata and optional system-resolved branding.
- `broker-catalog`: preserve ADMIN-only broker administration while requiring an automatic Logo.dev branding attempt when configured, without making a logo a prerequisite for a valid broker.
- `investment-transactions-and-positions`: include asset metadata needed by SELL when its held asset is inactive, and identify expired FX with a structured code.
- `financial-portfolio-shell-and-read-experience`: add the `ROLE_USER` operation launcher to the authenticated shell while retaining the existing read pages and keeping `ROLE_ADMIN` outside the personal wallet.
- `frontend-authentication-session`: add the profile-menu settings entry, protected settings route, current-user refresh, and session cleanup after password change or closure.
- `identity-and-primary-wallet`: allow only the authenticated principal to update profile/password and close an eligible account without deleting financial history.
- `security-event-auditing`: add sanitized audit events for email/password changes and account closure.

## Impact

- Frontend: Next.js App Router, same-origin finance Route Handlers, TanStack Query keys/mutations, Valore shell, finance components, Vitest/RTL, and a small Playwright smoke set.
- Backend: tightly scoped asset registration/enrichment and branding metadata, broker branding enrichment, position response metadata, structured expired-FX error, self-service identity mutations, and transactional closure eligibility. Existing cash and transaction financial rules remain authoritative.
- Persistence/providers: additive Flyway metadata only if required; Brapi remains the B3 logo/metadata priority, Alpha Vantage remains a financial quote/validation provider rather than the US visual-logo delivery mechanism, and Logo.dev handles US stock logos and broker matching/delivery. Secret keys remain server-side; Logo.dev's documented publishable key may reach the browser only in requests to its official image CDN.
- Existing OpenAPI DTOs, security, migrations, BigDecimal behavior, and read experience remain compatibility constraints. PRD authorization and account-lifecycle language is aligned by this change before implementation is declared complete.

## Final refinement

- Give every `ROLE_USER` a first-class `/carteira/ativos` discovery route and make the same provider-backed discovery available from BUY. Registration remains limited to `ticker` and `mercado`, creates no position, transaction, or cash movement, and persists only financially validated canonical metadata.
- Separate public B3 discovery from validation and quotation: lack of `BRAPI_TOKEN` must not turn a supported public search into a generic provider outage. Normalize discovery results and expose neither provider payloads nor technical failure detail.
- Fetch the last quote automatically after asset selection or registration while retaining an editable unit price. Refine catalog administration into compact dialog-based lists; the principal asset registration UI becomes provider-assisted while retaining compatible ADMIN contracts.
- Make average price visible in positions and BUY/SELL review using a string-aware formatter. Refine every editable financial value into a strict locale-aware text input that never uses `Number` as canonical state, rejects letters/scientific notation/ambiguous paste, and preserves accepted scale exactly.
- Restore provider-backed CVM broker validation by exact canonical CNPJ. The runtime path must not reject a registration from a partial versioned snapshot, root matching, or a locally hardcoded regulatory baseline. A provider outage remains a temporary validation failure.
