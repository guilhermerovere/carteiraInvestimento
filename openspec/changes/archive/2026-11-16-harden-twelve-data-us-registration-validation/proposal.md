## Why

`TwelveDataQuoteAdapter.validate` currently validates US registration from `/quote` alone, defaults a missing quote `type` to `ACAO`, and accepts metadata without first proving a positive price and valid quote timestamp. This conflicts with the `asset-catalog` trust boundary and with Twelve Data's real contracts, where `/stocks` supplies instrument classification while the documented `/quote` response supplies quotation data but no `type`.

## What Changes

- Validate an exact US identity through `/stocks`, accepting only a supported `Common Stock`/Equity or ETF classification and rejecting absent, mutual-fund, or unmapped types without ticker-based or missing-field inference.
- Require `/quote` to agree with the `/stocks` identity on canonical symbol, accepted US venue, and USD currency before registration metadata is returned.
- Require the quotation `close` to parse as a strictly positive decimal and its Unix `timestamp` to be present and positive before registration.
- Deliver the behavior as three independent vertical TDD cycles—identity/type, positive price, then valid timestamp—with Red, minimum Green, and Refactor evidence completed before the next cycle begins.
- Measure and compare JaCoCo branch coverage as a ratio before and after the change, requiring the final ratio to be strictly greater than the consolidated baseline while preserving the existing line/branch gates and exclusions; record covered/missed counters and the numeric delta.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `asset-catalog`: tighten the existing controlled US registration validation so Twelve Data reference identity/classification and quotation evidence must agree and be usable before persistence.

## Impact

- Backend provider adapter and its focused tests: `TwelveDataQuoteAdapter` and `QuoteProviderAdaptersTest`.
- External calls remain server-side `GET /stocks` followed by `GET /quote`; no credential, raw provider payload, request DTO, database schema, frontend, or public API shape changes.
- Twelve Data request usage increases from one `/quote` call to one `/stocks` plus one `/quote` call for a registration validation.
- The existing Maven/JaCoCo gate remains unchanged; full consolidated verification requires a working Docker/Testcontainers environment.
