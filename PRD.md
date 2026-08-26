# PRD — Sistema de Gestao de Ativos, Carteira e Corretoras

## 1. Visao do Produto

Desenvolver uma plataforma full-stack para gestao de investimentos em acoes nacionais e internacionais, com autenticacao, carteira individual por usuario, controle de compras e vendas, saldo em caixa, calculo de preco medio, integracao com APIs externas, validacao regulatoria de corretoras, painel administrativo, auditoria e execucao completa via Docker.

O sistema deve seguir **Spec-Driven Development (SDD)** com OpenSpec.

---

## 2. Stack Obrigatoria

### Backend
- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Security 6
- JWT
- Spring Cloud OpenFeign
- PostgreSQL 16+
- Caffeine Cache
- Flyway
- Swagger / OpenAPI

### Frontend
- Next.js 15+
- TypeScript
- Tailwind CSS
- Shadcn UI
- Recharts
- TanStack Query

### Infraestrutura
- Docker
- Docker Compose
- `.env`
- `.env.example`
- Git
- Flyway para versionamento do banco

---

## 3. Estrutura do Projeto

```text
carteira-investimento/
- backend/
  - src/
  - Dockerfile
  - pom.xml
- frontend/
  - src/
  - Dockerfile
  - package.json
- openspec/
- .env
- .env.example
- docker-compose.yml
- README.md
- AGENTS.md
```

---

## 4. Arquitetura do Backend

O backend deve seguir tres camadas principais:

```text
domain/
- entidades
- enums
- excecoes
- regras de negocio

infrastructure/
- configuracoes
- seguranca
- clientes de APIs
- adapters

presentation/
- controllers
- DTOs
- tratamento de erros
```

### Entidades principais
- Usuario
- Corretora
- Acao
- Carteira
- Posicao
- Transacao
- LogAuditoria
- HistoricoCotacao

### Padroes obrigatorios
- Adapter
- Strategy
- Factory

Os padroes devem isolar as integracoes externas do dominio.

---

## 5. Perfis de Usuario

### ROLE_USER
Pode:
- criar conta;
- fazer login;
- consultar o proprio perfil;
- visualizar somente sua propria carteira;
- realizar depositos e saques;
- registrar compras e vendas;
- consultar posicoes, graficos e logs proprios.

### ROLE_ADMIN
Pode:
- acessar `/admin`;
- consultar metricas gerais;
- acompanhar status das APIs externas;
- visualizar falhas de validacao da CVM;
- visualizar auditoria global.

O administrador **nao deve ter acesso aos dados privados de carteira de outros usuarios**.

---

## 6. Autenticacao e Seguranca

### Cadastro
Endpoint:

```http
POST /api/v1/auth/register
```

Requisitos:
- e-mail valido;
- e-mail unico;
- senha com no minimo 8 caracteres;
- 1 letra maiuscula;
- 1 letra minuscula;
- 1 numero;
- 1 caractere especial;
- senha armazenada com BCrypt, strength 12;
- novo usuario recebe `ROLE_USER`;
- criar automaticamente uma carteira principal com saldo zero.

### Login
Endpoint:

```http
POST /api/v1/auth/login
```

Retornar JWT contendo:
- `usuarioId`;
- `email`;
- `role`.

### Usuario autenticado

```http
GET /api/v1/auth/me
```

### Isolamento de dados

Toda operacao privada deve usar o `usuario_id` obtido do JWT/SecurityContext.

Um usuario nunca pode consultar ou modificar recursos pertencentes a outro usuario.

Tentativas de acesso cruzado devem retornar:

```http
403 Forbidden
```

e gerar registro de auditoria.

---

## 7. Administrador Padrao

Na inicializacao do backend, um `CommandLineRunner` deve verificar a existencia do administrador configurado no `.env`.

Variaveis:

```env
ADMIN_NAME=Administrador do Sistema
ADMIN_EMAIL=admin@carteira.com
ADMIN_PASSWORD=Admin@2026Secure
```

Se nao existir:
- criar usuario;
- aplicar BCrypt a senha;
- atribuir `ROLE_ADMIN`;
- marcar como ativo.

Se ja existir:
- nao executar nenhuma alteracao.

O processo deve ser idempotente.

---

## 8. Corretoras e Compliance

### Cadastro de corretora

```http
POST /api/v1/corretoras
```

Entrada principal:
- CNPJ;
- numero opcional;
- complemento opcional.

Fluxo:

