## Why

O contrato de seguranca ja produz `403 Forbidden` com `ProblemDetail` sanitizado, mas o caminho real do `AccessDeniedHandler` nao persiste automaticamente o evento `ACESSO_NEGADO`. O teste atual mascara a lacuna ao inserir esse evento diretamente no port de auditoria, deixando sem comprovacao a auditoria exigida para uma negacao de autorizacao HTTP real.

## What Changes

- Conectar o tratamento real de `AccessDeniedException` ao port de auditoria isolada existente para registrar `ACESSO_NEGADO` de um principal autenticado.
- Reutilizar o UUID do principal autenticado, o endpoint da requisicao e o correlation ID ja resolvido pelo filtro, com resultado `NEGADO` e severidade `ALERTA` definidos pelo modelo de auditoria.
- Manter a escrita em transacao isolada e tornar sua falha nao intrusiva: a resposta original `403` e seu `application/problem+json` sanitizado permanecem inalterados.
- Substituir ou complementar a cobertura de persistencia manual por um teste do fluxo HTTP autenticado e sem permissao, usando probe restrito a testes quando necessario.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `security-event-auditing`: exige que a negacao de autorizacao HTTP real de um usuario autenticado seja auditada automaticamente como `ACESSO_NEGADO`, com metadados correlacionaveis e sem alterar o contrato `403`.

## Impact

- Backend: `SecurityProblemDetailHandler`, sua injecao na configuracao Spring Security e a integracao com `AuditoriaIsoladaPort` e `CorrelationIdFilter`.
- Testes: cobertura do handler e/ou de Spring MVC/Testcontainers para uma negacao de role no fluxo HTTP, substituindo a prova manual de `ACESSO_NEGADO` onde apropriado.
- Sem alteracao de endpoints de producao, contratos publicos, JWT, schema/migrations, frontend, carteira ou regras financeiras.
