## Why

Valore currently supports authenticated financial reading but has no operational frontend for deposits, withdrawals, BUY, or contextual SELL. The academic requirement also needs every `ROLE_USER` to add a valid instrument to the shared catalog, while preserving precise amounts, private server-side authentication, and safe retries for financial writes.

## What Changes

- Add one responsive operational experience for deposits, withdrawals, BUY, and SELL, with review, explicit confirmation, clear success and recoverable error states, and selective cache invalidation.
- Add remote asset and active-broker selectors; let `ROLE_USER` request controlled registration of an asset from ticker and market, then select it and continue BUY without creating a position or transaction.
- Enrich B3 issuer logos from Brapi, US stock logos from Logo.dev's ticker CDN, and broker brands from Logo.dev Search API plus its image CDN. Persist only system-resolved references and use a deterministic fallback.
- Add exact decimal input, estimation, and outbound BFF serialization; bind each confirmed payload to one `Idempotency-Key` and support recovery from an ambiguous POST without changing its payload.
- Extend the position response only with asset metadata needed to sell a held inactive asset, and add a structured `FX_EXPIRED` error code without changing existing pricing formulas or transaction payloads.
- Keep analytics dashboards, charts, administrative catalog CRUD, broker registration by `ROLE_USER`, manual logo input, and media storage out of scope.

## Capabilities

### New Capabilities

- `financial-operations-frontend`: same-origin operational UI and BFF flows for cash movements, BUY/SELL, controlled asset registration, selectors, precise values, idempotency, review, recovery, and responsive accessible feedback.

### Modified Capabilities

- `asset-catalog`: allow controlled `ROLE_USER` asset registration in the canonical catalog, with provider-derived metadata and optional system-resolved branding.
- `broker-catalog`: preserve ADMIN-only broker administration while requiring an automatic Logo.dev branding attempt when configured, without making a logo a prerequisite for a valid broker.
- `investment-transactions-and-positions`: include asset metadata needed by SELL when its held asset is inactive, and identify expired FX with a structured code.
- `financial-portfolio-shell-and-read-experience`: add the `ROLE_USER` operation launcher to the authenticated shell while retaining the existing read pages and keeping `ROLE_ADMIN` outside the personal wallet.

## Impact

- Frontend: Next.js App Router, same-origin finance Route Handlers, TanStack Query keys/mutations, Valore shell, finance components, Vitest/RTL, and a small Playwright smoke set.
- Backend: tightly scoped asset registration/enrichment and branding metadata, broker branding enrichment, position response metadata, and a structured expired-FX error. Existing cash and transaction financial rules remain authoritative.
- Persistence/providers: additive Flyway metadata only if required; Brapi remains the B3 logo/metadata priority, Alpha Vantage remains a financial quote/validation provider rather than the US visual-logo delivery mechanism, and Logo.dev handles US stock logos and broker matching/delivery. Secret keys remain server-side; Logo.dev's documented publishable key may reach the browser only in requests to its official image CDN.
- Existing OpenAPI DTOs, security, migrations, BigDecimal behavior, and read experience remain compatibility constraints. The PRD and current `asset-catalog` spec say only ADMIN may create assets; this change records the explicitly authorized academic `ROLE_USER` requirement without editing the PRD. An APPLY task will align the PRD before this change is completed or archived.
