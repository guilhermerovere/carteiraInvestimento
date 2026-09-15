# PRD - Sistema de Gestao de Ativos, Carteira e Corretoras

## 1. Visao do Produto

Desenvolver uma plataforma full-stack para gestao de investimentos em acoes nacionais e internacionais, com autenticacao, carteira individual para usuarios `ROLE_USER`, controle de caixa, compras e vendas, calculos financeiros, cotacoes externas, validacao regulatoria de corretoras, auditoria, painel administrativo e execucao completa via Docker.

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
- BUY e SELL devem suportar ativos `B3` com moeda `BRL` e ativos `US_MARKET` com moeda `USD`; a moeda da `Transacao` deve ser coerente com o mercado e a moeda definidos no ativo.
- Para ativos em USD, o sistema deve obter a taxa real atual `USD/BRL` antes da confirmacao da transacao.
- Twelve Data e o provedor ativo para cotacoes dos EUA e cambio `USD/BRL`.
- Alpha Vantage nao participa do fluxo ativo de discovery, cotacao ou cambio.
- Para ativos B3, nao existe conversao externa de moeda e a taxa de cambio considerada e `1.00000000`.
- O patrimonio consolidado, valor investido e lucro/prejuizo do dashboard devem ser apresentados em BRL.
- A capability completa de compras e vendas depende da capability de cambio `USD/BRL` e deve habilitar negociacao efetiva de ativos B3/BRL e US/USD. Ativos US nao ficam limitados ao catalogo ou a market quotes.

### 2.2. Corretoras e ativos sao catalogos globais

`Corretora` e `Acao` sao entidades globais do sistema.

Nao existe relacionamento direto permanente entre uma corretora e uma acao.

A corretora utilizada deve ser registrada na `Transacao`. Toda transacao deve referenciar uma corretora ativa e valida do catalogo global pelo identificador permitido no contrato; compra e venda nao aceitam corretora textual livre. O catalogo de corretoras e pre-requisito da capability de compras e vendas e `corretora_id` nao deve ser removido da transacao antes de sua implementacao. Corretora inativa permanece referenciavel apenas por transacoes historicas, sem alterar o historico existente.

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
- consultar, editar somente numero e complemento, ativar e desativar corretoras;
- cadastrar ativos;
- atualizar cotacoes manualmente;
- administrar os catalogos globais.

`ROLE_ADMIN` nao deve excluir fisicamente corretora que possa ser referenciada por historico financeiro.

`ROLE_USER` pode:
- consultar corretoras ativas;
- consultar ativos;
- cadastrar um novo ativo financeiramente valido no catalogo canonico informando somente ticker e mercado;
- selecionar corretora ativa pelo identificador permitido ao registrar futura transacao;
- usar esses catalogos em suas proprias operacoes;
- nunca editar, ativar, desativar ou excluir ativos, administrar metadata ou cadastrar e administrar corretoras.

O cadastro controlado de ativo por `ROLE_USER` nao cria Posicao, nao cria Transacao, nao executa BUY e nao altera caixa.

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

Eventos de corretoras devem permanecer sanitizados: nao podem conter payload bruto de provider, credenciais ou tokens.

### 2.7. Historico de cotacoes

Uma nova entrada em `historico_cotacoes` deve ser criada somente quando uma nova cotacao for obtida com sucesso de um provedor externo.

A cotacao real de um ativo deve ser obtida pela capability de market quotes, por meio do provider configurado para o mercado do ativo.

A cotacao recebida do provider e o seu historico representam dados de mercado e nao podem ser alterados por edicao de preco feita pelo usuario durante uma transacao.

Retorno de cotacao pelo cache:
- nao gera nova entrada no historico.

Nesta versao do produto:
- nao existe expiracao automatica do historico de cotacoes.

### 2.8. Identidade, usuario e carteira principal

Existem somente as roles `ROLE_USER` e `ROLE_ADMIN`. Cada usuario possui exatamente uma role, representada como enum no dominio e protegida por constraint apropriada no PostgreSQL. Nao deve existir tabela de roles nesta versao.

A cardinalidade estrutural entre usuario e carteira e:

```text
Usuario 1 -> 0..1 Carteira
```

Regras:
- `ROLE_USER` possui exatamente uma carteira principal, obrigatoria;
- `ROLE_ADMIN` nao possui carteira;
- o cadastro de `ROLE_USER` cria usuario e carteira principal atomicamente, na mesma transacao;
- a carteira principal e criada com saldo inicial zero;
- nenhum endpoint financeiro faz parte da etapa de identidade, autenticacao, autorizacao, carteira principal minima e auditoria de seguranca.

As funcionalidades financeiras descritas nas demais secoes permanecem como escopo global futuro do produto e somente podem ser implementadas por changes posteriores especificas.

---

## 3. Stack Obrigatoria

### Backend
- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Security compativel com Spring Boot 4.1.1, preferencialmente na versao gerenciada pelo dependency management/BOM do Spring Boot
- Spring Boot Actuator
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
- package.json
- package-lock.json
- PRD.md
- .env
- .env.example
- .gitignore
- docker-compose.yml
- README.md
- AGENTS.md
```

O `package.json` e o `package-lock.json` da raiz pertencem ao tooling do repositorio e ao OpenSpec e devem ser preservados.

O frontend e um projeto Node independente e deve possuir:

```text
frontend/package.json
frontend/package-lock.json
```

O `package.json` da raiz nao deve ser transformado no projeto Next.js. Diretorios `node_modules` devem permanecer ignorados pelo Git.

---

## 5. Arquitetura do Backend

```text
domain/
- entidades
- enums
- excecoes e regras puras de dominio

application/
- use cases
- servicos de aplicacao
- orquestracao transacional
- ports/contratos necessarios pelos casos de uso

infrastructure/
- JPA
- implementacoes de repositories/ports
- Spring Security
- JWT
- BCrypt
- configuracoes
- adapters externos

