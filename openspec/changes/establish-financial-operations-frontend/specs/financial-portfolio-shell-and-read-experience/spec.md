## MODIFIED Requirements

### Requirement: Shell autenticado, rotas e navegacao financeira
The system SHALL make `/carteira`, `/carteira/posicoes`, `/carteira/transacoes`, and `/carteira/movimentacoes` exclusive to a confirmed ROLE_USER. Desktop SHALL retain persistent Valore navigation for Carteira, Posições, Transações, and Movimentações, with a visible accessible collapse/expand control and no clipping/overflow. Mobile SHALL retain a compact header and bottom navigation for Carteira, Posições, Transações, and Mais, through which Movimentações remains reachable. The header SHALL retain page context, theme, profile, and logout. For ROLE_USER, the shell SHALL add an `Operar` launcher with Comprar, Depositar, and Sacar actions; it MUST NOT offer global SELL, which remains contextual to an open position. If sessionStorage contains an ambiguous financial POST, the shell SHALL show a persistent accessible recovery indicator and a `Revisar operação` action that reopens a read-only intent with safe details and same-key/same-payload retry. It MUST NOT offer cancellation or editing. ROLE_ADMIN SHALL remain outside the personal wallet experience and SHALL NOT receive personal financial operations.

#### Scenario: Navegacao de usuario autenticado
- **WHEN** a confirmed ROLE_USER opens a wallet route
- **THEN** the responsive read shell remains available and the new Operar launcher starts BUY, deposit, or withdrawal without replacing existing navigation

#### Scenario: SELL is contextual
- **WHEN** the shell is rendered for ROLE_USER
- **THEN** SELL is not shown as a global launcher option and can be started from an open position

#### Scenario: Admin acessa rota de carteira
- **WHEN** ROLE_ADMIN accesses a wallet route or the application shell
- **THEN** the existing access-denied behavior remains, the session is retained, and no personal wallet launcher is shown

#### Scenario: Operar em viewport mobile
- **WHEN** ROLE_USER opens Operar on mobile
- **THEN** the launcher opens an accessible responsive operation container without displacing or hiding the bottom navigation content

#### Scenario: Recovery of ambiguous financial intent
- **WHEN** ROLE_USER returns to the shell while an ambiguous POST intent remains in sessionStorage
- **THEN** a persistent accessible indicator opens safe read-only operation details and manual retry with the original key and payload, without claiming cancellation
