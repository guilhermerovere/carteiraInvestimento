# Backend Foundation Specification

## Purpose

Estabelece um backend executável, testável e compatível com a stack obrigatória do produto, sem antecipar entidades, segurança de negócio ou regras financeiras.

## Requirements

### Requirement: Estrutura e toolchain do backend
O repositório SHALL disponibilizar o backend em `backend/`, construído com Maven, Java 21 e Spring Boot 4.1.1. O build SHALL incluir os habilitadores técnicos previstos no PRD para Web, validação, JPA, Spring Security compatível, Actuator, OpenFeign, cache Caffeine, Flyway, PostgreSQL, OpenAPI, logging e testes, sem declarar H2 em qualquer escopo. Dependências cobertas pelo dependency management do Spring Boot ou pelo BOM compatível do Spring Cloud MUST usar as versões gerenciadas, salvo override tecnicamente necessário e documentado.

#### Scenario: Build reproduzível do backend
- **WHEN** um desenvolvedor executa o Maven Wrapper do diretório `backend/` com Java 21
- **THEN** o projeto compila com Spring Boot 4.1.1 e resolve uma matriz compatível de Spring Cloud/OpenFeign, Springdoc, Spring Security, Testcontainers e demais dependências Spring

#### Scenario: Gerenciamento de versões Spring
- **WHEN** o modelo efetivo de dependências Maven é inspecionado
- **THEN** dependências gerenciadas pelos BOMs não possuem versões manuais redundantes e qualquer override necessário está acompanhado de justificativa técnica

#### Scenario: Ausência de H2
- **WHEN** as dependências Maven do backend são inspecionadas
- **THEN** H2 não está presente em dependências de produção nem de teste

### Requirement: Configuração externa da aplicação
O backend MUST centralizar sua configuração em `application.yml` e SHALL obter conexão PostgreSQL, credenciais, porta e demais valores configuráveis por variáveis de ambiente, com valores locais não sensíveis apenas quando apropriado ao desenvolvimento.

#### Scenario: Configuração por ambiente
- **WHEN** o backend é iniciado com as variáveis de ambiente documentadas
- **THEN** ele utiliza a URL, o usuário, a senha e a porta fornecidos sem exigir edição do artefato empacotado

#### Scenario: Variável obrigatória ausente
- **WHEN** uma configuração obrigatória para conectar ao banco não está disponível
- **THEN** a inicialização falha de forma explícita em vez de recorrer a um banco em memória

### Requirement: Persistência governada por Flyway
O backend SHALL usar PostgreSQL como único banco relacional, SHALL habilitar Flyway com validação durante a inicialização e MUST configurar Hibernate com `ddl-auto: validate`. Esta fundação MUST NOT criar o schema das entidades do domínio financeiro ou de identidade.

#### Scenario: Banco PostgreSQL vazio
- **WHEN** o backend inicia conectado a uma instância PostgreSQL 16 limpa
- **THEN** o Flyway inicializa e valida seu estado com sucesso e o Hibernate não cria nem altera tabelas

#### Scenario: Estado Flyway inválido
- **WHEN** o banco contém um histórico de migrations incompatível com os artefatos versionados
- **THEN** a inicialização do backend falha durante a validação do Flyway

#### Scenario: Inspeção das migrations da fundação
- **WHEN** os scripts Flyway desta change são inspecionados
- **THEN** eles não contêm DDL para Usuario, Carteira, Corretora, Acao, Posicao, Transacao, MovimentacaoCaixa, CarteiraSnapshot, LogAuditoria ou outras estruturas completas do domínio

### Requirement: Base arquitetural sem domínio prematuro
O backend SHALL possuir áreas base `domain`, `infrastructure` e `presentation` coerentes com o PRD, mas MUST NOT conter entidades JPA de domínio, regras financeiras, autenticação, JWT, RBAC definitivo, clientes reais de APIs externas ou endpoints funcionais de negócio nesta change.

#### Scenario: Inspeção da estrutura de código
- **WHEN** a árvore de fontes do backend é inspecionada
- **THEN** as três áreas arquiteturais existem e não incluem implementações das funcionalidades explicitamente excluídas

