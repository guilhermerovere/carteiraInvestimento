# PRD - Sistema de Gestao de Ativos, Carteira e Corretoras

## 1. Visao do Produto

Desenvolver uma plataforma full-stack para gestao de investimentos em acoes nacionais e internacionais, com autenticacao, carteira individual por usuario, controle de caixa, compras e vendas, calculos financeiros, cotacoes externas, validacao regulatoria de corretoras, auditoria, painel administrativo e execucao completa via Docker.

O sistema deve seguir **Spec-Driven Development (SDD)** com OpenSpec.

O `PRD.md` define o escopo global do produto. As implementacoes devem ser divididas em changes menores no OpenSpec.

---

## 2. Decisoes de Dominio Consolidadas

Estas decisoes eliminam ambiguidades que nao devem ser redefinidas durante a implementacao.

### 2.1. Moeda base

- A moeda base da carteira e `BRL`.
- O saldo em caixa e sempre armazenado em BRL.
- Ativos B3 possuem cotacao em BRL.
- Ativos dos EUA possuem cotacao em USD.
- Para ativos em USD, o sistema deve obter a taxa `USD/BRL`.
- AlphaVantage e o provedor primario para cotacoes dos EUA e cambio `USD/BRL`.
- TwelveData e o fallback quando o provedor primario estiver indisponivel.
- Para ativos B3, a taxa de cambio considerada e `1`.
- O patrimonio consolidado, valor investido e lucro/prejuizo do dashboard devem ser apresentados em BRL.

### 2.2. Corretoras e ativos sao catalogos globais

`Corretora` e `Acao` sao entidades globais do sistema.

Nao existe relacionamento direto permanente entre uma corretora e uma acao.

A corretora utilizada deve ser registrada na `Transacao`.

```text
Usuario
-> Carteira
-> Transacao
   -> Acao
   -> Corretora
```

### 2.3. Permissoes sobre catalogos

`ROLE_ADMIN` pode:
- cadastrar corretoras;
- atualizar corretoras quando necessario;
- cadastrar ativos;
- atualizar cotacoes manualmente;
- administrar os catalogos globais.

`ROLE_USER` pode:
- consultar corretoras;
- consultar ativos;
- usar esses catalogos em suas proprias operacoes;
- nunca alterar os catalogos globais.

### 2.4. Evolucao patrimonial

O sistema deve possuir snapshots diarios da carteira.

Deve existir no maximo um snapshot por carteira por data.

O snapshot do dia deve ser criado ou atualizado quando ocorrer:
- deposito;
- saque;
- compra;
- venda;
- consulta do resumo da carteira apos atualizacao de cotacoes.

Snapshots de dias anteriores nao devem ser alterados.

### 2.5. Movimentacoes de caixa

Depositos e saques devem possuir registro proprio em `movimentacoes_caixa`.

Compras e vendas permanecem em `transacoes`.

O saldo da carteira e atualizado pelas duas categorias, mas cada uma possui seu historico separado.

### 2.6. Auditoria administrativa e privacidade

O administrador pode visualizar eventos globais de seguranca, disponibilidade, compliance e operacao do sistema.

A auditoria administrativa nao deve expor:
- saldo de carteira;
- patrimonio individual;
- posicoes;
- quantidade de ativos;
- valores financeiros de transacoes privadas;
- preco medio;
- detalhes financeiros particulares de outros usuarios.

Eventos administrativos podem conter:
- usuario relacionado;
- tipo de evento;
- data/hora;
- severidade;
- endpoint;
- resultado;
- provedor externo;
- dados de compliance de corretoras quando aplicavel.

### 2.7. Historico de cotacoes

Uma nova entrada em `historico_cotacoes` deve ser criada somente quando uma nova cotacao for obtida com sucesso de um provedor externo.

Retorno de cotacao pelo cache:
- nao gera nova entrada no historico.

Nesta versao do produto:
- nao existe expiracao automatica do historico de cotacoes.

