# Carteira Investimento

Projeto acadêmico desenvolvido no 6º semestre da UNIFEF por Guilherme França Dela Rovere, sob orientação do professor Jefferson Antonio Ribeiro Passerine.

Esta etapa estabelece somente a fundação técnica do produto. Autenticação, JWT, RBAC definitivo, entidades e regras financeiras, integrações externas reais e telas de negócio pertencem a changes futuras.

## Matriz técnica aprovada

| Componente | Versão | Gerenciamento |
| --- | --- | --- |
| Java | 21 | Maven Compiler via Spring Boot parent |
| Spring Boot | 4.1.1 | `spring-boot-starter-parent` |
| Spring Framework | 7.0.9 | BOM do Spring Boot |
| Spring Security | 7.1.1 | BOM do Spring Boot |
| Spring Cloud / OpenFeign | 2025.1.2 / 5.0.2 | `spring-cloud-dependencies` |
| Springdoc OpenAPI | 3.1.0 | versão explícita |
| Testcontainers | 2.0.5 | BOM do Spring Boot |
| Flyway | 12.4.0 | BOM do Spring Boot |
| PostgreSQL JDBC | 42.7.13 | BOM do Spring Boot |

As versões gerenciadas não possuem overrides individuais. O logging usa SLF4J por meio do starter de logging gerenciado pelo Spring Boot.

## Estrutura do repositório

```text
backend/             Spring Boot e Maven Wrapper
frontend/            Next.js e lockfile Node independente
openspec/            especificações e changes do produto
package.json         tooling OpenSpec da raiz
package-lock.json    lockfile do tooling OpenSpec da raiz
docker-compose.yml   PostgreSQL, backend e frontend
```

Os manifests Node da raiz não pertencem ao frontend. Dependências e comandos da aplicação web devem ser executados em `frontend/`.

## Configuração local

O contrato completo está em `.env.example`. Para preparar um ambiente local no PowerShell:

```powershell
Copy-Item .env.example .env
```

O `.env` é ignorado pelo Git. Os valores incluídos no repositório são apenas exemplos acadêmicos; tokens reais não devem ser versionados. `JWT_*`, `ADMIN_*`, `BRAPI_TOKEN`, `ALPHAVANTAGE_API_KEY` e `TWELVE_DATA_API_KEY` estão reservados para changes futuras e ainda não são consumidos.

Ao executar o backend diretamente no host, use uma URL JDBC acessível pelo host, por exemplo:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/carteira_db'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = 'postgrespassword'
$env:BACKEND_PORT = '8080'
```

No Compose, o backend usa `postgres-db` como hostname interno. `NEXT_PUBLIC_API_URL` é uma configuração pública incorporada ao build do frontend e deve conter a URL vista pelo navegador, normalmente `http://localhost:8080`; nunca armazene segredos em variáveis `NEXT_PUBLIC_*`.

## Backend

No Windows, a partir de `backend/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
.\mvnw.cmd package
```

`test` executa os testes unitários. `verify` também executa os testes de integração `*IT`, que exigem Docker disponível para iniciar PostgreSQL `16.15-alpine3.24` via Testcontainers. Não existe fallback H2.

Flyway é o único mecanismo de evolução do banco e Hibernate usa `ddl-auto: validate`. O diretório `backend/src/main/resources/db/migration/` está deliberadamente sem DDL de domínio nesta change.

Spring Security está em modo permissivo temporário, sem login, usuário gerado, JWT ou RBAC. Essa configuração deve ser substituída pela change de identidade/autenticação.

Com o backend pronto:

- Healthcheck: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

O Actuator expõe apenas `health`, sem detalhes sensíveis. O esquema Bearer exibido no OpenAPI é preparatório e não implementa autenticação.

## Frontend

No Windows, a partir de `frontend/`:

```powershell
npm.cmd ci
npm.cmd run lint
npm.cmd run typecheck
npm.cmd run build
npm.cmd run dev
```

O frontend usa Next.js 16.3.3 com App Router, TypeScript 6.0.3, Tailwind CSS 4.3.3, configuração Shadcn UI, Recharts e TanStack Query. A rota `/` é somente uma confirmação da fundação técnica e não contém login, dashboard ou tela de negócio.

## Execução integrada com Docker

As imagens estão fixadas em PostgreSQL `16.15-alpine3.24`, Node `24.19.0-alpine3.23` e Java 21 Temurin. Na raiz:

```powershell
docker compose config
docker compose up --build
```

O Compose persiste os dados PostgreSQL e aplica a seguinte ordem por healthchecks reais:

```text
PostgreSQL aceita conexões via pg_isready
-> backend inicia Flyway e Hibernate validate
-> /actuator/health retorna status UP
-> frontend inicia
```

Portas padrão configuráveis: PostgreSQL `5432`, backend `8080` e frontend `3000`.

Para encerrar:

```powershell
docker compose down
```

O volume não é removido por esse comando.

## APIs externas

BrasilAPI, ViaCEP, Brapi, AlphaVantage e TwelveData pertencem ao escopo global do PRD, mas nenhum cliente ou integração real é implementado nesta fundação.
