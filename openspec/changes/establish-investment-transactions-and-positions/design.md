## Context

See [proposal.md](proposal.md) for motivation and the two delta specs for normative behavior. The backend already has PostgreSQL/Flyway through V8, Hibernate validation, a BRL wallet and immutable cash ledger, `UPDATE ... RETURNING` balance mutations, transaction-scoped cash idempotency, daily snapshots, JWT-derived ownership, ProblemDetail/correlation ID, global Ativo/Corretora catalogs, local immutable `historico_cambio` and normal/isolated audit ports.

The current seams shape this design: cash resolves the primary wallet before reservation, reserves idempotency before mutating the wallet, and currently rewrites snapshot investment fields to zero; Ativo and Corretora ports offer only ordinary reads; the FX history port only writes; `AuditoriaCommand` carries no instant and its adapter uses `Instant.now()`. The existing Graphify map located these same seams. No stored Explore report for this change was found, so this design uses the PRD, main specs, specified archives, V1–V8 and current implementation as authority.

## Goals / Non-Goals

**Goals:**

- Introduce one transactional application boundary for BUY/SELL whose persisted result is replayable without reading volatile financial state.
- Make the wallet row the serialization point for all financial mutations in one wallet while protecting catalog lifecycle with the weakest sufficient PostgreSQL lock.
- Keep cost accounting exact enough for the declared formulas and database scales, with explicit overflow/invariant checks.
- Evolve snapshot composition once so transaction and cash flows share local-state semantics.
- Reuse existing security, error, correlation and auditing infrastructure with minimal compatible extensions.

**Non-Goals:**

- No provider invocation, market valuation, current quote lookup, cash USD, frontend/dashboard, transaction correction/reversal, short selling or deletion.
- No changes to V1–V8, PRD or existing market-quote/provider behavior.
- No distributed lock or separate idempotency service.

## Decisions

### Domain model and arithmetic

Add pure domain records/aggregates for `Transacao`, `Posicao` and transaction type. Request fields `quantidade`, `precoUnitario` and `taxas` are parsed directly as `BigDecimal`, never through `double`/`float`, and accepted only when their exact value is representable as `NUMERIC(18,8)` without value-changing rounding. Thus `10`, `10.0`, `10.00000000` and `10.0000000000` are equivalent and valid, while `10.123456789` is invalid because the ninth decimal is significant. Input precision/scale overflow, non-positive quantity/price and negative fees are static 400 errors. Canonical fingerprint decimals use `stripTrailingZeros().toPlainString()`, so extra trailing zeros are equivalent. `HALF_EVEN` applies only to derived materializations defined by the contract: final BRL money uses scale 2 and average price uses scale 8. It is never used to repair an excessive-precision input. Exact intermediates remain arbitrary-precision `BigDecimal`; a valid input whose derived result cannot fit a persistent type or invariant is a 409 with full rollback.

`totalInvestidoBrl` is authoritative cost. BUY adds the rounded final operation cost in cents, persists `resultadoRealizadoBrl = NULL`, then derives average price from total/quantity. Partial SELL derives a cent-scale cost base from sold quantity times the stored scale-8 average, subtracts it from total invested and leaves the stored average unchanged. Total SELL alone uses the whole remaining total cost. SELL persists a mandatory signed result and adds it to signed accumulated realized profit. A partial sale that would leave an open position with non-positive/inconsistent materialized cost, or a derived result such as accumulated profit that cannot fit `NUMERIC(18,2)`, is rejected as a 409 result invariant with full rollback instead of silently clamping, deleting the position, changing its average or inventing cost.

Alternative considered: carry unrounded hidden cost or reconstruct total from average price. Rejected because the prompt makes the cent-scale total the materialized source of truth and explicitly assigns final residual absorption to total SELL.

### V9 schema and relational integrity

Create only `V9__establish_investment_transactions_and_positions.sql`. It creates:

