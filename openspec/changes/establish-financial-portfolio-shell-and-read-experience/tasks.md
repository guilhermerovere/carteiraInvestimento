## 1. Dependencies and theme foundation

- [ ] 1.1 Add `next-themes` and server-only `lossless-json` to frontend manifests without adding `decimal.js`, and verify the lockfile resolves exactly those dependency decisions
- [ ] 1.2 Add the narrow class-based ThemeProvider integration with Light, Dark and System persistence/anti-flash behavior, and verify the root layout hydrates without relevant mismatch warnings
- [ ] 1.3 Preserve the existing QueryProvider and prove theme integration does not create a second query/auth provider through a focused layout test

## 2. Design tokens and global styling

- [ ] 2.1 Replace foundation-only CSS with semantic Shadcn-compatible light/dark tokens for surfaces, interaction and status semantics, and verify no scattered raw theme colors are needed by financial components
- [ ] 2.2 Define financial typography, tabular numeric treatment, focus styles and reduced-motion behavior, and verify contrast/focus checks for both themes
- [ ] 2.3 Implement accessible ThemeToggle with Light/Dark/System selection and visible state, and verify keyboard operation and persisted selection tests

## 3. Authenticated shell

- [ ] 3.1 Create the ROLE_USER guarded portfolio route-group layout using the existing server-only role guard, and verify no client component gains token or backend access
- [ ] 3.2 Implement AppShell desktop sidebar, contextual header, profile and existing logout affordance without operation controls, and verify semantic landmarks and active navigation behavior
- [ ] 3.3 Implement collapsible sidebar interaction with reduced-motion support, and verify focus remains usable through collapse and expansion

## 4. Navigation and compatibility routes

- [ ] 4.1 Add `/carteira`, `/carteira/posicoes`, `/carteira/transacoes` and `/carteira/movimentacoes`, and verify each has the ROLE_USER route guard
- [ ] 4.2 Convert `/inicio` into a guarded compatibility redirect to `/carteira`, and verify existing admin routing and access-denied semantics remain unchanged
- [ ] 4.3 Update protected-route pre-check configuration for the new portfolio paths while preserving safe returnTo behavior, and verify anonymous redirects and ROLE_ADMIN denial tests

## 5. Finance BFF shared infrastructure

- [ ] 5.1 Create server-only finance configuration/transport that reuses auth cookie/config conventions and attaches Bearer only server-side, and verify browser code cannot import secrets or Authorization
- [ ] 5.2 Add shared safe ProblemDetail, correlation-ID round-trip and no-store response mapping for finance handlers, and verify sensitive upstream content is sanitized
- [ ] 5.3 Implement strict pagination and refresh validation (only page/size; no refresh body/query) and verify unsupported input never reaches Spring

## 6. BigDecimal lossless DTO normalization

- [ ] 6.1 Implement raw-text lossless parsing before any numeric conversion, and verify `0.10000001`, `0.00000001`, positive and negative large decimals survive unchanged
- [ ] 6.2 Define centralized backend DTO, normalized frontend DTO and mapper modules for summary, positions, transactions and cash, and verify all finance decimal fields are strings
- [ ] 6.3 Create string-aware Money, Percentage, Quantity and DateTime utilities that retain source strings, and verify arbitrary-magnitude BRL, negative percentage and fractional quantity formatting without generic Number conversion

## 7. Portfolio summary

- [ ] 7.1 Add summary and summary-refresh BFF Route Handlers mapped to the existing valuation endpoints, and verify GET does not mutate while POST forwards no body/query
- [ ] 7.2 Add portfolio query-key factory, summary hook and explicit refresh mutation that invalidates only summary/no optimistic result, and verify duplicate prevention
- [ ] 7.3 Build the `/carteira` hierarchy with primary patrimônio, contextual unrealized performance, secondary metrics and valued-position preview, and verify all summary states render

## 8. Positions read experience

- [ ] 8.1 Add positions BFF list/detail Route Handlers and normalized paginated DTOs mapped to current backend fields, and verify safe integer pagination counters and no fabricated values
- [ ] 8.1a Implement positions enrichment only by `ativoId` from the latest summary, and verify missing valuation is labelled unavailable rather than calculated, guessed or stale
- [ ] 8.2 Implement desktop responsive financial positions table with prioritised fields and accessible disclosure for secondary data, and verify table semantics and pagination
- [ ] 8.3 Implement compact mobile PositionCard/list representation with expandable details, and verify it replaces oversized horizontal-table behavior at mobile breakpoints

