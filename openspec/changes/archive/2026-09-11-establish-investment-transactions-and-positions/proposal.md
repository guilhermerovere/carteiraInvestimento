## Why

O produto já possui carteira BRL, caixa idempotente e catálogos locais de ativos, corretoras e observações USD/BRL, mas ainda não registra compras e vendas nem mantém custódia e resultado realizado. Esta change estabelece o núcleo financeiro transacional que conecta essas capabilities com precisão, atomicidade, concorrência e replay determinístico, sem antecipar valuation de mercado.

## What Changes

- Adiciona `Transacao` imutável para BUY/SELL de ativos B3/BRL e US/USD, com corretora obrigatória, preço confirmado, taxas BRL, proveniência por `exchangeRateId` e snapshot autoritativo da taxa usada.
- Adiciona `Posicao` persistente e única por carteira/ativo, inclusive quando zerada, com quantidade, custo materializado, preço médio ponderado e lucro realizado acumulado.
- Define fórmulas financeiras em `BigDecimal`, `HALF_EVEN`, escalas 8 e 2, venda parcial/total, absorção final de resíduos, proibição de short selling e tratamento explícito de overflow.
- Torna BUY/SELL atômicos com saldo, posição, transação, snapshot diário, auditoria e idempotência na mesma transação PostgreSQL.
- Reutiliza a carteira como lock comum e estabelece ordem global de locks entre idempotência, Ativo, Corretora, Carteira, Posicao, Snapshot e finalização, protegendo também o lifecycle dos catálogos até o commit.
- Adiciona replay 201 do resultado original, materializado em colunas tipadas, e fingerprint canônico versionado compatível com o contrato do caixa.
- Expõe exatamente cinco endpoints privados para criar/listar/consultar transações e listar/consultar posições, com paginação, ownership, ProblemDetail, correlation ID e OpenAPI.
- Planeja uma única migration forward-only `V9__establish_investment_transactions_and_positions.sql`, sem alterar V1–V8.
- Evolui a composição de snapshots do caixa: custo local deixa de ser zerado artificialmente, o primeiro snapshot do dia com posição aberta mantém os três campos dependentes de valuation em `NULL`, sem copiar o dia anterior, e movimentos somente de caixa preservam valuation conhecido do mesmo dia quando aplicável.
- Torna determinístico o instante de auditoria dos eventos financeiros DEPOSITO, SAQUE, COMPRA e VENDA, reutilizando o único instante capturado pela respectiva operação e mantendo fallback retrocompatível apenas para callers não financeiros existentes.
- Mantém fora de escopo valuation de mercado, providers dentro do POST, frontend/dashboard, caixa USD, venda a descoberto e alteração/exclusão de transação confirmada.

## Capabilities

### New Capabilities

- `investment-transactions-and-positions`: transações BUY/SELL, posições, cálculos financeiros, FX local, idempotência/replay, concorrência, locking, APIs, segurança, auditoria e snapshots locais resultantes.

### Modified Capabilities

- `cash-movements`: composição de snapshots após a existência de posições, preservando custo/valuation local aplicável e removendo zeros artificiais em depósitos e saques.

## Impact

- Backend nas camadas domain, application, infrastructure e presentation, incluindo novos ports/use cases, persistência JPA/JDBC, locking PostgreSQL, validações, respostas HTTP e documentação OpenAPI.
- Banco PostgreSQL/Flyway com V9 para `transacoes`, `posicoes`, `transacoes_idempotencia`, constraints/FKs/índices e evolução de nullability/constraints de `carteira_snapshots`.
- Evoluções pontuais nos ports de Ativo, Corretora e histórico FX para leituras locais protegidas; no mecanismo compartilhado de auditoria para aceitar instante determinístico; e na composição de snapshots do caixa.
- Testes unitários e PostgreSQL Testcontainers para domínio, API, schema, segurança, idempotência, rollback, locks, lifecycle concorrente, snapshots e auditoria, sem H2 nem providers externos no POST.
