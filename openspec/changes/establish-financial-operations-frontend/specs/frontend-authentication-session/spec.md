## MODIFIED Requirements

### Requirement: Protecao de rotas e autorizacao por role
`/configuracoes` SHALL require a confirmed authenticated principal and allow ROLE_USER or ROLE_ADMIN to manage only that principal. `/admin` and subroutes SHALL remain ROLE_ADMIN-only; `/carteira` and subroutes SHALL remain ROLE_USER-only. Role mismatch SHALL preserve the valid session and render access denied. The profile menu SHALL expose Configuracoes immediately before Sair on desktop and mobile with keyboard, focus, Escape/outside-click and touch behavior.

#### Scenario: Current principal opens settings
- **WHEN** a confirmed ROLE_USER or ROLE_ADMIN activates Configuracoes from the profile menu
- **THEN** `/configuracoes` opens for that same principal without becoming administrative user management

### Requirement: Current-user refresh and sensitive-session cleanup
After name/email success, the frontend SHALL refresh `['auth','me']` so all profile displays use current values without exposing or replacing the JWT. After password change, the same-origin BFF SHALL delete the current `auth_session` and the client SHALL clear auth/personal caches and navigate to login. After account closure it SHALL additionally clear financial recovery state and prevent authenticated back/cache access. The UI MUST NOT claim global JWT revocation where no token-version/blacklist exists.

#### Scenario: Profile data changes
- **WHEN** name or email update succeeds
- **THEN** the profile menu immediately shows the current server-confirmed identity

#### Scenario: Password or account closure succeeds
- **WHEN** password change or closure is confirmed
- **THEN** the current browser session/caches are cleared and navigation returns to login
