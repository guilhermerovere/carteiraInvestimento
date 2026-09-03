## Purpose

Define o contrato stateless de login e autenticacao JWT Bearer, a obtencao segura do principal atual e a separacao observavel entre falhas de autenticacao e de autorizacao.

## ADDED Requirements

### Requirement: Login por credenciais
`POST /api/v1/auth/login` SHALL autenticar e-mail canonico e senha, responder `200 OK` em caso de sucesso com exatamente `accessToken`, `tokenType` igual a `Bearer` e `expiresIn`. `expiresIn` SHALL representar a duracao restante do access token em segundos. O evento de login bem sucedido MUST ser persistido antes de o login ser considerado concluido e antes de retornar o token. A resposta MUST NOT conter senha, `senha_hash` ou refresh token.

#### Scenario: Login bem sucedido
- **WHEN** um usuario ativo fornece e-mail e senha validos
- **THEN** o sistema persiste primeiro a auditoria de sucesso e responde `200 OK` com `accessToken`, `tokenType` `Bearer` e `expiresIn` em segundos, sem senha, hash ou refresh token

#### Scenario: Credenciais invalidas
- **WHEN** o e-mail nao identifica um usuario ou a senha nao confere
- **THEN** o sistema responde `401 Unauthorized` sem revelar qual credencial falhou

#### Scenario: Login de usuario inativo
- **WHEN** um usuario persistido e inativo fornece credenciais corretas
- **THEN** o sistema responde `401 Unauthorized` e nao emite token

### Requirement: Access token JWT verificavel
O access token SHALL ser assinado com HS256 usando segredo UTF-8 fornecido por `JWT_SECRET_KEY` com no minimo 32 bytes. Sua duracao SHALL vir de `JWT_EXPIRATION_HOURS`, com padrao atual de 24 horas, e SHALL conter `sub` com o UUID do usuario, `role`, `iat`, `exp`, `jti`, `iss` e `aud`. `iss` SHALL corresponder a `JWT_ISSUER=carteira-investimento-backend` e `aud` a `JWT_AUDIENCE=carteira-investimento-api`; ambas sao configuracoes nao secretas e SHALL ser validadas na autenticacao.

#### Scenario: Conteudo do token emitido
- **WHEN** o login e concluido com sucesso
- **THEN** o token assinado com HS256 contem todas as claims obrigatorias e uma `exp` coerente com a configuracao vigente

#### Scenario: Segredo insuficiente
- **WHEN** `JWT_SECRET_KEY` representa menos de 256 bits
- **THEN** a aplicacao falha explicitamente na inicializacao e nao opera com assinatura enfraquecida

#### Scenario: Expiracao nao configurada
- **WHEN** `JWT_EXPIRATION_HOURS` nao e fornecida
- **THEN** novos tokens recebem duracao de 24 horas

#### Scenario: Expiracao invalida
- **WHEN** `JWT_EXPIRATION_HOURS` nao representa uma duracao positiva valida
- **THEN** a aplicacao falha explicitamente na inicializacao

#### Scenario: Issuer ou audience invalidos
- **WHEN** uma rota protegida recebe token com `iss` ou `aud` diferente da configuracao atual
- **THEN** o sistema rejeita a autenticacao com `401 Unauthorized`

### Requirement: Usuario autenticado atual
`GET /api/v1/auth/me` SHALL obter a identidade exclusivamente do `SecurityContext`, MUST NOT aceitar `usuarioId` como fonte de identidade e SHALL retornar somente `id`, `nome`, `email`, `role` e `ativo`.

#### Scenario: Consulta autenticada do proprio usuario
- **WHEN** um usuario autenticado chama `/api/v1/auth/me`
- **THEN** o sistema retorna somente os cinco campos permitidos do usuario representado pelo principal autenticado

#### Scenario: Identificador fornecido pelo cliente
- **WHEN** o cliente inclui um `usuarioId` diferente em parametro, cabecalho ou corpo ao chamar `/api/v1/auth/me`
- **THEN** esse valor nao e usado para selecionar ou autorizar a identidade retornada

### Requirement: Estado atual validado em toda requisicao protegida
Para cada requisicao protegida, o sistema MUST validar criptograficamente o JWT, inclusive `iss` e `aud`, e consultar o usuario indicado por `sub` no banco antes de estabelecer o principal. Um token valido para usuario inexistente, atualmente inativo ou cuja claim `role` divirja da role persistida atual MUST resultar em `401 Unauthorized`. As authorities MUST ser construidas somente da role persistida atual.

#### Scenario: Usuario desativado depois da emissao
- **WHEN** um JWT criptograficamente valido pertence a um usuario que se tornou inativo depois de sua emissao
- **THEN** a requisicao protegida responde `401 Unauthorized`

#### Scenario: Usuario removido depois da emissao
- **WHEN** um JWT criptograficamente valido referencia um UUID sem usuario persistido atual
- **THEN** a requisicao protegida responde `401 Unauthorized`

#### Scenario: Usuario ativo e token valido
- **WHEN** o token e valido e o usuario persistido existe, esta ativo e possui a role declarada
- **THEN** o sistema estabelece o principal autenticado no `SecurityContext` com authorities construidas da role persistida

#### Scenario: Role alterada depois da emissao
- **WHEN** um JWT criptograficamente valido possui claim `role` diferente da role atualmente persistida para seu `sub`
- **THEN** a requisicao protegida responde `401 Unauthorized` e nao estabelece principal autenticado

### Requirement: Fronteira de rotas publicas e privadas
O sistema SHALL manter publicos pelo menos cadastro, login, healthcheck, OpenAPI e Swagger. Todas as demais rotas SHALL exigir autenticacao, alem das restricoes de role especificadas por suas capabilities.

#### Scenario: Acesso a rota publica
- **WHEN** um cliente sem token acessa cadastro, login, healthcheck, OpenAPI ou Swagger
- **THEN** a seguranca nao exige autenticacao para essa rota

#### Scenario: Acesso anonimo a rota privada
- **WHEN** um cliente sem token acessa uma rota que nao esta explicitamente liberada
- **THEN** o sistema responde `401 Unauthorized`

#### Scenario: Role insuficiente
- **WHEN** um usuario autenticado acessa uma rota para a qual sua role nao possui permissao
- **THEN** o sistema responde `403 Forbidden`

### Requirement: Semantica uniforme de erros de seguranca
O sistema MUST responder `401 Unauthorized` para token ausente quando obrigatorio, invalido, expirado, com `iss`/`aud` invalidos, para usuario inexistente ou inativo ou para claim `role` divergente da role persistida. O sistema MUST responder `403 Forbidden` somente para principal autenticado validamente sem role ou permissao necessaria. Ambas as respostas SHALL usar `application/problem+json`, ser coerentes com o contrato `ProblemDetail` global e nao expor detalhes internos ou credenciais.

#### Scenario: Token invalido ou expirado
- **WHEN** uma rota protegida recebe token malformado, com assinatura invalida ou expirado
- **THEN** o sistema responde `401 Unauthorized` com `ProblemDetail` sanitizado

#### Scenario: Acesso negado apos autenticacao
- **WHEN** um principal valido nao possui autorizacao para a operacao
- **THEN** o sistema responde `403 Forbidden` com `ProblemDetail` sanitizado

### Requirement: Autenticacao stateless limitada ao access token
Esta capability MUST NOT emitir refresh token, manter blacklist de tokens ou oferecer logout server-side. Depois da expiracao, o usuario SHALL realizar novo login para obter outro access token.

#### Scenario: Token expirado
- **WHEN** o access token expira
- **THEN** o sistema nao o renova automaticamente e exige novo login