```text
Receber CNPJ
→ remover caracteres nao numericos
→ validar digitos verificadores
→ consultar Receita Federal
→ exigir situacao ATIVA
→ consultar CVM
→ exigir registro ativo
→ consultar endereco por CEP
→ persistir corretora
```

### Regras

- CNPJ duplicado nao e permitido.
- Corretora inativa na Receita Federal nao pode ser cadastrada.
- Corretora sem registro ativo na CVM nao pode ser cadastrada.
- Falha regulatoria deve retornar:

```http
422 Unprocessable Entity
```

- Nenhuma informacao deve ser persistida se a validacao da CVM falhar.
- A falha deve gerar log de auditoria.

### Outros endpoints

```http
GET  /api/v1/corretoras
GET  /api/v1/corretoras/{id}
GET  /api/v1/corretoras/cnpj/{cnpj}
POST /api/v1/corretoras/{id}/acoes/{acaoId}
```

Listagens devem suportar paginacao.

---

## 9. Ativos e Cotacoes

### Cadastro

```http
POST /api/v1/acoes
```

O sistema deve identificar automaticamente o mercado pelo ticker.

### B3
Exemplos:

```text
PETR4
VALE3
HGLG11
IVVB11
```

Configuracao:
- mercado: `B3`;
- moeda: `BRL`;
- provedor principal: Brapi.

### Mercado americano
Exemplos:

```text
AAPL
TSLA
MSFT
NVDA
```

Configuracao:
- mercado: `US_MARKET`;
- moeda: `USD`;
- provedor: AlphaVantage ou TwelveData.

### Regras
- ticker duplicado nao e permitido;
- ticker inexistente retorna `404 Not Found`;
- cotacao deve ser armazenada com data/hora;
- consultas repetidas para o mesmo ticker em menos de 10 minutos devem usar cache Caffeine.

### Endpoints

```http
POST /api/v1/acoes
GET  /api/v1/acoes
GET  /api/v1/acoes/ticker/{ticker}
PUT  /api/v1/acoes/{id}/atualizar-cotacao
GET  /api/v1/acoes/{id}/historico
```

---

## 10. APIs Externas

| Servico | Uso |
|---|---|
| BrasilAPI CNPJ | Dados da Receita Federal |
| BrasilAPI CVM | Validacao de corretoras |
| ViaCEP | Endereco por CEP |
| Brapi | Cotacoes B3 |
| AlphaVantage / TwelveData | Cotacoes dos EUA |

### Regras
- clientes HTTP devem utilizar OpenFeign;
- integracoes devem ficar isoladas do dominio;
- timeout de aproximadamente 3 a 5 segundos;
- cotacao deve utilizar cache de 10 minutos;
- falhas externas relevantes devem ser tratadas e convertidas em respostas padronizadas.

---

## 11. Carteira e Caixa

Cada usuario deve possuir sua propria carteira.

### Resumo

```http
GET /api/v1/carteira/resumo
```

Deve retornar:
- patrimonio total;
- saldo em caixa;
- valor aplicado;
- lucro/prejuizo;
- posicoes consolidadas.

### Caixa

```http
POST /api/v1/carteira/caixa/deposito
POST /api/v1/carteira/caixa/saque
```

Regras:
- saldo nao pode ficar negativo;
- cada deposito ou saque gera auditoria.

---

## 12. Compras e Vendas

Endpoint:

```http
POST /api/v1/carteira/transacoes
```

Dados principais:
- ticker;
- tipo `BUY` ou `SELL`;
- quantidade;
- preco unitario;
- taxas;
- data da negociacao;
- corretora.

### Compra

So pode ocorrer quando:

```text
Saldo em Caixa >= Valor Total da Compra
```

Calculo:

```text
Custo = (Quantidade × Preco Unitario) + Taxas

Novo PM =
[(Quantidade Anterior × PM Anterior) + Custo]
/
(Quantidade Anterior + Quantidade Comprada)
```

Depois da compra:
- debitar caixa;
- atualizar posicao;
- atualizar preco medio;
- atualizar total investido;
- registrar transacao;
- gerar auditoria.

### Venda

So pode ocorrer quando:

```text
Quantidade Vendida <= Quantidade em Custodia
```

Nao e permitido venda a descoberto.

Calculo:

```text
Custo Base = Quantidade Vendida × PM Atual

Valor Liquido =
(Quantidade Vendida × Preco de Venda) - Taxas

Lucro/Prejuizo Realizado =
Valor Liquido - Custo Base
```

A venda nao altera o preco medio das unidades restantes.

Se a posicao ficar zerada:

