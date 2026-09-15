## Context

See proposal.md for motivation. `carteira_snapshots` already distinguishes materialized valuation from local/unknown state, and `/api/v1/carteira/resumo` already owns current per-position `valorAtualBrl`. The frontend already has a Valore summary shell, cards, `Minhas posições`, theme tokens and Recharts vocabulary.

The real mockups in `docs/ui-reference/` are the visual source of truth for this change: `Imagem do Codex 13 de set. de 2026, 21_17_22.png`, `Imagem do Codex 13 de set. de 2026, 21_17_39.png` and `Imagem do Codex 13 de set. de 2026, 21_17_49.png`. The textual requirements supplied with this change are the functional source of truth for the two charts, because the principal mockup does not render them.

## Goals / Non-Goals

**Goals:**

- Add a private, read-only chronological evolution series from snapshots with materialized valuation only.
- Add a current donut composition of individual ACAO tickers using authoritative summary values.
- Keep the visual hierarchy of the approved summary: heading/refresh, compact metric cards, chart surface, then the existing Minhas posições surface.

**Non-Goals:**

- No migration, snapshot write, market/provider call, interpolation, synthetic value, FII/ETF slice, proventos, recommendation, goal, tax or additional analytics.
- No replacement of the current summary cards, positions table, refresh semantics, valuation formulas or position listing authority.

## Decisions

### 1. A dedicated snapshot endpoint supplies portfolio evolution only

Add `GET /api/v1/carteira/graficos/evolucao` behind the existing ROLE_USER/SecurityContext boundary. Its projection is `dataReferencia`, `totalInvestidoBrl` and `resultadoNaoRealizadoBrl`, ordered ascending. The query filters at the database boundary for the materialized-valuation tuple (`valuationInstant`, `valorPosicoesBrl`, `lucroNaoRealizadoBrl` and `patrimonioTotalBrl` all non-null), so the browser never needs to distinguish a snapshot without valuation from a zero-valued materialized snapshot.

Using `/resumo` for history was rejected because it represents only current valuation and would require invented history. Revaluing historical dates was rejected because it would call providers and change persisted-history meaning.

### 2. Materialized valuation is the objective eligibility gate

A point requires non-null `valuationInstant`, `valorPosicoesBrl`, `lucroNaoRealizadoBrl` and `patrimonioTotalBrl`. `totalInvestidoBrl` is read from the same snapshot only after this gate passes. The response omits snapshots without materialized valuation rather than representing them as zero, null chart points, or interpolated gaps.

This identifies existing materialized valuation without adding validation or recalculation, and avoids asserting a historical result when valuation was unavailable.

### 3. The donut derives from the current summary rather than a second aggregate

Extend summary positions with canonical `tipo`; the BFF preserves it as a string. The client filters `tipo === 'ACAO'` and defined `valorAtualBrl`, keeping one donut datum per ticker. It uses decimal strings for values and only converts a bounded display value at the chart-render boundary if the existing chart library requires it; labels/tooltips retain formatted authoritative decimal strings.

A backend composition endpoint was rejected: it would duplicate the summary's authoritative current valuation, caching and error semantics. Grouping by type was rejected because the required visual unit is ticker.

### 4. Chart placement follows the approved summary composition

The primary mockup establishes full-width content, the card scale and the large bounded `Minhas posições` surface. Preserve that order: heading and refresh remain first; KPI cards retain their current proportional prominence; a single chart band is inserted after those cards and before `Minhas posições`; the table remains full-width below it. On wide desktop, Evolução occupies the dominant left portion (roughly two-thirds) and Composição the complementary right portion (roughly one-third), in equal-height cards with the mock's border, radius and internal padding. This is the smallest adaptation needed to add the required charts without inventing a competing page layout.

At tablet width, retain the band when each chart remains legible; otherwise stack Evolução before Composição. On phone widths, stack both cards, maintain a usable chart height, move/flow legends inside each card, and retain the existing mobile positions disclosure. Theme semantic tokens drive surfaces, grid/elements and financial states; tooltip/legend text and series styles must distinguish values without color alone.

### 5. Same-origin read boundary and query isolation

Add a narrow `/api/finance/portfolio/charts/evolution` BFF GET following the current no-store, server-side bearer and safe-error conventions. Add a dedicated query key for evolution; summary refresh invalidates/refetches summary and evolution only after it succeeds because it may materialize a current-day snapshot. Loading or failure in one chart is isolated from cards, positions and the other chart.

## Risks / Trade-offs

- [Few or no snapshots with materialized valuation] → render an explicit empty state; never backfill points.
- [A current valuation is unavailable] → omit only the affected ACAO slice and state that composition is incomplete; do not infer it from invested value.
- [Decimal chart library input] → keep financial state as decimal strings and confine any display conversion to rendering, with tests for formatting and no calculation.
- [Dense screen widths] → use the established responsive container and stacked cards before allowing clipped legends or a horizontally scrolling chart.
- [Refresh changes current-day snapshot] → invalidate the two documented read keys only after authoritative refresh success.

## Migration Plan

1. Deploy the additive endpoint and summary-position type field with OpenAPI/BFF support.
2. Deploy chart UI with empty/error states so existing accounts without snapshots with materialized valuation remain usable.
3. Roll back frontend independently; the additive endpoint and response field are backward-compatible. No database migration or data backfill is required.

## Open Questions

None. The endpoint shape, materialized-valuation rule, action-only composition and visual adaptation are fixed by this change.

## 6. Refinamento visual dos cards de resumo

Os cards superiores não usam `carteira_snapshots`, `usePortfolioEvolution`, série temporal, `hasUsefulHistory`, linhas, áreas ou barras históricas. Eles usam exclusivamente os valores canônicos já recebidos de `/api/v1/carteira/resumo`, preservados como strings e calculados com `Decimal` quando houver proporções ou comparações.

Patrimônio total exibe uma barra horizontal segmentada de Caixa (`saldoCaixaBrl`) e Posições (`valorPosicoesBrl`). Saldo em caixa e Valor das posições exibem suas participações em `patrimonioTotalBrl`, respectivamente `saldoCaixaBrl / patrimonioTotalBrl` e `valorPosicoesBrl / patrimonioTotalBrl`, somente quando o denominador é positivo; caso contrário, mostram estado neutro. Total investido compara sempre duas barras compactas, Investido (`totalInvestidoBrl`) e Atual (`valorPosicoesBrl`). Resultado realizado usa ícone, valor e o texto “Resultado acumulado com vendas”. Resultado não realizado usa ícone, sinal textual e um marcador direcional neutro/negativo/positivo sem se apresentar como percentual ou escala financeira.

As superfícies permanecem preenchidas no terço inferior do card em Light e Dark, sem dados inventados, duplicados, interpolados ou convertidos para `Number` antes da fronteira de renderização. `Number` pode ser usado apenas para a largura CSS já calculada com `Decimal`. A série de snapshots continua exclusiva da Evolução patrimonial principal; a composição por ação continua exclusiva das posições atuais elegíveis.
