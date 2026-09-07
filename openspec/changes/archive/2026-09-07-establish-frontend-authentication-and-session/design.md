## Context

O backend mantém o contrato Bearer stateless: login retorna `accessToken`, `tokenType` e `expiresIn`; cadastro não autentica; e `/api/v1/auth/me` resolve usuário, role e ativo atuais. O frontend Next.js App Router possui apenas layout, página de fundação e `QueryProvider`. Consulte `proposal.md` e as delta specs para o comportamento contratado.

## Goals / Non-Goals

**Goals:**

- Introduzir uma fronteira BFF same-origin que mantenha o JWT inacessível ao JavaScript do browser.
- Centralizar regras sensíveis de backend, cookie, Origin, erros e sessão em colaboradores server-only reutilizáveis.
- Fazer a identidade e a autorização dependerem sempre de `/auth/me`, não de claims locais.
- Estabelecer uma experiência coerente para sessão ausente, inválida e sem permissão, com testes automatizados proporcionais ao risco.

**Non-Goals:**

- Alterar endpoints, CORS, autenticação ou ciclo de token do Spring.
- Criar refresh token, blacklist, logout upstream, revogação ou token CSRF dedicado.
- Entregar dashboards financeiros ou administrativos funcionais, catálogo, transações ou outros domínios de negócio.

## Decisions

### BFF same-origin com Route Handlers

Os quatro endpoints públicos ao browser serão `POST /api/auth/login`, `POST /api/auth/register`, `GET /api/auth/me` e `POST /api/auth/logout`. Login, cadastro e me farão a chamada correspondente ao Spring dentro do servidor; logout será apenas local. O BFF normaliza respostas seguras, `Cache-Control: no-store` onde aplicável, ProblemDetail e correlation ID, e nunca repassa token ou Authorization.

Alternativas consideradas: chamar Spring diretamente do browser exigiria CORS e exporia o token ao runtime client; usá-lo foi descartado. Replicar contratos ou lógica de autenticação no frontend também foi descartado porque o Spring já é autoridade de segurança.

### Cookie HttpOnly como única posse da sessão

O BFF grava o token em `auth_session` com `httpOnly: true`, `sameSite: 'lax'`, `path: '/'`, sem `domain` e expiração de `expiresIn`. Secure deriva do esquema de APP_ORIGIN: HTTPS=true e HTTP=false. Produção real exige APP_ORIGIN HTTPS, mas build Next de produção local via `http://localhost` mantém sessão. Criação e exclusão compartilham configuração-base.

Alternativas consideradas: localStorage, sessionStorage, estado React e cache de queries foram descartados por exporem o JWT a JavaScript e por contrariarem o contrato da change.

### Módulos server-only compartilhados

O frontend separará colaboradores server-only sensíveis para configuração, cliente backend, cookie, Origin, ProblemDetail/correlation ID e resolvedor atual. Login e cadastro Spring não recebem Bearer; Bearer é adicionado apenas a chamadas protegidas. Helpers simples de proxy ficam separados e se limitam a nome do cookie, caminhos protegidos e returnTo; proxy não importa cliente backend, resolvedor, JWT, Authorization, ProblemDetail ou escrita de sessão.

Alternativa considerada: repetir fetch, headers e tratamento de status em cada handler ou guard. Foi descartada pela divergência provável em cookie, 401/403 e erro seguro.

### `/auth/me` confirma sessão, identidade e autorização

Uma cookie presente representa somente uma pista de sessão. O Route Handler de me chama o resolvedor server-only e retorna perfil seguro; guard/layout chama o mesmo resolvedor diretamente, sem HTTP Next→Next. O browser chama o handler. Nenhum decodifica claims. O resultado confirmado informa `/inicio` para `ROLE_USER` e `/admin` para `ROLE_ADMIN`.

`proxy.ts` tem somente pre-check. Sem cookie, encaminha para login com returnTo sanitizado. Com cookie, guard/layout confirma pelo resolvedor; se retorna 401, redireciona ao login sem depender de apagar cookie. GET `/api/auth/me` remove cookie por Set-Cookie e retorna 401 seguro; cliente limpa query. Login permanece público com cookie inválido, evitando loop.