### Requirement: Seguranca temporariamente permissiva
O backend SHALL substituir a configuracao temporariamente permissiva por autenticacao JWT Bearer e autorizacao por role. Cadastro, login, healthcheck, OpenAPI e Swagger SHALL permanecer publicos; toda rota nao explicitamente publica SHALL exigir autenticacao e MUST NOT provocar a criacao de usuario ou senha padrao do framework.

#### Scenario: Acesso tecnico apos identidade
- **WHEN** um cliente sem credenciais acessa Swagger, OpenAPI ou o healthcheck
- **THEN** a configuracao permite o acesso sem criar usuario ou senha padrao

#### Scenario: Acesso anonimo fora da lista publica
- **WHEN** um cliente sem credenciais acessa uma rota que nao esta explicitamente publica
- **THEN** o backend rejeita a requisicao com `401 Unauthorized`

#### Scenario: Inspecao da configuracao definitiva
- **WHEN** a configuracao de seguranca e revisada
- **THEN** ela aplica autenticacao JWT Bearer, validacao do usuario e da role atuais e regras de autorizacao sem preservar liberacao global de requisicoes

#### Scenario: Fronteira de camadas da identidade
- **WHEN** os fluxos de cadastro, login, principal atual e bootstrap sao revisados
- **THEN** a orquestracao transacional ocorre na camada application, que usa ports implementados pela infrastructure, enquanto presentation somente traduz contratos HTTP

### Requirement: Documentacao HTTP e erros padronizados
O backend SHALL expor a interface Swagger em `/swagger-ui.html` e a descricao OpenAPI correspondente, ambas publicamente acessiveis e documentadas com o esquema Bearer JWT para rotas protegidas. Erros processados pela camada HTTP, inclusive falhas de autenticacao e autorizacao, SHALL usar `ProblemDetail`, preservar codigos HTTP adequados e MUST NOT incluir segredos ou detalhes internos sensiveis.

#### Scenario: Consulta do Swagger
- **WHEN** o backend esta em execucao e um cliente sem token acessa `/swagger-ui.html`
- **THEN** a interface OpenAPI e carregada e apresenta o esquema Bearer aplicavel as rotas protegidas

#### Scenario: Erro tratado pela aplicacao
- **WHEN** a camada HTTP processa uma falha coberta pelo tratamento global
- **THEN** a resposta possui media `application/problem+json`, status coerente e um corpo `ProblemDetail` sem dados sensiveis

#### Scenario: Falha de autenticacao
- **WHEN** a infraestrutura de seguranca rejeita uma requisicao por falta ou invalidade de autenticacao
- **THEN** o `AuthenticationEntryPoint` produz `401 Unauthorized` no mesmo formato `ProblemDetail` sanitizado

#### Scenario: Falha de autorizacao
- **WHEN** a infraestrutura de seguranca rejeita um principal autenticado por falta de permissao
- **THEN** o `AccessDeniedHandler` produz `403 Forbidden` no mesmo formato `ProblemDetail` sanitizado

### Requirement: Healthcheck técnico do backend
O backend SHALL incluir Spring Boot Actuator e SHALL expor `GET /actuator/health` no servidor HTTP principal como prova de prontidão real da aplicação. O endpoint MUST estar acessível à infraestrutura durante a configuração de segurança temporária e MUST NOT expor detalhes sensíveis.

#### Scenario: Backend pronto
- **WHEN** o contexto Spring terminou de iniciar e as validações de Flyway, Hibernate e conexão PostgreSQL foram concluídas com sucesso
- **THEN** `GET /actuator/health` responde com sucesso e estado `UP`

#### Scenario: Backend ainda não pronto
- **WHEN** a aplicação não concluiu sua inicialização ou falhou na validação do banco
- **THEN** o healthcheck não informa o backend como saudável

### Requirement: Testes de integração com PostgreSQL real
A suíte de integração SHALL iniciar PostgreSQL por Testcontainers e SHALL verificar, no mínimo, a carga do contexto da aplicação, a conexão com PostgreSQL e a validação do Flyway. Testes que dependem de comportamento PostgreSQL MUST NOT substituir o banco por H2.

#### Scenario: Execução da suíte de integração
- **WHEN** os testes de integração são executados em um ambiente com Docker disponível
- **THEN** um container PostgreSQL é iniciado e as verificações de contexto, conexão e Flyway são aprovadas