- `transacoes` with the exact requested columns, scale/check constraints, type/result consistency and restrictive FKs. The existing `(carteira_id,usuario_id)` unique key supports the same composite ownership FK used by cash. `corretora_id` and `exchange_rate_id` reference `corretoras` and `historico_cambio` with `ON DELETE RESTRICT`.
- `posicoes` with the requested fields, `UNIQUE(carteira_id,acao_id)`, restrictive FKs and one CHECK expressing either the fully zero custody state or the fully positive open state; realized accumulated profit is signed.
- `transacoes_idempotencia` with unique `(carteira_id,idempotency_key)`, fingerprint and reservation/completion consistency checks. The typed completion projection includes at minimum `transacao_id`, `valor_origem NUMERIC(36,16)`, `saldo_caixa_brl_resultante`, `posicao_id`, `posicao_quantidade_resultante`, `posicao_preco_medio_brl_resultante`, `posicao_total_investido_brl_resultante`, `posicao_lucro_realizado_acumulado_brl_resultante` and `posicao_ultima_atualizacao_resultante`, plus the technical reservation/completion timestamps. `valor_origem` is not a column of `transacoes`: it belongs only to this idempotent completion projection and materializes the exact `quantidade * preco_unitario`; `NUMERIC(36,16)` covers the exact product of two `NUMERIC(18,8)` operands. No arbitrary JSON result is used.

V9 adds a unique key `(id,moeda)` to `acoes` and uses composite `(acao_id,moeda)` FK from `transacoes`, so database writes cannot diverge from the asset currency without altering V4. Transaction checks couple BRL to null FX and rate 1, and USD to non-null FX. The FX FK proves provenance; pair/freshness remain application checks because freshness is time-dependent and V8 structurally admits only USD→BRL.

V9 changes the three valuation-dependent snapshot columns `valor_posicoes_brl`, `lucro_nao_realizado_brl` and `patrimonio_total_brl` to nullable and replaces the old all-nonnegative CHECK with checks that forbid partial valuation state: the three columns are either all `NULL` or all non-null; when known, patrimony is coherent with cash plus position value. `saldo_caixa_brl` and `total_investido_brl` remain `NOT NULL` and nonnegative. The application writes `valor_posicoes_brl = 0`, `lucro_nao_realizado_brl = 0`, `patrimonio_total_brl = saldo_caixa_brl` and `total_investido_brl = 0` whenever no open position exists. Existing V6 rows remain valid because they contain zero/non-null values; no data rewrite is required.

Indexes follow supported reads: transactions by `(carteira_id,data_registro DESC,id DESC)`, transaction ownership/id lookup as needed, open/list positions via `(carteira_id,acao_id)` plus an index supporting open positions joined to ticker, and idempotency uniqueness. Avoid speculative filters.

Alternative considered: a trigger to inspect `acoes.moeda`. Rejected in favor of declarative composite FK, which is easier to validate and keeps lifecycle logic out of triggers.

### Static normalization, fingerprint and idempotent replay

Before touching volatile resources, presentation/application performs strict request-shape checks, validates the raw key, normalizes scale-8 decimals and `dataNegociacao`, and creates the fingerprint. `OffsetDateTime` is required at the HTTP boundary; it is converted to `Instant` and truncated to PostgreSQL microsecond precision before fingerprinting, persistence and response. This makes semantically equal offsets identical and prevents first-response/replay drift from TIMESTAMPTZ precision.

Extract the existing length-prefixed SHA-256 writer into a generic canonical fingerprint component used by cash and transactions without changing cash outputs. Transaction fingerprint version is independent and contains only the specified normalized request fields, including an explicit null/non-null marker for FX.

After an ordinary read resolves the authenticated user's wallet id (not a row lock), reserve with `INSERT ... ON CONFLICT DO NOTHING RETURNING`. As in cash, a contender waits on an uncommitted unique row; after winner rollback it may reserve, and after commit it reads the completed result. Existing reservation comparison happens before lifecycle/FX/balance/position work. A matching completed row loads the immutable Transacao plus the typed stored response projection and returns immediately; it does not recalculate `valorOrigem`, read current cash, read current Posicao, capture a new operation instant or validate FX again. Mismatches are 409. Incomplete committed rows are internal invariant failures.

Alternative considered: reconstruct replay from current wallet/position or JSON response. Rejected because mutable projections would drift and arbitrary JSON weakens schema validation; typed columns preserve the original contract.

### Transaction boundary and deterministic lock order

