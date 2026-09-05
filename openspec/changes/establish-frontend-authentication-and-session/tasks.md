## 1. Configuração e stack de testes

- [ ] 1.1 Inspecionar versões e padrões do projeto frontend e adicionar Vitest, React Testing Library e os scripts/configurações mínimos; verificar que a suíte vazia e o build Next.js executam no diretório `frontend/`.
- [ ] 1.2 Adicionar Playwright somente com configuração e um caminho crítico same-origin; verificar que o teste pode iniciar contra o frontend configurado.
- [ ] 1.3 Substituir a dependência funcional de `NEXT_PUBLIC_API_URL` por `BACKEND_API_URL` e `APP_ORIGIN` server-only em `.env.example`, Compose e container frontend; verificar que nenhum bundle público contém a URL interna do backend.
- [ ] 1.4 Remover da página de fundação a exibição/configuração pública do backend; verificar por teste de renderização que a resposta pública não revela `BACKEND_API_URL`.

## 2. Núcleo server-only de sessão

- [ ] 2.1 Criar módulo server-only de configuração validada para `BACKEND_API_URL` e `APP_ORIGIN`; verificar testes para valor válido, ausente e origem canônica normalizada.
- [ ] 2.2 Criar tipos seguros para usuário atual, login, cadastro, ProblemDetail, erro HTTP e erro de rede; verificar testes de parsing que preservam status, title, detail, instance e correlation ID sem token.
- [ ] 2.3 Criar cliente server-only do Spring que encaminha Bearer somente quando recebe token explícito e propaga ProblemDetail/correlation ID com sanitização; verificar testes de URL, método, Authorization server-side e falha de rede.
- [ ] 2.4 Criar `auth_session` com configuração-base compartilhada; verificar HttpOnly, SameSite=Lax, Path=/, host-only, expiração por expiresIn e Secure=true/false para APP_ORIGIN HTTPS/HTTP.
- [ ] 2.5 Criar validador server-only de Origin baseado exclusivamente em `APP_ORIGIN`; verificar que Origin canônico passa e ausente, Host confiado isoladamente ou origem divergente falham.
- [ ] 2.6 Criar resolvedor server-only do usuário atual que consulta `/api/v1/auth/me` sem decodificar JWT; verificar retorno seguro, classificação 401/403 e ausência de dados internos.

## 3. Route Handlers BFF

- [ ] 3.1 Implementar `POST /api/auth/login` com validação de Origin, chamada Spring e resposta `204 no-store`; verificar upstream correto, cookie HttpOnly por `expiresIn` e ausência de `accessToken` no corpo e headers expostos.
- [ ] 3.2 Implementar `POST /api/auth/register` com validação de Origin e propagação segura de `201`; verificar que não cria cookie nem login automático e não devolve credenciais.
- [ ] 3.3 Implementar `GET /api/auth/me` usando exclusivamente o cookie server-side e o resolvedor atual; verificar Bearer apenas no upstream, retorno seguro, no-store e remoção de cookie quando upstream devolve 401.
- [ ] 3.4 Implementar `POST /api/auth/logout` com validação de Origin e remoção coerente de cookie; verificar `204`, Set-Cookie de exclusão e rejeição de Origin inválido sem alterar sessão.
- [ ] 3.5 Cobrir handlers para ProblemDetail, `X-Correlation-ID` e erro de rede; verificar que JWT, Authorization e detalhes internos não aparecem nas respostas de erro.

## 4. Cliente, rotas e guards

- [ ] 4.1 Criar sanitizador compartilhado de `returnTo` e testes para caminho interno aceito e URL absoluta, `//host` e esquemas externos rejeitados.
- [ ] 4.2 Adicionar `proxy.ts` somente com nome de cookie, caminhos protegidos e sanitização de returnTo para pre-check; verificar redirecionamento anônimo e ausência de backend client, resolvedor, JWT, Authorization, ProblemDetail, escrita de sessão, decode ou role como autoridade.
- [ ] 4.3 Criar guards/layouts que chamam diretamente o resolvedor current-user, sem HTTP Next→Next; verificar sessão ausente, cookie com 401 redirecionado ao login sem depender de apagá-lo e role insuficiente sem logout.
- [ ] 4.4 Criar páginas neutras `/login`, `/register`, `/inicio`, `/admin` e `/acesso-negado`; verificar que `/inicio` aceita apenas `ROLE_USER`, `/admin` apenas `ROLE_ADMIN` e acesso negado preserva sessão.
- [ ] 4.5 Implementar formulários de login e cadastro contra o BFF; verificar login 204 seguido de confirmação de me, cadastro 201 seguido de navegação a `/login` e mensagens seguras de erro.

## 5. Estado de autenticação TanStack Query

- [ ] 5.1 Criar query `['auth', 'me']` e mutations de login, cadastro e logout com retry desabilitado; verificar configurações de retry e chamadas same-origin em testes de hooks/componentes.
- [ ] 5.2 Implementar transições de cache após login, logout e 401; verificar que login confirma ou invalida me, e logout/401 removem estado autenticado e conduzem ao login quando aplicável.
- [ ] 5.3 Implementar fluxo de 403 que preserva `auth_session` e `['auth', 'me']`; verificar por teste que acesso negado não dispara logout nem invalida identidade atual.

## 6. Verificação integrada

- [ ] 6.1 Executar a suíte Vitest completa e corrigir falhas; verificar cobertura dos fluxos login, cadastro, me, logout, Origin, cookie, guards, returnTo, erros e cache.
- [ ] 6.2 Executar os poucos fluxos Playwright definidos e verificar browser same-origin, redirecionamento de rota protegida e ausência observável de token na resposta de login.
- [ ] 6.3 Executar lint, typecheck e build do frontend; verificar que imports server-only não entram no bundle client-side e que as páginas protegidas compilam.
- [ ] 6.4 Executar a subida integrada por Docker Compose com ambiente de exemplo seguro; verificar que o frontend recebe `BACKEND_API_URL`/`APP_ORIGIN` server-only e não requer CORS no Spring.
