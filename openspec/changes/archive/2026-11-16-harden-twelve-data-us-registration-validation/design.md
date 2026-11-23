## Context

See `proposal.md` for motivation and `specs/asset-catalog/spec.md` for the behavioral contract. Today `TwelveDataQuoteAdapter.validate` calls only `/quote`, checks its symbol/venue/currency, and uses `typeOrCommonStock`, which maps a missing quote `type` to `ACAO`. Unlike `discover`, it does not require a positive `close` or positive `timestamp` before returning registration metadata.

The current Twelve Data contracts separate the needed evidence:

- The official [`/stocks` catalog documentation](https://twelvedata.com/docs/asset-catalogs/stocks-list) documents `symbol`, `name`, `currency`, `exchange`, `mic_code`, `country`, and `type`, including Common Stock and ETF classifications.
- The official [`/quote` documentation](https://twelvedata.com/docs/market-data) documents `symbol`, `name`, `exchange`, `mic_code`, `currency`, `timestamp`, and decimal-string `close`. Its documented response attributes and example do not include `type`, so validation cannot require that field.

The adapter already owns both Feign calls and the accepted US exchange/MIC sets, so the change stays inside the provider boundary. A planning baseline run of `.\mvnw.cmd clean verify` from `backend/` ran 220 tests but stopped with 19 Testcontainers initialization errors because no valid Docker environment or Docker CLI was available; therefore no consolidated baseline ratio was produced in this environment. The implementation must obtain a successful baseline in a Docker-capable environment before changing production code.

## Goals / Non-Goals

**Goals:**

- Make reference identity/classification and quotation validity mandatory for the existing registration validation path.
- Preserve current sanitized semantic-not-found versus provider-unavailable error categories.
- Produce auditable, credential-free Red/Green/Refactor evidence for three strictly sequential vertical cycles.
- Compare the consolidated JaCoCo branch ratio before and after implementation without changing coverage policy.

**Non-Goals:**

- No change to discovery behavior, provider selection, DTOs, controllers, persistence, database schema, branding, frontend, credentials, retries, or caching.
- No live-provider test, new dependency, coverage exclusion, reduced threshold, or assertion against an undocumented `/quote.type` field.
- No attempt to persist price or timestamp; they are validation gates for registration metadata.

## Decisions

### 1. Use `/stocks` as identity and classification authority

`validate` will canonicalize the request, call `/stocks` with that symbol, validate the envelope, and select an exact candidate whose canonical symbol, United States country/accepted US venue, and USD currency agree. Its explicit supported type maps Common Stock/Equity to `ACAO` and ETF/Exchange Traded Fund to `ETF`; missing, Mutual Fund, and other types are rejected. If exact eligible results conflict, validation fails safely instead of selecting an arbitrary identity.

This reuses the existing reference-data records and helpers. Keeping `/quote` as the classification source was rejected because its documented response does not contain `type`; retaining `typeOrCommonStock` was rejected because absence is not evidence of Equity.

### 2. Use `/quote` as an independent, concordant quotation gate

Only after reference identity/type succeeds will `validate` request `/quote` for the canonical reference symbol. The quotation must be a non-error payload whose canonical symbol equals the reference symbol, whose exchange or MIC is an accepted US venue, and whose currency is USD. `close` must parse as `BigDecimal` with `signum() > 0`; `timestamp` must be non-null and greater than zero. The returned `AssetMetadata` uses name/type from `/stocks`; quote name and type are not authorities.

Reusing `discoveryQuote` directly was rejected because its `Optional.empty()` semantics intentionally suppress discovery candidates, whereas registration validation must preserve the existing distinction between semantic invalidity and provider unavailability. Adding price/timestamp to `AssetMetadata` was rejected because those values are gates, not persisted catalog metadata.

### 3. Preserve existing failure taxonomy and secret boundary

Malformed/missing required provider payload data remains `AssetValidationUnavailableException` when it indicates an unusable provider response; an exact but unsupported/divergent instrument remains `IllegalArgumentException` under current 400 conventions. HTTP authorization, throttling, timeout, and upstream errors retain existing sanitized categories. Logs contain provider and failure category only, never the API key, request URL, or raw payload.

Returning all failures as not-found was rejected because it hides recoverable provider outages. Exposing provider messages was rejected because it can leak operational details.

### 4. Execute three vertical TDD cycles with an evidence barrier

Each cycle changes a focused test and the smallest production path needed for that behavior, then refactors while the full adapter test class remains green. The next cycle MUST NOT begin until the prior cycle has recorded:

1. **Red:** exact command, non-zero exit, failing test name, and assertion/behavior showing the intended missing capability.
2. **Green:** the same focused command, zero exit, and test count/result after the minimum implementation.
3. **Refactor:** the full `QuoteProviderAdaptersTest` command, zero exit, and test count/result after cleanup.

Evidence is appended beneath the matching task in `tasks.md` as short sanitized excerpts. It MUST omit API keys, full request URLs, environment dumps, raw provider bodies, and unrelated logs. The cycles are:

- Cycle 1: `/stocks` exact US/USD identity plus explicit Equity/Common Stock or ETF classification, including rejection of absent/unsupported type and proof that a quote with no `type` is acceptable.
- Cycle 2: strictly positive, parseable `/quote.close` before metadata is returned.
- Cycle 3: present, strictly positive `/quote.timestamp` before metadata is returned.

A single broad horizontal Red followed by all production changes was rejected because it would obscure which behavior drove each implementation step.

### 5. Use deterministic client mocks at the provider boundary

Tests will mock `TwelveDataClient`, provide contract-shaped `/stocks` and `/quote` records with no credential material, verify the canonical symbol passed between calls, and verify that `/quote` is not called when identity/type fails. Mocks are justified because the subject is adapter decision logic; live calls would require secrets, consume credits, vary with market/provider state, and make Red/Green evidence non-reproducible. The official documentation review supplies contract evidence; mocks do not pretend to prove the external service itself.

### 6. Compare JaCoCo branch ratios from successful consolidated reports

Before production/test edits, run `.\mvnw.cmd clean verify` from `backend/` in a working Docker/Testcontainers environment and read the top-level `BRANCH` counter from `backend\target\site\jacoco\jacoco.xml`. Record `covered`, `missed`, and the computed ratio `covered / (covered + missed)` as the baseline. After all three cycles, run the identical command and computation, then record the final ratio and numeric delta.

Acceptance requires the final full build to pass, the final branch ratio to be strictly greater than the recorded consolidated baseline and at least the configured `0.70` gate, and no changes to JaCoCo thresholds/exclusions. Record baseline/final `covered` and `missed` counters, both ratios, and the positive numeric ratio delta. Focused TDD reports are diagnostic only and MUST NOT be compared with the consolidated baseline.

## Risks / Trade-offs

- [An extra `/stocks` call increases latency and consumes one additional provider credit per registration validation] → Keep calls sequential so invalid identity never consumes a quote credit; do not broaden the result set or add unrelated provider calls.
- [The same ticker can appear in multiple markets or results] → Require exact canonical symbol plus US country/venue and USD; reject conflicting exact eligible results rather than use list order.
- [Provider classifications may use aliases] → Keep the accepted mapping explicit and limited to supported Equity/Common Stock and ETF labels; cover each accepted/rejected label with focused cases.
- [A price or timestamp can become stale immediately after validation] → Treat both as proof of a usable quote, not as a persisted execution price or freshness guarantee.
- [Consolidated coverage cannot run without Docker/Testcontainers] → Treat a working Docker environment as a prerequisite and do not substitute a partial report or weaken the gate.

## Migration Plan

1. In a Docker-capable environment, capture the clean consolidated baseline test result and branch ratio.
2. Execute cycles 1–3 in order, recording Red/Green/Refactor evidence after each phase.
3. Run the complete backend verification and compare the final consolidated branch ratio with the baseline and existing gate.
4. Validate the OpenSpec change strictly and present implementation evidence for review. Deployment is application-only; rollback restores the adapter/test changes and needs no data migration.
