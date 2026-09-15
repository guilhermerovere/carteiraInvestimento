## 1. Backend contract and permission foundation

- [x] 1.1 Record the existing cash, transaction, catalog, quote and FX request/response contracts in backend contract tests or API fixtures; verify fixtures match OpenAPI and controller DTOs.
- [x] 1.2 Add ROLE_USER asset-registration use case with ticker and market-only request; verify valid creation is independent of portfolio, cash and broker state.
- [x] 1.3 Authorize controlled asset POST for ROLE_USER while retaining existing ADMIN asset capabilities; verify ROLE_USER can create and cannot edit, delete, activate or deactivate.
- [x] 1.4 Reuse the current trim/uppercase ticker canonicalizer and global unique-ticker constraint; verify case/whitespace normalization and database duplicate enforcement.
- [x] 1.5 Make concurrent duplicate asset registration recoverable using database uniqueness and canonical reload/reuse; verify parallel requests produce one canonical asset.
- [x] 1.6 Reject invalid B3/US ticker-market combinations and provider-known instrument types outside the current `ACAO`/`FII`/`ETF` enum; verify 400 and no persistence.
- [x] 1.7 Enrich valid user-created assets from the existing financial metadata provider; verify provider outages do not create an unvalidated asset and returned metadata is server-derived.
- [x] 1.8 Keep inactive duplicate assets administrative-only and non-BUY-selectable; verify a USER cannot reactivate or mutate one.
- [x] 1.9 Verify global catalog semantics across two users; verify both find the same asset while portfolios and positions remain isolated.

## 2. Automatic branding and backend response extensions

- [x] 2.1 Verify Brapi `logourl`, `requestedSymbol`/`symbol`/`changed`, and Logo.dev ticker/CDN contracts; add adapter fixtures for present, absent, renamed and unavailable responses.
- [x] 2.2 Add optional system-owned asset branding identifier/reference to persistence and catalog responses; verify request DTOs reject logo, logoUrl, provider and arbitrary metadata.
- [x] 2.3 Persist B3 `logourl` as system-owned `BRAPI`/`logoReference` only after HTTPS and trusted-host validation; verify valid BBAS3/PETR4 creation survives absent/malformed logo.
- [x] 2.4 Use Logo.dev ticker CDN references for supported US tickers while Alpha Vantage remains financial-only; verify no Alpha API key is used in image delivery and logo failure does not affect validation.
- [x] 2.5 Configure Logo.dev broker branding: server-side Search API with `strategy=match` and secret key, direct publishable-key delivery only through `img.logo.dev`; verify current vendor docs permit each key's intended use.
- [x] 2.6 Implement high-confidence broker brand resolution using only canonical legal/trade names or an actual existing official domain; verify exactly one exact normalized match persists its canonical domain and ambiguous results do not.
- [x] 2.7 Persist broker `LOGO_DEV`/domain `logoReference` and bounded metadata refresh/cache policy; verify broker list does not perform per-row provider calls.
- [x] 2.8 Keep broker creation and administration ADMIN-only while attempting branding after authoritative validation; verify provider failure does not roll back a compliant broker and ADMIN cannot submit a logo or image URL.
- [x] 2.9 Add optional asset and broker branding metadata to DTOs with explicit Brapi and Logo.dev hosts; verify responses contain no secret credential or provider payload and frontend constructs the publishable image URL.
- [x] 2.10 Add optional name, market, currency and active status to Position response using the existing asset join; verify an inactive held asset has enough data for SELL without the public catalog.
- [x] 2.11 Add the structured `FX_EXPIRED` code to the existing expired-rate 409 mapping; verify other 409 codes/details remain unchanged.
- [x] 2.12 Add a focused total-SELL residual test where `totalInvestidoBrl` differs by one cent from quantity times rounded average; verify all residual cost is removed and accounting invariants remain intact.
- [x] 2.13 Add placeholders for `LOGO_DEV_SECRET_KEY` and the frontend publishable key to `.env.example` and Compose; verify no real keys are committed and each variable is passed only to its intended service.
- [x] 2.14 Wire `LOGO_DEV_SECRET_KEY` through backend server-only configuration and `NEXT_PUBLIC_LOGO_DEV_PUBLISHABLE_KEY` (or the existing equivalent convention) through frontend public configuration; verify Logo.dev origin restrictions and that the secret is absent from bundles, HTML and browser network responses.
- [x] 2.15 Align `PRD.md` before declaring this change complete or archiving: document controlled ROLE_USER canonical asset registration and retained USER/ADMIN limits; verify PRD no longer contradicts implemented authorization.
- [x] 2.16 Document role-specific asset POST `oneOf` request schemas and statuses/responses in OpenAPI; verify USER schema rejects ADMIN name/type fields while existing ADMIN body remains compatible.
- [x] 2.17 Keep financial provider lookup outside short persistence transactions; verify provider timeout does not hold a database write transaction and uniqueness handles concurrent canonicalization.

