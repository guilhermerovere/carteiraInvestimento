## Why

O esqueleto atual ainda diverge da fundação técnica definida no `PRD.md`: o backend está em `carteiraInvestimento/` e inclui H2, enquanto não existem frontend e execução integrada via Docker Compose. Esta change preserva Java 21 e Spring Boot 4.1.1 e estabelece uma base técnica reproduzível antes das changes de identidade, modelo de dados e regras de negócio.

## What Changes

- **BREAKING** Renomear e reorganizar `carteiraInvestimento/` como `backend/`, preservando Java 21, Maven e Spring Boot 4.1.1.
- Remover H2 e configurar PostgreSQL 16 com imagem versionada, Flyway habilitado e Hibernate restrito a `ddl-auto: validate`.
- Adicionar ao backend as dependências técnicas previstas no PRD necessárias à fundação, incluindo Actuator e Spring Security compatível com Spring Boot 4.1.1 e gerenciado por BOM, sem implementar autenticação, domínio financeiro ou integrações externas reais.
- Substituir a configuração mínima atual por `application.yml` orientado a variáveis de ambiente e criar `.env` e `.env.example` para o ambiente local, sem versionar segredos reais.
- Preparar Swagger/OpenAPI, tratamento global de erros com `ProblemDetail` e a estrutura base de pacotes `domain`, `infrastructure` e `presentation`.
- Criar um frontend base com Next.js 15+, TypeScript e Tailwind, sem telas funcionais de negócio.
- Preservar `package.json` e `package-lock.json` da raiz como tooling do repositório/OpenSpec e criar manifests e lockfile próprios em `frontend/`, mantendo `node_modules` ignorado pelo Git.
- Adicionar Dockerfiles para backend e frontend e um `docker-compose.yml` raiz que integre PostgreSQL, backend e frontend, com healthchecks reais do banco e do backend e ordem de prontidão `PostgreSQL healthy -> backend healthy -> frontend`.
- Configurar testes de integração do backend contra PostgreSQL real por meio de Testcontainers, incluindo validação da inicialização do Flyway.
- Manter o Flyway operacional sem criar o schema completo do domínio; migrations de identidade e dados ficam reservadas para changes posteriores.
- Não introduzir entidades, autenticação/JWT, regras financeiras, integrações externas reais nem funcionalidades de negócio.

## Capabilities

### New Capabilities

- `backend-foundation`: Define a base executável do backend em Java 21/Spring Boot 4.1.1, sua configuração, persistência PostgreSQL/Flyway, healthcheck Actuator, tratamento de erros, organização arquitetural e testes de integração.
- `frontend-foundation`: Define a base executável do frontend em Next.js 15+, TypeScript e Tailwind, preparada para evolução posterior sem telas de negócio.
- `containerized-local-environment`: Define a execução local integrada e configurável de PostgreSQL, backend e frontend por Docker Compose.

### Modified Capabilities

Nenhuma. Ainda não existem specs principais no projeto.

## Impact

- Estrutura do repositório: movimentação de `carteiraInvestimento/` para `backend/` e criação de `frontend/`, arquivos de ambiente e infraestrutura na raiz.
- Backend: `pom.xml`, matriz Spring Boot 4.1.1/Spring Cloud/Springdoc, configuração Spring Security temporariamente permissiva, Actuator, estrutura de pacotes, contrato básico de erro, documentação OpenAPI, Dockerfile e testes.
- Frontend: novo projeto Next.js independente em `frontend/`, com manifests próprios, configuração TypeScript/Tailwind, dependências de base e Dockerfile; os manifests Node da raiz permanecem dedicados ao tooling do repositório.
- Persistência e execução: PostgreSQL substitui H2 em desenvolvimento e testes de integração; Flyway passa a ser o único mecanismo de evolução do schema; Docker Compose torna-se o caminho de execução integrada e aguarda healthchecks reais antes de liberar serviços dependentes.
- Compatibilidade: comandos e caminhos que apontam para `carteiraInvestimento/` deixarão de funcionar e deverão usar `backend/`.
- Git: a implementação está destinada à branch `feature/fundacao-do-projeto`; esta proposta não realiza merge nem implementação.