Alternativa considerada: usar middleware/proxy como guard definitivo. Foi descartada porque o proxy não deve chamar a autoridade de identidade nem tomar decisão baseada em token local.

### Separação explícita entre 401 e 403

O adaptador upstream classificará `401` como sessão inválida e `403` como autorização insuficiente. Um 401 do BFF de me limpará o cookie na própria resposta; fluxos client-side limparão `['auth', 'me']` e estado autenticado. Um 403 preserva cookie e query de me e resulta em `/acesso-negado` ou resposta 403, nunca logout. A proteção por role será aplicada após sessão confirmada.

Alternativa considerada: tratar qualquer erro de segurança como logout. Foi descartada pois remove sessões válidas de usuários sem permissão e contradiz o contrato Spring.

### CSRF mínimo por Origin e configuração server-only

Cada Route Handler mutável validará `Origin` contra `APP_ORIGIN` normalizada/configurada no servidor; Host enviado pelo cliente não é a fonte de verdade. SameSite=Lax complementa esta validação. `BACKEND_API_URL` e `APP_ORIGIN` substituem a dependência funcional de `NEXT_PUBLIC_API_URL`; o Compose fornecerá uma URL interna alcançável pelo container e uma origem pública canônica.

Alternativas consideradas: CORS no Spring e token CSRF dedicado. CORS conflita com a fronteira same-origin; token dedicado não se justifica com o escopo atual e a proteção Origin+SameSite.

### UI, navegação e TanStack Query

Login e cadastro serão Client Components com mutations sem retry. Login, após 204, carrega ou invalida me antes da navegação. Cadastro, após 201 sem cookie, não cria sessão, não altera me e segue para login. Logout limpa query; 401 limpa estado; 403 não o remove.

As páginas `/inicio` e `/admin` serão deliberadamente neutras e protegidas; `/acesso-negado` explica a autorização insuficiente mantendo a sessão.

### Estratégia de testes

Vitest e React Testing Library cobrirão módulos puros, Route Handlers e componentes/queries com mocks controlados do upstream. Os testes de handler verificarão URL/método upstream, Bearer somente server-side, headers/cookies, expiresIn, Origin, ProblemDetail, correlation ID e classes de falha. Testes de guard e componentes cobrirão sessão ausente, roles, 401/403, `returnTo` e transições de cache. Playwright será adicionado apenas para poucos fluxos críticos de navegador que validem redirecionamento same-origin e ausência observável de token; não substituirá a cobertura rápida de unidade/integração.

## Risks / Trade-offs

- [Cookie de sessão presente com token expirado pode causar redirecionamentos repetidos] → o 401 de me remove o cookie quando a resposta puder emitir `Set-Cookie`, o cliente limpa cache e o guard não trata cookie como sessão válida.
- [`Secure` impede cookie em HTTP local se aplicado indiscriminadamente] → derivar Secure de APP_ORIGIN e testar HTTP/HTTPS, mantendo demais atributos invariantes.
- [Divergência de URL entre browser e container] → documentar `APP_ORIGIN` como origem pública e `BACKEND_API_URL` como endereço server-to-server, com exemplos no Compose.
- [Erro upstream pode vazar detalhes internos] → limitar o contrato BFF a ProblemDetail seguro, status e correlation ID, omitindo token, Authorization e detalhes de infraestrutura.
- [Server Components não podem limpar cookie arbitrariamente] → concentrar remoção em Route Handlers/respostas mutáveis e projetar redirecionamentos para evitar depender de escrita de cookie em contexto somente de leitura.

## Migration Plan

1. Adicionar as variáveis server-only ao exemplo de ambiente e ao serviço frontend do Compose, preservando compatibilidade local durante a transição.
2. Publicar o BFF e a UI autenticada junto com seus testes; o browser deixa de depender de `NEXT_PUBLIC_API_URL`.
3. Confirmar em ambiente integrado login, registro, logout e recuperação de 401; monitorar `X-Correlation-ID` para suporte.
4. Rollback: reverter o deployment frontend desta change. Não há migração de dados nem alteração no backend; cookies antigos expiram naturalmente e logout local pode removê-los.

## Open Questions

- Nenhuma. A porta/origem concreta de cada ambiente será definida pelos valores de `APP_ORIGIN` e `BACKEND_API_URL`, sem alterar a arquitetura ou os requisitos.