## 3. Decimal and BFF mutation foundation

- [x] 3.1 Add `decimal.js-light` to the frontend dependency manifest with approximately 50 significant digits; verify package configuration/build resolves without converting financial state to Number.
- [x] 3.2 Implement exact outbound decimal-string serialization using `LosslessNumber`; verify raw JSON numeric tokens preserve `0.10000001`, `0.00000001` and `123456789.12345678` exactly.
- [x] 3.3 Add field-specific BFF validation for UUID, decimal precision/scale, offset timestamp, allowlists, Origin and Idempotency-Key; verify invalid and excess-precision values are rejected without rounding.
- [x] 3.4 Add same-origin BFF deposit/withdraw POST routes for the existing backend endpoints; verify body fields are exactly valor/optional descricao and no-store/correlation/safe ProblemDetail conventions are preserved.
- [x] 3.5 Add same-origin BFF transaction POST route; verify the exact eight backend fields and that visual/quote/FX numeric metadata is excluded.
- [x] 3.6 Add same-origin BFF controlled asset POST and existing paginated asset/broker, quote and FX proxies as needed; verify methods, query allowlists and server-side bearer forwarding.
- [x] 3.7 Add canonical decimal arithmetic and HALF_EVEN estimate helpers; verify money estimates round only at documented final BRL boundaries.
- [x] 3.8 Add deterministic pt-BR DecimalInput parser/formatter primitives; verify comma decimal, intermediate text, scale limits, ambiguous grouping and no silent rounding.
- [x] 3.9 Add MoneyInput and QuantityInput with contract-specific scales and accessible field errors; verify mobile decimal input mode and preservation of valid precision.
- [x] 3.10 Add local date/time serialization with explicit ISO-8601 offset; verify invalid input, UTC offsets and applicable DST transitions.

## 4. Idempotent intent and recovery

- [x] 4.1 Implement a canonical frozen operation intent containing operation kind, final payload and one UUID Idempotency-Key; verify rerenders/editing before confirmation do not create keys.
- [x] 4.2 Disable financial mutation automatic retries and guard pending/double-click submits; verify one confirmation dispatches at most one POST attempt.
- [x] 4.3 Persist minimal ambiguous POST recovery state in sessionStorage and remove it on definitive success/rejection; verify no JWT, auth, password, provider secret or full unrelated portfolio state is stored.
- [x] 4.4 Add shell-discoverable ambiguous intent indicator and read-only recovery surface with safe operation details/context; verify retry uses identical frozen payload/key and UI has no edit/cancel action.
- [x] 4.5 Add safe pre-POST abandonment and FX replacement lifecycle; verify abandoning before POST clears intent, while new FX after an expired 409 requires review and a new key.

## 5. Shell, selectors, and branding presentation

