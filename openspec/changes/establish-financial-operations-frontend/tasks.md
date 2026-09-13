## 1. Backend contract and permission foundation

- [ ] 1.1 Record the existing cash, transaction, catalog, quote and FX request/response contracts in backend contract tests or API fixtures; verify fixtures match OpenAPI and controller DTOs.
- [ ] 1.2 Add ROLE_USER asset-registration use case with ticker and market-only request; verify valid creation is independent of portfolio, cash and broker state.
- [ ] 1.3 Authorize controlled asset POST for ROLE_USER while retaining existing ADMIN asset capabilities; verify ROLE_USER can create and cannot edit, delete, activate or deactivate.
- [ ] 1.4 Reuse the current trim/uppercase ticker canonicalizer and global unique-ticker constraint; verify case/whitespace normalization and database duplicate enforcement.
- [ ] 1.5 Make concurrent duplicate asset registration recoverable using database uniqueness and canonical reload/reuse; verify parallel requests produce one canonical asset.
- [ ] 1.6 Reject invalid B3/US ticker-market combinations and provider-known instrument types outside the current `ACAO`/`FII`/`ETF` enum; verify 400 and no persistence.
- [ ] 1.7 Enrich valid user-created assets from the existing financial metadata provider; verify provider outages do not create an unvalidated asset and returned metadata is server-derived.
- [ ] 1.8 Keep inactive duplicate assets administrative-only and non-BUY-selectable; verify a USER cannot reactivate or mutate one.
- [ ] 1.9 Verify global catalog semantics across two users; verify both find the same asset while portfolios and positions remain isolated.

## 2. Automatic branding and backend response extensions

- [ ] 2.1 Verify Brapi `logourl`, `requestedSymbol`/`symbol`/`changed`, and Logo.dev ticker/CDN contracts; add adapter fixtures for present, absent, renamed and unavailable responses.
- [ ] 2.2 Add optional system-owned asset branding identifier/reference to persistence and catalog responses; verify request DTOs reject logo, logoUrl, provider and arbitrary metadata.
- [ ] 2.3 Persist B3 `logourl` as system-owned `BRAPI`/`logoReference` only after HTTPS and trusted-host validation; verify valid BBAS3/PETR4 creation survives absent/malformed logo.
- [ ] 2.4 Use Logo.dev ticker CDN references for supported US tickers while Alpha Vantage remains financial-only; verify no Alpha API key is used in image delivery and logo failure does not affect validation.
- [ ] 2.5 Configure Logo.dev broker branding: server-side Search API with `strategy=match` and secret key, direct publishable-key delivery only through `img.logo.dev`; verify current vendor docs permit each key's intended use.
- [ ] 2.6 Implement high-confidence broker brand resolution using only canonical legal/trade names or an actual existing official domain; verify exactly one exact normalized match persists its canonical domain and ambiguous results do not.
- [ ] 2.7 Persist broker `LOGO_DEV`/domain `logoReference` and bounded metadata refresh/cache policy; verify broker list does not perform per-row provider calls.
- [ ] 2.8 Keep broker creation and administration ADMIN-only while attempting branding after authoritative validation; verify provider failure does not roll back a compliant broker and ADMIN cannot submit a logo or image URL.
- [ ] 2.9 Add optional asset and broker branding metadata to DTOs with explicit Brapi and Logo.dev hosts; verify responses contain no secret credential or provider payload and frontend constructs the publishable image URL.
- [ ] 2.10 Add optional name, market, currency and active status to Position response using the existing asset join; verify an inactive held asset has enough data for SELL without the public catalog.
- [ ] 2.11 Add the structured `FX_EXPIRED` code to the existing expired-rate 409 mapping; verify other 409 codes/details remain unchanged.
- [ ] 2.12 Add a focused total-SELL residual test where `totalInvestidoBrl` differs by one cent from quantity times rounded average; verify all residual cost is removed and accounting invariants remain intact.
- [ ] 2.13 Add placeholders for `LOGO_DEV_SECRET_KEY` and the frontend publishable key to `.env.example` and Compose; verify no real keys are committed and each variable is passed only to its intended service.
- [ ] 2.14 Wire `LOGO_DEV_SECRET_KEY` through backend server-only configuration and `NEXT_PUBLIC_LOGO_DEV_PUBLISHABLE_KEY` (or the existing equivalent convention) through frontend public configuration; verify Logo.dev origin restrictions and that the secret is absent from bundles, HTML and browser network responses.
- [ ] 2.15 Align `PRD.md` before declaring this change complete or archiving: document controlled ROLE_USER canonical asset registration and retained USER/ADMIN limits; verify PRD no longer contradicts implemented authorization.
- [ ] 2.16 Document role-specific asset POST `oneOf` request schemas and statuses/responses in OpenAPI; verify USER schema rejects ADMIN name/type fields while existing ADMIN body remains compatible.
- [ ] 2.17 Keep financial provider lookup outside short persistence transactions; verify provider timeout does not hold a database write transaction and uniqueness handles concurrent canonicalization.

