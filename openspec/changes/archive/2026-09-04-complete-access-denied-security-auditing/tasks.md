## 1. Correlation ID, colaborador e fronteiras de auditoria

- [x] 1.1 Criar o colaborador interno de auditoria de acesso negado, centralizando validacao de principal autenticado nao anonimo, UUID, endpoint, correlation ID, `ACESSO_NEGADO`/`NEGADO`/`ALERTA`, chamada isolada ao port, contencao de falha e diagnostico sanitizado; verificar por testes unitarios do colaborador.
- [x] 1.2 Configurar uma unica inscricao Servlet de `CorrelationIdFilter` com ordenacao explicita anterior ao `springSecurityFilterChain`, sem adicionar a mesma instancia dentro da `SecurityFilterChain`; verificar por teste a precedencia e uma unica execucao por requisicao.
- [x] 1.3 Integrar o colaborador ao `SecurityProblemDetailHandler.handle(...)` depois de garantir correlation ID disponivel no fluxo da filter chain; verificar que uma falha de `AuditoriaIsoladaPort` preserva `403`, `application/problem+json` e `ProblemDetail` sanitizado.
- [x] 1.4 Integrar o colaborador ao `GlobalExceptionHandler.handleAccessDenied(...)` sem alterar seu contrato HTTP; verificar que uma falha de `AuditoriaIsoladaPort` preserva `403`, `application/problem+json` e `ProblemDetail` sanitizado.

## 2. Cobertura dos fluxos HTTP e da ordem do filtro

- [x] 2.1 Criar ou adaptar somente infraestrutura de teste para comprovar a ordem efetiva `CorrelationIdFilter -> Spring Security` e uma unica execucao do filtro por requisicao, inclusive quando a seguranca responde antes do MVC.
- [x] 2.2 Criar ou adaptar somente infraestrutura de teste para uma regra de autorizacao na `SecurityFilterChain`; verificar que principal autenticado sem permissao recebe `403`, `application/problem+json`, `ProblemDetail` sanitizado e `X-Correlation-ID`.
- [x] 2.3 Criar ou adaptar somente infraestrutura de teste para um probe com `@PreAuthorize`, sem adicionar endpoint funcional de producao; verificar que principal autenticado sem permissao recebe `403`, `application/problem+json`, `ProblemDetail` sanitizado e `X-Correlation-ID`.
- [x] 2.4 Para o fluxo da `SecurityFilterChain`, validar em integracao HTTP com PostgreSQL exatamente um `ACESSO_NEGADO` com usuario, endpoint, correlation ID igual ao header, resultado `NEGADO` e severidade `ALERTA` corretos.
- [x] 2.5 Para o fluxo de `@PreAuthorize` resolvido pelo MVC, validar em integracao HTTP com PostgreSQL exatamente um `ACESSO_NEGADO` com usuario, endpoint, correlation ID igual ao header, resultado `NEGADO` e severidade `ALERTA` corretos.
- [x] 2.6 Verificar, para cada requisicao de negacao coberta, que somente uma das duas fronteiras audita, que nao ha dois eventos `ACESSO_NEGADO` para o mesmo usuario e correlation ID e que a configuracao dos filtros nao causa duplicacao.
- [x] 2.7 Adicionar regressao para confirmar que health, login, register e respostas `401`/`403` existentes continuam recebendo correlation ID, sem introduzir auditoria de `401`.

## 3. Verificacao da change

- [x] 3.1 Executar os testes unitarios, de seguranca e de integracao afetados, incluindo falha de auditoria em ambas as fronteiras, e verificar que `401` e os eventos existentes nao sofrem regressao.
- [x] 3.2 Executar a suite Maven aplicavel e `npx.cmd openspec validate complete-access-denied-security-auditing --strict`, verificando que codigo e artefatos permanecem coerentes.