- [x] 5.1 Add the ROLE_USER Operar launcher with Comprar, Depositar and Sacar actions; verify ADMIN sees no personal operation launcher and SELL stays contextual.
- [x] 5.2 Add responsive Dialog/Sheet operation container reusing existing Shadcn primitives; verify desktop dialog and mobile near-full-screen sheet scroll/focus behavior.
- [x] 5.3 Implement EntityLogo with loading, remote failure and deterministic ticker/name fallback; verify no broken image in Light/Dark and accessible image/fallback naming.
- [x] 5.4 Implement paginated remote AssetSelector with logo, ticker, name, market and currency; verify search, keyboard selection and page-bound loading match backend parameters.
- [x] 5.5 Implement compact AssetRegistrationForm with ticker and market only; verify ROLE_USER cannot submit name, currency, logo, provider, UUID, status or metadata.
- [x] 5.6 Integrate not-found asset registration into AssetSelector and BUY; verify successful registration updates only catalog.assets, selects the returned canonical asset and continues BUY without purchasing.
- [x] 5.7 Handle asset duplicates and inactive matches in the selector; verify duplicate recovery selects/reloads the canonical asset and inactive assets are not reactivated or offered for BUY.
- [x] 5.8 Implement paginated active BrokerSelector with broker branding/name fields and load-more; verify filtering is limited to loaded pages and UUID is not the primary label.
- [x] 5.9 Implement no-active-broker state; verify asset registration remains available, BUY cannot confirm, and no broker-creation action is shown.
- [x] 5.10 Configure exact remote image hosts for Brapi and `img.logo.dev`; verify no wildcard or arbitrary URL is accepted and Logo.dev publishable key is used only for official CDN images.
- [x] 5.11 Implement exact active ticker precheck with `/api/v1/acoes/ticker/{ticker}` before offering registration; verify fuzzy results without exact match do not suppress “Cadastrar ticker”.
- [x] 5.12 Handle Brapi historical ticker rename by selecting current canonical ticker and explaining the mapping; verify no alias is created and existing canonical records are reused.

## 6. Market context and operation flows

- [x] 6.1 Add manual quote consultation with price, currency, provider and freshness; verify quote remains optional and price stays user-editable.
- [x] 6.2 Handle quote 404/502 without clearing the form; verify a manual price can continue where the backend contract permits.
- [x] 6.3 Add USD/BRL context using the returned persisted FX record; verify freshness uses registradoEm and POST stores only exchangeRateId.
- [x] 6.4 Add FX_EXPIRED recovery that preserves fields, refreshes rate, recalculates estimates and requires renewed review; verify replacement FX gets a new payload/key.
- [x] 6.5 Implement deposit form/review with current balance, amount and estimated projected cash; verify scale-two BRL validation and exact cash contract.
- [x] 6.6 Implement withdrawal form/review with current balance, amount and estimated projected cash; verify over-known-balance warning is advisory and does not claim backend rejection.
- [x] 6.7 Implement BUY B3 form/review with asset, broker, quantity, editable quote/manual price, fees and offset date; verify B3 payload omits/nulls FX per real contract.
- [x] 6.8 Implement BUY US form/review with FX panel and BRL estimates; verify exchangeRateId is required and numeric FX is absent from transaction POST.
- [x] 6.9 Add BUY average-cost estimate (current average, purchase cost and estimated new average); verify estimates do not change the audited backend formula or present themselves as booked values.
- [x] 6.10 Add contextual SELL from open position with locked asset, quantity available, average cost, broker, fees and date; verify SELL cannot switch the selected asset.
- [x] 6.11 Support SELL of inactive held assets using Position metadata and manual price when quote is unavailable; verify no active catalog entry is required.
- [x] 6.12 Add SELL quantity/known-holding advisory and estimated net proceeds/realized result; verify partial SELL display keeps the existing average cost unchanged.
- [x] 6.13 Implement shared review/edit/confirm and operation result states; verify first submit opens review, edit returns to form, and only explicit confirm POSTs.
- [x] 6.14 Handle success from backend response as authoritative and announce via aria-live; verify later query-refetch failure does not turn a successful POST into an error.
- [x] 6.15 Implement precise success/error messaging for 400, 401, 403, 404, generic 409, 502 and ambiguous network failure; verify 409 detail text is not semantically parsed.

## 7. Query behavior, accessibility, and responsive refinement

