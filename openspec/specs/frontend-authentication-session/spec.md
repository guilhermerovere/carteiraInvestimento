# Frontend Authentication Session Specification

## Purpose

Estabelece uma sessão frontend segura sobre os contratos existentes, mantendo JWT fora do JavaScript do navegador e preservando autenticação e autorização do backend.

## Requirements

### Requirement: Fronteira same-origin de autenticação
O frontend SHALL expor endpoints same-origin para login, cadastro, usuário atual e logout. O browser MUST comunicar-se apenas com eles e MUST NOT receber `accessToken`, `Authorization` ou credenciais do backend. Login e cadastro SHALL chamar o Spring server-side sem Bearer de sessão; login retorna o JWT somente ao BFF. A consulta ao usuário atual SHALL chamar o Spring server-side com `Authorization: Bearer <JWT>` obtido de `auth_session`. O token MUST ser usado somente server-side em chamadas protegidas que realmente exijam Bearer; o frontend não SHALL adicionar CORS ao Spring.

#### Scenario: Login intermediado
- **WHEN** o browser envia credenciais válidas ao login same-origin
- **THEN** o frontend chama o login Spring sem Bearer, grava a sessão e responde sem token

#### Scenario: Cadastro intermediado
- **WHEN** o browser envia cadastro válido ao endpoint same-origin
- **THEN** o frontend chama o cadastro Spring sem Bearer e preserva 201 sem criar sessão

### Requirement: Cookie de sessão confidencial
Após login bem-sucedido, o frontend SHALL gravar JWT somente em `auth_session`. O cookie MUST ser HttpOnly, SameSite=Lax, Path=/, host-only sem Domain e expirar a partir de `expiresIn`. `Secure` MUST ser true quando `APP_ORIGIN` usa HTTPS e false quando usa HTTP; produção real MUST configurar APP_ORIGIN com HTTPS. Criação e exclusão MUST compartilhar a mesma configuração-base. JWT MUST NOT estar em armazenamento browser, estado React, TanStack Query ou corpo de resposta.

#### Scenario: Origem HTTPS
- **WHEN** APP_ORIGIN usa HTTPS
- **THEN** auth_session é gravado com Secure=true e os demais atributos obrigatórios

#### Scenario: Origem HTTP local
- **WHEN** APP_ORIGIN usa HTTP, inclusive build Next de produção em http://localhost
- **THEN** auth_session é gravado com Secure=false e os demais atributos obrigatórios

### Requirement: Usuário atual como autoridade de sessão
O endpoint same-origin de usuário atual SHALL ler auth_session server-side, reutilizar um resolvedor server-only compartilhado e retornar somente dados seguros. Guards e layouts server-side SHALL chamar diretamente esse resolvedor, sem HTTP contra o próprio Route Handler. A sessão só é válida após confirmação pelo Spring `/api/v1/auth/me`; o frontend MUST NOT usar decode local de JWT como autoridade de identidade, role ou ativo.

#### Scenario: Sessão confirmada
- **WHEN** o Spring confirma o principal atual
- **THEN** o frontend expõe somente dados seguros atuais

### Requirement: Proteção de rotas e autorização por role
`/` SHALL resolver a sessão server-side pelo resolvedor de usuário atual: sem sessão confirmada SHALL redirecionar para `/login`, ROLE_USER confirmado SHALL redirecionar para `/carteira` e ROLE_ADMIN confirmado SHALL redirecionar para `/admin`. `/carteira` e suas subrotas SHALL exigir sessão confirmada e ROLE_USER. `/inicio` SHALL exigir sessão confirmada e ROLE_USER e, somente após o guard server-side confirmar o usuário, SHALL redirecionar para `/carteira` como compatibilidade. `/admin` SHALL exigir sessão confirmada e ROLE_ADMIN. Após login sem `returnTo` interno permitido para a role confirmada, ROLE_USER SHALL seguir para `/carteira` e ROLE_ADMIN SHALL seguir para `/admin`; um `returnTo` seguro permitido para a role SHALL ser preservado, enquanto destino inseguro ou incompatível SHALL usar a landing natural da role. `proxy.ts` SHALL ser somente pre-check de cookie ausente e poderá conhecer apenas nome do cookie, caminhos protegidos e sanitização de returnTo; MUST NOT chamar Spring, importar cliente backend/current user/Authorization/ProblemDetail, ler ou decodificar JWT, alterar sessão ou decidir autorização final. A role SHALL ser decidida pelo usuário atual confirmado, nunca por JWT lido no browser. Role insuficiente SHALL resultar em acesso negado/403 e preservar sessão.

#### Scenario: Raiz sem sessão confirmada
- **WHEN** uma pessoa sem sessão confirmada acessa `/`
- **THEN** a aplicação redireciona server-side para `/login`