presentation/
- controllers
- DTOs
- tratamento de erros HTTP
```

Cadastro, login, consulta do principal atual e provisionamento do administrador devem ser orquestrados pela camada `application`. O dominio nao depende de JPA, Spring Security, HTTP ou adapters externos; a infraestrutura implementa os ports definidos pela application e a presentation somente traduz contratos HTTP.

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

Cada usuario possui exatamente uma das duas roles existentes: `ROLE_USER` ou `ROLE_ADMIN`.

Nesta versao, a role deve ser um enum do dominio persistido em `usuarios`, com constraint de banco que aceite somente esses dois valores. Nao deve ser criada tabela de roles.

### ROLE_USER

Pode:
- criar conta;
- fazer login;
- consultar o proprio perfil;
- consultar catalogo de corretoras ativas;
- consultar catalogo de ativos;
- cadastrar novo ativo financeiramente valido no catalogo canonico informando somente ticker e mercado;
- visualizar somente sua propria carteira;
- realizar depositos e saques;
- registrar compras e vendas;
- consultar suas posicoes;
- consultar seus graficos;
- consultar seus proprios logs.
- alterar o proprio nome e e-mail;
- alterar a propria senha mediante confirmacao da senha atual;
- encerrar a propria conta somente com saldo em caixa zero e nenhuma posicao aberta.

### ROLE_ADMIN

Pode:
- acessar `/admin`;
- listar, consultar, cadastrar, editar os dados permitidos, ativar e desativar corretoras;
- cadastrar e manter ativos;
- atualizar cotacoes manualmente;
- consultar metricas gerais;
- acompanhar status das APIs;
- consultar falhas CVM;
- consultar auditoria global sem dados financeiros privados.

O administrador nao deve acessar carteiras privadas de outros usuarios.

`ROLE_ADMIN` nao possui carteira. `ROLE_USER` possui exatamente uma carteira principal.

---

## 7. Autenticacao e Seguranca

### 7.1. Campos minimos de Usuario

```text
id UUID
nome
email
senha_hash
role
ativo
criado_em
atualizado_em
```

### 7.2. Normalizacao e unicidade de e-mail

- aplicar `trim` ao e-mail recebido;
- converter o e-mail para lowercase antes de persistir;
- armazenar somente a forma canonica do e-mail;
- garantir unicidade case-insensitive tambem no PostgreSQL, por constraint ou indice apropriado;
- nao depender apenas da validacao da aplicacao para impedir duplicidade.

### 7.3. Senha

- utilizar BCrypt para armazenamento de senha, com strength 12;
- nunca armazenar senha em texto puro;
- nunca retornar senha ou `senha_hash` em respostas.

### 7.4. Cadastro

```http
POST /api/v1/auth/register
```

Requisitos:
- e-mail valido;
- e-mail canonico e unico conforme a secao 7.2;
- senha com no minimo 8 caracteres;
- ao menos 1 letra maiuscula;
- ao menos 1 letra minuscula;
- ao menos 1 numero;
- ao menos 1 caractere especial;
- senha armazenada com BCrypt strength 12;
- usuario recebe `ROLE_USER`;
- usuario nasce com `ativo=true`;
- usuario, carteira principal e auditoria de cadastro devem ser criados atomicamente na mesma transacao;
- a carteira principal deve receber por padrao o nome `Carteira Principal` e saldo inicial zero;
- retornar `201 Created`;
- nao autenticar automaticamente o usuario;
- nao gerar JWT;
- retornar somente `id`, `nome`, `email`, `role` e `ativo`;
- nao retornar senha, `senha_hash` ou token.

### 7.5. Login e Bearer JWT

```http
POST /api/v1/auth/login
```

Resposta de sucesso (`200 OK`):

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

`expiresIn` representa a duracao restante do access token em segundos. A resposta nao deve retornar senha, `senha_hash` ou refresh token. O evento de login bem sucedido deve ser persistido antes de considerar o login concluido e antes de retornar o token.

O JWT deve utilizar:
- algoritmo HMAC SHA-256 (`HS256`);
- segredo com pelo menos 256 bits;
- segredo fornecido por variavel de ambiente;
- expiracao configurada por `JWT_EXPIRATION_HOURS`, cujo valor padrao atual e 24 horas;
- issuer fornecido por `JWT_ISSUER`, com valor `carteira-investimento-backend`;
- audience fornecida por `JWT_AUDIENCE`, com valor `carteira-investimento-api`.

Claims obrigatorias:
- `sub`: UUID do usuario;
- `role`;
- `iat`;
- `exp`;
- `jti`;
- `iss`;
- `aud`.

Nao implementar refresh token nesta versao. Apos a expiracao do access token, o usuario deve realizar novo login.

Nao implementar blacklist de token ou logout server-side nesta etapa.

### 7.6. Usuario autenticado

```http
GET /api/v1/auth/me
```

Requisitos:
- utilizar exclusivamente o usuario autenticado pelo `SecurityContext`;
- nao aceitar `usuarioId` fornecido pelo cliente;
- retornar somente `id`, `nome`, `email`, `role` e `ativo`.

### 7.7. Usuario persistido, ativo e role atual

- usuario inativo nao pode realizar login;
- usuario inativo nao pode continuar utilizando JWT emitido anteriormente;
- durante a autenticacao de toda requisicao protegida, apos validar criptograficamente o JWT, carregar no banco o usuario indicado por `sub`;
- validar `iss` e `aud` durante a autenticacao;
- token criptograficamente valido cujo usuario nao exista, esteja inativo ou possua role persistida diferente da claim `role` deve resultar em `401 Unauthorized`;
- construir as authorities exclusivamente a partir da role atualmente persistida;
- nao criar endpoint administrativo para ativar ou desativar usuarios nesta etapa.

### 7.8. Semantica de 401 e 403

Retornar `401 Unauthorized` para:
- token ausente quando autenticacao for obrigatoria;
- token invalido;
- token expirado;
- usuario inexistente;
- usuario inativo.
- claim `role` divergente da role atualmente persistida.

Retornar `403 Forbidden` quando:
- usuario autenticado nao possui a role necessaria;
- usuario autenticado tenta acessar recurso que nao possui permissao.

### 7.9. Isolamento de dados

Toda operacao privada deve usar a identidade autenticada, cujo UUID esta no claim `sub`, por meio do `SecurityContext`.

Nunca confiar em um `usuario_id` recebido do frontend para autorizar acesso a recursos privados.

Tentativa de acesso cruzado:

```http
403 Forbidden
```

A tentativa deve gerar auditoria.

### 7.10. Configuracoes e encerramento da propria conta

- usuario autenticado pode alterar somente o proprio nome e e-mail;
- nome deve ser trimado, obrigatorio e limitado ao contrato persistido;
- e-mail deve ser trimado, lowercase, valido e unico de forma case-insensitive;
- alteracao de senha exige a senha atual correta e aplica a politica da secao 7.4;
- alteracoes sensiveis devem gerar auditoria sanitizada, sem senha, hash, JWT, Authorization, payload cru ou valores financeiros;
- a UX de exclusao usa encerramento logico, pois FKs e historico financeiro/auditoria impedem exclusao fisica segura;
- encerramento exige senha atual, confirmacao explicita, saldo em caixa igual a zero e nenhuma posicao com quantidade maior que zero, revalidados transacionalmente pelo backend;
- posicoes historicas zeradas, transacoes, movimentacoes, lucro realizado, snapshots e auditoria permanecem preservados;
- a identidade encerrada fica inativa, tem dados pessoais anonimizados de modo unico e nao pode voltar a autenticar ou operar;
- a sessao frontend atual e limpa apos troca de senha ou encerramento; sem token-version/blacklist, a aplicacao nao declara revogacao global de outros JWTs apos troca de senha;
- JWT de conta encerrada deixa de funcionar pela validacao obrigatoria do usuario persistido ativo em toda requisicao protegida.

### 7.11. Escopo inicial historico

Esta etapa cobre somente identidade, autenticacao, autorizacao, criacao minima da carteira principal e auditoria de seguranca.

Permanecem fora do escopo desta etapa:
- frontend de login e cadastro;
- armazenamento de token no navegador;
- refresh token;
- logout server-side;
- recuperacao de senha;
- endpoint de ativacao ou desativacao de usuarios;
- depositos;
- saques;
- transacoes;
- posicoes;
- corretoras;
- ativos;
- cotacoes;
- cambio;
- snapshots;
- dashboard;
- consulta administrativa dos logs;
- demais funcionalidades financeiras.

Consequentemente, nenhum endpoint financeiro deve ser implementado nesta etapa. As secoes financeiras deste PRD documentam o produto completo e permanecem sujeitas a changes futuras.

---

## 8. Administrador Padrao

Na inicializacao, um `CommandLineRunner` deve verificar a existencia do administrador configurado por variaveis de ambiente.

```env
ADMIN_NAME=Administrador do Sistema
ADMIN_EMAIL=admin@carteira.com
ADMIN_PASSWORD=Admin@2026Secure
```

`ADMIN_NAME`, `ADMIN_EMAIL` e `ADMIN_PASSWORD` sao obrigatorios nesta versao e formam um conjunto indivisivel. O processo deve ser fail-fast: se qualquer configuracao estiver ausente, parcial ou invalida, o startup deve falhar explicitamente antes de criar administrador parcial.

O processo deve ser idempotente e seguir estas regras:
- normalizar o e-mail configurado conforme a secao 7.2;
- validar `ADMIN_PASSWORD` pela mesma politica da secao 7.4 e armazena-la com BCrypt strength 12;
- se o e-mail ainda nao existir, criar usuario `ROLE_ADMIN`, ativo, com senha protegida por BCrypt strength 12 e auditoria de criacao na mesma transacao;
- `ROLE_ADMIN` nao recebe carteira;
- se o mesmo administrador ja existir, nao duplicar;
- nunca sobrescrever automaticamente senha ou role de usuario existente;
- se o e-mail configurado para administrador ja existir como `ROLE_USER`, falhar explicitamente em vez de promover o usuario silenciosamente;
- configuracao `ADMIN_*` ausente, parcial ou invalida deve falhar explicitamente, sem criar administrador parcial ou com credenciais invalidas.

---

## 9. Corretoras e Compliance

Corretoras formam um catalogo global administrado por `ROLE_ADMIN`.

Corretora nao pertence a usuario individual. Nao deve existir `usuario_id` em Corretora para ownership; todos os usuarios consultam o mesmo catalogo global de corretoras ativas.

### Modelo conceitual

```text
id UUID
cnpj
razaoSocial
nomeFantasia opcional
cep
logradouro
bairro
cidade
uf
numero opcional
complemento opcional
ativo
criadoEm
atualizadoEm
```

Nao adicionar `usuarioId`, mercado, ativo financeiro, saldo, dados de carteira ou campos financeiros em Corretora.

### Cadastro

```http
POST /api/v1/corretoras
```

Acesso:
- somente `ROLE_ADMIN`.

Entrada:
- CNPJ obrigatorio;
- numero opcional;
- complemento opcional.

Fluxo:

```text
CNPJ
-> remover formatacao e validar localmente
-> Receita Federal e verificacao de situacao cadastral exigida
-> CVM e verificacao de registro ativo
-> CEP e enriquecimento do endereco
-> persistir
```

### Regras

- CNPJ pode ser recebido formatado, mas a aplicacao deve remover a formatacao, validar formalmente os digitos verificadores e persistir exatamente 14 digitos.
- CNPJ e obrigatorio, globalmente unico, imutavel depois da criacao e persistido como `CHAR(14)` ou equivalente que preserve exatamente o contrato canonico regulatorio retornado pela CVM.
- PATCH de Corretora nao pode alterar CNPJ. Matriz e estabelecimentos/filiais com a mesma raiz de oito digitos representam a mesma instituicao regulada e nao podem gerar registros distintos.
- Corretora deve estar `ATIVA` na Receita Federal.
- Corretora deve possuir registro ativo na CVM.
- `razaoSocial` deve ser obtida da Receita, e obrigatoria para concluir o cadastro. `nomeFantasia`, quando fornecido pela Receita, deve ser persistido como opcional.
- `razaoSocial` e `nomeFantasia` nao sao editaveis manualmente. Dados manuais nao podem substituir a identidade oficial obtida da fonte externa.
- O endereco cadastral persistido deve conter `cep`, `logradouro`, `bairro`, `cidade` e `uf`, obtidos no fluxo Receita/CEP. CEP deve possuir exatamente 8 digitos canonicos e UF exatamente 2 caracteres canonicos em maiusculo.
- `numero` e texto manual opcional: aplicar trim, converter blank em `null` e limitar a 20 caracteres. Nao obter numero automaticamente do CEP.
- `complemento` e texto manual opcional: aplicar trim, converter blank em `null` e limitar a 160 caracteres.
- Depois do cadastro, `ROLE_ADMIN` pode editar manualmente somente `numero` e `complemento`. CNPJ, razaoSocial, nomeFantasia, cep, logradouro, bairro, cidade, uf e informacoes de compliance da Receita/CVM nao aceitam PATCH manual.
- Falha de validacao CVM deve retornar `422 Unprocessable Entity`.
- Reprovacao regulatoria, inclusive situacao cadastral da Receita nao aceita ou registro CVM inativo, nao persiste Corretora e retorna `422 Unprocessable Entity`.
- A consulta regulatoria usa exclusivamente o cadastro publico oficial de intermediarios da CVM. Apos normalizar o CNPJ, procura primeiro os 14 digitos exatos; somente se nao houver esse registro procura exatamente um intermediario/corretora suportado e ativo com a mesma raiz de oito digitos. Match de raiz ausente, inativo ou ambiguo e rejeitado. O CNPJ retornado pela CVM e a identidade regulatoria canonica persistida e usada na duplicidade, enquanto o endereco pode continuar vindo do estabelecimento informado. Ela mantem um snapshot em memoria de ultima leitura valida, com TTL de refresh de 6 horas, e carrega no cold start um baseline versionado derivado do formato oficial. Refresh remoto bem-sucedido substitui o snapshot; falha remota preserva o baseline ou a ultima leitura valida. Assim, indisponibilidade temporaria nao deve ser confundida com CNPJ ausente da CVM; somente sem qualquer snapshot uma falha tecnica de CVM retorna `502 Bad Gateway` sanitizado. CNPJ confirmado ausente ou ambiguo retorna `422` com a mensagem `Este CNPJ não está cadastrado na CVM.`
- Nao persistir payload bruto completo recebido de provider, nem retornar esse payload em contrato publico.
- Falhas de compliance devem gerar auditoria sanitizada.
- Corretora ativa pode ser usada em nova Transacao.
- Corretora inativa nao pode ser usada em nova Transacao, permanece existente e pode continuar referenciada por Transacoes historicas sem altera-las.
- O lifecycle deve ser administrado por ativacao e desativacao; `DELETE` fisico nao e fluxo normal.
- Criacao, edicao administrativa, ativacao e desativacao devem gerar auditoria sanitizada.

### Endpoints

```http
POST /api/v1/corretoras
GET  /api/v1/corretoras
GET  /api/v1/corretoras/{id}
GET  /api/v1/corretoras/cnpj/{cnpj}
PATCH /api/v1/corretoras/{id}
PATCH /api/v1/corretoras/{id}/ativo
```

Nao deve existir endpoint de vinculacao permanente entre corretora e acao.

Listagens devem possuir paginacao.

`ROLE_USER` pode somente consultar corretoras ativas. `ROLE_ADMIN` pode listar e consultar corretoras, inclusive conforme necessario para sua administracao.

### Respostas

`ROLE_USER` recebe somente os dados necessarios para identificar e selecionar Corretora ativa: no minimo `id`, `razaoSocial`, `nomeFantasia` quando existir e `cnpj`. Detalhes de endereco e compliance pertencem ao contrato administrativo quando necessarios e nao devem expor payload bruto de provider.

---

## 10. Ativos e Cotacoes

Ativos formam um catalogo global. `ROLE_ADMIN` preserva sua administracao e `ROLE_USER` pode somente registrar novo ativo financeiramente valido pelo fluxo controlado.

### Cadastro

```http
POST /api/v1/acoes
```

Acesso:
- `ROLE_ADMIN` cadastra com ticker, nome, tipo e mercado;
- `ROLE_USER` cadastra com ticker e mercado somente; nome, tipo, moeda e metadata sao derivados pelo servidor apos validacao financeira.

`ROLE_USER` nao pode editar, excluir, ativar, desativar ou administrar metadata de ativo. O cadastro de ativo nao cria Posicao, nao cria Transacao, nao executa BUY e nao altera caixa. Corretoras continuam administradas somente por `ROLE_ADMIN`.

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
- a capability de market quotes deve fornecer a cotacao real mais recente do ativo pelo provider configurado;
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

`exchange-rates`, ou capability equivalente, e pre-requisito da capability completa de transacoes. Para ativos `US_MARKET`, ela deve fornecer taxa real atual `USD/BRL`, instante da cotacao, fonte/provider, historico necessario para rastreabilidade, precisao financeira e tratamento de indisponibilidade.

### Provedores

```text
AlphaVantage
-> primario

