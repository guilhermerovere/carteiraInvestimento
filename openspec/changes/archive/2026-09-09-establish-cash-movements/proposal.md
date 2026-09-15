## Why

`ROLE_USER` já possui uma carteira principal com saldo de caixa, mas ainda não consegue registrar a origem desse saldo nem movimentá-lo de forma segura. Esta change estabelece a primeira capability financeira de domínio, com rastreabilidade, idempotência e garantias transacionais necessárias antes de compras e vendas.

## What Changes

- Adiciona operações privadas de depósito e saque em BRL para a carteira principal do usuário autenticado.
- Adiciona ledger financeiro imutável de `MovimentacaoCaixa` e preserva `Carteira.saldoCaixaBrl` como saldo materializado de leitura rápida.
- Adiciona idempotência persistida por carteira, tipo de operação e `Idempotency-Key`, inclusive para retries concorrentes.
- Adiciona atualização atômica e condicionada do saldo para impedir saque acima do disponível sob concorrência.
- Adiciona snapshot diário mínimo da carteira, atualizado na mesma transação e sem antecipar posições ou dashboard.
- Adiciona consultas privadas de saldo e histórico paginado, além de auditoria técnica sanitizada para depósitos e saques.
- Mantém fora de escopo compra, venda, transação, posição, câmbio, frontend e BFF.

## Capabilities

### New Capabilities

- `cash-movements`: Movimentação de caixa BRL da carteira principal, incluindo saldo, ledger imutável, idempotência, concorrência, snapshots mínimos, auditoria sanitizada e APIs privadas.

### Modified Capabilities

- Nenhuma.

## Impact

- Backend Spring: domínio, application ports/use cases, persistência PostgreSQL/JPA, segurança das rotas e apresentação HTTP.
- Banco PostgreSQL/Flyway: migration forward-only após V5 para ledger, snapshots, restrição de saldo e integridade associada.
- APIs: quatro rotas privadas em `/api/v1/carteira/caixa`, com JWT, `ProblemDetail` e `X-Correlation-ID` preservados.
- Testes: domínio, application, API e integração PostgreSQL/Testcontainers, com cenários de atomicidade e concorrência.