---

## 3. Stack Obrigatoria

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
- SLF4J
- Maven

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

## 4. Estrutura do Projeto

```text
carteira-investimento/
- backend/
- frontend/
- openspec/
- PRD.md
- .env
- .env.example
- .gitignore
- docker-compose.yml
- README.md
- AGENTS.md
```

---

## 5. Arquitetura do Backend

```text
domain/
- entidades
- enums
- excecoes
- regras de negocio

infrastructure/
- configuracoes
- seguranca
- clientes externos
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
- HistoricoCotacao
- Carteira
- Posicao
- Transacao
- MovimentacaoCaixa
- CarteiraSnapshot
- LogAuditoria

### Padroes obrigatorios

- Adapter
- Strategy
- Factory

As integracoes externas devem permanecer isoladas do dominio.

---

## 6. Perfis de Usuario

### ROLE_USER

Pode:
- criar conta;
- fazer login;
- consultar o proprio perfil;
- consultar catalogo de corretoras;
- consultar catalogo de ativos;
- visualizar somente sua propria carteira;
- realizar depositos e saques;
- registrar compras e vendas;
- consultar suas posicoes;
- consultar seus graficos;
- consultar seus proprios logs.

### ROLE_ADMIN

Pode:
- acessar `/admin`;
- cadastrar e manter corretoras;
- cadastrar e manter ativos;
- atualizar cotacoes manualmente;
- consultar metricas gerais;
- acompanhar status das APIs;
- consultar falhas CVM;
- consultar auditoria global sem dados financeiros privados.

O administrador nao deve acessar carteiras privadas de outros usuarios.

---

## 7. Autenticacao e Seguranca

### Cadastro

```http
POST /api/v1/auth/register
```

Requisitos:
- e-mail valido;
- e-mail unico;
- senha com no minimo 8 caracteres;
- ao menos 1 letra maiuscula;
- ao menos 1 letra minuscula;
- ao menos 1 numero;
- ao menos 1 caractere especial;
- senha armazenada com BCrypt strength 12;
- usuario recebe `ROLE_USER`;
- uma carteira principal deve ser criada automaticamente com saldo zero.

### Login

```http
POST /api/v1/auth/login
```

O JWT deve conter:
- `usuarioId`;
- `email`;
- `role`.

### Usuario autenticado

```http
GET /api/v1/auth/me
```

### Isolamento de dados

Toda operacao privada deve usar o `usuario_id` obtido do JWT e `SecurityContext`.

Nunca confiar em um `usuario_id` recebido do frontend para autorizar acesso a recursos privados.

Tentativa de acesso cruzado:

```http
403 Forbidden
```

A tentativa deve gerar auditoria.

---

## 8. Administrador Padrao

Na inicializacao, um `CommandLineRunner` deve verificar a existencia do administrador configurado por variaveis de ambiente.

```env
ADMIN_NAME=Administrador do Sistema
ADMIN_EMAIL=admin@carteira.com
ADMIN_PASSWORD=Admin@2026Secure
```

Se nao existir:
- criar usuario;
- aplicar BCrypt;
- atribuir `ROLE_ADMIN`;
- marcar como ativo.

Se ja existir:
- nao alterar o registro.

O processo deve ser idempotente.

---

## 9. Corretoras e Compliance

Corretoras formam um catalogo global administrado por `ROLE_ADMIN`.

### Cadastro

```http
POST /api/v1/corretoras
```

Acesso:
- somente `ROLE_ADMIN`.

Entrada:
- CNPJ;
- numero opcional;
- complemento opcional.

Fluxo:

```text
CNPJ
-> sanitizar
-> validar
-> Receita Federal
-> CVM
-> CEP
-> persistir
```

### Regras

- CNPJ duplicado nao e permitido.
- CNPJ deve ser validado localmente antes de chamadas externas.
- Corretora deve estar `ATIVA` na Receita Federal.
- Corretora deve possuir registro ativo na CVM.
- Falha de validacao CVM deve retornar `422 Unprocessable Entity`.
- Em falha regulatoria nenhuma corretora deve ser persistida.
- Falhas de compliance devem gerar auditoria.

### Endpoints

```http
POST /api/v1/corretoras
GET  /api/v1/corretoras
GET  /api/v1/corretoras/{id}
GET  /api/v1/corretoras/cnpj/{cnpj}
```

Nao deve existir endpoint de vinculacao permanente entre corretora e acao.

Listagens devem possuir paginacao.

---

## 10. Ativos e Cotacoes

Ativos formam um catalogo global administrado por `ROLE_ADMIN`.

### Cadastro

```http
POST /api/v1/acoes
```

Acesso:
- somente `ROLE_ADMIN`.

O sistema deve identificar o mercado pelo ticker.

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
- provedor: Brapi.

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
- provedor primario: AlphaVantage;
- fallback: TwelveData.

### Regras

- ticker duplicado nao e permitido;
- ticker inexistente retorna `404 Not Found`;
- a cotacao deve preservar a moeda original do ativo;
- toda cotacao externa deve possuir data/hora;
- consultas repetidas em menos de 10 minutos devem utilizar Caffeine;
- retorno do cache nao gera nova linha no historico;
- nova cotacao externa bem-sucedida gera uma linha em `historico_cotacoes`.

### Endpoints

```http
POST /api/v1/acoes
GET  /api/v1/acoes
GET  /api/v1/acoes/ticker/{ticker}
PUT  /api/v1/acoes/{id}/atualizar-cotacao
GET  /api/v1/acoes/{id}/historico
```

`POST` e `PUT` sao exclusivos de `ROLE_ADMIN`.

Consultas `GET` podem ser utilizadas por usuarios autenticados.

---

## 11. Cambio USD/BRL

A carteira possui moeda base BRL.

Para ativos `US_MARKET`, o sistema deve obter a taxa atual `USD/BRL`.

### Provedores

```text
AlphaVantage
-> primario