TwelveData
-> fallback
```

### Regras

- a taxa deve possuir cache para reduzir chamadas externas;
- a taxa real usada em uma transacao US deve ser persistida na propria transacao como `taxaCambioBrl`, com `NUMERIC(18,8)`;
- a taxa usada em uma transacao nunca deve ser recalculada retroativamente;
- `taxaCambioBrl` nao e editavel pelo usuario e o backend nao deve aceitar taxa arbitraria enviada pelo browser como autoridade financeira; o vinculo seguro entre a taxa real e a transacao sera definido na capability de FX/transacoes.
- para ativos B3, `taxaCambioBrl = 1.00000000`, sem chamada a provider FX;
- se uma taxa USD/BRL valida nao puder ser fornecida, uma nova BUY ou SELL US nao pode ser confirmada. Nao usar conversao `1:1`, taxa manual, taxa antiga sem contrato, zero ou fallback inventado;
- patrimonio atual de ativos US deve utilizar a cotacao atual em USD multiplicada pela taxa USD/BRL atual apropriada, e nao a taxa historica da compra.

Uma alteracao posterior do dolar nao modifica uma `Transacao` historica. A estrategia detalhada de cache, stale e fallback permanece para futura change de `exchange-rates`.

---

## 12. APIs Externas

| Servico | Uso |
|---|---|
| BrasilAPI CNPJ | Dados cadastrais |
| Cadastro publico oficial de intermediarios da CVM | Validacao regulatoria |
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

### Dependencias de capabilities

```text
broker-catalog
-> exchange-rates
-> investment-transactions-and-positions
-> valuation/patrimonio
-> frontend financeiro
-> dashboard/E2E
```

`exchange-rates` deve estar concluida antes de habilitar BUY e SELL US. A capability de transacoes deve preservar o suporte B3/BRL e suportar US/USD; valuation/patrimonio compoe cotacao atual do ativo US com cambio atual, sem reutilizar cambio historico de compra.

---

## 13. Carteira e Caixa

Cada `ROLE_USER` deve possuir exatamente uma carteira principal criada no cadastro. `ROLE_ADMIN` nao possui carteira.

A moeda da carteira e sempre BRL.

`saldoCaixaBrl` permanece como caixa base: BUY B3 debita e SELL B3 credita valor em BRL; BUY US debita e SELL US credita o valor consolidado em BRL pela taxa real da operacao. Nao existe caixa USD nesta etapa.

Na etapa de identidade, a carteira se limita a sua criacao transacional com saldo inicial zero. Os endpoints e comportamentos financeiros desta secao pertencem a changes futuras.

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
- preco unitario confirmado na moeda do ativo;
- taxas consolidadas em BRL;
- data da negociacao;
- corretoraId.

A transacao exige `corretora_id`, recebido pelo identificador permitido no contrato. A corretora deve existir no catalogo global, estar valida e ativa no momento de nova transacao. `ROLE_USER` nao deve enviar nome livre ou corretora textual arbitraria.

BUY e SELL devem resolver localmente uma Corretora existente, ativa e previamente admitida pelo catalogo. A transacao financeira nao deve chamar Receita, CVM ou CEP para confirmar uma operacao. Revalidacao regulatoria periodica, se necessaria no futuro, pertence a evolucao propria do catalogo; o lifecycle local da Corretora controla sua selecao em novas Transacoes.

BUY e SELL aceitam ativo `B3` com `moeda = BRL` e ativo `US_MARKET` com `moeda = USD`. Ativo inexistente, fora do catalogo ou com moeda divergente e invalido: nao permitir ativo US com preco em BRL nem ativo B3 com preco em USD. Ativo US depende de taxa real USD/BRL valida da capability de exchange-rates; sem ela, a nova operacao US e bloqueada. Operacoes B3/BRL nao dependem de FX.

Ativo inativo nao pode receber nova compra nem aumento de posicao. Uma posicao existente em ativo inativo pode ser vendida para reduzir ou encerrar a custodia. A desativacao do ativo nao altera transacoes, posicoes ou historicos ja registrados.

Ao iniciar uma compra ou venda, o sistema deve obter a cotacao real mais recente pela capability de market quotes e apresenta-la como preco unitario inicial sugerido, na moeda original do ativo. Para ativo US, antes da confirmacao, tambem deve apresentar a taxa real atual USD/BRL e permitir visualizar quantidade, preco unitario confirmado em USD, valor da operacao em USD e valor convertido em BRL.

O usuario pode editar o preco unitario antes de confirmar a transacao. Essa edicao altera somente o preco unitario da transacao em preparacao e nao deve alterar a cotacao recebida do provider, `HistoricoCotacao` ou o historico de mercado.

O fluxo e:

```text
GET cotacao real
-> frontend apresenta preco unitario sugerido
-> usuario pode editar antes da confirmacao
-> POST registra preco unitario confirmado
```

O POST financeiro nao altera `HistoricoCotacao` nem dados recebidos do provider. Ao confirmar a transacao, o sistema deve registrar o preco unitario efetivamente confirmado pelo usuario. Esse preco e a autoridade para o valor da transacao, debito ou credito do caixa, atualizacao da posicao e, nas compras, preco medio ponderado; ele nao precisa ser igual a cotacao de mercado. Assim, cotacao real do provider, preco unitario confirmado e `taxaCambioBrl` real sao valores distintos: por exemplo, AAPL pode ter cotacao provider USD 220, preco confirmado USD 215 e cambio USD/BRL 5.40.

`Transacao` confirmada e fato financeiro imutavel. Preco editavel significa editavel somente antes da confirmacao; depois do POST concluido, o preco confirmado pertence ao historico da transacao. Nao devem existir `PUT`, `PATCH` ou `DELETE` para alterar historico confirmado.

Taxas sao obrigatorias, denominadas e consolidadas em BRL, devem ser maiores ou iguais a zero e devem utilizar `BigDecimal`. Para US, o valor bruto do ativo em USD e convertido separadamente para BRL e as taxas BRL sao incorporadas ao valor financeiro final conforme BUY ou SELL. Nao utilizar `double` ou `float`; taxas nao devem ser arredondadas prematuramente para duas casas antes dos calculos.

### Normalizacao para BRL

Para B3:

```text
taxaCambioBrl = 1.00000000
```

Para US:

```text
taxaCambioBrl = cotacao USD/BRL no momento da operacao
```

Para US, `valorOrigem` e o valor derivavel na moeda original: `quantidade * precoUnitarioConfirmado`, antes da conversao e conforme a semantica de taxas. O contrato deve permitir apresentar valor em USD e valor convertido em BRL, sem exigir campo persistente duplicado quando esses valores puderem ser derivados com seguranca. `precoUnitario` e editavel somente antes da confirmacao; `taxaCambioBrl` deve vir de exchange-rates, nao e editavel e deve ser real e rastreavel.

Para US:

```text
Valor Bruto USD = Quantidade * Preco Unitario Confirmado USD
Valor Bruto BRL = Valor Bruto USD * Taxa Cambio BRL
```

Aplicar `BigDecimal` e `RoundingMode.HALF_EVEN`; nao arredondar preco unitario ou taxa de cambio para duas casas antes da conversao. O valor consolidado final em BRL permanece `NUMERIC(18,2)`.

O sistema deve persistir na transacao:
- moeda original;
- preco unitario original;
- `taxas`: taxas consolidadas em BRL;
- taxa de cambio utilizada;
- valor total convertido para BRL;
- resultado realizado em BRL quando aplicavel.

### Compra

```text
Saldo em Caixa BRL >= Custo Total BRL
```

```text
Custo Origem =
Quantidade * Preco Unitario Confirmado