- [x] 7.1 Add selective cash invalidation for summary, cash balance and cash-movement prefix; verify no broad invalidate-all call occurs.
- [x] 7.2 Add selective BUY/SELL invalidation for summary, positions, transactions and cached exact asset position; verify query-key factory is reused.
- [x] 7.3 Verify asset registration invalidates only catalog.assets and never financial position/summary/transaction caches.
- [x] 7.4 Complete keyboard, focus trap/restore, Escape-before-POST, labels, descriptions, aria-describedby/invalid and screen-reader selector/form behavior; verify RTL accessibility assertions.
- [x] 7.5 Refine mobile safe area, keyboard, scrolling, touch target and action placement; verify content and confirmation controls remain reachable at phone viewport sizes.
- [x] 7.6 Verify desktop, tablet and mobile layout in Light and Dark for launcher, selectors, forms, review, success and errors; verify long financial values wrap/read without clipping.
- [x] 7.7 Verify reduced motion, high contrast and non-color-only state cues across operation and fallback components; verify automated accessibility checks pass.

## 8. Verification and delivery readiness

- [x] 8.1 Add backend tests for USER asset creation/authorization, canonicalization, duplicates/concurrency, provider metadata/logo absence/failure, ADMIN-only broker management, Position metadata, inactive SELL and FX_EXPIRED.
- [x] 8.2 Add backend regression tests for cash/auth/isolation and BUY/SELL accounting without changing average-cost implementation; verify suite covers the SELL-total residual case.
- [x] 8.3 Add Vitest/RTL coverage for locale parsing, field-specific precision/HALF_EVEN, quote scale 4 vs transaction scale 8, raw numeric JSON tokens, BFF validation and idempotent recovery; verify listed precision edge cases.
- [x] 8.4 Add Vitest/RTL coverage for EntityLogo B3/US/broker, exact-vs-fuzzy selector, rename and unsupported-type registration, duplicate race, Logo.dev exact/ambiguous/unavailable search and secret/publishable-key boundaries, ambiguous recovery discoverability, quote/FX failures, operations, review, errors, success and selective invalidation.
- [x] 8.5 Add Playwright smoke for deposit, withdrawal, BUY B3, BUY US, contextual SELL and in-flow asset registration; verify shell discovery and same-key/body retry for an ambiguous POST or FX-expired recovery.
- [x] 8.6 Run production build, TypeScript, lint and focused frontend/backend test suites; verify all required commands complete successfully.
- [x] 8.7 Complete manual/automated visual QA in Light and Dark at desktop/tablet/mobile, including mobile keyboard and safe-area states; attach reviewable results.
- [x] 8.8 Run `npx.cmd openspec validate establish-financial-operations-frontend --strict`; verify strict validation passes after all artifacts and decisions are complete.

## 9. Manual QA refinement, administration and account settings