```text
PM = 0
Total Investido = 0
```

Todos os calculos financeiros devem utilizar `BigDecimal` e `RoundingMode.HALF_EVEN`.

---

## 13. Indicadores da Carteira

### Patrimonio

```text
Patrimonio =
Saldo em Caixa +
Σ(Quantidade × Cotacao Atual)
```

### Lucro nao realizado

```text
Lucro Nao Realizado =
Valor Atual das Posicoes - Total Investido
```

### Rentabilidade

```text
Rentabilidade (%) =
(Lucro Nao Realizado / Total Investido) × 100
```

### Alocacao

```text
Alocacao do Ativo (%) =
Valor Atual do Ativo / Patrimonio Total × 100
```

---

## 14. Auditoria

A aplicacao deve possuir dois niveis de log:

### Log da aplicacao
- SLF4J;
- preferencialmente estruturado em JSON.

### Auditoria no PostgreSQL

Tabela:

```text
logs_auditoria
```

Registrar eventos como:
- compra;
- venda;
- deposito;
- saque;
- login;
- acesso negado;
- alteracao cadastral;
- falha CVM.

### Endpoints do usuario

```http
GET /api/v1/auditoria/transacoes
GET /api/v1/auditoria/eventos
```

O usuario so pode visualizar seus proprios registros.

---

## 15. Painel Administrativo

Rotas protegidas por:

```java
@PreAuthorize("hasRole('ADMIN')")
```

### Endpoints

```http
GET /api/v1/admin/metricas
GET /api/v1/admin/auditoria/cvm-falhas
GET /api/v1/admin/integracoes/status
GET /api/v1/admin/auditoria/global
```

### Metricas principais
- total de usuarios;
- total de corretoras;
- total de ativos;
- total de transacoes;
- volume financeiro;
- quantidade de bloqueios CVM;
- status das integracoes externas.

---

## 16. Frontend

O frontend deve seguir como referencia visual uma plataforma de acompanhamento de investimentos no estilo Investidor10.

### Autenticacao
- `/login`
- `/register`

Usuarios nao autenticados devem ser redirecionados para login.

Usuarios `ROLE_USER` nao podem acessar `/admin`.

### Dashboard do investidor

Deve conter cards:
- patrimonio total;
- valor aplicado;
- saldo em caixa;
- lucro/prejuizo.

### Abas

#### 1. Visao Geral
- evolucao patrimonial;
- principais posicoes;
- maiores altas e baixas.

#### 2. Meus Ativos
Tabela com:
- ticker;
- mercado;
- quantidade;
- preco medio;
- cotacao;
- total investido;
- posicao atual;
- lucro/prejuizo;
- percentual da carteira.

#### 3. Compras e Vendas
- historico de transacoes;
- modal para nova compra/venda.

#### 4. Graficos
- alocacao por ativo;
- alocacao por mercado;
- alocacao por setor;
- fluxo mensal.

#### 5. Logs e Caixa
- depositos;
- saques;
- auditoria do usuario.

### Admin

Pagina:

```text
/admin
```

Deve possuir:
- metricas globais;
- status das APIs;
- bloqueios da CVM;
- auditoria global.

---

## 17. Tratamento de Erros

Utilizar:

```java
@RestControllerAdvice
ProblemDetail
```

Padronizar respostas para:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
422 Unprocessable Entity
502 Bad Gateway
```

---

## 18. Banco de Dados

PostgreSQL deve possuir:

```text
usuarios
corretoras
acoes
historico_cotacoes
carteiras
posicoes
transacoes
logs_auditoria
```

IDs devem utilizar UUID.

### Precisao

Cotacoes e preco medio:

```sql
NUMERIC(15,4)
```

Valores monetarios:

```sql
NUMERIC(18,2)
```

Quantidades podem possuir ate 8 casas decimais.

---

## 19. Versionamento do Banco

O banco deve ser versionado exclusivamente pelo **Flyway**.

Diretorio:

```text
backend/src/main/resources/db/migration/
```

Exemplo:

```text
V1__create_initial_schema.sql
V2__create_database_indexes.sql
V3__add_sector_to_acoes.sql
```

Regras:
- migrations executadas nunca devem ser alteradas;
- cada mudanca estrutural exige nova migration;
- Flyway deve executar automaticamente no startup;
- Hibernate nao pode alterar o schema automaticamente.

Configuracao obrigatoria:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
```

O historico sera mantido em:

```text
flyway_schema_history
```

---

## 20. Variaveis de Ambiente