TwelveData
-> fallback
```

### Regras

- a taxa deve possuir cache para reduzir chamadas externas;
- a taxa usada em uma transacao deve ser persistida na propria transacao;
- a taxa usada em uma transacao nunca deve ser recalculada retroativamente;
- para ativos B3, `taxaCambioBrl = 1`;
- patrimonio atual de ativos US deve utilizar a cotacao atual em USD multiplicada pela taxa USD/BRL atual.

---

## 12. APIs Externas

| Servico | Uso |
|---|---|
| BrasilAPI CNPJ | Dados cadastrais |
| BrasilAPI CVM | Validacao regulatoria |
| ViaCEP | Endereco |
| Brapi | Cotacoes B3 |
| AlphaVantage | Cotacoes US e USD/BRL |
| TwelveData | Fallback US e USD/BRL |

### Regras

- clientes HTTP devem utilizar OpenFeign;
- integracoes devem ser isoladas por adapters;
- timeout entre 3 e 5 segundos;
- cotacoes e cambio devem utilizar cache;
- falhas externas devem ser convertidas em respostas padronizadas;
- fallback so deve ser utilizado quando o provedor primario falhar ou estiver indisponivel.

---

## 13. Carteira e Caixa

Cada usuario deve possuir uma carteira principal criada no cadastro.

A moeda da carteira e sempre BRL.

### Resumo

```http
GET /api/v1/carteira/resumo
```

Retornar:
- patrimonio total em BRL;
- saldo em caixa em BRL;
- valor aplicado em BRL;
- lucro/prejuizo nao realizado em BRL e percentual;
- posicoes consolidadas;
- data da ultima atualizacao.

### Caixa

```http
POST /api/v1/carteira/caixa/deposito
POST /api/v1/carteira/caixa/saque
```

Regras:
- valores de deposito e saque sao em BRL;
- saldo nunca pode ficar negativo;
- deposito cria `MovimentacaoCaixa` do tipo `DEPOSITO`;
- saque cria `MovimentacaoCaixa` do tipo `SAQUE`;
- cada operacao gera auditoria;
- cada operacao atualiza o snapshot do dia.

---

## 14. Compras e Vendas

```http
POST /api/v1/carteira/transacoes
```

Dados principais:
- ticker;
- tipo `BUY` ou `SELL`;
- quantidade;
- preco unitario na moeda do ativo;
- taxas na moeda do ativo;
- data da negociacao;
- corretoraId.

A corretora deve existir no catalogo global e estar valida.

### Normalizacao para BRL

Para B3:

```text
taxaCambioBrl = 1
```

Para US:

```text
taxaCambioBrl = cotacao USD/BRL no momento da operacao
```

O sistema deve persistir na transacao:
- moeda original;
- preco unitario original;
- taxas originais;
- taxa de cambio utilizada;
- valor total convertido para BRL;
- resultado realizado em BRL quando aplicavel.

### Compra

```text
Saldo em Caixa BRL >= Custo Total BRL
```

```text
Custo Origem =
(Quantidade * Preco Unitario) + Taxas