- [x] 9.1 Refine proposal, design, tasks, PRD and affected delta specs for operation presentation, administrative catalogs and account settings; verify strict OpenSpec validation before implementation.
- [x] 9.2 Move desktop Operar from the header/floating layer to the lower sidebar immediately above Recolher; verify expanded alignment and no global SELL.
- [x] 9.3 Keep Operar functional, centered, named and tooltip-enabled in collapsed sidebar, and integrate it through the existing mobile Mais navigation without a header duplicate.
- [x] 9.4 Center and constrain the desktop operation Dialog and preserve responsive tablet/mobile sheet scrolling, focus, safe-area and reachable actions.
- [x] 9.5 Introduce typed operation-result presentation for deposit, withdrawal, BUY and SELL; verify friendly summaries and that no raw JSON/DTO/ProblemDetail is rendered.
- [x] 9.6 Centralize visible pt-BR safe error mapping for required statuses, structured FX_EXPIRED and ambiguous network outcomes; verify technical detail remains internal.
- [x] 9.7 Distinguish exact asset absence from validation-provider unavailability; preserve ticker, offer retry, and keep USER registration unavailable until mandatory validation can run.
- [x] 9.8 Implement a dedicated responsive ROLE_ADMIN Valore shell and simple catalog overview with no personal wallet or operation controls.
- [x] 9.9 Add minimal same-origin ADMIN BFF routes for existing broker and asset list/create/edit/lifecycle contracts with server-side bearer and Origin validation.
- [x] 9.10 Implement ADMIN broker list/detail presentation and create/edit forms using only real contract fields and automatic branding fallback.
- [x] 9.11 Implement ADMIN broker activate/deactivate controls with accessible confirmation and friendly pt-BR success/error feedback.
- [x] 9.12 Implement ADMIN asset list and create form using ticker, name, type and market only, with EntityLogo fallback.
- [x] 9.13 Implement ADMIN asset name edit and activate/deactivate controls without structural identity changes or physical DELETE.
- [x] 9.14 Verify ADMIN/USER route authorization and responsive Light/Dark behavior across overview, brokers and assets.
- [x] 9.15 Add accessible desktop/mobile profile menus with Configuracoes immediately before Sair and protect `/configuracoes` for the current principal.
- [x] 9.16 Implement backend self-profile name/email updates with trim, validation, canonicalization, case-insensitive uniqueness and current-principal ownership.
- [x] 9.17 Implement backend current-password verification and password replacement using the existing policy and BCrypt contract without exposing sensitive data.
- [x] 9.18 Implement transactional account closure with wallet lock, zero-cash and no-open-position revalidation, inactive/anonymized identity, preserved history and no schema migration.
- [x] 9.19 Add sanitized audit events for email/password changes and account closure, with no sensitive payload or financial values.
- [x] 9.20 Add minimal same-origin account BFF routes for profile, password and closure with Origin validation and no browser JWT exposure.
- [x] 9.21 Implement the Settings page sections Dados pessoais, Seguranca and Zona de perigo with immediate current-user refresh and pt-BR feedback.
- [x] 9.22 Add strong closure confirmation requiring current password and explicit text, with known ambiguous-intent, cash and position blocking guidance.
- [x] 9.23 Clear auth/personal caches and redirect after password change or closure; verify closed-account login and cached/back access fail under persisted-user validation.
- [x] 9.24 Add RTL tests for deposit/withdraw/BUY/SELL friendly results, absence of raw JSON, pt-BR mapping, sidebar placement/collapse/mobile and provider-unavailable registration UX.
- [x] 9.25 Add/reuse backend and BFF tests for ADMIN broker/asset list/create/edit/lifecycle and role authorization.
- [x] 9.26 Add backend settings tests for ownership, canonical email/duplicate, password policy/current verification/hash safety, closure eligibility/race/history and sanitized audit.
- [x] 9.27 Add frontend settings tests for profile menu/guard, name/email/password, conflicts, Danger Zone confirmation/blocking/success, redirect, accessibility and no raw JSON.
- [x] 9.28 Extend focused Playwright smoke for sidebar Operar/dialog, friendly deposit, unavailable provider, ADMIN catalogs, profile rename and closure blocking using non-destructive fixtures.
- [x] 9.29 Complete and correct visual QA in Light/Dark at 1440, 1100, 768, 390x844 and 360x740 for USER operations/settings and ADMIN catalogs.
- [x] 9.30 Verify labels, descriptions, focus/restore, keyboard/Escape/outside click, aria-live/invalid/describedby, touch targets, reduced motion and non-color-only state.
- [x] 9.31 Run full backend `clean verify`; verify unit/integration/Flyway/security regressions are green.
- [x] 9.32 Run frontend Vitest/RTL, lint, typecheck and production build; verify all scripts are green.
- [x] 9.33 Run focused Playwright and `npm audit`; document only known non-fixable dev-only findings without force upgrades.
- [x] 9.34 Run strict change validation and `npx.cmd openspec validate --all`; verify both pass after all artifacts/tasks are coherent.
- [x] 9.35 Run final git diff/status checks, inspect unexpected files, and report completion without commit/archive/merge/rebase/Graphify updates.

## 10. Final product correction: discovery, precision and minimalist presentation