One Spring transaction contains reservation through audit and completion. For every new request, lock acquisition is:

1. idempotency reservation;
2. Ativo `FOR SHARE`/`PESSIMISTIC_READ`;
3. Corretora `FOR SHARE`/`PESSIMISTIC_READ`;
4. Carteira `FOR UPDATE`/`PESSIMISTIC_WRITE`;
5. Posicao `FOR UPDATE` when present;
6. current-day snapshot through wallet-serialized upsert;
7. audit write and idempotency completion.

Add purpose-specific protected-read methods to Ativo/Corretora persistence rather than changing ordinary catalog reads. PostgreSQL share locks allow concurrent readers but block lifecycle UPDATE until commit. SELL loads an inactive Ativo through the protected unrestricted lookup; BUY additionally validates active. Corretora must be active in both.

The wallet lock is explicit before position access. BUY then uses the existing conditional debit `UPDATE ... RETURNING`; SELL uses checked credit. Either may perform provisional balance mutation before a later invariant is discovered because the surrounding transaction rolls it back. Position creation is serialized by the wallet lock and still protected by its UNIQUE constraint. This order is compatible with cash, which reserves then mutates/locks the wallet and never locks Ativo/Corretora/Posicao.

Alternative considered: position-level locking alone for different assets. Rejected because balance and snapshot are wallet-wide and the requested global order designates Carteira as the common financial lock.

### Local FX and provider isolation

Extend the FX history port with an id lookup that returns the immutable persisted observation without cache/provider access. For a new US operation, capture `dataRegistro` exactly once from the `Clock` only after idempotency has established that the request is not replay. Read the immutable FX row after the comparison and protected catalog reads but before monetary mutation; it requires no lifecycle lock. Validate USD→BRL with the inclusive deadline `dataRegistro <= historicoCambio.registradoEm + 5 minutos`, using that same `dataRegistro`, with no second `Clock` read and without using `instanteCotacao`. At the exact deadline the observation is valid; the next representable instant is 409. Confirmed replay captures no new `dataRegistro` and performs no FX validation. B3 bypasses the FX port completely. Market quote, Receita, CVM, CEP and FX provider ports are absent from the transaction service dependencies, making the no-provider rule structurally testable.

### Snapshot composition shared by transactions and cash

Replace the narrow cash snapshot write contract with a local snapshot composer/persistence port that runs only after the wallet lock. It obtains `SUM(total_investido_brl)` and open-position existence from PostgreSQL. Transaction mutations always invalidate valuation when any open position remains; with none, all locally known zero values and cash-only patrimony are written.

Cash-only mutations preserve the current day's known `valor_posicoes_brl` and `lucro_nao_realizado_brl` when both are non-null and recompute patrimony from new cash. With an open position, valuation state is coherent: if valuation is unknown, `valor_posicoes_brl`, `lucro_nao_realizado_brl` and `patrimonio_total_brl` are all `NULL`, while real cash and locally recomputed total invested remain populated. An absent current-day snapshot with open positions has no valuation to preserve: the first DEPOSITO/SAQUE of the new day writes that unknown triplet, never copies valuation from a previous date and never calls a provider. With no open position, it writes the fully known zero state and patrimony equal to cash. `INSERT ... ON CONFLICT DO UPDATE` remains the database uniqueness defense. No previous date is targeted.

Alternative considered: have each flow write its own snapshot formula. Rejected because that caused the existing artificial-zero bug and would let transaction/cash semantics drift.

### API, ownership and stable response models

Use two ROLE_USER-only controllers under `/api/v1/carteira`, strict local DTOs and explicit response records. POST accepts exactly the seven body fields from the prompt and the mandatory header. Define one public Transacao model with exactly `id`, `ativoId`, `ticker`, `corretoraId`, `exchangeRateId`, `tipo`, `quantidade`, `moeda`, `precoUnitario`, `taxas`, `taxaCambioBrl`, `valorTotalBrl`, `resultadoRealizadoBrl`, `dataNegociacao` and `dataRegistro`; reuse it in `POST response.transacao`, transaction detail and transaction list items. Define one public Posicao model with exactly `id`, `ativoId`, `ticker`, `quantidade`, `precoMedioBrl`, `totalInvestidoBrl`, `lucroRealizadoAcumuladoBrl` and `ultimaAtualizacao`; reuse it in `POST response.posicao`, position detail and position list items. Neither model contains valuation. `valorOrigem` remains derived from immutable transaction operands but is stored in idempotency completion, not in Transacao; replay uses the stored value.

