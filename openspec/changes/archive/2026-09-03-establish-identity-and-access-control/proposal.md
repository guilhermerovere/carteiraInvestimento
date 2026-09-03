## Why

A fundacao atual deixa o backend deliberadamente permissivo e ainda nao oferece identidade persistida nem uma fronteira real de autenticacao e autorizacao. Esta change estabelece essa fronteira agora para que as futuras capacidades privadas e administrativas possam se apoiar em identidade confiavel, isolamento por principal autenticado e auditoria de seguranca, sem antecipar operacoes financeiras.

## What Changes

- Introduz `Usuario` persistido com UUID, e-mail canonico e unico sem distincao de caixa no PostgreSQL, senha protegida por BCrypt strength 12, role unica em enum (`ROLE_USER` ou `ROLE_ADMIN`) e estado ativo/inativo.
- Introduz a carteira principal minima: cada `ROLE_USER` cadastrado, ativo, recebe a `Carteira Principal` com saldo zero e auditoria na mesma transacao; `ROLE_ADMIN` nao recebe carteira.
- Disponibiliza cadastro, login e consulta do proprio usuario em `/api/v1/auth/register`, `/api/v1/auth/login` e `/api/v1/auth/me`, com respostas publicas delimitadas e sem expor senha, hash, refresh token ou token no cadastro.
- Introduz autenticacao stateless por JWT Bearer HS256, com segredo UTF-8 externo de no minimo 32 bytes, expiracao configuravel, issuer/audience configurados e validados e claims obrigatorias, sem refresh token, blacklist ou logout server-side.
- Substitui a seguranca temporariamente permissiva por rotas publicas explicitamente delimitadas e autenticacao obrigatoria nas demais rotas, com semantica `401`/`403` em `ProblemDetail`.
- Valida o usuario persistido em toda requisicao protegida para invalidar o acesso de usuarios inexistentes, inativos ou com role divergente da claim, construindo authorities da role atual do banco.
- Mantem o provisionamento inicial por `CommandLineRunner` e `ADMIN_*` obrigatorios, de forma idempotente, sem carteira, sem sobrescrever credenciais ou role e fail-fast para configuracao ausente, parcial, invalida ou conflito com `ROLE_USER`.
- Introduz auditoria persistida, correlacionavel e sanitizada para os eventos minimos de seguranca, sem endpoints de consulta nesta change.
- Adiciona migrations Flyway para `usuarios`, `carteiras` e `logs_auditoria`, preservando PostgreSQL, Hibernate `ddl-auto: validate` e Testcontainers.
- Preserva healthcheck, OpenAPI e Swagger publicos, os contratos sanitizados da fundacao e a cadeia de prontidao existente.
- **BREAKING**: rotas antes liberadas pela configuracao temporaria passam a exigir JWT, exceto as rotas publicas explicitamente definidas.

## Capabilities

### New Capabilities

- `identity-and-primary-wallet`: identidade persistida, roles, estado ativo/inativo, cadastro transacional com carteira principal e provisionamento do administrador inicial.
- `bearer-authentication-and-authorization`: login, JWT Bearer, usuario autenticado, regras de rotas e semantica de autenticacao/autorizacao.
- `security-event-auditing`: persistencia e sanitizacao dos eventos minimos de seguranca com correlacao.

### Modified Capabilities

- `backend-foundation`: substitui a permissividade temporaria, mantem health/OpenAPI/Swagger publicos e integra falhas de seguranca ao contrato `ProblemDetail` existente sem regredir Flyway, Hibernate validate ou os testes de fundacao.

## Impact

- Backend: regras puras em `domain`; use cases, ports e orquestracao transacional em `application`; adapters JPA, repositories, Spring Security, JWT, BCrypt e configuracoes em `infrastructure`; DTOs, controllers e erros HTTP em `presentation`.
- Persistencia: novas migrations PostgreSQL com FKs, constraints de role, unicidade de carteira e unicidade case-insensitive de e-mail.
- Configuracao: revisao de `FoundationSecurityConfiguration`, da exclusao de `UserDetailsServiceAutoConfiguration` em `CarteiraInvestimentoApplication`, e inclusao documentada de `JWT_SECRET_KEY`, `JWT_EXPIRATION_HOURS`, `JWT_ISSUER=carteira-investimento-backend`, `JWT_AUDIENCE=carteira-investimento-api` e `ADMIN_*` em `application.yml`, `.env.example` e `docker-compose.yml` quando aplicavel.
- Contratos HTTP: tres endpoints de autenticacao, esquema Bearer no OpenAPI e respostas `401`/`403` coerentes com o `ProblemDetail` sanitizado.
- Testes: preservacao e adaptacao de `ApplicationFoundationIT`, `FlywayValidationIT`, `PostgreSqlContainerSupport`, `MissingDatabaseConfigurationTest` e testes de erro, acrescida de cobertura PostgreSQL/Testcontainers para identidade, carteira, JWT, roles, inatividade, bootstrap e auditoria.
- Fora de impacto: frontend, operacoes financeiras, endpoints de carteira, ativacao/desativacao, recuperacao ou troca de senha e consulta de logs permanecem fora do escopo.
