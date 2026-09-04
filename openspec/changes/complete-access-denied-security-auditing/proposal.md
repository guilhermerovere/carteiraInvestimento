## Why

O contrato de seguranca ja produz `403 Forbidden` com `ProblemDetail` sanitizado, mas duas lacunas impedem auditoria completa. Method security com `@PreAuthorize` e resolvida pelo Spring MVC antes de alcancar o `AccessDeniedHandler`; e uma negacao dentro da `SecurityFilterChain` ocorre antes de o `CorrelationIdFilter` disponibilizar seu valor, impedindo a persistencia correlacionada e a devolucao do header.

## What Changes

- Centralizar a preparacao e o disparo da auditoria de acesso negado em um colaborador interno reutilizavel.
- Integrar esse colaborador nas duas fronteiras que produzem `403` de autorizacao: `SecurityProblemDetailHandler` para a filter chain e `GlobalExceptionHandler` para method security resolvida pelo MVC.
- Garantir a ordem `HTTP -> CorrelationIdFilter -> Spring Security -> MVC`, com uma unica execucao efetiva do `CorrelationIdFilter` por requisicao.
- Preservar, em ambas as fronteiras, o contrato HTTP `403`, `application/problem+json` e `ProblemDetail` sanitizado quando a auditoria falhar; respostas `401` e `403` continuam recebendo correlation ID, sem auditar `401`.
- Cobrir explicitamente os dois fluxos HTTP autenticados sem permissao, a ausencia de duplicacao de filtros/eventos e os fluxos existentes que devolvem correlation ID.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `security-event-auditing`: exige auditoria automatica de toda negacao de autorizacao HTTP relevante que resulte em `403` para principal autenticado, independentemente de a decisao ocorrer na filter chain ou em method security.

## Impact

- Backend: registro/ordem unica de `CorrelationIdFilter`, colaborador interno de auditoria de acesso negado, `SecurityProblemDetailHandler`, `GlobalExceptionHandler`, `AuditoriaIsoladaPort` e `CorrelationIdFilter`.
- Testes: cobertura HTTP/Testcontainers explicita para as negacoes da filter chain e de `@PreAuthorize`, a ordem e execucao unica do filtro, e correlation ID nos fluxos publicos e de seguranca existentes.
- Sem alteracao de endpoints de producao, contratos publicos, JWT, schema/migrations, login, cadastro, frontend, carteira, regras financeiras, filas, async ou event listener global de `AuthorizationDeniedEvent`.