Transaction and position lists use the exact envelope fields `items`, `page`, `size`, `totalElements` and `totalPages`, fixed ordering and allow only `page`/`size`; every other query parameter on these list routes is a strict-contract 400. Transaction detail first determines whether the UUID exists; own returns 200, foreign returns 403 through an application authorization exception that invokes the existing access-denied audit path, absent returns 404. Position list returns only positive quantity; position detail is selected by authenticated wallet plus `ativoId` and may return a zero position, while absent own position is 404. No entity/Page/provider payload is exposed.

### Deterministic audit time with backward compatibility

Add an optional explicit event instant to `AuditoriaCommand` (or equivalent overload/factory) while retaining the current constructor behavior for callers that do not possess one. `AuditoriaPersistenceAdapter` uses the supplied instant when present and retains its existing fallback only for non-financial legacy events such as security, login and compliance. DEPOSITO and SAQUE must pass the `operationInstant` already captured by the cash operation. COMPRA and VENDA must pass the single captured, microsecond-normalized `dataRegistro`. No financial path may call `Instant.now()` independently for its audit event. Add the two new enum values; no financial details enter the command. Regression tests assert movement/audit equality for DEPOSITO/SAQUE and Transacao/Posicao/audit equality for COMPRA/VENDA; snapshot date remains derived from the same instant in `America/Sao_Paulo`. This shared compatible extension does not make security-auditing a modified capability.

### Error mapping and test strategy

Introduce narrow transaction/position exceptions and map them to the specified 404/409/500 ProblemDetail responses while reusing security handlers for 401/403. Strict validation remains local, avoiding global Jackson behavior changes. Provider exceptions are not reachable from POST.

All database, idempotency and lock tests use the existing PostgreSQL Testcontainers base. Deterministic latches/barriers and transaction probes cover blocking/rollback without long sleeps. Unit tests cover arithmetic/fingerprint; MVC/integration tests cover exact JSON/OpenAPI/RBAC; schema tests inspect V9 constraints, FKs, indexes and Hibernate validation.

## Risks / Trade-offs

- [Wallet serialization limits same-wallet throughput, including different assets] → accept the deliberate correctness boundary; test different-wallet concurrency separately and keep transactions short.
- [Share-lock SQL generated by JPA varies] → use explicit native `FOR SHARE` queries if lock-mode output is not stable, and prove lifecycle blocking with PostgreSQL integration tests.
- [High-precision multiplication or derived average overflows] → validate operands, exact intermediates and every persisted result before completion; map business-invalid results to 409 with full rollback.
- [Snapshot upsert and valuation preservation regress under mixed cash/transaction traffic] → centralize the composer and cover known/null/no-position plus concurrent snapshot scenarios.
- [Idempotency row commits incomplete due to programming error] → completion consistency CHECKs plus internal 500; normal failures roll back the reservation.
- [Foreign transaction existence check reveals ownership through 403 as explicitly required] → expose no financial body and use sanitized ACESSO_NEGADO metadata only.
- [Legacy audit callers have independent time] → preserve fallback only for non-financial existing events; tests require the already captured operation instant for DEPOSITO, SAQUE, COMPRA and VENDA.

## Migration Plan

1. Add and validate the single forward-only V9 after V8; do not edit old migrations or backfill financial history.
2. Deploy schema before code that maps the new entities and nullable snapshot valuation fields; Hibernate `ddl-auto=validate` remains the gate.
3. Deploy ports/domain/service/API changes together so no supported route can create partial transaction state.
4. Verify with PostgreSQL Testcontainers and the full backend suite before release. Application rollback may remove the new routes, but V9 and any newly recorded immutable financial facts remain; structural correction must be a later forward migration, never destructive rollback.

## Open Questions

None. The missing local Explore report did not leave a behavior or financial decision unresolved because the prompt and authoritative sources define the required contracts.
