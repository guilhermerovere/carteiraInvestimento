## 1. Modelo, precisão e schema

- [x] 1.1 Criar domínio/ports FX próprios com observação imutável e Clock para `registradoEm`; verificar BigDecimal sem double/float, par USD/BRL e timestamps obrigatórios em testes unitários.
- [x] 1.2 Implementar normalização única `setScale(8, HALF_EVEN)` e validação posterior; verificar exatamente oito, menos de oito, mais de oito, tie HALF_EVEN, round-to-zero, overflow NUMERIC(18,8), zero e negativo.
- [x] 1.3 Adicionar `V8__create_historico_cambio.sql` com coluna `provider`, campos/checks/índice acordados e sem UNIQUE; verificar PostgreSQL/Testcontainers, Hibernate validate e que V1–V7 não mudaram.

## 2. Providers e classificação

- [x] 2.1 Configurar FX com URLs/keys atuais e timeout Feign de quatro segundos, DTOs/classificadores privados; verificar startup sem key e ausência de segredo em logs/erros.
- [x] 2.2 Implementar Alpha `CURRENCY_EXCHANGE_RATE` com `from_currency=USD`, `to_currency=BRL`, par, taxa, `6. Last Refreshed` e `7. Time Zone`; verificar MockWebServer para request, 200 rate limit, 200 credencial, 200 sem taxa, timeout, conexão, 429, 5xx, decode e timestamp/timezone inválido.
- [x] 2.3 Implementar Twelve `/exchange_rate` com `symbol=USD/BRL`, `rate` e `timestamp` Unix; verificar MockWebServer para request/contrato temporal, 200 rate limit, 200 credencial, 200 sem taxa, timeout, conexão, 429, 5xx, decode e sem `currency_conversion`.
- [x] 2.4 Implementar taxonomia que examina primeiro erro semântico e então HTTP/estrutura; verificar fallback somente elegível e que falha de credencial/configuração não chama fallback continuamente.

## 3. Cache, transações e auditoria

- [x] 3.1 Implementar cache Caffeine `USD:BRL` e deadline absoluta `registradoEm + 5 minutos`; verificar hit preserva id/moedas/taxa/provider/instantes sem nova chamada/linha e nunca sobrevive à deadline.
- [x] 3.2 Usar Clock/Ticker controlável para testar expiração funcional, nova resolução após deadline e ausência de stale, sem Thread.sleep longo ou flaky.
- [x] 3.3 Implementar lock local/recheck; verificar miss concorrente em uma instância realiza idealmente uma resolução/persistência e compartilha UUID, sem Redis/distributed lock.
- [x] 3.4 Implementar persistência em bean transacional separado ou TransactionTemplate: HTTP externo fora de transação, save+flush+commit curto, cache depois; verificar transação ativa/inativa e rollback sem cache, sem self-invocation.
- [x] 3.5 Registrar auditoria isolada best-effort quando resolução termina sem FX válido, inclusive cadeia encerrada por credencial; verificar nenhuma auditoria em hit/sucesso/fallback bem-sucedido e falha de auditoria não substitui 502.

## 4. HTTP, segurança e contrato futuro

- [x] 4.1 Criar exclusivamente GET `/api/v1/cambio/usd-brl` e DTO com id, moedas, taxa escala 8, instanteCotacao, provider e registradoEm; verificar ausência de mutações/refresh/histórico público.
- [x] 4.2 Integrar ProblemDetail/correlation ID; verificar 401 anônimo, 200 USER/ADMIN, 502 upstream/configuração, 500 independente e ausência de payload/key/header/stack trace.
- [x] 4.3 Documentar OpenAPI Bearer/roles/schema e contrato futuro `exchangeRateId`; verificar que US usa `now <= registradoEm + 5 minutos`, B3 não usa id e futuro expirado será 409, sem implementar Transacao.

## 5. Validação integrada

- [x] 5.1 Criar integração PostgreSQL/Testcontainers para V8, checks, índice, observações repetidas e Hibernate validate, e MockWebServer/servidor local para providers; verificar ausência de internet pública e H2.
- [x] 5.2 Executar `./mvnw.cmd test`, `./mvnw.cmd verify`, `npx.cmd openspec validate establish-exchange-rates --strict` e `git diff --check`; verificar cobertura de fallback, cache/deadline, concorrência, transação, rollback, RBAC, ProblemDetail, correlation ID e auditoria antes de archive.
