## Context

See `proposal.md` for motivation and the delta specs for behavior. The frontend is Next.js 16.3.3 App Router with Tailwind 4, TanStack Query 5, Shadcn configuration, Vitest/RTL and Playwright. It currently has a root QueryProvider, server-only auth resolver/guard, `auth_session` HttpOnly cookie, same-origin `/api/auth/*`, protected `/inicio` and `/admin`, and a minimal dark-only foundation UI. The backend already provides all essential ROLE_USER contracts: GET/POST `/api/v1/carteira/resumo`, GET positions and transactions with detail, GET cash balance/history, and sanitised ProblemDetail with correlation IDs.

## Goals / Non-Goals

**Goals:**

- Establish a durable authenticated portfolio shell and visual system while preserving the existing session boundary.
- Provide precise, authoritative and responsive read-only finance views through a server-side BFF boundary.
- Make theme, accessibility, error isolation and focused tests first-class implementation concerns.

**Non-Goals:**

- No financial mutation, operation form, selectors, idempotency key, optimistic financial state, dashboard chart, Recharts use, admin experience, backend API change or new app-wide provider beyond theme integration.
- No decimal arithmetic for estimates or inputs; `decimal.js` is deferred to `establish-financial-operations-frontend`.

## Decisions

### Preserve Browser → BFF → Spring auth boundary

Browser client modules call only same-origin `/api/finance/*` using credentials `same-origin`; they do not import server auth, `BACKEND_API_URL`, JWT or Authorization. Route Handlers delegate to `src/server/finance/*`, which reads `auth_session` server-side and invokes Spring with Bearer using the existing config and safe-error conventions. Server layouts use the existing guard directly, not Next-to-Next HTTP. 401 and 403 follow the established auth behavior; finance handlers do not clear cookies arbitrarily.

Alternative considered: browser-to-Spring with CORS or a second finance session. Rejected because it leaks/duplicates auth authority and contradicts the existing session contract.

### Route and shell composition

Add a protected `(portfolio)` route group guarded with `requireRole('ROLE_USER', '/carteira')`; `/inicio` remains guarded and redirects to `/carteira`. Keep `/admin` structurally and behaviorally independent. Render layouts, guard and static shell structure as Server Components. Isolate interactive sidebar collapse, ThemeToggle, mobile disclosures, pagination, refresh and query-backed panels as Client Components. Desktop uses collapsible sidebar plus compact header; tablet reduces/collapses navigation; mobile replaces it with compact header plus safe-area-aware bottom navigation, placing Movimentações under More.

Alternative considered: make the whole portfolio layout a Client Component. Rejected because it weakens the server guard/layout model and expands client JavaScript unnecessarily.

### Theme foundation and visual language

Add `next-themes` because no equivalent theme state exists. Use its class strategy, system preference and persisted selection; place its narrow ThemeProvider below the root document structure with hydration suppression only on the themed root attribute, and use its anti-flash script behavior. Extend `globals.css` into semantic Shadcn-compatible tokens for light and dark, including surfaces, popovers, border, interaction, disabled, destructive, success, warning and info. Light is off-white/white with restrained financial green; dark is charcoal/nearly black with deep purple. Keep the current system font unless implementation inspection identifies an already configured font; define typographic scale, labels and tabular numbers for financial values.

Alternative considered: custom localStorage theme hook. Rejected because it duplicates system/persistence/anti-flash concerns that `next-themes` handles.

### Lossless financial BFF normalization

Add `lossless-json` as a server-side dependency. The shared finance transport reads successful backend response text, parses with lossless numeric preservation before JavaScript Number conversion, validates shape, and maps known financial fields to canonical decimal strings in finance DTOs. IDs/enums/timestamps stay typed strings; pagination counters retain integer semantics only after safe validation. Server-side finance schemas/types/mappers centralize this boundary. The BFF JSON is serialized only after normalization, so React receives decimal strings. `Intl.NumberFormat` is used solely by `Money`, `Percentage`, `Quantity`, and `DateTime` display utilities after explicit safe display conversion or string-aware formatting; original DTO strings remain the source of truth.

`decimal.js` is not added: no calculations, inputs, estimates or payload assembly occur in this read experience. Re-evaluate it for operations.

### Finance BFF mapping and query contract

Implement only these route families, adapting exact paths to current conventions:

| BFF | Spring contract | Query key |
|---|---|---|
| GET `/api/finance/portfolio/summary` | GET `/api/v1/carteira/resumo` | `portfolio.summary` |
| POST `/api/finance/portfolio/summary/refresh` | POST `/api/v1/carteira/resumo/atualizar` | mutation; invalidates summary and position views only when materialized values are consumed |
| GET `/api/finance/positions?page&size` | GET `/api/v1/carteira/posicoes` | `portfolio.positions(page,size)` |
| GET `/api/finance/positions/[assetId]` | GET `/api/v1/carteira/posicoes/{assetId}` | optional detail key |
| GET `/api/finance/transactions?page&size` | GET `/api/v1/carteira/transacoes` | `portfolio.transactions(page,size)` |
| GET `/api/finance/transactions/[id]` | GET `/api/v1/carteira/transacoes/{id}` | `portfolio.transaction(id)` |
| GET `/api/finance/cash` | GET `/api/v1/carteira/caixa` | `portfolio.cashBalance` |
| GET `/api/finance/cash/movements?page&size` | GET `/api/v1/carteira/caixa/movimentacoes` | `portfolio.cashMovements(page,size)` |