## 3. Decimal and BFF mutation foundation

- [ ] 3.1 Add `decimal.js-light` to the frontend dependency manifest with approximately 50 significant digits; verify package configuration/build resolves without converting financial state to Number.
- [ ] 3.2 Implement exact outbound decimal-string serialization using `LosslessNumber`; verify raw JSON numeric tokens preserve `0.10000001`, `0.00000001` and `123456789.12345678` exactly.
- [ ] 3.3 Add field-specific BFF validation for UUID, decimal precision/scale, offset timestamp, allowlists, Origin and Idempotency-Key; verify invalid and excess-precision values are rejected without rounding.
- [ ] 3.4 Add same-origin BFF deposit/withdraw POST routes for the existing backend endpoints; verify body fields are exactly valor/optional descricao and no-store/correlation/safe ProblemDetail conventions are preserved.
- [ ] 3.5 Add same-origin BFF transaction POST route; verify the exact eight backend fields and that visual/quote/FX numeric metadata is excluded.
- [ ] 3.6 Add same-origin BFF controlled asset POST and existing paginated asset/broker, quote and FX proxies as needed; verify methods, query allowlists and server-side bearer forwarding.
- [ ] 3.7 Add canonical decimal arithmetic and HALF_EVEN estimate helpers; verify money estimates round only at documented final BRL boundaries.
- [ ] 3.8 Add deterministic pt-BR DecimalInput parser/formatter primitives; verify comma decimal, intermediate text, scale limits, ambiguous grouping and no silent rounding.
- [ ] 3.9 Add MoneyInput and QuantityInput with contract-specific scales and accessible field errors; verify mobile decimal input mode and preservation of valid precision.
- [ ] 3.10 Add local date/time serialization with explicit ISO-8601 offset; verify invalid input, UTC offsets and applicable DST transitions.

## 4. Idempotent intent and recovery

- [ ] 4.1 Implement a canonical frozen operation intent containing operation kind, final payload and one UUID Idempotency-Key; verify rerenders/editing before confirmation do not create keys.
- [ ] 4.2 Disable financial mutation automatic retries and guard pending/double-click submits; verify one confirmation dispatches at most one POST attempt.
- [ ] 4.3 Persist minimal ambiguous POST recovery state in sessionStorage and remove it on definitive success/rejection; verify no JWT, auth, password, provider secret or full unrelated portfolio state is stored.
- [ ] 4.4 Add shell-discoverable ambiguous intent indicator and read-only recovery surface with safe operation details/context; verify retry uses identical frozen payload/key and UI has no edit/cancel action.
- [ ] 4.5 Add safe pre-POST abandonment and FX replacement lifecycle; verify abandoning before POST clears intent, while new FX after an expired 409 requires review and a new key.

## 5. Shell, selectors, and branding presentation

- [ ] 5.1 Add the ROLE_USER Operar launcher with Comprar, Depositar and Sacar actions; verify ADMIN sees no personal operation launcher and SELL stays contextual.
- [ ] 5.2 Add responsive Dialog/Sheet operation container reusing existing Shadcn primitives; verify desktop dialog and mobile near-full-screen sheet scroll/focus behavior.
- [ ] 5.3 Implement EntityLogo with loading, remote failure and deterministic ticker/name fallback; verify no broken image in Light/Dark and accessible image/fallback naming.
- [ ] 5.4 Implement paginated remote AssetSelector with logo, ticker, name, market and currency; verify search, keyboard selection and page-bound loading match backend parameters.
- [ ] 5.5 Implement compact AssetRegistrationForm with ticker and market only; verify ROLE_USER cannot submit name, currency, logo, provider, UUID, status or metadata.
- [ ] 5.6 Integrate not-found asset registration into AssetSelector and BUY; verify successful registration updates only catalog.assets, selects the returned canonical asset and continues BUY without purchasing.
- [ ] 5.7 Handle asset duplicates and inactive matches in the selector; verify duplicate recovery selects/reloads the canonical asset and inactive assets are not reactivated or offered for BUY.
- [ ] 5.8 Implement paginated active BrokerSelector with broker branding/name fields and load-more; verify filtering is limited to loaded pages and UUID is not the primary label.
- [ ] 5.9 Implement no-active-broker state; verify asset registration remains available, BUY cannot confirm, and no broker-creation action is shown.
- [ ] 5.10 Configure exact remote image hosts for Brapi and `img.logo.dev`; verify no wildcard or arbitrary URL is accepted and Logo.dev publishable key is used only for official CDN images.
- [ ] 5.11 Implement exact active ticker precheck with `/api/v1/acoes/ticker/{ticker}` before offering registration; verify fuzzy results without exact match do not suppress “Cadastrar ticker”.
- [ ] 5.12 Handle Brapi historical ticker rename by selecting current canonical ticker and explaining the mapping; verify no alias is created and existing canonical records are reused.

## 6. Market context and operation flows

