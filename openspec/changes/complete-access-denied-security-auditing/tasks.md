## 1. Integracao da auditoria de acesso negado

- [ ] 1.1 Integrar o `AccessDeniedHandler` existente ao `AuditoriaIsoladaPort` para registrar `ACESSO_NEGADO` com UUID do principal autenticado, endpoint, correlation ID, resultado `NEGADO` e severidade `ALERTA`, verificando a chamada por teste unitario do handler.
- [ ] 1.2 Conter falhas do port isolado com diagnostico sanitizado e preservar o mesmo `403` com `application/problem+json` e `ProblemDetail` sanitizado, verificando o cenario por teste do handler com falha simulada de persistencia.

## 2. Cobertura do fluxo HTTP real

- [ ] 2.1 Criar ou adaptar somente a infraestrutura de teste para uma rota/probe protegida por role, sem adicionar endpoint funcional de producao, verificando que um JWT valido de usuario sem permissao recebe `403`.
- [ ] 2.2 Substituir ou complementar a insercao manual de `ACESSO_NEGADO` por teste de integracao HTTP com PostgreSQL que valide o unico registro persistido, usuario, endpoint, resultado, severidade e reutilizacao do `X-Correlation-ID` valido.
- [ ] 2.3 Executar os testes de auditoria e seguranca afetados e a suite Maven aplicavel, verificando que os eventos existentes e os contratos de autenticacao/autorizacao permanecem sem regressao.
