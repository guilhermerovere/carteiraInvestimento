## Context

See [proposal.md](proposal.md) for motivation and the [cash-movements spec](specs/cash-movements/spec.md) for the contract. The backend uses PostgreSQL/Flyway, Hibernate validation, JWT-derived principals, ProblemDetail, correlation IDs and technical audit. ROLE_USER has one primary Carteira; existing migrations end at V5.

## Goals / Non-Goals

**Goals:**

- Keep balance, immutable ledger, idempotency, snapshot and success audit in one PostgreSQL transaction.
- Deliver deterministic exactly-once effects and replay under real PostgreSQL concurrency.
- Preserve primary-wallet ownership and sanitization boundaries.

**Non-Goals:**

- No frontend, BFF, administrative cash route, wallet selector, COMPRA, VENDA, Transacao, Posicao, FX, quotes, dashboard, reversal, edit or deletion.
- No distributed idempotency service or external dependency.

## Decisions

### Ledger, wallet/user integrity and snapshots

Carteira.saldoCaixaBrl is the fast available-balance projection. An immutable MovimentacaoCaixa records UUID id, carteira_id, usuario_id, type, positive BRL value, normalized optional description and data_hora. The schema enforces NUMERIC(18,2), non-negative wallet balance, positive movement value, allowed types and ON DELETE RESTRICT.

Independent FKs are insufficient: the migration adds the minimum relation that makes (carteira_id, usuario_id) belong to the same wallet ownership. PostgreSQL uses a composite FK to a unique (id, usuario_id) on carteiras, preserving existing individual integrity; wallet A cannot be recorded with user B.

The PRD declares CarteiraSnapshot.id; snapshots therefore use an explicit UUID PK plus UNIQUE(carteira_id, data_referencia). Upsert writes result cash to balance/patrimony, 0.00 to positions/invested/unrealized profit, and only targets the new operation's day. No endpoint or previous-date mutation is added.

### Monetary normalization and canonical fingerprint

Use BigDecimal; reject non-positive values, scale above two and values outside NUMERIC(18,2) without rounding or binary floating point. Validate prospective resulting balance too: an individually valid deposit that overflows it is sanitized 409 with complete rollback.

Normalize amount by removing insignificant zeros and emitting plain non-exponent decimal; 100, 100.0 and 100.00 are equivalent. Description is trim, blank to null. SHA-256 hashes a versioned UTF-8 canonical record with fixed field order, explicit type tags and length-prefixed values, including explicit null. It does not use Locale, delimiter concatenation or arbitrary property order; it is never exposed, logged or audited. Raw key is untrimmed and must match ^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$.

### Safe PostgreSQL idempotency and transaction order

PostgreSQL is final idempotency authority. The reservation adapter uses an atomic mechanism equivalent to INSERT ON CONFLICT DO NOTHING RETURNING; it MUST NOT catch a normal UNIQUE violation and continue using the same PostgreSQL transaction. It distinguishes created/existing. PostgreSQL waits for a concurrent uncommitted reservation: after winner commit the contender reads/replays final result; after rollback it reserves and executes.

Reservation, financial mutation and final result share one transaction. A key is definitively consumed only by successful commit. Insufficient funds, result overflow, movement/snapshot/audit failure or any rollback reverses reservation too and persists no success result.

New operation flow:

1. Resolve principal and primary wallet.
2. Validate request and raw key.
3. Capture once operationInstant = clock.instant().
4. Atomically reserve key.
5. Existing row: validate fingerprint; mismatch is 409, match replays stored original result with no effect.
6. New row: atomically mutate balance; persist movement; upsert snapshot; transactional sanitized audit; persist final original response result.
7. Commit before response.

Deposit and withdrawal return result balance from PostgreSQL. Withdrawal uses conditional saldo >= valor; no returned row is 409. Deposit rejects resulting numeric overflow before applying. SELECT -> validate -> UPDATE and pessimistic locking are rejected. Replay returns stored movement and stored balance result, never current balance, and remains 201.

### Single operation instant

Injected Clock is sole source for a new operation's instant; movement time, permitted audit time and snapshot date derive from it. Snapshot date is derived in configured application.cash-movements.snapshot-zone, default America/Sao_Paulo, never host zone. Replay uses persisted original timestamp and no new instant/snapshot update.

### API, ownership, errors and audit

All four routes require ROLE_USER; wallet comes only from SecurityContext principal. A valid user without primary wallet gets sanitized 500 with X-Correlation-ID, never 403/404 or third-party fallback. Anonymous is 401; admin is 403 everywhere and retained denied-access auditing is isolated.

Unknown JSON properties are rejected locally on cash DTO/controller only; global auth, asset-catalog and market-quote JSON behavior remains unchanged. Explicit responses and history envelope disclose only contract fields. Errors map to 400/409/500 and never disclose SQL, constraints, stack trace, fingerprint, key, uncontracted balance or third-party data.

Authorized DEPOSITO/SAQUE success audit uses ordinary transactional audit, never REQUIRES_NEW or AuditoriaIsoladaPort. It records only allowed technical metadata and commits only with financial effects. The isolated port remains for security/access denial.

## Risks / Trade-offs

- [Concurrent key callers] → conflict-safe reservation and PostgreSQL/Testcontainers tests for commit/rollback; never recover from a UNIQUE violation in the active transaction.
- [Partial finance] → all financial writes and reservation share a transaction; inject failures after balance mutation.
- [Day-boundary drift] → one captured instant and explicit zone.
- [Sanitization regression] → direct HTTP and persisted-audit assertions.
- [Legacy invalid data] → baseline preflight and forward-only Flyway, no destructive correction.

## Migration Plan

1. Inspect V1–V5 and add forward-only V6 migrations for ledger, idempotency, UUID snapshot identity, composite wallet/user relationship, checks, unique constraints and indexes.
2. Verify Flyway/Hibernate and direct constraints on PostgreSQL/Testcontainers.
3. Deploy code only after migration succeeds; existing balances remain and future committed cash creates evidence.
4. Recover only by compatible forward migration; never edit/rollback applied DDL.

## Open Questions

None.

## Refinements and verification contracts

Reservation MUST use PostgreSQL conflict-safe semantics equivalent to `INSERT ... ON CONFLICT DO NOTHING RETURNING`; a normal unique violation MUST NOT be caught and reused in the same transaction. Existing uncommitted reservations are awaited; committed winners are replayed and rolled-back winners may be reserved again. Reservation, balance, movement, snapshot, audit, and final result share one transaction and all failures roll back the reservation.

Fingerprint input is a versioned UTF-8 canonical record with fixed order, explicit type tags, length prefixes, and an explicit null marker. BigDecimal uses plain non-exponential normalized decimal (`100`, `100.0`, `100.00` equivalent); description is trim/blank-null; no Locale, arbitrary property order, or delimiter concatenation is allowed.

HTTP is fixed at 400 malformed input/key/pagination, 401 anonymous, 403 admin/denied, 409 insufficient funds/result overflow/fingerprint mismatch, and sanitized 500 for missing primary wallet or unexpected failure. All ProblemDetail responses preserve X-Correlation-ID and omit SQL, constraints, stack traces, private IDs, balances, fingerprints, keys, and third-party data. Strict unknown-field rejection is local to cash DTOs/controllers. Replay is 201 and returns the stored movement, original timestamp, and original resulting balance without new effects.
