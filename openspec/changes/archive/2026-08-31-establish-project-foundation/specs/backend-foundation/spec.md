## Purpose

Estabelece um backend executável, testável e compatível com a stack obrigatória do produto, sem antecipar entidades, segurança de negócio ou regras financeiras.

## ADDED Requirements

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

### Requirement: Segurança temporariamente permissiva
O backend SHALL incluir Spring Security na versão compatível gerenciada pelo Spring Boot 4.1.1, com uma configuração de fundação explicitamente marcada como temporária e permissiva. Essa configuração MUST NOT criar usuários, login, JWT, autenticação ou RBAC definitivo e SHALL ser substituída pela futura change de identidade/autenticação.

#### Scenario: Acesso técnico durante a fundação
- **WHEN** um cliente acessa Swagger ou o healthcheck antes da implementação de identidade
- **THEN** a configuração temporária permite o acesso sem gerar usuário, senha padrão ou fluxo de login

#### Scenario: Inspeção do escopo de segurança
- **WHEN** a configuração de segurança desta change é revisada
- **THEN** ela está identificada como temporária e não contém autenticação, JWT, usuários ou regras definitivas de autorização

### Requirement: Documentação HTTP e erros padronizados
O backend SHALL expor a interface Swagger em `/swagger-ui.html` e a descrição OpenAPI correspondente. Erros processados pela camada HTTP SHALL usar `ProblemDetail` e preservar códigos HTTP adequados, sem incluir segredos ou detalhes internos sensíveis.

#### Scenario: Consulta do Swagger
- **WHEN** o backend está em execução e um cliente acessa `/swagger-ui.html`
- **THEN** a interface OpenAPI é carregada sem exigir uma funcionalidade de negócio implementada

#### Scenario: Erro tratado pela aplicação
- **WHEN** a camada HTTP processa uma falha coberta pelo tratamento global
- **THEN** a resposta possui mídia `application/problem+json`, status coerente e um corpo `ProblemDetail` sem dados sensíveis

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
