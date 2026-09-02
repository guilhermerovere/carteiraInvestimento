## MODIFIED Requirements

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
