# account-settings Specification

## Purpose
Define the protected Valore account-settings UI/BFF and safe pt-BR behavior for profile, password and account closure.

## Requirements

### Requirement: Protected accessible settings page
The authenticated profile menu SHALL link to `/configuracoes`, which SHALL present Dados pessoais, Seguranca and Zona de perigo in a comfortable centered responsive layout. ROLE_USER and ROLE_ADMIN may access only their own settings. Forms SHALL provide labels, descriptions, focus-visible behavior, `aria-invalid`/`aria-describedby`, polite status announcements, keyboard/touch support and Light/Dark contrast. Zona de perigo SHALL use a semantic destructive token rather than the regular brand accent.

#### Scenario: Settings navigation
- **WHEN** a confirmed principal selects Configuracoes
- **THEN** the three focused settings sections render for that principal and Sair remains available

### Requirement: Profile and password forms
Dados pessoais SHALL update name and email with exact backend validations and immediately refresh every current-user display. Duplicate email SHALL show `Este email ja esta em uso.` Seguranca SHALL require Senha atual, Nova senha and Confirmar nova senha; frontend confirmation MUST match before POST. Incorrect current password SHALL show `A senha atual esta incorreta.` Success messages SHALL be `Nome atualizado com sucesso.`, `Email atualizado com sucesso.` and `Senha alterada com sucesso.` Password fields MUST be cleared and never stored or logged.

#### Scenario: Name or email succeeds
- **WHEN** a valid own-profile mutation succeeds
- **THEN** the exact pt-BR success is announced and the current profile menu reflects server-confirmed data

#### Scenario: Password succeeds
- **WHEN** current/new/confirmation passwords are valid
- **THEN** password fields are cleared, success is announced, and the current browser session proceeds to login cleanup

### Requirement: Strong closure confirmation and safe blocking
Zona de perigo SHALL not close an account with one click. A modal SHALL require current password and explicit `EXCLUIR MINHA CONTA` confirmation with non-color-only destructive language. A known ambiguous financial intent in sessionStorage SHALL block the browser attempt and link to its review, without treating storage as ledger authority. Backend codes for nonzero cash/open positions SHALL map respectively to cash-zero and sell-position guidance with a wallet link. No raw ProblemDetail/JSON SHALL render.

#### Scenario: Known ambiguous operation
- **WHEN** the current tab has an ambiguous pending financial intent
- **THEN** closure submit is unavailable and the user is directed to review that operation first

#### Scenario: Closure is financially blocked
- **WHEN** backend revalidation reports cash or open positions
- **THEN** the modal remains safe and shows the corresponding natural pt-BR resolution without changing the account

#### Scenario: Closure succeeds
- **WHEN** strong confirmation succeeds for an eligible account
- **THEN** the UI announces `Sua conta foi excluida com sucesso.`, clears session/personal/recovery state and redirects to login

### Requirement: Same-origin settings boundary and safe errors
The browser SHALL call only same-origin `/api/account/*` routes. The BFF SHALL use server-side `auth_session`, forward Bearer only server-side, validate Origin and exact body allowlists, use no-store, and never expose JWT/Authorization/backend secret. Business UI SHALL map safe status/code to pt-BR and MUST NOT automatically render backend title/detail/instance, arbitrary objects or correlation ids.

#### Scenario: Settings mutation crosses BFF
- **WHEN** a settings form submits
- **THEN** same-origin validation/forwarding preserves credentials server-side and returns only the safe contract needed by UI logic