Custo BRL =
(Custo Origem * Taxa Cambio BRL) + Taxas BRL

Novo Custo Total BRL =
Custo Total BRL Anterior + Custo BRL

Novo Preco Medio BRL =
Novo Custo Total BRL / Nova Quantidade
```

O preco medio ponderado e obrigatorio. Em cada nova compra, o calculo deve considerar a quantidade atual, o preco medio atual, a quantidade comprada e o preco unitario efetivamente confirmado na nova compra, com as taxas e a conversao para BRL aplicaveis ao custo da operacao.

Exemplo conceitual, sem taxas e com valores em BRL:

```text
Compra 1: 10 unidades a BRL 20.00
Compra 2: 20 unidades a BRL 25.00

Custo acumulado: BRL 700.00
Quantidade: 30
Preco medio: BRL 23.33
```

O valor exibido do exemplo deve respeitar a politica de precisao e arredondamento definida neste PRD.

Exemplo conceitual US, sem taxas:

```text
Compra 1: 1 AAPL a USD 200.00, USD/BRL 5.00000000 -> BRL 1000.00
Compra 2: 1 AAPL a USD 200.00, USD/BRL 5.50000000 -> BRL 1100.00

Total investido BRL: 2100.00
Quantidade: 2
Preco medio BRL: 1050.00000000
```

Portanto, para ativo US, `precoMedioBrl` considera o custo convertido em BRL com a taxa efetivamente utilizada em cada compra.

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

Valor Bruto Origem =
Quantidade Vendida * Preco Unitario Confirmado

Valor Liquido BRL =
(Valor Bruto Origem * Taxa Cambio BRL) - Taxas BRL

Lucro/Prejuizo Realizado BRL =
Valor Liquido BRL - Custo Base BRL
```

