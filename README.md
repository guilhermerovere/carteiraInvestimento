# Valore — Carteira de Investimentos

Aplicação full-stack para gestão de uma carteira de investimentos em ações brasileiras e norte-americanas. Ela reúne autenticação segura, controle de caixa, operações de compra e venda, cotações e câmbio, catálogo de ativos e corretoras, valorização da carteira e gráficos de acompanhamento.

O projeto foi desenvolvido com Spec-Driven Development (OpenSpec). As entregas concluídas estão registradas em `openspec/changes/archive/` e o [PRD](PRD.md) é a referência do escopo do produto.

## O que já foi entregue

- Cadastro, login e sessão segura por JWT Bearer HS256; no frontend, o token permanece em cookie HttpOnly e é encaminhado pelo BFF do Next.js somente no servidor.
- Controle de acesso por `ROLE_USER` e `ROLE_ADMIN`, proteção de rotas, respostas sanitizadas em `ProblemDetail`, correlação por `X-Correlation-ID` e auditoria de eventos de segurança.
- Criação automática da **Carteira Principal** de saldo inicial zero para cada usuário; administradores não possuem carteira.
- Catálogo global de ativos B3 e US, com validação, ciclo de vida, busca e cadastro controlado pelo usuário autenticado.
- Catálogo administrativo de corretoras com CNPJ canônico, validação por fontes regulatórias, endereço enriquecido, ativação/desativação e identidade visual resolvida pelo sistema.
- Consulta e histórico de cotações, cache e atualização administrativa. B3 usa Brapi; o mercado americano e o câmbio USD/BRL usam provedores configuráveis com fallback.
- Depósitos e saques em BRL, ledger imutável, idempotência por `Idempotency-Key`, proteção contra concorrência e saldo insuficiente.
- Operações BUY e SELL para B3/BRL e US/USD, com corretora obrigatória, taxa de câmbio rastreável, posições, preço médio em BRL e bloqueio de venda a descoberto.
- Resumo de patrimônio, valorização da carteira em BRL, histórico de snapshots e gráficos de evolução patrimonial e composição por ações.
- Área do usuário com carteira, posições, transações, movimentações e configurações de perfil, senha e encerramento seguro de conta.
- Área administrativa para ativos e corretoras, além de interface responsiva, acessível, com temas claro/escuro/sistema.

## Arquitetura

```text
Browser
  │
  ▼
Frontend Next.js (porta 3000)
  ├── páginas, componentes e gráficos
  └── BFF same-origin /api/*
          │ cookie HttpOnly → Bearer no servidor
          ▼
Backend Spring Boot (porta 8080)
  ├── presentation: controllers e contratos HTTP/OpenAPI
  ├── application: casos de uso e regras transacionais
  ├── domain: entidades e regras financeiras
  └── infrastructure: JPA, Flyway, segurança e adapters externos
          │
          ▼
PostgreSQL 16 (porta 5432)
```

O banco é evoluído exclusivamente por migrations Flyway; o Hibernate utiliza `ddl-auto: validate`, portanto não cria nem altera o schema automaticamente.

## Stack

| Camada | Tecnologias |
| --- | --- |
| Frontend | Next.js 16, React 19, TypeScript, Tailwind CSS, TanStack Query, Recharts |
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA, OpenFeign, Caffeine, Springdoc/OpenAPI |
| Dados | PostgreSQL 16, Flyway |
| Qualidade | JUnit, Testcontainers, Vitest, Testing Library e Playwright |
| Infraestrutura | Docker, Docker Compose e imagens Alpine multi-stage |

## Pré-requisitos

Para executar o projeto completo, instale e inicie o Docker Desktop. Em Windows, use o PowerShell na raiz deste repositório.

Para desenvolvimento fora dos containers, também são necessários Java 21 e Node.js compatível com o frontend. Os Dockerfiles usam Node 24 e Java 21.

## Configuração

Crie o arquivo local de ambiente a partir do modelo versionado:

```powershell
Copy-Item .env.example .env
```

Edite `.env` antes de subir a aplicação. O arquivo real não deve ser enviado ao Git.

As configurações essenciais são:

| Grupo | Variáveis |
| --- | --- |
| PostgreSQL | `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT` |
| Backend | `BACKEND_PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| Segurança | `JWT_SECRET_KEY`, `JWT_EXPIRATION_HOURS`, `JWT_ISSUER`, `JWT_AUDIENCE` |
| Administrador inicial | `ADMIN_NAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` |
| Frontend/BFF | `FRONTEND_PORT`, `BACKEND_API_URL`, `APP_ORIGIN` |
| Integrações | `BRAPI_*`, `ALPHAVANTAGE_*`, `TWELVE_DATA_*`, `BRASIL_API_*`, `VIA_CEP_URL`, `LOGO_DEV_*` |

`JWT_SECRET_KEY` precisa ter ao menos 32 bytes UTF-8. As três variáveis `ADMIN_*` são obrigatórias como conjunto: se estiverem ausentes, parciais ou inválidas, o backend falha no início para não criar um administrador incompleto.

O administrador local é criado de modo idempotente a partir de `ADMIN_EMAIL`, `ADMIN_NAME` e `ADMIN_PASSWORD` definidos no seu `.env`. Use essas credenciais para entrar pela primeira vez; não há senha padrão fixa no código.

## Executar com Docker

### Subir o projeto

Valide a composição e crie/recrie as imagens:

```powershell
docker compose config --quiet
docker compose up --build
```

O segundo comando mantém os logs no terminal. Para deixar os containers em segundo plano, use:

```powershell
docker compose up --build -d
```

A ordem de prontidão é:

```text
PostgreSQL saudável
  → backend executa Flyway e valida o Hibernate
  → /actuator/health retorna UP
  → frontend inicia