Use a central query-key factory, no broad cache reset and no polling. Refresh invalidates/refetches `portfolio.summary` only: market refresh does not alter custody. The summary response is authority for valuation; positions is authority for persisted custody and pagination. Enrichment joins only `ativoId`; an unmatched custody item displays valuation unavailable/not updated, never a browser calculation, a guessed match, or an unlabeled stale value. Endpoint responses need not be atomically consistent. Transactions show broker ID only as a secondary identifier because current response has no broker name. Cash history contains no per-line balance, which is never invented.

### Strict transport, precision and correlation

Every finance handler validates request shape before Spring and always emits `Cache-Control: no-store`. Pagination accepts only `page`/`size`, validates limits, and converts response counters to Number only after finite, non-negative safe-integer validation. Refresh accepts no body or query. The transport flows raw Spring text → `lossless-json` → contract validation → mapper → frontend DTO with canonical decimal strings → BFF JSON. Money/Percentage/Quantity format canonical strings with string-aware formatting; no generic string→Number→Intl path is permitted for arbitrary magnitudes. DateTime may use Date for safe temporal strings. When the established infrastructure accepts a safe incoming correlation ID, the BFF forwards it; a safe Spring response value is returned unchanged to the browser. ProblemDetail remains sanitized.

### Theme scope and routing authority

`next-themes` uses class strategy and a narrow provider; `suppressHydrationWarning` is applied only to `<html>`. Tokens are global, so login/admin are tested for functional, readable Light/Dark behavior but are not visually redesigned. `/inicio` redirect happens only after the server guard confirms ROLE_USER; proxy only handles missing-cookie pre-check and safe returnTo, never authorization.

### Errors, freshness and state handling

Reuse and generalize safe ProblemDetail/correlation parsing so BFF responses preserve safe `status`, `title`, `detail`, `instance`, `X-Correlation-ID` and `Cache-Control: no-store`, never upstream URL or secrets. 409 refresh is a concurrent valuation message plus retry/refetch. 502 is market unavailability and preserves a successful cached UI state. Skeletons reserve panel/table/card space. Error boundaries are panel-scoped: a transaction failure does not erase a loaded summary. Empty states are neutral and carry no disabled operation CTA. Market freshness distinguishes calculation (`valuationInstant`) from quote and FX detail timestamps with accessible tooltip/popover/disclosure.

### Shared components and Shadcn scope

Build shared read components such as AppShell, FinancialPageHeader, ThemeToggle, Money, Percentage, Quantity, GainLoss, FinancialMetric, MarketFreshness, ProblemDetailAlert, EmptyState, ErrorState, FinancialSkeleton, TransactionTypeBadge, PositionCard and responsive table/list primitives. Generate/use only Shadcn primitives required by the implementation: Button, Card, Table, Sheet, DropdownMenu, Tooltip, Skeleton, Badge, Pagination and Separator. No input/operation components are introduced.

### Validation strategy

Vitest/RTL covers shell/routes/navigation, theme Light/Dark/System/toggle/persistence, lossless parsing, DTO normalization and formatters; summary/positions/transactions/cash panels; pagination; loading/empty/error; 401/403/409/502; correlation support code and structural responsive variants. Playwright smoke tests use existing login and a controlled backend/mock pattern to validate landing shell/summary, theme reload persistence, portfolio-to-positions navigation, lists and localized refresh. Tests assert semantics and behavior, not large visual snapshots.

## Risks / Trade-offs

- [Spring numeric JSON shape may vary by serialization configuration] → capture raw text and test representative numeric tokens before normalization; reject malformed required finance shapes as safe BFF errors.
- [Summary valuations and custody listing have different field sets/freshness] → model them separately and label freshness; never merge by assumption.
- [Theme provider can introduce hydration flash] → use class-based provider and root anti-flash/hydration pattern, covered by focused tests.
- [Mobile density can hide financial context] → use prioritised cards plus keyboard-accessible disclosures instead of a squeezed table.
- [Refresh can fail after data exists] → retain authoritative prior response and isolate mutation feedback.

## Migration Plan

1. Add dependencies and visual tokens without changing backend contracts.
2. Introduce shell, guarded routes and `/inicio` compatibility redirect; verify admin regression.
3. Add server-only finance BFF/normalization and read pages behind same-origin routes.
4. Add unit/component and focused Playwright smoke coverage, run lint, typecheck, Vitest, Playwright, the package production build and visual QA at Light/Dark desktop/tablet/mobile.
5. Rollback consists of reverting the frontend change; backend APIs and the existing session cookie remain unchanged.

## Open Questions

Nenhuma.
