## 1. Fluxo de integracao JWT

- [x] 1.1 Confirmar no codigo e nos testes que nao existe IT equivalente e adicionar `JwtLoginFlowIT`, estendendo `PostgreSqlContainerSupport`; persistir um `ROLE_USER` com hash produzido pelo `PasswordEncoder` BCrypt real e verificar que o arquivo permanece restrito ao pacote de integracao.
- [x] 1.2 Implementar no mesmo teste a sequencia `POST /api/v1/auth/login` -> extracao de `accessToken` -> `GET /api/v1/auth/me` com o mesmo valor em `Authorization: Bearer`; verificar `tokenType`, `expiresIn`, o principal retornado e a ausencia de `senha`/`senhaHash` no JSON de login.

## 2. Verificacao e registro

- [x] 2.1 Executar `./mvnw.cmd -Dit.test=JwtLoginFlowIT verify` a partir de `backend` e registrar o resultado; se passar sem alteracao de producao, registrar explicitamente que e verificacao de comportamento existente, nao ciclo TDD.
- [x] 2.2 Executar a suite de seguranca/identidade pertinente definida pela revisao da change, revisar o diff e validar a change com `npx.cmd openspec validate validate-jwt-login-integration --strict`; verificar que nao houve alteracao de runtime, migration ou artefatos Graphify.
