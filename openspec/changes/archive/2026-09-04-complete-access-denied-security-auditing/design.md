## Context

Ver `proposal.md` para a motivacao e a delta spec de `security-event-auditing` para o contrato. A configuracao atual possui duas fronteiras distintas de negacao de autorizacao. Uma regra da `SecurityFilterChain` produz `AccessDeniedException`, que e tratada por `ExceptionTranslationFilter` e encaminhada ao `SecurityProblemDetailHandler`. Ja uma negacao por `@PreAuthorize` passa pelo proxy de method security, produz `AuthorizationDeniedException` (subclasse de `AccessDeniedException`) e e resolvida pelo `GlobalExceptionHandler` dentro do Spring MVC.

O diagnostico do fluxo da filter chain mostrou que o `CorrelationIdFilter` ainda nao havia executado quando o `SecurityProblemDetailHandler` foi chamado. Assim, o correlation ID necessario ao `AccessDeniedAuditingService` nao estava disponivel, a auditoria era contida sem persistencia e a resposta `403` nao recebia `X-Correlation-ID`. No fluxo MVC, o filtro ja havia executado antes do controller; esse caminho permanece valido.

## Goals / Non-Goals

**Goals:**

- Executar exatamente uma vez o `CorrelationIdFilter` antes da entrada no Spring Security, na sequencia `HTTP -> CorrelationIdFilter -> Spring Security -> MVC`.
- Registrar uma auditoria tipada, correlacionada e isolada para cada `403` de autorizacao relevante de principal autenticado, nas duas fronteiras existentes.
- Centralizar a construcao do comando, a qualificacao do principal e a contencao de falhas, sem transferir essas responsabilidades aos handlers HTTP.
- Manter o correlation ID de entrada valido ou gerado na resposta, inclusive para respostas `401` e `403` anteriores ao MVC, sem criar auditoria de `401`.

**Non-Goals:**

- Unificar os dois mecanismos de resposta HTTP, alterar o fluxo de autenticacao, JWT, regras de role, endpoints funcionais, login, cadastro, schema/migrations ou eventos existentes.
- Auditar `401`, introduzir filas, processamento async, auditoria consultavel ou um event listener global de `AuthorizationDeniedEvent`.
- Criar probe em fontes de producao, misturar auditoria nos controllers ou registrar o mesmo `CorrelationIdFilter` simultaneamente na camada Servlet e dentro da `SecurityFilterChain`.

## Decisions

### 1. Ordenar uma unica inscricao Servlet de CorrelationIdFilter antes do Spring Security

O `CorrelationIdFilter` continuara registrado uma unica vez na fronteira Servlet e recebera ordem explicita anterior ao `springSecurityFilterChain`. A implementacao deve escolher o mecanismo de ordenacao compativel com a estrutura atual, preferindo essa inscricao Servlet unica quando o filtro ja for registrado como filtro Servlet.

Nao se deve adicionar a mesma instancia de `CorrelationIdFilter` na `SecurityFilterChain` enquanto seu registro Servlet permanecer ativo. Isso causaria dupla execucao, poderia alterar ou repetir efeitos no header/MDC e violaria o contrato de uma execucao por requisicao. O teste deve comprovar tanto a precedencia efetiva quanto a execucao unica.

Com essa ordem, toda requisicao segue:

```text
HTTP request
-> CorrelationIdFilter
-> Spring Security
-> MVC quando aplicavel
```

O filtro preserva seu contrato: reutiliza `X-Correlation-ID` valido e canonico; gera UUID quando ausente ou invalido; disponibiliza-o no atributo da requisicao; devolve-o no header; e nao copia headers completos nem dados sensiveis. Isso tambem se aplica a respostas `401` e `403` que terminam antes do MVC, sem que `401` seja auditado.

### 2. Centralizar a auditoria em um colaborador interno sem responsabilidade HTTP

Introduzir ou manter um colaborador interno dedicado, por exemplo `AccessDeniedAuditingService`, responsavel exclusivamente por preparar e disparar `ACESSO_NEGADO`. Ele recebera a requisicao ja correlacionada e consultara o `SecurityContext` para:

- aceitar somente principal autenticado e nao anonimo;
- obter o UUID do usuario autenticado;
- obter o endpoint da URI da requisicao e o correlation ID por `CorrelationIdFilter.correlationId(request)`;
- construir `AuditoriaCommand` com `TipoEvento.ACESSO_NEGADO`, `ResultadoAuditoria.NEGADO` e `SeveridadeAuditoria.ALERTA`;
- chamar `AuditoriaIsoladaPort.recordIsoladamente`;
- conter toda falha de auditoria e emitir somente diagnostico operacional sanitizado.