## 9. Transactions read experience

- [ ] 9.1 Add transactions BFF list/detail Route Handlers and normalized page/detail DTOs, and verify broker is rendered only as backend-provided identifier
- [ ] 9.2 Implement transactions list with textual BUY/SELL, ticker, broker ID, date, BRL value and realized result where applicable, and verify non-color-only type/result semantics
- [ ] 9.3 Add accessible transaction details for quantity, currency, unit price, fees, historical FX and timestamps, and verify no edit/delete/operation form is exposed

## 10. Cash movements read experience

- [ ] 10.1 Add cash-balance and cash-movements BFF handlers with normalized DTOs, and verify history fields exactly match the existing contract
- [ ] 10.2 Implement cash balance plus paginated movement history with neutral empty state, and verify it never invents per-row resulting balance
- [ ] 10.3 Verify the read experience does not expose deposit/withdraw mutation endpoints, Idempotency-Key or related controls

## 11. Error, ProblemDetail and freshness experience

- [ ] 11.1 Implement panel-scoped loading skeleton, empty and retryable error primitives with reserved layout dimensions, and verify a list failure preserves loaded summary content
- [ ] 11.2 Implement ProblemDetailAlert with secondary copyable correlation ID and correct 401/403 behavior via existing session flow, and verify cookie/session preservation rules
- [ ] 11.3 Implement 409 valuation-concurrency and 502 market-unavailability refresh feedback while retaining safe prior data, and verify retry/refetch behavior
- [ ] 11.4 Implement MarketFreshness disclosure separating valuation, quote and FX instants, and verify no simultaneous-timestamp claim is rendered

## 12. Responsive and mobile behavior

- [ ] 12.1 Tune desktop/tablet shell breakpoints so sidebar reduction does not constrain content, and verify each route remains legible at representative widths
- [ ] 12.2 Add safe-area-aware mobile header and bottom navigation with More → Movimentações, and verify touch target size and active-route state
- [ ] 12.3 Ensure financial metric and list layouts preserve information priority without fixed-width overflow, and verify mobile cards/disclosures are keyboard reachable

## 13. Accessibility

- [ ] 13.1 Audit landmarks, headings, labels, visible focus and keyboard paths for shell, navigation, theme control, pagination and disclosures, and verify with RTL role-based tests
- [ ] 13.2 Add `aria-live` announcements for refresh and error outcomes and appropriate table/sheet/tooltip semantics, and verify screen-reader text is concise and present
- [ ] 13.3 Verify WCAG AA contrast in both themes and status communication by sign/text/icon in addition to color, and document the automated/manual audit result

## 14. Vitest and React Testing Library

- [ ] 14.1 Add unit tests for theme modes/persistence, tokens/classes, AppShell, responsive navigation and Light/Dark login/admin regressions, and verify focused tests pass
- [ ] 14.2 Add BFF strict transport/parser/normalizer tests for no-store, correlation, safe pagination and precision, and verify Numbers are never canonical financial DTO values
- [ ] 14.3 Add component tests for summary, positions, transactions, cash, pagination, loading, empty, errors, 401, 403, 409 and 502, and verify component isolation

## 15. Playwright smoke tests

- [ ] 15.1 Add controlled login-to-`/carteira` smoke flow and verify shell plus summary render without exposing token data
- [ ] 15.2 Add Light→Dark→reload persistence smoke flow and verify selected theme remains applied
- [ ] 15.3 Add portfolio preview→positions and navigation→transactions/movements smoke flows, and verify each selected route renders
- [ ] 15.4 Add controlled refresh smoke flow showing local loading and returned summary state, plus readable login/admin theme regression smoke, and verify no financial operation E2E is added

## 16. Validation

- [ ] 16.1 Run frontend lint, typecheck, Vitest, focused Playwright smoke suite and the package production build, and resolve regressions within this change scope
- [ ] 16.1a Perform and document manual/structural visual QA for Light and Dark at desktop, tablet and mobile, covering overflow, hierarchy, contrast, spacing, density, navigation, table/cards and loading/empty/error states
- [ ] 16.2 Run `npx.cmd openspec validate establish-financial-portfolio-shell-and-read-experience --strict` and verify the change is strictly valid
- [ ] 16.3 Run `git diff --check` and `git status`, and verify only intended planning/implementation files are present with no commit, archive, backend change or Graphify update