A venda:
- credita o valor liquido em BRL no caixa;
- reduz a quantidade da posicao;
- nao altera o preco medio BRL das unidades restantes;
- registra resultado realizado em BRL;
- preserva o preco unitario de venda confirmado na transacao para futuros calculos de resultado realizado;
- gera auditoria;
- atualiza snapshot do dia.

Venda US registra preco de venda em USD, usa e preserva a taxa USD/BRL real da operacao, calcula e credita o valor liquido convertido em BRL. Venda parcial nao recalcula o preco medio; venda total aplica a regra de zeramento abaixo. `Transacao` permanece imutavel.

Se a posicao zerar:

```text
quantidade = 0
precoMedioBrl = 0
totalInvestidoBrl = 0
```

Todos os calculos financeiros devem utilizar `BigDecimal` e `RoundingMode.HALF_EVEN`.

Nao utilizar `float` ou `double` para valores financeiros.

`quantidade`, `preco_unitario` e `preco_medio_brl` devem preservar a precisao definida na secao 21. O arredondamento `HALF_EVEN` para `NUMERIC(18,2)` ocorre somente na materializacao monetaria final em BRL.

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

`total_investido_brl` pode ser calculado localmente a partir do custo da posicao. `valor_atual_posicoes_brl`, `lucro_nao_realizado_brl` e patrimonio baseado em mercado dependem de valuation com cotacao atual apropriada. Preco da ultima compra, preco medio ou preco confirmado em transacao nao podem ser tratados como cotacao atual de mercado.

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
- a introducao de Posicao deve evoluir a composicao do snapshot para que deposito ou saque posterior nao sobrescreva campos de investimento com zero;
- `total_investido_brl` pode ser atualizado pelo estado local de custo;
- valuation completo, patrimonio baseado em mercado e lucro nao realizado real pertencem a capability posterior de valuation;
- compra e venda nao devem chamar providers externos apenas para preencher snapshot.

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

