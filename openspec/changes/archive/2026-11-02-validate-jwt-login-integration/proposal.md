## Why

O contrato de identidade ja declara o login JWT e o acesso protegido, mas a cobertura atual os verifica em recortes separados ou com JWTs fabricados. Falta uma evidencia de integracao que exercite o fluxo que um cliente efetivamente usa, preservando PostgreSQL, Flyway e a configuracao real de seguranca.

## What Changes

- Adiciona um unico teste de integracao de comportamento para um usuario persistido com senha BCrypt: login HTTP real, extracao do JWT retornado e reutilizacao desse mesmo token em uma rota protegida.
- Verifica no fluxo completo que a resposta de login nao expoe senha em claro nem `senhaHash`.
- Reutiliza `PostgreSqlContainerSupport`, Spring Boot, Flyway e MockMvc existentes; nao altera contratos, producao, schema, provedores ou configuracao de seguranca.
- Registra a verificacao como cobertura de comportamento existente, se a aplicacao ja estiver verde, e nao como ciclo TDD.

## Capabilities

### New Capabilities

Nenhuma. Esta change nao introduz comportamento de produto.

### Modified Capabilities

Nenhuma. Os requisitos de `bearer-authentication-and-authorization` permanecem inalterados; a change acrescenta somente evidencia de integracao para requisitos ja normativos.

## Impact

- Testes: um novo IT no pacote `backend/src/test/java/.../integration`, usando a infraestrutura PostgreSQL/Testcontainers/Flyway existente.
- Componentes exercitados, sem alteracao: persistencia de usuario, `PasswordEncoder` BCrypt, `POST /api/v1/auth/login`, emissao JWT, filtro resource-server e `GET /api/v1/auth/me`.
- Fora de escopo: frontend/BFF, APIs de ativos, migrations, qualquer mudanca em runtime, refresh/logout ou novos cenarios de seguranca.
