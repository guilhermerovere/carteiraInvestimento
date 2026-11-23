## Context

See `proposal.md` for motivation and `specs/asset-catalog/spec.md` for the behavioral contract. Today `BrapiQuoteAdapter` implements quote, metadata validation, and discovery, but both `validate` and `obter` deserialize `/api/v2/stocks/quote`; classification reads `data.type` and falls back to a stock-only ticker regex when that field is absent. Discovery calls `/api/available` and returns only strings through `AssetDiscoveryProviderPort`, so `GET /api/v1/acoes/discovery` exposes only `ticker` and `mercado`.

The BFF currently fixes discovery to B3 and forwards the two-field response. `AssetSelector` can show a provider ticker suggestion, but on a catalog miss it offers registration without showing the enriched, non-persistent preview already required by `asset-catalog`. Registration does re-resolve metadata server-side before saving, and that trust boundary must remain: preview data is never authority for persistence.

The PRD constrains B3 assets to Brapi, B3/BRL, server-derived metadata, global catalog identity, and a USER request containing only ticker and market. This change has no data-model or Flyway impact and must preserve all pre-existing Graphify artifacts.

## Goals / Non-Goals

**Goals:**

- Make Brapi identity/classification and quotation independent, explicit inputs to one consistency check.
- Reuse one canonical B3 resolution path for enriched preview and final registration validation.
- Keep provider I/O outside the short database write transaction and prove that every failure path saves nothing.
- Define a lossless, allowlisted preview contract from backend through BFF to the frontend confirmation UI.
- Make the regression executable from captured payload shapes, including quotes without `type`.

**Non-Goals:**

- Adding domain asset types beyond `ACAO`, `FII`, and `ETF`, changing B3 ticker syntax, or supporting BDRs, indices, mutual funds, and other unmapped subtypes.
- Changing quote-history behavior, portfolio valuation, BUY/SELL, US provider selection, admin creation, database schema, or stored rows already in the catalog.
- Trusting a preview at confirmation time, persisting preview quotes, or falling back to ticker-suffix inference when provider identity is incomplete.

## Decisions

### 1. Split Brapi contracts by responsibility and remove type inference from quote payloads

The Brapi client will model `/api/v2/tickers` with a dedicated identity DTO containing at least canonical ticker, issuer/security name, subtype, and optional trusted logo data. It normalizes both the prior `stocks[]` envelope (`stock`, `type`, `logo`) and the currently observed `results[]` envelope (`symbol`, `subType`, `logoUrl`) before applying the same rules. `/api/v2/stocks/quote` keeps a dedicated quote DTO containing canonical/requested symbol, price, currency, and quote instant. Quote `type`, when absent or present, will not participate in classification.

The identity mapper will normalize subtype case and accept exactly `unit` → `ACAO`, `stock` → `ACAO`, `fii` → `FII`, and `etf` → `ETF`. Null, blank, or every other subtype is an unsupported instrument and is rejected; the existing suffix regex fallback is removed. This keeps support aligned with `TipoAtivo` instead of guessing from `11`, which is shared by different B3 instrument classes.

Alternative rejected: keep `/api/v2/stocks/quote` as the metadata source and extend suffix heuristics. Real quote payloads omit classification, and the ticker format cannot reliably distinguish a unit, FII, ETF, or another unsupported `11` instrument.

### 2. Resolve identity first, then quote the canonical identity and require agreement

For an exact validation, the adapter will canonicalize the requested ticker, select the exact provider identity (or the provider-declared current canonical identity), map its subtype, then request the quote for that canonical ticker. A valid B3 resolution requires:

- a unique usable identity with a B3-compatible canonical ticker and supported subtype;
- a quote whose returned canonical symbol equals the identity ticker;
- quote currency `BRL` and a valid current price;
- only optional failure for branding; name falls back to the canonical ticker under the existing rule.

For discovery by ticker or issuer name, `/api/v2/tickers` supplies candidate identities and `/api/v2/stocks/quote` supplies their prices; the implementation should use the endpoint's multi-symbol request when more than one candidate is previewed, then join by canonical ticker rather than response position. Candidates without one unambiguous matching quote are omitted or fail the exact-preview path; they are never silently paired.

Identity/quote mismatch or malformed required provider data is an upstream consistency failure and will use the existing sanitized provider-unavailable path (502). A recognized but unsupported subtype remains invalid input under the capability's current 400 convention. A genuine provider not-found result keeps the existing not-found convention. Registration repeats the complete resolution at confirmation time so a stale or forged preview cannot supply persisted metadata.

Alternative rejected: merge the two payloads by list position or accept the requested ticker when symbols differ. Either approach can attach a price to the wrong identity and violates the no-persistence-on-divergence requirement.

### 3. Use one application preview value and preserve the persistence boundary

The discovery provider contract will return an application-level preview value instead of `List<String>`. It carries only canonical ticker, name, supported type, market, currency, optional system-owned branding, and current price. The B3 adapter will share its identity/quote resolution logic between discovery and `AssetMetadataProviderPort.validate`; the registration service consumes only freshly resolved server metadata, not a client preview.