#### Scenario: Raiz por role confirmada
- **WHEN** ROLE_USER ou ROLE_ADMIN confirmado acessa `/`
- **THEN** a aplicação redireciona server-side respectivamente para `/carteira` ou `/admin`

#### Scenario: Login sem returnTo
- **WHEN** o login confirma ROLE_USER ou ROLE_ADMIN e não existe `returnTo` permitido
- **THEN** a aplicação navega respectivamente para `/carteira` ou `/admin`

#### Scenario: Sessão ausente
- **WHEN** uma rota protegida é acessada sem cookie
- **THEN** proxy redireciona para login com returnTo interno seguro

#### Scenario: Compatibilidade de início confirmada
- **WHEN** ROLE_USER confirmado server-side acessa `/inicio`
- **THEN** a aplicação redireciona para `/carteira` sem tratar a presença isolada do cookie como prova de role

#### Scenario: Admin em carteira
- **WHEN** ROLE_ADMIN confirmado acessa `/carteira`
- **THEN** recebe acesso negado sem logout

#### Scenario: User em admin
- **WHEN** ROLE_USER confirmado acessa `/admin`
- **THEN** recebe acesso negado sem logout

#### Scenario: Role insuficiente
- **WHEN** usuário confirmado acessa rota cuja role não é permitida
- **THEN** recebe acesso negado sem logout

#### Scenario: ReturnTo malicioso
- **WHEN** returnTo é URL absoluta, protocol-relative, javascript: ou outro destino não sanitizado
- **THEN** o sistema usa somente o fallback interno seguro existente

### Requirement: Retorno seguro após autenticação
O frontend SHALL aceitar returnTo apenas como caminho interno absoluto e MUST rejeitar URL absoluta, `//host`, esquema externo e qualquer open redirect.

#### Scenario: Destino externo rejeitado
- **WHEN** returnTo é externo ou malicioso
- **THEN** o frontend usa destino interno seguro padrão

### Requirement: Semântica de 401 e 403
401 SHALL invalidar sessão; 403 SHALL preservar auth_session e usuário atual. Guard server-side que recebe 401 pelo resolvedor MUST redirecionar a `/login` sem depender de apagar cookie. Route Handler de me que recebe 401 MUST remover o cookie por Set-Cookie e retornar 401 seguro. `/login` MUST ser pública mesmo com cookie inválido. Respostas de autenticação relevantes SHALL usar no-store.

#### Scenario: 401 no guard
- **WHEN** cookie existe e o resolvedor retorna 401 no guard
- **THEN** a navegação é redirecionada ao login sem loop

#### Scenario: 403 autenticado
- **WHEN** consulta protegida recebe 403
- **THEN** sessão e dados atuais são preservados e acesso negado é apresentado

### Requirement: Operações mutáveis protegidas por origem
POST login, register e logout SHALL exigir Origin presente, bem-formado e igual a APP_ORIGIN normalizada. A validação MUST NOT usar Host como fallback de confiança.

#### Scenario: Origem válida
- **WHEN** Origin é igual a APP_ORIGIN normalizada
- **THEN** a operação pode prosseguir

#### Scenario: Origem rejeitada
- **WHEN** Origin está ausente, malformado ou divergente
- **THEN** a operação é rejeitada sem alterar sessão

### Requirement: Logout local seguro
Logout same-origin SHALL validar Origin, remover auth_session com atributos coerentes e responder 204. Após logout, o cliente MUST limpar estado e navegar para login. MUST NOT criar logout Spring, blacklist, refresh token ou revogação server-side.

#### Scenario: Logout válido
- **WHEN** logout recebe Origin válido
- **THEN** remove o cookie e retorna 204

### Requirement: Erros seguros e rastreáveis
O frontend SHALL distinguir erro HTTP, ProblemDetail e infraestrutura, preservando status, title, detail, instance e X-Correlation-ID quando disponíveis, sem vazar JWT, Authorization ou detalhes internos.

#### Scenario: Falha de rede
- **WHEN** o Spring não é alcançável
- **THEN** o browser recebe erro seguro de infraestrutura distinto de HTTP

### Requirement: Estado de autenticação no cliente
O cliente SHALL tratar usuário atual como `['auth', 'me']` e usar mutations sem retry automático. A query MUST NOT repetir inutilmente 401/403. Após login 204, SHALL carregar ou invalidar me e só considerar sessão autenticada após confirmação. Após cadastro 201, MUST NOT criar cookie, sessão, usuário autenticado ou invalidar me; SHALL navegar para login. Logout ou 401 SHALL limpar estado. 403 MUST preservar cookie e me.

#### Scenario: Login confirmado
- **WHEN** login retorna 204
- **THEN** o cliente confirma me antes de tratar sessão como autenticada

#### Scenario: Cadastro sem sessão
- **WHEN** cadastro retorna 201
- **THEN** não altera me nem estabelece identidade e navega para login