Deve existir uma estrutura minima de `LogAuditoria` para eventos de seguranca.

Campos minimos:

```text
id UUID
usuario_id nullable
tipo_evento
resultado
severidade
endpoint
correlation_id
data_hora
```

`usuario_id` deve aceitar `NULL` para eventos sem usuario identificavel, como tentativa de login com e-mail inexistente. `endpoint` tambem deve aceitar `NULL` para eventos sem origem HTTP, como a criacao do administrador inicial.

`correlation_id` e obrigatorio em todo evento. Em requisicoes HTTP, usar `X-Correlation-ID` somente quando tiver UUID valido no formato canonico e comprimento limitado a esse formato; se estiver ausente ou invalido, gerar um UUID. Em eventos de sistema sem requisicao HTTP, gerar um UUID de correlacao.

Eventos minimos da etapa de identidade e seguranca:
- cadastro de usuario;
- login bem sucedido;
- login falho;
- tentativa de uso por usuario inativo;
- acesso negado;
- criacao do administrador inicial.

Eventos futuros do produto:
- compra;
- venda;
- deposito;
- saque;
- alteracao administrativa;
- falha CVM;
- falha de integracao relevante.

Criacao de Corretora, edicao administrativa de numero ou complemento, ativacao, desativacao e falhas de compliance devem gerar auditoria sanitizada.

Nunca registrar em logs da aplicacao ou auditoria persistida:
- senha;
- `senha_hash`;
- JWT;
- header `Authorization`;
- credenciais;
- corpo HTTP completo;
- payload bruto de provider;
- credenciais ou tokens de provider;
- saldo;
- posicoes;
- quantidades;
- valores de transacoes;
- demais dados financeiros privados.

### Endpoints do usuario

```http
GET /api/v1/auditoria/transacoes
GET /api/v1/auditoria/eventos
```

O usuario so pode visualizar seus proprios registros.

Esses endpoints de consulta nao fazem parte da etapa de identidade e seguranca atual.

### Auditoria administrativa

A auditoria global deve mostrar apenas metadados operacionais, de compliance e seguranca.

Nao deve revelar dados financeiros privados de outras carteiras.

O endpoint administrativo de consulta dos logs nao faz parte da etapa atual.

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

Para autenticacao e autorizacao, aplicar obrigatoriamente a semantica de `401 Unauthorized` e `403 Forbidden` definida na secao 7.8.

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
Usuario 1 -> 0..1 Carteira
Carteira -> Posicoes
Carteira -> Transacoes
Carteira -> MovimentacoesCaixa
Carteira -> CarteiraSnapshots
Transacao -> Acao
Transacao -> Corretora
HistoricoCotacao -> Acao
```

Regras de integridade:
- `ROLE_USER` deve possuir exatamente uma carteira principal;
- `ROLE_ADMIN` nao deve possuir carteira;
- o banco deve garantir que cada usuario possua no maximo uma carteira;
- a criacao do usuario `ROLE_USER` e de sua carteira deve ocorrer na mesma transacao;
- a role em `usuarios` deve aceitar somente `ROLE_USER` ou `ROLE_ADMIN` por constraint apropriada;
- o e-mail canonico deve possuir unicidade case-insensitive garantida no PostgreSQL.
- Corretora e catalogo global, sem `usuario_id` para ownership.
- Corretora deve possuir CNPJ canonico de 14 digitos, globalmente unico e imutavel, com `razaoSocial` oficial obrigatoria, endereco cadastral e lifecycle local.
- a futura referencia `transacoes.corretora_id` deve apontar para `corretoras.id` e preservar o historico financeiro; conceitualmente, utilizar `ON DELETE RESTRICT`, sem criar migration nesta etapa.

Nao deve existir:
- `corretora_id` em `acoes`;
- relacionamento fixo `Acao -> Corretora`.

IDs:
- UUID.

Cotacoes:

```sql
NUMERIC(15,4)
```

Precos unitarios, precos medios, cambio e taxas:

```sql
NUMERIC(18,8)
```

`quantidade`, `preco_unitario`, `preco_medio_brl` e `taxa_cambio_brl` utilizam `NUMERIC(18,8)`. `taxas` tambem utiliza `NUMERIC(18,8)`, deve ser maior ou igual a zero e nao deve sofrer arredondamento prematuro. A implementacao deve preservar `BigDecimal` e aplicar `RoundingMode.HALF_EVEN` somente na materializacao monetaria final em BRL.

Valores monetarios consolidados:

```sql
NUMERIC(18,2)
```

Quantidades:

```sql
NUMERIC(18,8)
```

---

## 22. Campos Minimos das Entidades

### Usuario

```text
id UUID
nome
email
senha_hash
role
ativo
criado_em
atualizado_em
```

### Carteira

```text
id
usuario_id
nome
saldo_caixa_brl
data_criacao
```

Regras:
- `usuario_id` deve ser unico;
- saldo inicial igual a zero;
- obrigatoria para `ROLE_USER`;
- proibida para `ROLE_ADMIN`.

### LogAuditoria

```text
id UUID
usuario_id nullable
tipo_evento
resultado
severidade
endpoint
correlation_id
data_hora
```

### Corretora

```text
id UUID
cnpj CHAR(14)
razao_social
nome_fantasia nullable
cep
logradouro
bairro
cidade
uf
numero nullable
complemento nullable
ativo
criado_em
atualizado_em
```

`cnpj` deve conter exatamente 14 digitos canonicos, ter unicidade global e nao ser alterado depois da criacao. `cep` deve conter 8 digitos canonicos; `uf` deve possuir 2 caracteres em maiusculo.

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
JWT_ISSUER=carteira-investimento-backend
JWT_AUDIENCE=carteira-investimento-api

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

`JWT_SECRET_KEY` deve fornecer um segredo UTF-8 com pelo menos 32 bytes para assinatura `HS256`. `JWT_EXPIRATION_HOURS` possui valor padrao atual de 24 horas. `JWT_ISSUER` e `JWT_AUDIENCE` nao sao segredos e devem ser fornecidos a `application.yml`, `.env.example` e `docker-compose.yml` quando esta change for implementada.

As configuracoes `ADMIN_*` sao obrigatorias e devem ser validadas na inicializacao. Ausencia, parcialidade ou invalidade deve causar falha explicita e documentada conforme a secao 8.

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
-> pg_isready healthy
-> Backend
-> Flyway
-> Hibernate validate
-> GET /actuator/health retorna healthy
-> Frontend
```