O colaborador nao escrevera status, headers, media type nem corpo HTTP. Concentrar essa regra nos controllers foi rejeitado porque perderia negacoes anteriores ao controller e repetiria a regra. Um event listener global de `AuthorizationDeniedEvent` foi rejeitado: ele representa a decisao de autorizacao, nao o contrato HTTP final, exigiria qualificacao adicional e nao e necessario para os dois caminhos atuais.

### 3. Integrar o colaborador em duas fronteiras mutuamente exclusivas

`SecurityProblemDetailHandler.handle(...)` chamara o colaborador antes de escrever o `403` para negacoes produzidas pela `SecurityFilterChain`. Pela decisao de ordering, esse caminho ja tera correlation ID para registrar `ACESSO_NEGADO` com UUID, endpoint, correlation ID, `NEGADO` e `ALERTA` corretos, e devolvera o mesmo ID no header.

`GlobalExceptionHandler.handleAccessDenied(...)` continuara chamando o mesmo colaborador antes de devolver o `ProblemDetail` para `AuthorizationDeniedException` ou `AccessDeniedException` resolvidas pelo MVC em method security. O fluxo B validado permanece inalterado.

Os handlers permanecem donos de seus contratos HTTP. Em ambos os caminhos, a falha do port nao pode alterar status `403`, media type `application/problem+json`, `ProblemDetail` sanitizado ou propagar erro de infraestrutura ao cliente.

Nao e necessario mecanismo global de deduplicacao. Uma negacao na filter chain impede a requisicao de atingir o controller; uma negacao de `@PreAuthorize` ocorre somente depois que a filter chain autorizou a requisicao e e resolvida pelo MVC. Assim, somente uma das duas fronteiras trata cada negacao observada. A inscricao unica do filtro elimina tambem duplicacao de efeitos de correlation ID.

### 4. Provar a cadeia inteira com infraestrutura exclusiva de teste

A infraestrutura de teste usara somente fontes/configuracoes de teste quando precisar de recursos restritos por role. A cobertura deve exercitar separadamente uma regra de autorizacao na `SecurityFilterChain` e um controller/probe protegido com `@PreAuthorize`.

Para ambos, uma requisicao com JWT valido de usuario sem permissao devera verificar `403`, `application/problem+json`, `ProblemDetail` sanitizado, `X-Correlation-ID`, usuario, endpoint, correlation ID, resultado `NEGADO`, severidade `ALERTA` e exatamente um registro `ACESSO_NEGADO` no PostgreSQL. Os testes tambem devem comprovar a ordem e execucao unica do `CorrelationIdFilter`, e preservar correlation ID nos fluxos de health, login, register e respostas de seguranca existentes. A cobertura de falha simulada de `AuditoriaIsoladaPort` deve confirmar a preservacao do contrato e apenas diagnostico sanitizado.

## Risks / Trade-offs

- [Registro duplicado do filtro] -> manter uma unica inscricao Servlet, nao adicionar a mesma instancia a `SecurityFilterChain` e comprovar uma execucao por request em teste.
- [Correlation ID ausente em resposta que termina na seguranca] -> ordenar explicitamente o filtro antes de `springSecurityFilterChain` e cobrir `401`/`403` em teste.
- [Principal autenticado sem UUID utilizavel] -> o colaborador nao registra dados derivados de payload ou credencial; usa somente a identidade validada ja presente no `SecurityContext`.
- [Auditoria indisponivel durante a negacao] -> capturar a falha, registrar diagnostico sanitizado e manter a resposta original de cada handler.
- [Duplicacao de eventos por mudanca futura no fluxo] -> os testes exigem um unico evento por requisicao; a separacao atual das fronteiras elimina a necessidade de deduplicacao global.
- [Probe de teste escapar para producao] -> declara-lo somente na arvore/configuracao de testes e verificar que nenhuma rota funcional nova e criada.
- [Regressao dos eventos existentes] -> manter enums, port, adapter e seu isolamento; executar as suites de auditoria e seguranca afetadas.

## Migration Plan

1. Nenhuma migration, mudanca de configuracao externa ou rollout de contrato e necessaria.
2. Publicar a ordem unica do filtro e a integracao nas duas fronteiras; novas negacoes autenticadas passam a gerar linhas adicionais de auditoria conforme o contrato existente.
3. Em rollback de aplicacao, nenhuma estrutura persistida precisa ser revertida; eventos ja gravados permanecem como historico valido.