`AtivoApplicationService.descobrir` and the controller remain read-only and perform no `AtivoPort.save`. `registrar` will continue checking requested/canonical duplicates and performing provider I/O before constructing and saving the candidate. Tests will verify `save` is never called for unsupported subtype, missing required fields, identity/quote divergence, or provider failure.

Alternative rejected: store a temporary asset or preview token. The capability requires a non-persistent preview, and server-side revalidation makes temporary catalog state unnecessary.

### 4. Enrich the existing discovery route rather than add a second registration API

`GET /api/v1/acoes/discovery?q=...&mercado=B3` will return preview items with:

- `ticker`, `nome`, `tipo`, `mercado`, `moeda`;
- nullable `logoProvider` and `logoReference` after the existing trust checks;
- `cotacaoAtual` from the quote source.

The route remains authorized for USER/ADMIN, query-parameter allowlisting remains strict, and it performs no catalog mutation or quote-history write. `POST /api/v1/acoes` remains the only controlled USER registration operation and still accepts exactly `ticker` and `mercado`. This is additive at the backend JSON level for current two-field consumers and avoids a second create-like workflow.

Alternative rejected: expand POST to accept preview fields. That would move the metadata trust boundary to the browser and contradict the PRD.

### 5. Make the BFF a strict lossless boundary for preview data

The BFF discovery route will validate its query and backend response, map only the preview allowlist, and serialize `cotacaoAtual` as the existing lossless `DecimalString` convention. The shared `AssetDiscovery` TypeScript contract and a dedicated mapper will include the enriched fields. Provider credentials, raw Brapi fields, and arbitrary metadata remain absent. The BFF does not classify, rename, or repair a provider result; malformed backend previews become the existing sanitized finance error.

Alternative rejected: let the browser consume raw backend/provider objects. That would duplicate validation and expose fields outside the product contract.

### 6. Require an explicit frontend confirmation from the enriched preview

For an already catalogued active asset, the selector keeps the current direct-selection behavior. For an uncatalogued B3 result, selecting a discovery item stores and renders the preview with logo/fallback, ticker, name, type, B3/BRL, and formatted current price. Only this valid preview enables “Cadastrar ativo”. Confirmation calls the existing registration method with the preview's canonical ticker and market; success then invalidates the catalog query and selects the persisted `Asset`.

Provider unavailability, unsupported identity, or source divergence shows the existing sanitized/retryable error treatment and no confirmation action. Absence of trusted branding uses `EntityLogo`'s monogram and does not block confirmation.

Alternative rejected: keep the current ticker-only registration card. It does not satisfy the catalog's preview requirement and hides classification errors until after mutation is attempted.

### 7. Implement in TDD slices with real-shaped fixtures

Work proceeds strictly Red → Green → Refactor:

1. Red: add raw JSON fixtures/tests for SANB11 (`unit`), HGLG11 (`fii`), BOVA11 (`etf`), and PETR4 (`stock`) whose quote `data` omits `type`; add divergence/no-save tests and failing backend/BFF/frontend preview assertions.
2. Green: implement the minimum separate DTOs, join/validation rules, enriched contract, strict BFF mapper, and confirmation UI required to pass those tests.
3. Refactor: remove the quote-type/suffix fallback and obsolete ticker-only discovery structures, consolidate shared canonical-resolution code, then rerun focused and full suites.

Fixtures will be deserialized through the production ObjectMapper DTO shapes, not assembled only with constructors, so field omission and provider nesting remain part of the regression.

## Risks / Trade-offs

- [Two Brapi calls increase latency and failure surface] → use multi-symbol quote requests for discovery, avoid database transactions around provider I/O, and expose a retryable sanitized preview failure.
- [Fuzzy identity search can return ambiguous or unrelated candidates] → canonicalize, retain only B3-compatible supported identities, join strictly by ticker, and require a unique exact identity for registration.
- [Provider response shape changes again] → keep endpoint-specific DTOs, deserialize real-shaped fixtures, reject missing required fields, and never restore heuristic classification.
- [Backend enrichment can reach the frontend before its new mapper/UI] → deploy backend first because added JSON fields are compatible with current consumers, then deploy BFF and frontend together.
- [Preview becomes stale before confirmation] → re-resolve both identity and quote on POST and keep database uniqueness as the final race-safe authority.
- [Replacing public discovery could accidentally require a token] → preserve the current optional-credential behavior for public identity search and test that no Authorization header is required there; quote authentication remains governed by existing configuration.

## Migration Plan

1. Add and validate endpoint-specific DTOs and resolution tests without changing persistence or schema.
2. Deploy the enriched backend discovery response and corrected registration validation.
3. Deploy the BFF mapper/contracts and frontend preview/confirmation flow together.
4. Run focused backend adapter/service/controller tests, frontend unit tests, the complete backend and frontend suites, and the strict OpenSpec validation before considering the change complete.

Rollback is code-only: revert frontend/BFF first and backend last. No database rollback, data rewrite, or migration is required. Assets created before this fix are not reclassified automatically; auditing or repairing historical rows would require a separate change.

## Open Questions

None. The source authority, supported subtype map, failure boundary, preview fields, confirmation trust boundary, and rollout order are fixed by this change.
