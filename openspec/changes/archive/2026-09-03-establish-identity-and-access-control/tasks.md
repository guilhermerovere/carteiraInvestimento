## 1. Dependencias e configuracao segura

- [x] 1.1 Adicionar o suporte Spring Security OAuth2 Resource Server/Jose usando o dependency management existente e verificar com `backend\mvnw.cmd dependency:tree` que nao ha versoes manuais redundantes nem H2
- [x] 1.2 Modelar propriedades validadas para JWT e `ADMIN_*`, incluindo segredo UTF-8 de no minimo 32 bytes, expiracao positiva com padrao 24, `JWT_ISSUER=carteira-investimento-backend`, `JWT_AUDIENCE=carteira-investimento-api` e conjunto `ADMIN_*` obrigatorio, e verificar testes de inicializacao para valores validos, ausentes, parciais e invalidos
- [x] 1.3 Atualizar `application.yml`, `.env.example` e o ambiente do backend em `docker-compose.yml` com `JWT_SECRET_KEY`, `JWT_EXPIRATION_HOURS`, `JWT_ISSUER`, `JWT_AUDIENCE` e `ADMIN_*`, sem segredo real, e verificar `docker compose config`

## 2. Schema PostgreSQL governado por Flyway

- [x] 2.1 Criar migration forward-only de `usuarios` com UUID, campos obrigatorios, timestamps, check de e-mail canonico, indice unico case-insensitive e constraint das duas roles, e verificar as constraints em PostgreSQL Testcontainers
- [x] 2.2 Criar migration forward-only de `carteiras` com UUID, FK unica para usuario, nome, `NUMERIC(18,2)` para caixa e data de criacao, e verificar que uma segunda carteira para o mesmo usuario e rejeitada
- [x] 2.3 Criar migration forward-only de `logs_auditoria` com FK de usuario nullable, endpoint nullable e todos os metadados minimos, e verificar que eventos anonimos e de sistema podem ser persistidos com os campos nulos apropriados
- [x] 2.4 Adaptar `FlywayValidationIT` para a nova sequencia sem perder o cenario de historico incompativel e verificar o teste de integracao com PostgreSQL real

## 3. Dominio, application e adapters de persistencia

- [x] 3.1 Implementar `Usuario`, `Carteira`, `Role` e regras de canonicalizacao/politica de senha no dominio, e verificar testes unitarios para trim/lowercase, roles admitidas e senha minima
- [x] 3.2 Definir ports de usuario, carteira e auditoria e os use cases/application services de cadastro, login, principal atual e bootstrap, incluindo suas fronteiras transacionais, e verificar testes unitarios de orquestracao sem dependencias HTTP ou JPA
- [x] 3.3 Implementar mappings JPA separados dos contratos HTTP e adapters/repositories que implementam os ports, e verificar que Hibernate `ddl-auto: validate` inicia contra as migrations
- [x] 3.4 Configurar BCrypt strength 12 e autenticacao repository-backed, remover conscientemente a exclusao de `UserDetailsServiceAutoConfiguration` e verificar que a aplicacao nao gera usuario ou senha padrao
- [x] 3.5 Implementar traducao da violacao concorrente de e-mail unico para `409 Conflict` e verificar em PostgreSQL que variacoes de caixa/espacos nao criam uma segunda identidade

## 4. Auditoria tecnica sanitizada

- [x] 4.1 Implementar enums/modelo JPA/repository de `LogAuditoria` e um servico interno com entrada tipada, e verificar que a API interna nao aceita cabecalhos, corpos HTTP ou objetos arbitrarios
- [x] 4.2 Implementar filtro de correlation ID que usa `X-Correlation-ID` somente quando UUID canonico valido e limitado e gera UUID quando ausente ou invalido, e verificar que requisicoes auditadas produzem identificador correlacionavel sem copiar o corpo
- [x] 4.3 Implementar auditoria de cadastro e bootstrap na transacao de criacao, auditoria de login bem sucedido antes de retornar token e transacoes isoladas para login falho, usuario inativo e acesso negado, e verificar a persistencia dos seis tipos minimos de evento, inclusive endpoint nulo e UUID gerado para bootstrap
- [x] 4.4 Adicionar testes de sanitizacao de logs e registros persistidos cobrindo senha, hash, JWT, `Authorization`, credenciais, corpo completo e dados financeiros proibidos, e verificar que nenhum valor sentinela aparece na saida

## 5. Cadastro e carteira principal

- [x] 5.1 Implementar o use case transacional de cadastro que fixa `ROLE_USER`, cria usuario ativo, `Carteira Principal` com saldo zero e auditoria na mesma transacao, e verificar rollback integral quando a carteira ou auditoria falha
- [x] 5.2 Criar DTOs e `POST /api/v1/auth/register` com validacao de nome, e-mail e politica de senha, resposta `201` somente com `id`, `nome`, `email`, `role` e `ativo`, sem token, senha ou hash, e verificar testes MVC para sucesso, `400` e `409`
- [x] 5.3 Adicionar testes PostgreSQL para cardinalidade usuario-carteira e concorrencia de cadastro, e verificar exatamente uma carteira por `ROLE_USER`, saldo zero e ausencia de duplicatas