- [ ] 6.1 Add manual quote consultation with price, currency, provider and freshness; verify quote remains optional and price stays user-editable.
- [ ] 6.2 Handle quote 404/502 without clearing the form; verify a manual price can continue where the backend contract permits.
- [ ] 6.3 Add USD/BRL context using the returned persisted FX record; verify freshness uses registradoEm and POST stores only exchangeRateId.
- [ ] 6.4 Add FX_EXPIRED recovery that preserves fields, refreshes rate, recalculates estimates and requires renewed review; verify replacement FX gets a new payload/key.
- [ ] 6.5 Implement deposit form/review with current balance, amount and estimated projected cash; verify scale-two BRL validation and exact cash contract.
- [ ] 6.6 Implement withdrawal form/review with current balance, amount and estimated projected cash; verify over-known-balance warning is advisory and does not claim backend rejection.
- [ ] 6.7 Implement BUY B3 form/review with asset, broker, quantity, editable quote/manual price, fees and offset date; verify B3 payload omits/nulls FX per real contract.
- [ ] 6.8 Implement BUY US form/review with FX panel and BRL estimates; verify exchangeRateId is required and numeric FX is absent from transaction POST.
- [ ] 6.9 Add BUY average-cost estimate (current average, purchase cost and estimated new average); verify estimates do not change the audited backend formula or present themselves as booked values.
- [ ] 6.10 Add contextual SELL from open position with locked asset, quantity available, average cost, broker, fees and date; verify SELL cannot switch the selected asset.
- [ ] 6.11 Support SELL of inactive held assets using Position metadata and manual price when quote is unavailable; verify no active catalog entry is required.
- [ ] 6.12 Add SELL quantity/known-holding advisory and estimated net proceeds/realized result; verify partial SELL display keeps the existing average cost unchanged.
- [ ] 6.13 Implement shared review/edit/confirm and operation result states; verify first submit opens review, edit returns to form, and only explicit confirm POSTs.
- [ ] 6.14 Handle success from backend response as authoritative and announce via aria-live; verify later query-refetch failure does not turn a successful POST into an error.
- [ ] 6.15 Implement precise success/error messaging for 400, 401, 403, 404, generic 409, 502 and ambiguous network failure; verify 409 detail text is not semantically parsed.

## 7. Query behavior, accessibility, and responsive refinement

- [ ] 7.1 Add selective cash invalidation for summary, cash balance and cash-movement prefix; verify no broad invalidate-all call occurs.
- [ ] 7.2 Add selective BUY/SELL invalidation for summary, positions, transactions and cached exact asset position; verify query-key factory is reused.
- [ ] 7.3 Verify asset registration invalidates only catalog.assets and never financial position/summary/transaction caches.
- [ ] 7.4 Complete keyboard, focus trap/restore, Escape-before-POST, labels, descriptions, aria-describedby/invalid and screen-reader selector/form behavior; verify RTL accessibility assertions.
- [ ] 7.5 Refine mobile safe area, keyboard, scrolling, touch target and action placement; verify content and confirmation controls remain reachable at phone viewport sizes.
- [ ] 7.6 Verify desktop, tablet and mobile layout in Light and Dark for launcher, selectors, forms, review, success and errors; verify long financial values wrap/read without clipping.
- [ ] 7.7 Verify reduced motion, high contrast and non-color-only state cues across operation and fallback components; verify automated accessibility checks pass.

## 8. Verification and delivery readiness

- [ ] 8.1 Add backend tests for USER asset creation/authorization, canonicalization, duplicates/concurrency, provider metadata/logo absence/failure, ADMIN-only broker management, Position metadata, inactive SELL and FX_EXPIRED.
- [ ] 8.2 Add backend regression tests for cash/auth/isolation and BUY/SELL accounting without changing average-cost implementation; verify suite covers the SELL-total residual case.
- [ ] 8.3 Add Vitest/RTL coverage for locale parsing, field-specific precision/HALF_EVEN, quote scale 4 vs transaction scale 8, raw numeric JSON tokens, BFF validation and idempotent recovery; verify listed precision edge cases.
- [ ] 8.4 Add Vitest/RTL coverage for EntityLogo B3/US/broker, exact-vs-fuzzy selector, rename and unsupported-type registration, duplicate race, Logo.dev exact/ambiguous/unavailable search and secret/publishable-key boundaries, ambiguous recovery discoverability, quote/FX failures, operations, review, errors, success and selective invalidation.
- [ ] 8.5 Add Playwright smoke for deposit, withdrawal, BUY B3, BUY US, contextual SELL and in-flow asset registration; verify shell discovery and same-key/body retry for an ambiguous POST or FX-expired recovery.
- [ ] 8.6 Run production build, TypeScript, lint and focused frontend/backend test suites; verify all required commands complete successfully.
- [ ] 8.7 Complete manual/automated visual QA in Light and Dark at desktop/tablet/mobile, including mobile keyboard and safe-area states; attach reviewable results.
- [ ] 8.8 Run `npx.cmd openspec validate establish-financial-operations-frontend --strict`; verify strict validation passes after all artifacts and decisions are complete.
