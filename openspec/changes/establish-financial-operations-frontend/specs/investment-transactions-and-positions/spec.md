## MODIFIED Requirements

### Requirement: APIs privadas, listagens e posicoes zeradas
The system SHALL expose exactly POST `/api/v1/carteira/transacoes`, GET `/api/v1/carteira/transacoes`, GET `/api/v1/carteira/transacoes/{id}`, GET `/api/v1/carteira/posicoes`, and GET `/api/v1/carteira/posicoes/{ativoId}`. All SHALL be exclusive to ROLE_USER and derive user/wallet from `SecurityContext`; anonymous receives 401 and ROLE_ADMIN 403. The public Transaction model SHALL retain exactly `id`, `ativoId`, `ticker`, `corretoraId`, `exchangeRateId`, `tipo`, `quantidade`, `moeda`, `precoUnitario`, `taxas`, `taxaCambioBrl`, `valorTotalBrl`, `resultadoRealizadoBrl`, `dataNegociacao`, and `dataRegistro`, reused in POST response, GET detail, and list. The public Position model SHALL contain `id`, `ativoId`, `ticker`, `nome`, `mercado`, `moeda`, `ativo`, optional system-owned `logoProvider`/`logoReference`, `quantidade`, `precoMedioBrl`, `totalInvestidoBrl`, `lucroRealizadoAcumuladoBrl`, and `ultimaAtualizacao`; it SHALL be reused in POST response, GET detail, and list. Position responses SHALL NOT contain valuation data. The added market/currency/lifecycle/branding values SHALL come from the persisted canonical asset record, including when inactive, and MUST NOT be resolved through the active public catalog. Transaction pagination remains `{items,page,size,totalElements,totalPages}`, page 0/size 20 defaults, 1..100 size, fixed `dataRegistro DESC,id DESC`; positions retain the same envelope, no filters, `ticker ASC,ativoId ASC`, and only quantity greater than zero in the list. Unknown list query parameters SHALL return 400. Position detail MAY return a persisted zero-quantity position.

#### Scenario: Listagem de custodia
- **WHEN** a user lists open and zero-quantity positions in storage
- **THEN** the response contains only open positions, in ticker/asset-id order, with canonical asset metadata and no valuation

#### Scenario: Consulta de posicao zerada
- **WHEN** a user queries an owned persisted zero-quantity position
- **THEN** the response includes its zeroed current cost, historical realized profit, and current canonical asset metadata

#### Scenario: Position references inactive asset
- **WHEN** an open position references an inactive catalog asset
- **THEN** its response still includes ticker, name, market, currency, lifecycle status, and optional branding reference for contextual SELL

### Requirement: Ownership, erros e OpenAPI
GET Transaction detail for another user SHALL return 403 and use the existing `ACESSO_NEGADO` mechanism; missing Transaction returns 404. Missing own Position returns 404. Invalid/unknown JSON, UUID/type/number, input not exactly representable in `NUMERIC(18,8)`, own-input overflow, nonpositive quantity/price, negative fees, invalid key, forbidden list query, B3 with exchangeRateId, or US without exchangeRateId SHALL return 400. Insufficient cash, oversell, SELL without Position, inactive asset on BUY, inactive broker, unsuitable/missing/expired FX, fingerprint mismatch, invalid partial position, realized-profit overflow, any nonrepresentable derived result, or SELL fees exceeding gross SHALL return 409. A 409 caused specifically by an FX identifier whose `registradoEm + 5 minutes` deadline has passed SHALL include structured ProblemDetail property `code: "FX_EXPIRED"`; other conflicts SHALL NOT use that code. Missing primary wallet, persistence, transactional audit, or internal invariant failure SHALL return 500. Transaction POST SHALL not return 502 due to a provider and SHALL not use 422. All errors use sanitized ProblemDetail and `X-Correlation-ID`.

OpenAPI SHALL document Bearer, ROLE_USER, endpoints, Idempotency-Key, strict request, exact transaction response, expanded position metadata, B3/US/FX, scale, pagination, ownership, absence of valuation, and 400/401/403/404/409/500 responses including the `FX_EXPIRED` structured code.

#### Scenario: Acesso cruzado
- **WHEN** a ROLE_USER requests a Transaction owned by another user
- **THEN** the system returns correlated sanitized 403 and records `ACESSO_NEGADO` without private financial data

#### Scenario: POST não chama provider
- **WHEN** BUY or SELL is confirmed or rejected
- **THEN** no quote, FX, Receita, CVM, or CEP provider is called and this flow does not produce 502 due to a provider

#### Scenario: FX expirado tem codigo estruturado
- **WHEN** a US transaction uses an observation after `registradoEm + 5 minutes`
- **THEN** it returns 409 with `code=FX_EXPIRED`, without financial effects; unrelated 409 responses omit that code

## ADDED Requirements

### Requirement: Position metadata supports SELL without active-catalog lookup
Position response metadata used for SELL SHALL describe the persisted asset identity needed by the UI: canonical ticker/name, market, currency, active state, and optional system-owned branding reference. The backend SHALL retrieve this metadata from the referenced asset even when it is inactive. The metadata extension MUST NOT change ownership, position balances, transaction contracts, the existing FX validity rule, or BUY/SELL calculation rules.

#### Scenario: SELL inactive asset with unavailable quote
- **WHEN** a user owns an open position in an inactive asset and its public quote returns 404
- **THEN** Position detail/list still provides the metadata and quantity needed for manual-price SELL preparation