```

Após a prontidão, acesse:

| Serviço | Endereço |
| --- | --- |
| Aplicação | http://localhost:3000 |
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Healthcheck | http://localhost:8080/actuator/health |
| PostgreSQL | `localhost:5432` (ou a porta definida em `POSTGRES_PORT`) |

### Acompanhar e diagnosticar

```powershell
# Estado e saúde dos serviços
docker compose ps

# Logs de todos os serviços
docker compose logs -f

# Logs de um serviço específico
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres-db

# Confirmar a disponibilidade da API
Invoke-RestMethod http://localhost:8080/actuator/health
```

### Parar ou encerrar

Há três operações diferentes; escolha a que corresponde ao que deseja preservar:

```powershell
# Para os containers, mas mantém-nos para uma retomada rápida.
docker compose stop

# Inicia novamente containers que foram apenas parados.
docker compose start

# Encerra e remove containers e rede; o volume do PostgreSQL é preservado.
docker compose down
```

Para remover também os dados locais do PostgreSQL e começar do zero, execute o comando abaixo. Ele é destrutivo para os dados do ambiente Docker local:

```powershell
docker compose down -v
```

Depois de `down -v`, suba novamente com `docker compose up --build`; o Flyway recriará o schema a partir das migrations e o bootstrap criará o administrador definido no `.env`.

## Capacidades da aplicação

### Acesso e segurança

- `POST /api/v1/auth/register` cria um `ROLE_USER` e sua carteira principal.
- `POST /api/v1/auth/login` devolve um access token para o BFF; o navegador não armazena o token em JavaScript.
- `GET /api/v1/auth/me` consulta o principal autenticado.
- Rotas privadas exigem Bearer válido. `401` identifica ausência/falha de autenticação e `403` representa falta de permissão.

### Carteira e operações financeiras

- Caixa: `POST /api/v1/carteira/caixa/deposito`, `POST /api/v1/carteira/caixa/saque`, `GET /api/v1/carteira/caixa` e histórico paginado.
- Investimentos: `POST /api/v1/carteira/transacoes`, listagem/consulta de transações e de posições.
- Patrimônio: `GET /api/v1/carteira/resumo`, `POST /api/v1/carteira/resumo/atualizar` e `GET /api/v1/carteira/graficos/evolucao`.
- Câmbio: `GET /api/v1/cambio/usd-brl` fornece uma observação USD/BRL rastreável.

Operações que alteram caixa ou investimentos usam uma `Idempotency-Key`. O frontend gera e preserva essa chave durante recuperação de uma confirmação ambígua; clientes da API devem fazer o mesmo ao repetir uma solicitação.

### Catálogos e administração

- Ativos: `GET`/`POST /api/v1/acoes`, busca por ticker, discovery, edição limitada e ativação/desativação conforme a role.
- Cotações: `GET /api/v1/acoes/{id}/cotacao`, histórico e atualização manual administrativa.
- Corretoras: `/api/v1/corretoras` oferece consulta para usuários e administração completa para `ROLE_ADMIN`.
- Conta: `/api/v1/account/profile`, `/password` e `/closure` permitem atualizar dados, alterar senha e encerrar a própria conta após as validações financeiras.

Consulte o Swagger para contratos completos, payloads, paginação, códigos de erro e autorização de cada rota.

## Integrações externas

| Integração | Uso |
| --- | --- |
| Brapi | Cotações e metadata de ativos B3 |
| Alpha Vantage | Cotações US e taxa USD/BRL configuráveis |
| Twelve Data | Fallback para cotações US e câmbio USD/BRL |
| BrasilAPI (CNPJ e CVM) | Validação cadastral e regulatória de corretoras |
| ViaCEP | Complemento de endereço de corretoras |
| Logo.dev | Identidade visual de corretoras e ativos US |

As credenciais opcionais dessas integrações ficam no `.env`. Erros de provedores são tratados como falhas externas sanitizadas; informações financeiras não são inventadas quando uma fonte está indisponível.

## Estrutura do repositório

```text
.
├── backend/                  # API Spring Boot, domínio, migrations e testes
├── frontend/                 # Aplicação Next.js, BFF, UI e testes
├── openspec/                 # Especificações e histórico das changes concluídas
├── docker-compose.yml        # PostgreSQL + backend + frontend
├── .env.example              # Contrato de configuração local
├── PRD.md                    # Fonte de verdade do produto
└── README.md
```

## Testes e verificações

Com Docker disponível para o PostgreSQL iniciado pelo Testcontainers:

```powershell
# Backend: a partir da pasta backend
Set-Location backend
.\mvnw.cmd test
.\mvnw.cmd verify

# Frontend: em outro terminal, a partir da pasta frontend
Set-Location frontend
npm.cmd run lint
npm.cmd run typecheck
npm.cmd run test
npm.cmd run test:e2e
```

`test` executa a suíte unitária e `verify` inclui os testes de integração do backend (`*IT`). Os testes de integração dependem de PostgreSQL real via Testcontainers; não há fallback H2.

## Notas de segurança e dados

- Nunca versione `.env`, tokens, senhas ou chaves de APIs reais.
- Não use os valores de exemplo fora do desenvolvimento local.
- Migrations Flyway já aplicadas não devem ser editadas; alterações estruturais exigem nova migration.
- Antes de usar `docker compose down -v`, confirme que não há dados locais que precise preservar.
