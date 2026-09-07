## Why

O backend já oferece cadastro, login JWT e consulta do principal atual, mas o frontend ainda não possui autenticação nem uma forma segura de manter sessão. Esta change cria essa fundação sem expor o JWT ao JavaScript do navegador e sem alterar os contratos Spring existentes.

## What Changes

- Adiciona uma camada BFF same-origin em Route Handlers do Next.js para login, cadastro, consulta do usuário atual e logout local.
- Armazena o JWT recebido do Spring exclusivamente em cookie `auth_session` HttpOnly e encaminha o Bearer somente em chamadas server-side.
- Adiciona páginas de login, cadastro, início protegido, área administrativa protegida e acesso negado, com restauração de sessão, guards e regras por role.
- Define tratamento frontend de `401`, `403`, `ProblemDetail`, `X-Correlation-ID`, `returnTo` seguro, validação de `Origin` e cache de autenticação com TanStack Query.
- Estabelece configuração server-only por `BACKEND_API_URL` e `APP_ORIGIN`, removendo a dependência funcional e a exibição de `NEXT_PUBLIC_API_URL`.
- Estabelece a stack de testes frontend para os fluxos e invariantes de autenticação e sessão.

## Capabilities

### New Capabilities

- `frontend-authentication-session`: autenticação e sessão do frontend por BFF Next.js, cookie HttpOnly, usuário atual, autorização por role, proteção de rotas e tratamento seguro de falhas.

### Modified Capabilities

- `frontend-foundation`: substitui a configuração pública direta da URL do backend por configuração server-only para o BFF e evolui a página de fundação para não exibir essa URL.
- `containerized-local-environment`: disponibiliza ao container frontend as variáveis server-only necessárias para o BFF alcançar o backend e validar a origem canônica.

## Impact

- Afeta o projeto Next.js em `frontend/`, seus Route Handlers, páginas, providers, testes e dependências de teste.
- Afeta `.env.example` e `docker-compose.yml` na configuração do frontend; não altera o contrato HTTP, segurança, CORS ou implementação do backend Spring.
- O browser passa a falar somente com endpoints same-origin do Next.js; `BACKEND_API_URL`, JWT e `Authorization` permanecem no servidor.
