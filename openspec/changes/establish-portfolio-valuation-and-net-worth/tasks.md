## 1. Persistência

- [ ] 1.1 Criar V10 com estado_versao, valuation_instant e checks dos três estados; verificar V1→V10 no PostgreSQL.
- [ ] 1.2 Mapear colunas e projections/ports; verificar Hibernate validate.
- [ ] 1.3 Incrementar versão +1 exato em depósito/saque, BUY e SELL no mesmo commit; verificar replays/rollbacks +0.
- [ ] 1.4 Ajustar composer para preservar valuation em cash, invalidar negociação aberta e zerar última posição; verificar snapshots.

## 2. Domínio

- [ ] 2.1 Implementar fórmulas B3/US BigDecimal e materialização HALF_EVEN; verificar lucro/prejuízo, escala e overflow 409.
- [ ] 2.2 Implementar totais e rentabilidade escala 4/null sem investimento; verificar múltiplas posições e zero.
- [ ] 2.3 Agregar lucro realizado de todas as posições, inclusive zeradas, sem somá-lo ao patrimônio; verificar cenário A zerada+B aberta.

## 3. Orquestração

- [ ] 3.1 Implementar leitura read-only REPEATABLE_READ única com carteira/saldo/versão/abertas/custos/ativos/realizado; verificar fotografia consistente.
- [ ] 3.2 Capturar valuationInstant após leitura e truncar em microssegundos; verificar ausência de nanos divergentes.
- [ ] 3.3 Garantir fechamento antes de quote/FX; verificar deterministicamente que provider não roda em transação/lock.
- [ ] 3.4 Implementar validação final FOR UPDATE e retry completo; verificar nova invocação de use cases e novo instant.
- [ ] 3.5 Retornar 409 na segunda perda sem provider sob lock; verificar descarte integral da primeira tentativa.

## 4. Integrações

- [ ] 4.1 Adicionar leitura interna de quote de ativo inativo somente para custódia aberta; verificar sem bypass/rota pública inalterada.
- [ ] 4.2 Resolver FX uma vez por tentativa US e nenhuma em B3/vazia; verificar compartilhamento.
- [ ] 4.3 Traduzir quote/FX necessário indisponível em 502 completo; verificar sem snapshot/resposta parcial.

## 5. API e snapshot

- [ ] 5.1 Implementar GET strict sem query/body, sem persistência; verificar empty GET e +0 versão.
- [ ] 5.2 Implementar POST strict sem corpo, refresh e snapshot do dia; verificar empty POST zeros+instant e +0 versão.
- [ ] 5.3 Implementar upsert: instant nulo ou novo > existente atualiza; menor/igual preserva; verificar todos os quatro casos.
- [ ] 5.4 Implementar DTO raiz/posição/cambio exatamente contratado; verificar posições zeradas omitidas.
- [ ] 5.5 Aplicar SecurityContext ROLE_USER, ProblemDetail e correlation ID; verificar 400/401/403/409/502.
- [ ] 5.6 Atualizar OpenAPI; verificar exatamente dois endpoints e contrato comum.

## 6. Testes comuns e V10

- [ ] 6.1 Cobrir B3, US, mista, half-even, percentual, overflow, posição zerada e realizado agregado.
- [ ] 6.2 Cobrir vazio: zero chamadas Quote/FX, GET imutável e POST materializado.
- [ ] 6.3 Validar V10 diretamente: desconhecido, local vazio, materializado, parcial rejeitado, V1→V10 e Hibernate validate.

## 7. Testes de concorrência PostgreSQL

- [ ] 7.1 Cobrir valuation+depósito, saque, BUY e SELL com provider lento.
- [ ] 7.2 Cobrir dois GET, dois POST e GET+POST; verificar versão e snapshot coerentes.
- [ ] 7.3 Cobrir retry, nova chamada de provider, novo instant e segunda perda 409.
- [ ] 7.4 Cobrir POST antigo tardio, instant menor, empate micros e instant maior; verificar snapshot antigo nunca sobrescreve novo.

## 8. Validação

- [ ] 8.1 Executar backend\mvnw.cmd test e verify com PostgreSQL Testcontainers.
- [ ] 8.2 Executar npx.cmd openspec validate establish-portfolio-valuation-and-net-worth --strict.
- [ ] 8.3 Executar git diff --check e git status; verificar ausência de código, migration, PRD, frontend, Graphify, archive e commit.