O projeto deve possuir `.env` e `.env.example`.

Variaveis minimas:

```env
POSTGRES_DB=carteira_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgrespassword
POSTGRES_PORT=5432

DB_URL=jdbc:postgresql://postgres-db:5432/carteira_db
DB_USERNAME=postgres
DB_PASSWORD=postgrespassword

BACKEND_PORT=8080

JWT_SECRET_KEY=chave-local-desenvolvimento-com-mais-de-256-bits-2026
JWT_EXPIRATION_HOURS=24

ADMIN_NAME=Administrador do Sistema
ADMIN_EMAIL=admin@carteira.com
ADMIN_PASSWORD=Admin@2026Secure

BRAPI_TOKEN=
ALPHAVANTAGE_API_KEY=
TWELVE_DATA_API_KEY=

FRONTEND_PORT=3000
NEXT_PUBLIC_API_URL=http://localhost:8080
```

O `.env` academico pode conter apenas credenciais locais/de demonstracao.

Segredos reais nao devem ser versionados.

---

## 21. Docker

A aplicacao completa deve executar por:

```bash
docker compose up --build
```

O `docker-compose.yml` deve subir:
1. PostgreSQL;
2. backend Spring Boot;
3. frontend Next.js.

### Ordem esperada

```text
PostgreSQL
-> Backend
-> Flyway
-> Hibernate validate
-> Seeder ADMIN
-> Frontend
```

### Portas

```text
Frontend:   3000
Backend:    8080
PostgreSQL: 5432
```

---

## 22. Documentacao

Swagger disponivel em:

```text
http://localhost:8080/swagger-ui.html
```

Deve possuir suporte a autenticacao Bearer JWT.

O `README.md` deve conter:
- descricao do projeto;
- requisitos;
- como configurar `.env`;
- como executar via Docker;
- APIs externas utilizadas;
- credenciais locais do administrador;
- links de acesso;
- instrucoes de testes.

---

## 23. Entregaveis Obrigatorios

O repositorio final deve conter:

- backend completo;
- frontend completo;
- OpenSpec;
- Git versionado;
- Dockerfile do backend;
- Dockerfile do frontend;
- `docker-compose.yml`;
- `.env`;
- `.env.example`;
- migrations Flyway;
- README;
- colecao Postman ou Insomnia;
- DER atualizado;
- Swagger;
- aplicacao executavel via Docker.

---

## 24. Criterios de Aceite

O projeto sera considerado funcional quando:

- [ ] usuario consegue se cadastrar;
- [ ] usuario consegue fazer login;
- [ ] JWT protege os recursos privados;
- [ ] dados de usuarios diferentes permanecem isolados;
- [ ] administrador e criado automaticamente;
- [ ] corretoras sao validadas na Receita e CVM;
- [ ] ativos B3 e US podem ser cadastrados;
- [ ] cotacoes sao obtidas por APIs externas;
- [ ] cache evita consultas repetidas em menos de 10 minutos;
- [ ] usuario consegue depositar e sacar saldo;
- [ ] usuario consegue comprar acoes com saldo suficiente;
- [ ] sistema impede venda sem quantidade disponivel;
- [ ] preco medio e calculado corretamente;
- [ ] lucro/prejuizo realizado e calculado corretamente;
- [ ] dashboard apresenta posicoes e indicadores;
- [ ] operacoes importantes geram auditoria;
- [ ] painel administrativo e exclusivo de `ROLE_ADMIN`;
- [ ] banco e criado e evoluido via Flyway;
- [ ] Hibernate utiliza `ddl-auto: validate`;
- [ ] aplicacao completa sobe com `docker compose up --build`;
- [ ] Swagger esta acessivel;
- [ ] frontend, backend e PostgreSQL funcionam de forma integrada.

---

## 25. Restricoes do Desenvolvimento

Durante a implementacao:

1. Nao implementar funcionalidades fora deste PRD sem criar ou atualizar uma especificacao no OpenSpec.
2. Nao alterar migrations Flyway ja executadas.
3. Nao utilizar `ddl-auto: update` ou `create`.
4. Nao colocar segredos reais no Git.
5. Nao permitir acesso entre dados de usuarios diferentes.
6. Nao realizar calculos financeiros com `float` ou `double`; usar `BigDecimal`.
7. Nao permitir venda a descoberto.
8. Nao cadastrar corretora sem validacao ativa na CVM.
9. Nao permitir acesso as rotas administrativas por `ROLE_USER`.
10. Manter integracoes externas isoladas atraves de adapters.