Custo BRL =
Custo Origem * Taxa Cambio BRL

Novo Custo Total BRL =
Custo Total BRL Anterior + Custo BRL

Novo Preco Medio BRL =
Novo Custo Total BRL / Nova Quantidade
```

Depois da compra:
- debitar caixa em BRL;
- atualizar posicao;
- atualizar custo total investido em BRL;
- atualizar preco medio em BRL por unidade;
- registrar transacao;
- gerar auditoria;
- atualizar snapshot do dia.

### Venda

```text
Quantidade Vendida <= Quantidade em Custodia
```

Venda a descoberto nao e permitida.

```text
Custo Base BRL =
Quantidade Vendida * Preco Medio BRL

Valor Liquido Origem =
(Quantidade Vendida * Preco Venda) - Taxas

Valor Liquido BRL =
Valor Liquido Origem * Taxa Cambio BRL

Lucro/Prejuizo Realizado BRL =
Valor Liquido BRL - Custo Base BRL
```

A venda:
- credita o valor liquido em BRL no caixa;
- reduz a quantidade da posicao;
- nao altera o preco medio BRL das unidades restantes;
- registra resultado realizado em BRL;
- gera auditoria;
- atualiza snapshot do dia.

Se a posicao zerar:

```text
quantidade = 0
precoMedioBrl = 0
totalInvestidoBrl = 0
```

Todos os calculos financeiros devem utilizar `BigDecimal` e `RoundingMode.HALF_EVEN`.

Nao utilizar `float` ou `double` para valores financeiros.

---

## 15. Posicoes e Indicadores

### Valor atual de uma posicao B3

```text
Valor Atual BRL =
Quantidade * Cotacao Atual BRL
```

### Valor atual de uma posicao US

```text
Valor Atual BRL =
Quantidade * Cotacao Atual USD * Cambio Atual USD/BRL
```

### Patrimonio total

```text
Patrimonio BRL =
Saldo Caixa BRL + Soma(Valor Atual BRL das Posicoes)
```

### Lucro nao realizado

```text
Lucro Nao Realizado BRL =
Soma(Valor Atual BRL) - Soma(Total Investido BRL)
```

### Rentabilidade

Se total investido > 0:

```text
Rentabilidade (%) =
(Lucro Nao Realizado BRL / Total Investido BRL) * 100
```

Se total investido = 0:
- rentabilidade = `0`.

### Alocacao

Se patrimonio > 0:

```text
Alocacao (%) =
Valor Atual BRL do Ativo / Patrimonio BRL * 100
```

---

## 16. Evolucao Patrimonial

A evolucao patrimonial deve ser baseada em `carteira_snapshots`.

Cada snapshot deve armazenar:
- carteira;
- data de referencia;
- saldo de caixa em BRL;
- valor atual das posicoes em BRL;
- total investido em BRL;
- patrimonio total em BRL;
- lucro nao realizado em BRL.

Regras:
- no maximo um snapshot por carteira por dia;
- snapshot do dia atual pode ser atualizado;
- snapshots anteriores sao imutaveis.

Endpoint:

```http
GET /api/v1/carteira/graficos/evolucao
```

---

## 17. Auditoria

### Log da aplicacao

- SLF4J;
- preferencialmente JSON;
- nunca registrar senhas, tokens ou segredos.

### Auditoria persistida

Tabela:

```text
logs_auditoria
```

Eventos:
- cadastro;
- login;
- compra;
- venda;
- deposito;
- saque;
- acesso negado;
- alteracao administrativa;
- falha CVM;
- falha de integracao relevante.

### Endpoints do usuario

```http
GET /api/v1/auditoria/transacoes
GET /api/v1/auditoria/eventos
```

O usuario so pode visualizar seus proprios registros.

### Auditoria administrativa

A auditoria global deve mostrar apenas metadados operacionais, de compliance e seguranca.

Nao deve revelar dados financeiros privados de outras carteiras.

---

## 18. Painel Administrativo

Rotas:

```text
/api/v1/admin/**
```

Protecao:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Endpoints:

```http
GET /api/v1/admin/metricas
GET /api/v1/admin/auditoria/cvm-falhas
GET /api/v1/admin/integracoes/status
GET /api/v1/admin/auditoria/global
```

Metricas:
- total de usuarios;
- total de corretoras;
- total de ativos;
- total de transacoes;
- volume financeiro global agregado;
- quantidade de bloqueios CVM;
- status das integracoes.

Metricas devem ser agregadas e nao devem permitir abrir os dados privados de uma carteira individual.

---

## 19. Frontend

Referencia visual:
- plataforma de acompanhamento de investimentos no estilo Investidor10.

### Autenticacao

```text
/login
/register
```

Usuarios nao autenticados devem ser redirecionados ao login.

`ROLE_USER` nao pode acessar `/admin`.

### Dashboard

Cards:
- patrimonio total BRL;
- valor aplicado BRL;
- saldo em caixa BRL;
- lucro/prejuizo BRL e percentual.

### Abas

#### 1. Visao Geral
- evolucao patrimonial;
- principais posicoes;
- maiores altas e baixas.

#### 2. Meus Ativos
- ticker;
- mercado;
- moeda;
- quantidade;
- preco medio BRL;
- cotacao na moeda original;
- valor atual BRL;
- total investido BRL;
- lucro/prejuizo;
- percentual da carteira.

#### 3. Compras e Vendas
- historico;
- nova compra;
- nova venda;
- corretora utilizada;
- moeda;
- taxa de cambio quando aplicavel.

#### 4. Graficos
- evolucao patrimonial;
- alocacao por ativo;
- alocacao por mercado;
- alocacao por setor;
- fluxo mensal.

#### 5. Logs e Caixa
- depositos;
- saques;
- historico de movimentacoes;
- auditoria propria.

### Admin

```text
/admin
```

Deve possuir:
- metricas globais;
- status das APIs;
- bloqueios CVM;
- auditoria global sanitizada;
- administracao dos catalogos de corretoras e ativos.

---

## 20. Tratamento de Erros

Utilizar:

```java
@RestControllerAdvice
ProblemDetail
```

Padronizar:

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

## 21. Modelo de Dados

PostgreSQL deve possuir:

```text
usuarios
corretoras
acoes
historico_cotacoes
carteiras
posicoes
transacoes
movimentacoes_caixa
carteira_snapshots
logs_auditoria
```

### Relacionamentos

```text
Usuario -> Carteira
Carteira -> Posicoes
Carteira -> Transacoes
Carteira -> MovimentacoesCaixa
Carteira -> CarteiraSnapshots
Transacao -> Acao
Transacao -> Corretora
HistoricoCotacao -> Acao
```

Nao deve existir:
- `corretora_id` em `acoes`;
- relacionamento fixo `Acao -> Corretora`.

IDs:
- UUID.

Cotacoes:

```sql
NUMERIC(15,4)
```

Precos medios e cambio:

```sql
NUMERIC(18,8)
```

Valores monetarios consolidados:

```sql
NUMERIC(18,2)
```

Quantidades:

```sql
NUMERIC(18,8)
```

---

## 22. Campos Minimos das Entidades Financeiras

### Carteira

```text
id
usuario_id
nome
saldo_caixa_brl
data_criacao
```

### Posicao

```text
id
carteira_id
acao_id
quantidade
preco_medio_brl
total_investido_brl
lucro_realizado_acumulado_brl
ultima_atualizacao
```

Unicidade:

```text
(carteira_id, acao_id)
```

### Transacao

```text
id
carteira_id
usuario_id
acao_id
corretora_id
tipo
quantidade
moeda
preco_unitario
taxas
taxa_cambio_brl
valor_total_brl
resultado_realizado_brl
data_negociacao
data_registro
```

### MovimentacaoCaixa

```text
id
carteira_id
usuario_id
tipo
valor_brl
descricao
data_hora
```

Tipos:

```text
DEPOSITO
SAQUE
```

### CarteiraSnapshot

```text
id
carteira_id
data_referencia
saldo_caixa_brl
valor_posicoes_brl
total_investido_brl
patrimonio_total_brl
lucro_nao_realizado_brl
```

Unicidade:

```text
(carteira_id, data_referencia)
```

---

## 23. Versionamento do Banco

O banco deve ser versionado exclusivamente por Flyway.

Diretorio:

```text
backend/src/main/resources/db/migration/
```

Exemplo:

```text
V1__create_initial_schema.sql
V2__create_database_indexes.sql
V3__add_new_field.sql
```

Regras:
- migration executada nunca deve ser alterada;
- nova alteracao estrutural exige nova migration;
- Flyway executa automaticamente no startup;
- Hibernate nao cria nem altera tabelas.

Configuracao:

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

Historico:

```text
flyway_schema_history
```

---

## 24. Variaveis de Ambiente

O projeto deve possuir `.env` e `.env.example`.

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

O `.env` academico pode conter valores locais e de demonstracao.

Segredos reais nao devem ser versionados.

---

## 25. Docker

Execucao completa:

```bash
docker compose up --build
```

Servicos:
1. PostgreSQL 16+;
2. backend Spring Boot;
3. frontend Next.js.

Ordem:

```text
PostgreSQL
-> healthcheck
-> Backend
-> Flyway
-> Hibernate validate
-> Seeder ADMIN
-> Frontend
```

Portas:

```text
Frontend: 3000
Backend: 8080
PostgreSQL: 5432
```

A imagem PostgreSQL deve possuir versao fixa compativel com PostgreSQL 16+.

Nao utilizar `postgres:latest`.

---

## 26. Swagger e Documentacao

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

Deve possuir Bearer JWT.

O `README.md` deve conter:
- descricao;
- stack;
- configuracao `.env`;
- execucao Docker;
- APIs externas;
- credenciais locais do administrador;
- endpoints;
- testes.

---

## 27. Testes

### Seguranca
- cadastro;
- login;
- JWT invalido;
- `ROLE_USER` bloqueado em admin;
- acesso cruzado bloqueado.

### Corretoras
- CNPJ invalido;
- CNPJ duplicado;
- Receita inativa;
- CVM invalida;
- cadastro valido.

### Ativos
- ticker invalido;
- ticker duplicado;
- B3;
- US;
- fallback;
- cache.

### Carteira
- deposito;
- saque;
- saldo insuficiente.

### Transacoes
- compra valida;
- compra sem saldo;
- venda valida;
- venda acima da custodia;
- preco medio;
- resultado realizado;
- conversao USD/BRL.

### Persistencia
- migrations Flyway;
- integracao PostgreSQL.

Quando um comportamento depender de PostgreSQL, H2 nao deve substituir o teste de integracao.

---

## 28. Entregaveis Obrigatorios

- backend completo;
- frontend completo;
- OpenSpec;
- Git versionado;
- Dockerfile backend;
- Dockerfile frontend;
- `docker-compose.yml`;
- `.env`;
- `.env.example`;
- migrations Flyway;
- README;
- colecao Postman ou Insomnia;
- DER atualizado;
- Swagger;
- testes;
- aplicacao executavel via Docker.

---

## 29. Criterios de Aceite

- [ ] usuario consegue se cadastrar;
- [ ] carteira principal e criada no cadastro;
- [ ] usuario consegue fazer login;
- [ ] JWT protege recursos privados;
- [ ] usuarios nao acessam dados uns dos outros;
- [ ] administrador e criado de forma idempotente;
- [ ] apenas ADMIN altera catalogos globais;
- [ ] corretoras sao validadas na Receita e CVM;
- [ ] nao existe relacionamento fixo entre acao e corretora;
- [ ] ativos B3 e US podem ser cadastrados pelo ADMIN;
- [ ] usuarios autenticados podem consultar ativos e corretoras;
- [ ] Brapi fornece cotacoes B3;
- [ ] AlphaVantage e primario para US;
- [ ] TwelveData funciona como fallback;
- [ ] cotacoes usam cache;
- [ ] cache nao duplica historico de cotacoes;
- [ ] carteira utiliza BRL como moeda base;
- [ ] operacoes US persistem a taxa USD/BRL utilizada;
- [ ] patrimonio US e convertido para BRL;
- [ ] usuario consegue depositar e sacar;
- [ ] depositos e saques possuem historico proprio;
- [ ] usuario consegue comprar com saldo suficiente;
- [ ] venda a descoberto e bloqueada;
- [ ] preco medio em BRL e calculado corretamente;
- [ ] lucro/prejuizo realizado em BRL e calculado corretamente;
- [ ] snapshots diarios suportam evolucao patrimonial;
- [ ] dashboard apresenta indicadores consolidados;
- [ ] operacoes importantes geram auditoria;
- [ ] auditoria do ADMIN nao expoe dados financeiros privados;
- [ ] painel administrativo e exclusivo de `ROLE_ADMIN`;
- [ ] banco evolui exclusivamente via Flyway;
- [ ] Hibernate utiliza `ddl-auto: validate`;
- [ ] aplicacao sobe com `docker compose up --build`;
- [ ] Swagger esta acessivel;
- [ ] frontend, backend e PostgreSQL funcionam integrados.

---

## 30. Restricoes de Desenvolvimento

1. Nao implementar funcionalidades fora deste PRD sem change correspondente no OpenSpec.
2. Nao alterar migration Flyway ja executada.
3. Nao utilizar `ddl-auto: update`, `create` ou `create-drop`.
4. Nao versionar segredos reais.
5. Nao confiar em `usuario_id` enviado pelo cliente para autorizacao.
6. Nao permitir acesso entre dados privados de usuarios.
7. Nao utilizar `float` ou `double` para calculos financeiros.
8. Nao permitir venda a descoberto.
9. Nao cadastrar corretora sem validacao ativa na CVM.
10. Nao permitir `ROLE_USER` alterar catalogos globais.
11. Nao permitir `ROLE_USER` acessar rotas administrativas.
12. Nao criar relacionamento fixo entre `Acao` e `Corretora`.
13. Nao recalcular retroativamente taxa de cambio persistida em transacoes.
14. Nao criar historico de cotacao quando a resposta vier do cache.
15. Manter integracoes externas isoladas por Adapter, Strategy e Factory.
16. Manter somente uma change principal ativa por vez, salvo dependencia explicitamente documentada.
17. Changes concluidas devem ser testadas, revisadas e arquivadas antes da proxima etapa principal.
