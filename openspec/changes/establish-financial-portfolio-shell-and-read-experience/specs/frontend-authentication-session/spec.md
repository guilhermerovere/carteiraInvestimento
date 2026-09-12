## MODIFIED Requirements

### Requirement: Proteção de rotas e autorização por role
`/carteira` e suas subrotas SHALL exigir sessão confirmada e ROLE_USER. `/inicio` SHALL exigir sessão confirmada e ROLE_USER e, somente após o guard server-side confirmar o usuário, SHALL redirecionar para `/carteira` como compatibilidade. `/admin` SHALL exigir sessão confirmada e ROLE_ADMIN. `proxy.ts` SHALL ser somente pre-check de cookie ausente e poderá conhecer apenas nome do cookie, caminhos protegidos e sanitização de returnTo; MUST NOT chamar Spring, importar cliente backend/current user/Authorization/ProblemDetail, ler ou decodificar JWT, alterar sessão ou decidir autorização final. A role SHALL ser decidida exclusivamente pelo usuário atual confirmado server-side. Role insuficiente SHALL resultar em acesso negado/403 e preservar sessão.

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