O backend deve incluir Spring Boot Actuator e expor:

```http
GET /actuator/health
```

O Docker Compose deve usar esse endpoint como healthcheck do backend. O frontend somente pode iniciar depois que o backend estiver saudavel; processo ou container apenas iniciado nao comprova prontidao. O healthcheck do PostgreSQL com `pg_isready` permanece obrigatorio.

O seeder de administrador pertence a change futura de identidade/autenticacao e nao faz parte da fundacao tecnica inicial.

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
- cadastro transacional de usuario e carteira com saldo zero;
- normalizacao e unicidade case-insensitive de e-mail no PostgreSQL;
- usuario inativo bloqueado no login;
- JWT anteriormente emitido bloqueado quando o usuario se torna inativo;
- JWT bloqueado quando `iss` ou `aud` nao conferem;
- JWT bloqueado quando a claim `role` diverge da role atualmente persistida;
- JWT invalido;
- JWT expirado;
- claims obrigatorias do JWT;
- `ROLE_USER` bloqueado em admin;
- acesso cruzado bloqueado.
- semantica de `401` e `403`;
- criacao idempotente do administrador sem carteira;
- conflito entre `ADMIN_EMAIL` e `ROLE_USER` falha explicitamente;
- bootstrap falha para `ADMIN_*` ausente, parcial ou invalido, sem criar administrador parcial;
- auditoria minima de eventos de seguranca sem dados proibidos, com `endpoint` nulo em eventos de sistema e correlation ID sempre presente.

