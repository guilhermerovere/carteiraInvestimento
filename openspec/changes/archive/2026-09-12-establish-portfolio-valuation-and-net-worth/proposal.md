## Why

A carteira mantém caixa, posições e fatos de negociação, mas ainda não oferece uma valuation atual consistente nem um refresh diário explícito. Esta change torna patrimônio e rentabilidade verificáveis sem bloquear mutações financeiras durante integrações externas.

## What Changes

- Adiciona valuation B3/US em BRL, lucro não realizado, rentabilidade, totais e lucro realizado acumulado histórico informativo.
- Adiciona GET de resumo sem persistência e POST sem corpo para refresh/materialização diária, com contrato estrito comum.
- Planeja V10 para versão financeira, instant de valuation normalizado em microssegundos e três estados válidos de snapshot compatíveis com V6–V9.
- Define snapshot REPEATABLE_READ, integrações fora de transação, validação final, retry completo único e ordenação POST estrita por `valuationInstant`.
- Atualiza cash-movements para incrementar a versão e preservar valuation materializada do mesmo dia, recompondo patrimônio; valuation desconhecida continua desconhecida.
- Atualiza investment-transactions para incrementar a versão e invalidar valuation quando muda a composição de posições.
- Estende a leitura interna de quotes para custódia aberta em ativo inativo, sem mudar a rota pública.

## Capabilities

### New Capabilities

- `portfolio-valuation-and-net-worth`: valuation atual e materialização diária coerente da carteira.

### Modified Capabilities

- `cash-movements`: versão financeira exata e semântica de snapshot após movimentos de caixa.
- `investment-transactions-and-positions`: versão financeira exata e invalidação/localização do snapshot após BUY/SELL.
- `market-quotes`: leitura interna restrita de quote para ativo inativo ainda em custódia aberta.

## Impact

Afeta domínio, application, persistência, snapshots, API/OpenAPI e testes PostgreSQL Testcontainers. V10 é somente planejada. Não altera frontend, PRD, V1–V9, providers públicos, cache próprio, auditoria operacional, Graphify, archive ou commit.