## 6. Login, JWT e principal atual

- [x] 6.1 Implementar use case de autenticacao de credenciais com e-mail canonico, resposta publica indistinguivel para e-mail/senha invalidos, bloqueio de usuario inativo e auditoria bem sucedida persistida antes da emissao, e verificar os cenarios de login `200` e `401`
- [x] 6.2 Implementar emissao e validacao HS256 com `sub`, `role`, `iat`, `exp`, `jti`, `iss` e `aud`, usando os valores configurados de issuer/audience, e verificar testes para claims, assinatura, segredo insuficiente, audiencia/issuer, token malformado e expirado
- [x] 6.3 Implementar resolucao do usuario persistido em toda requisicao protegida, comparar a role atual e montar authorities do banco, e verificar `401` para usuario removido, inativo ou role divergente depois da emissao
- [x] 6.4 Criar DTOs e `POST /api/v1/auth/login` com `accessToken`, `tokenType=Bearer` e `expiresIn` da duracao restante em segundos, e verificar que a resposta `200` nunca inclui senha, hash ou refresh token
- [x] 6.5 Implementar `GET /api/v1/auth/me` exclusivamente a partir do `SecurityContext`, e verificar que retorna apenas `id`, `nome`, `email`, `role` e `ativo` e ignora qualquer `usuarioId` fornecido pelo cliente

## 7. Autorizacao e contratos de erro

- [x] 7.1 Substituir `FoundationSecurityConfiguration` por uma chain stateless com allowlist somente para register, login, health, OpenAPI e Swagger e autenticacao nas demais rotas, e verificar acessos anonimos publicos e `401` no restante
- [x] 7.2 Extrair uma fabrica sanitizada de `ProblemDetail` compartilhada pelo handler global, `AuthenticationEntryPoint` e `AccessDeniedHandler`, e verificar `application/problem+json` coerente para `401`, `403` e erros MVC existentes
- [x] 7.3 Configurar e testar autorizacao por `ROLE_USER`/`ROLE_ADMIN`, usando probes apenas de teste sem criar endpoint funcional futuro, e verificar `403` para principal autenticado sem role exigida
- [x] 7.4 Adaptar `OpenApiConfiguration` para tornar o esquema Bearer efetivo e manter UI/JSON publicos, e verificar no documento OpenAPI que rotas protegidas anunciam JWT sem exigir token para carregar Swagger

## 8. Administrador inicial

- [x] 8.1 Implementar `CommandLineRunner` e o use case transacional de provisionamento de `ADMIN_*` obrigatorios, com e-mail canonico, politica de senha do cadastro, BCrypt 12, `ROLE_ADMIN` ativo, sem carteira e auditoria de criacao, e verificar o primeiro startup em PostgreSQL vazio
- [x] 8.2 Implementar idempotencia sem sobrescrever senha ou role e verificar em dois startups que existe um unico admin, sem carteira e com o hash original preservado
- [x] 8.3 Implementar falha explicita para conflito do `ADMIN_EMAIL` com `ROLE_USER` e para configuracao ausente, parcial ou invalida antes do provisionamento, e verificar que nao ocorre promocao nem criacao parcial

## 9. Regressao, documentacao e validacao final

- [x] 9.1 Adaptar `PostgreSqlContainerSupport`, `ApplicationFoundationIT`, `MissingDatabaseConfigurationTest` e fixtures para fornecer somente a configuracao adicional necessaria, e verificar que startup, PostgreSQL, Flyway, Hibernate validate e falha por banco ausente continuam cobertos
- [x] 9.2 Preservar e adaptar `GlobalExceptionHandlerTest` e os testes de health/OpenAPI/Swagger, e verificar que detalhes permanecem sanitizados e os tres endpoints tecnicos continuam publicos
- [x] 9.3 Executar `backend\mvnw.cmd test` e `backend\mvnw.cmd verify` com Docker disponivel, corrigir todas as falhas e registrar que testes unitarios e Testcontainers passaram
- [x] 9.4 Atualizar README com JWT issuer/audience, variaveis admin obrigatorias, comportamento fail-fast, contratos exatos de register/login, uso Bearer, semantica `401`/`403`, auditoria/correlation ID e limites desta change, e verificar que nao documenta refresh, logout, frontend ou operacao financeira inexistente
- [x] 9.5 Executar `npx.cmd openspec validate establish-identity-and-access-control --strict` e revisar o diff final para confirmar que somente identidade, carteira minima, seguranca e auditoria foram implementadas