### Corretoras
- CNPJ invalido;
- CNPJ duplicado;
- CNPJ formatado e canonico de 14 digitos;
- Receita inativa;
- CVM invalida;
- falha tecnica de Receita, CVM ou CEP sem cadastro parcial;
- cadastro valido.
- matriz CVM `02.332.886/0001-04` valida e estabelecimento `02.332.886/0016-82` resolve para a mesma identidade regulatoria canonica;
- matriz ja cadastrada mais tentativa pela filial retorna `409` com `Esta corretora já está cadastrada.`, e match de raiz ambiguo e rejeitado sem persistencia;
- razao social obrigatoria obtida da Receita e dados externos nao editaveis manualmente;
- CEP/endereco canonicos e numero/complemento opcionais normalizados;
- `ROLE_USER` consulta somente corretoras ativas e nao altera o catalogo;
- `ROLE_ADMIN` cadastra, edita, ativa e desativa corretoras;
- PATCH administrativo altera somente numero e complemento;
- corretora inativa bloqueada em nova transacao e preservada em transacoes historicas;
- exclusao fisica bloqueada quando houver referencia por historico financeiro.

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
- BUY B3 valida;
- SELL B3 valida;
- BUY US valida;
- SELL US valida;
- compra sem saldo;
- venda valida;
- venda acima da custodia;
- compra de ativo inativo bloqueada e venda de posicao existente em ativo inativo permitida;
- cotacao real mais recente B3 e US sugerida ao iniciar compra ou venda;
- taxa USD/BRL real, com instante e provider, disponivel para transacao US;
- exibicao de valor US em USD e valor convertido em BRL antes da confirmacao;
- edicao do preco unitario sem alterar cotacao do provider ou historico de mercado;
- taxa de cambio nao editavel e taxa arbitraria do browser nao aceita como autoridade financeira;
- registro e uso do preco unitario confirmado;
- taxa historica preservada na transacao US;
- preco medio ponderado em BRL em novas compras, inclusive compras US com taxas de cambio diferentes;
- venda sem recalculo do preco medio remanescente;
- resultado realizado;
- indisponibilidade de FX bloqueia somente nova BUY/SELL US; B3/BRL continua funcionando sem FX;
- taxas com escala, precisao e arredondamento financeiro definidos.

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
- [ ] cadastro de `ROLE_USER` cria usuario e exatamente uma carteira principal, com saldo zero, atomicamente;
- [ ] `ROLE_ADMIN` nao possui carteira;
- [ ] existem somente `ROLE_USER` e `ROLE_ADMIN`, sem tabela de roles;
- [ ] e-mail e persistido em forma canonica e possui unicidade case-insensitive no PostgreSQL;
- [ ] senha e armazenada exclusivamente com BCrypt e nunca e exposta;
- [ ] usuario consegue fazer login;
- [ ] cadastro retorna `201`, cria usuario ativo, carteira `Carteira Principal` com saldo zero e auditoria na mesma transacao, nao autentica automaticamente e retorna somente `id`, `nome`, `email`, `role` e `ativo`;
- [ ] login retorna `200` com `accessToken`, `tokenType=Bearer` e `expiresIn` em segundos, persiste a auditoria de sucesso antes do token e nao expoe senha, hash ou refresh token;
- [ ] `/api/v1/auth/me` usa somente o `SecurityContext`, nao aceita `usuarioId` do cliente e retorna apenas os campos permitidos;
- [ ] JWT `HS256` possui as claims obrigatorias, `iss`/`aud` validados e expiracao configuravel, com padrao de 24 horas;
- [ ] usuario inexistente ou inativo, ou JWT com role divergente da role persistida, recebe `401`; authorities usam a role atual do banco;
- [ ] respostas de autenticacao e autorizacao distinguem corretamente `401` e `403`;
- [ ] JWT protege recursos privados;
- [ ] usuarios nao acessam dados uns dos outros;
- [ ] administrador e criado de forma idempotente, sem carteira e sem sobrescrever senha ou role existente;
- [ ] conflito de `ADMIN_EMAIL` com `ROLE_USER` falha explicitamente;
- [ ] `ADMIN_*` ausente, parcial ou invalido faz o startup falhar antes de qualquer criacao parcial;
- [ ] eventos minimos de seguranca geram auditoria sanitizada e correlacionavel, inclusive eventos de sistema sem endpoint HTTP;
- [ ] apenas ADMIN altera catalogos globais;
- [ ] corretoras sao validadas na Receita e CVM;
- [ ] corretoras formam catalogo global, sem `usuario_id` para ownership;
- [ ] CNPJ obrigatorio e imutavel e persistido com exatamente 14 digitos canonicos e unicidade global;
- [ ] razaoSocial obrigatoria e nomeFantasia opcional sao obtidos da Receita sem substituicao manual;
- [ ] cep, logradouro, bairro, cidade e uf sao persistidos pelo fluxo Receita/CEP sem payload bruto de provider;
- [ ] somente numero e complemento podem ser editados manualmente pelo ADMIN;
- [ ] `ROLE_USER` consulta somente corretoras ativas e seleciona corretora ativa pelo identificador permitido em nova transacao, sem nome livre;
- [ ] `ROLE_ADMIN` lista, consulta, cadastra, edita, ativa e desativa corretoras;
- [ ] corretora inativa nao pode ser usada em nova transacao, permanece em transacoes historicas e nao e excluida fisicamente como fluxo normal;
- [ ] futura FK `transacoes.corretora_id -> corretoras.id` preserva historico financeiro, conceitualmente com `ON DELETE RESTRICT`;
- [ ] nao existe relacionamento fixo entre acao e corretora;
- [ ] ativos B3 e US podem ser cadastrados pelo ADMIN;
- [ ] usuarios autenticados podem consultar ativos e corretoras;
- [ ] Brapi fornece cotacoes B3;
- [ ] AlphaVantage e primario para US;
- [ ] TwelveData funciona como fallback;
- [ ] cotacoes usam cache;
- [ ] cache nao duplica historico de cotacoes;
- [ ] carteira utiliza BRL como moeda base;
- [ ] exchange-rates fornece USD/BRL real, com instante, provider, rastreabilidade, precisao financeira e tratamento de indisponibilidade;
- [ ] operacoes US persistem a taxa USD/BRL real utilizada, imutavel historicamente, em `NUMERIC(18,8)`;
- [ ] patrimonio US e convertido para BRL;
- [ ] usuario consegue depositar e sacar;
- [ ] depositos e saques possuem historico proprio;
- [ ] usuario consegue comprar com saldo suficiente;
- [ ] venda a descoberto e bloqueada;
- [ ] compra de ativo inativo e bloqueada, enquanto venda de posicao existente em ativo inativo permanece permitida;
- [ ] BUY e SELL negociam B3/BRL e US/USD, com moeda coerente ao ativo e sem conversao USD/BRL inventada;
- [ ] cotacao real de ativo US e USD/BRL real sao exibidas antes da confirmacao;
- [ ] operacao US apresenta quantidade, preco confirmado e valor em USD, mais valor convertido em BRL;
- [ ] BUY e SELL usam corretora previamente admitida e ativa sem chamar Receita, CVM ou CEP dentro da transacao financeira;
- [ ] cotacao real mais recente do market quotes e sugerida ao iniciar compra ou venda;
- [ ] usuario pode editar o preco unitario antes da confirmacao sem alterar a cotacao do provider ou o historico de mercado;
- [ ] para US, preco unitario permanece em USD e taxaCambioBrl nao e editavel nem aceita do browser como autoridade financeira;
- [ ] transacao registra e utiliza o preco unitario efetivamente confirmado pelo usuario;
- [ ] preco medio ponderado em BRL e recalculado corretamente em novas compras, inclusive US com taxas de cambio diferentes;
- [ ] venda reduz a quantidade sem recalcular o preco medio das unidades remanescentes;
- [ ] SELL US preserva taxa real historica, credita caixa BRL e aplica a regra de zeramento da posicao quando total;
- [ ] indisponibilidade de FX bloqueia somente nova BUY/SELL US; B3/BRL funciona sem FX;
- [ ] HistoricoCotacao e dados do provider nao sao alterados pelo preco manual da transacao;
- [ ] taxas em BRL sao incorporadas corretamente ao valor financeiro final de BUY/SELL B3 e US;
- [ ] lucro/prejuizo realizado em BRL e calculado corretamente;
- [ ] snapshots diarios suportam evolucao patrimonial;
- [ ] deposito ou saque posterior a uma posicao nao sobrescreve campos de investimento do snapshot com zero;
- [ ] dashboard apresenta indicadores consolidados;
- [ ] operacoes importantes geram auditoria;
- [ ] auditoria do ADMIN nao expoe dados financeiros privados;
- [ ] painel administrativo e exclusivo de `ROLE_ADMIN`;
- [ ] banco evolui exclusivamente via Flyway;
- [ ] Hibernate utiliza `ddl-auto: validate`;
- [ ] aplicacao sobe com `docker compose up --build`;
- [ ] PostgreSQL e backend possuem healthchecks reais e o frontend aguarda o backend saudavel;
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
10. Nao permitir `ROLE_USER` administrar catalogos globais. Como excecao controlada, todo `ROLE_USER` pode adicionar ao catalogo global um ativo valido exclusivamente por `ticker` e `mercado`, apos validacao/canonicalizacao server-side pelo provider; essa acao nao cria posicao, transacao ou movimentacao de caixa e nao permite editar, ativar, desativar ou excluir ativos.
11. Nao permitir `ROLE_USER` acessar rotas administrativas.
12. Nao criar relacionamento fixo entre `Acao` e `Corretora`.
13. Nao permitir `ROLE_USER` enviar corretora textual livre em Transacao.
14. Nao excluir fisicamente Corretora como fluxo normal; preservar referencias do historico financeiro.
15. Nao recalcular retroativamente taxa de cambio persistida em transacoes.
16. Nao criar historico de cotacao quando a resposta vier do cache.
17. Manter integracoes externas isoladas por Adapter, Strategy e Factory.
18. Manter somente uma change principal ativa por vez, salvo dependencia explicitamente documentada.
19. Changes concluidas devem ser testadas, revisadas e arquivadas antes da proxima etapa principal.