- [x] 10.1 Update proposal, design, PRD and affected delta specs for the user asset route, provider discovery/registration, automatic quotation, average-price presentation, strict BigDecimal input behavior and compact catalog UI; validate the change strictly before implementation.
- [x] 10.2 Add or refine normalized B3/US discovery, validation and quotation so public B3 search is not blocked solely by a missing optional token; classify configuration, authorization, throttling, timeout and upstream errors safely without raw provider payloads.
- [x] 10.3 Implement the ROLE_USER `/carteira/ativos` route and navigation, reuse discovery in BUY, and support add-and-select without creating a position, transaction or cash movement.
- [x] 10.4 Fetch a quote automatically after selection/registration, preserve editable price and friendly fallback, and add bounded metadata enrichment/sync without per-row listing calls.
- [x] 10.5 Refine ADMIN assets and brokers to compact dialog/contextual UI, using provider-assisted asset registration while preserving compatible ADMIN APIs and lifecycle semantics.
- [x] 10.6 Complete string-aware display formatters and strict MoneyInput, QuantityInput and PriceInput behavior for every editable financial BigDecimal; cover focus/blur, paste, scale, precision, pt-BR messages and exact BFF numeric JSON.
- [x] 10.7 Surface formatted average price in positions and BUY/SELL form/review, including estimated BUY average and unchanged partial-SELL average.
- [x] 10.8 Apply the minimalist visual pass to user assets, positions, operation dialog and collapsed Operar popover while preserving settings, ADMIN, responsive Light/Dark and accessibility behavior.
- [x] 10.9 Add backend/Vitest/RTL/Playwright coverage for discovery, quote, registration, precision inputs, average price, catalog dialogs and collapsed Operar; retain deterministic provider mocks and optional live probes only when credentials exist.
- [x] 10.10 Run backend clean verify, frontend test/lint/typecheck/build, focused Playwright, strict change/all validation and final git diff checks; report credentials and manual visual review status without commit/archive/merge/rebase/Graphify updates.

## 11. Final manual-QA regressions: transaction resilience and collapsed launcher

- [x] 11.1 Record the transaction-history independence and collapsed Operar popover acceptance criteria in the affected design/spec artifacts; re-open final QA for these regressions.
- [x] 11.2 Keep `/carteira/transacoes` rendered from its persisted ledger query when complementary market/provider work fails, and refine the collapsed desktop Operar menu into an anchored compact popover with keyboard-safe behavior in Light and Dark.
- [x] 11.3 Add focused RTL and Playwright coverage for persisted transaction visibility under a market/provider failure, collapsed Operar positioning/actions, and final visual QA; run focused tests, frontend checks, strict OpenSpec validation and final diff review.

## 12. Final QA bugfix pass

- [x] 12.1 Enforce positive integer quantities end-to-end; add visual BRL/USD input prefixes, exact formatters, resilient broker refetch, the Ativos-to-BUY launcher, and persisted transaction ledger metadata without provider dependencies; run the complete required verification suite.


## 13. Final QA regression: BUY-to-history persistence

- [x] 13.1 Corrigir regressao de Transacoes apos BUY: rastrear POST, commit, GET do historico, BFF, mapper, query key, invalidacao/refetch, DTO nullable, joins, serializacao e paginacao; corrigir a causa raiz sem alterar regras financeiras do backend. Adicionar teste persistente de backend e Playwright do fluxo UI -> BFF -> backend real: abrir historico, concluir BUY, voltar/refazer historico, receber 200 e mostrar imediatamente a compra, inclusive com falha de provider de mercado, sem 404/502 nem tela de erro. Manter a task pendente ate esse Playwright passar.

## 13. Final QA formatting and portfolio resilience

- [x] 13.2 Remove BUY average-price previews; enforce digit-only positive quantities; apply BRL/USD two-decimal display and prefixes; render real per-asset average price; preserve persisted portfolio data under valuation outage and recover market values after refetch; retain history after BUY. Add focused regressions and run required verification.
- [x] 13.3 Replace active US discovery/quote/USD-BRL wiring with Twelve Data, remove Alpha Vantage runtime calls, preserve persisted portfolio data when market valuation is unavailable, and complete final format/valuation/provider QA.

## 14. QA corretoras / CVM

- [x] 14.1 Restore the established provider-backed CVM lookup by exact canonical CNPJ; retain canonical/masked CNPJ behavior, distinct 422/409/502 mappings, and non-blocking Logo.dev branding.
- [x] 14.2 Remove runtime regulatory authority from the partial `cad_intermed` baseline, snapshot refresh, and root matching; provider unavailability must map to `BROKER_VALIDATION_UNAVAILABLE`, never to absence.
- [x] 14.3 Verify XP, Ágora CTVM, and BTG Pactual CTVM use the same generic provider rule; verify an invalid broker, duplicate, provider outage, focused backend/frontend tests, full backend verification, Playwright Admin Corretoras, strict/all OpenSpec validation, and `git diff --check`.
