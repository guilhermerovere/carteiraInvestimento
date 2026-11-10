## Context

See `proposal.md` for motivation. The backend already exposes `GET /api/v1/acoes/discovery?q={q}&mercado={B3|US}` and dispatches by `Mercado`, but the same-origin BFF accepts only `q`, forcibly appends `mercado=B3`, and the selector calls `financeApi.discoverB3`. The existing Twelve Data adapter finds US candidates through `/stocks`, but gives every `AssetPreview` a null `currentPrice`; the current BFF mapper requires `cotacaoAtual` to be a lossless decimal string.

The live provider contract checked for this change is Twelve Data REST `GET https://api.twelvedata.com/quote?symbol=AAPL&apikey=...`. The official response supplies `symbol`, `name`, `exchange`, `mic_code`, `currency`, `timestamp`, and decimal-string `close` (besides other quote fields); `/quote` costs one credit per symbol. The secret stays in the server-side Feign query parameter and must never enter a BFF response, logs, browser code, fixture, or error message. [Official quote documentation](https://twelvedata.com/docs) and [symbol-catalog documentation](https://support.twelvedata.com/en/articles/5620513-how-to-find-all-available-symbols-at-twelve-data) confirm `/quote` and `/stocks` respectively.

## Goals / Non-Goals

**Goals:**

- Make an explicit B3/US choice the sole source of market selection from the selector through the BFF to backend discovery.
- Return an US preview only after a Twelve Data quote independently confirms the exact canonical US/USD instrument and a usable USD price.
- Keep discovery non-persistent and retain the present registration and BUY boundaries.
- Make provider unavailability retryable and safe for the UI.

**Non-Goals:**

- No changes to the controlled POST asset body, backend revalidation, authorization, catalog lifecycle, exchange-rate rules, transaction payload, or financial BUY execution.
- No client-side key, client-side provider call, new provider, caching policy change, quote-history write, inference of market from ticker, or broadening of supported instrument types.
- No mutation of Graphify artifacts.

## Decisions

### 1. Carry a typed market parameter; never infer market

`AssetSelector` will expose an accessible B3/US control and include its selected market in the React Query key, discovery request, exact-lookup/precheck state, registration confirmation, and rendered suggestion label. `financeApi` will replace the B3-specific method with a market argument. The BFF route will allowlist exactly `q` and `mercado`, validate the enum, and forward the selected value unchanged after URL encoding.

This retains the backend's explicit dispatch rule and prevents stale B3 suggestions from being reused after switching to US. Inferring from ticker syntax was rejected because the requirements deliberately support issuer-name discovery and B3/US syntax can be ambiguous in UI input.

### 2. Build a US preview from two authoritative steps

The Twelve Data adapter will retain `/stocks` as the search/identity/classification source. For every candidate already filtered to a supported US venue, USD, canonical ticker, and supported Equity/ETF type, it will call `/quote` with the candidate ticker before constructing `AssetPreview`.

The quote must be a non-error response whose canonical symbol equals the candidate, whose exchange or MIC is an accepted US venue, whose currency is exactly USD, whose timestamp is positive, and whose `close` parses as a positive `BigDecimal`. The preview uses that `close` as `currentPrice` and quote-derived symbol/venue/currency only as an independent validation gate; name and type remain derived from `/stocks` as today. This matches the documented `/quote` response and avoids treating a symbol search record as a current price.

Calling only `/quote` was rejected because it does not provide the existing search-result classification path. Returning a null/zero price, using `previous_close`/`extended_price`, or filling a synthetic price was rejected because the BFF contract and existing specification require a real current-price decimal.

### 3. Classify quote failures conservatively

An unavailable/malformed/rate-limited/unauthorized/upstream Twelve Data quote becomes the existing `AssetValidationUnavailableException`, translated to sanitized `502`; discovery then offers no registration and the selector preserves the typed ticker and displays retry. A semantic no-result, mismatched symbol/market/currency, blank/non-positive/non-decimal close, or missing/non-positive timestamp makes that candidate invalid and it is not previewed or confirmable; no asset is saved. The adapter logs only the provider and a category, never the credential or response body.

This mirrors the B3 rule that required quote failure blocks confirmation. Treating unavailable quotes as a quiet empty list was rejected because it would falsely imply that a valid search cannot be found and would suppress recovery.

### 4. Preserve controlled registration and financial separation

The preview confirmation continues to POST only `{ticker, mercado}` to the catalog BFF. The BFF remains an allowlisted boundary; the server's existing `registrar` redoes provider validation before persistence. Selecting/creating an asset must invalidate/read only catalog state and then allow the existing BUY dialog to fetch a quote and, for US, the existing persisted USD/BRL rate. The only financial POST remains the existing transaction endpoint after the user completes the BUY form.

Combining registration with BUY or passing preview metadata/price to registration was rejected because it would bypass revalidation and change authorization, idempotency, cash, and transaction invariants.

### 5. Bind every catalog and precheck result to the selected market

The archive review found that the registered-asset query still returned both markets, the exact lookup accepted an existing asset without comparing its `mercado`, and an in-flight precheck could commit state after a B3/US switch. `AssetSelector` therefore filters both registered and discovered candidates by the explicit market, accepts an exact match only when its market equals the market captured at request start, and invalidates the active precheck generation whenever the market changes.

An exact asset from another market is treated as absent for the selected market, allowing a matching discovery preview from that selected market to remain eligible. Every asynchronous success, not-found preview, unavailable error, and loading-state completion checks the request generation before updating state. This keeps the existing unscoped catalog endpoint and exact-ticker endpoint unchanged while making the UI's market boundary explicit and race-safe.

### 6. Extend the market-bound generation through registration

The follow-up review found that registration and its `409` conflict recovery were outside the precheck generation. A late `registerAsset` response could therefore select an asset from the market that initiated the request after the user had switched markets; the exact lookup used to recover a conflict had the same gap.

`AssetSelector` now uses one operation generation for precheck and registration. Registration captures the selected market and generation before calling the API, then checks both before invalidating catalog state, after that asynchronous invalidation, before selecting a successfully registered asset, and before applying any error or loading state. Conflict recovery checks the generation before and after its exact lookup and only selects an asset whose `mercado` matches the captured market. A market switch increments the shared generation, so all late outcomes become no-ops without restoring selection, preview, error, or busy state from the prior market.

## Risks / Trade-offs

- [One `/quote` per US discovery candidate consumes provider credits and increases latency] → retain the provider's filtered result set, perform no browser calls, and surface the existing retryable unavailable state rather than stale/synthetic data.
- [The `/stocks` list and `/quote` can disagree or change between calls] → require exact canonical symbol, accepted US venue, USD, timestamp, and positive close before preview; registration independently revalidates later.
- [Changing markets while a prior query is in flight can display stale results] → partition query cache and local preview/precheck state by explicit market, and clear preview/error selection on a market switch.
- [Provider details may leak through errors or test fixtures] → map to existing sanitized ProblemDetail and assert that BFF/client-visible payloads have only the preview allowlist.

## Migration Plan

1. Add failing adapter, BFF, selector, and E2E coverage first.
2. Implement typed market propagation and quote-qualified US discovery, then make the new tests green without changing persisted schema or API authorization.
3. Run backend, frontend, and E2E suites. Deployment is application-only; rollback is a code rollback with no data migration or catalog cleanup required.
