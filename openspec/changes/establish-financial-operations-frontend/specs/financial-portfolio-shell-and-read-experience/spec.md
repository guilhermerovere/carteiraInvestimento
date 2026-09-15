## MODIFIED Requirements

### Requirement: Shell autenticado, rotas e navegacao financeira
The system SHALL make `/carteira`, `/carteira/posicoes`, `/carteira/transacoes`, and `/carteira/movimentacoes` exclusive to a confirmed ROLE_USER. Desktop SHALL retain persistent Valore navigation for Carteira, Posicoes, Transacoes, and Movimentacoes, with a visible accessible collapse/expand control and no clipping/overflow. The ROLE_USER `Operar` launcher SHALL be in the lower sidebar immediately above Recolher/Expandir, aligned to the same grid/gutters; expanded mode shows icon and label, while collapsed mode keeps a centered icon, tooltip, accessible name and functional Comprar/Depositar/Sacar menu. It MUST NOT offer global SELL, which remains contextual to an open position. Mobile SHALL retain the compact header and bottom navigation for Carteira, Posicoes, Transacoes, and Mais, through which Movimentacoes and Operar remain reachable without a duplicate header launcher. The desktop/mobile header SHALL retain only page context, theme, profile and account actions. If sessionStorage contains an ambiguous financial POST, the shell SHALL show a persistent accessible recovery indicator and a `Revisar operacao` action that reopens a read-only intent with safe details and same-key/same-payload retry. It MUST NOT offer cancellation or editing. ROLE_ADMIN SHALL remain outside the personal wallet experience and SHALL NOT receive personal financial operations.

#### Scenario: Navegacao de usuario autenticado
- **WHEN** a confirmed ROLE_USER opens a wallet route
- **THEN** the responsive read shell remains available and lower-sidebar/mobile-Mais Operar starts BUY, deposit, or withdrawal without replacing navigation

#### Scenario: SELL is contextual
- **WHEN** the shell is rendered for ROLE_USER
- **THEN** SELL is not shown as a global launcher option and can be started from an open position

#### Scenario: Admin acessa rota de carteira
- **WHEN** ROLE_ADMIN accesses a wallet route or the application shell
- **THEN** the existing access-denied behavior remains, the session is retained, and no personal wallet launcher is shown

#### Scenario: Operar em viewport mobile
- **WHEN** ROLE_USER opens Mais on mobile
- **THEN** it exposes Comprar, Depositar and Sacar and opens an accessible responsive operation container without a duplicate header action or hidden bottom navigation content

#### Scenario: Operar em sidebar recolhida
- **WHEN** ROLE_USER collapses the desktop sidebar
- **THEN** Operar remains immediately above Expandir as a centered named icon with tooltip and a keyboard/touch functional compact anchored popover; its labels, icons, spacing and borders remain aligned without clipping or overflow in Light and Dark

#### Scenario: Recovery of ambiguous financial intent
- **WHEN** ROLE_USER returns to the shell while an ambiguous POST intent remains in sessionStorage
- **THEN** a persistent accessible indicator opens safe read-only operation details and manual retry with the original key and payload, without claiming cancellation

### Requirement: User asset discovery route and compact operation controls
The personal shell SHALL expose `/carteira/ativos` to every `ROLE_USER` in coherent desktop and mobile navigation. The page SHALL be a compact investment discovery surface rather than a CRUD form. A collapsed desktop Operar control SHALL use a small anchored popover for Comprar, Depositar and Sacar.

#### Scenario: Desktop user navigation
- **WHEN** a user opens the personal shell
- **THEN** Carteira, Posicoes, Ativos, Transacoes and Movimentacoes are reachable
- **AND** collapsed Operar opens only a compact anchored action popover.
