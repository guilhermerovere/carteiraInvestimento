## Purpose

Define a compact functional Valore administration experience over the existing global broker and asset catalog APIs.

## ADDED Requirements

### Requirement: Dedicated authorized administrative shell
`/admin`, `/admin/corretoras` and `/admin/ativos` SHALL require a confirmed ROLE_ADMIN and render a dedicated responsive Valore shell with Visao geral, Corretoras and Ativos plus theme, profile/settings and logout. It MUST NOT render wallet navigation, Operar, BUY, SELL, deposit or withdrawal. ROLE_USER SHALL receive the existing access-denied behavior and Spring remains the authorization authority.

#### Scenario: Admin enters catalog administration
- **WHEN** confirmed ROLE_ADMIN opens an administrative route
- **THEN** the administrative shell and catalog navigation render without personal financial controls

#### Scenario: User attempts administrative route
- **WHEN** confirmed ROLE_USER opens `/admin` or a subroute
- **THEN** access is denied without exposing catalog mutation controls or clearing the valid session

### Requirement: Same-origin administrative boundary
The browser SHALL use same-origin `/api/admin/*` routes. The BFF SHALL read `auth_session` server-side, forward Bearer only server-side, use no-store, validate exact query/body allowlists and UUIDs, validate Origin on mutations, and preserve only safe status/code/correlation metadata for presentation mapping. JWT, Authorization, backend URL, provider secrets, raw provider payload and raw ProblemDetail MUST NOT reach business UI.

#### Scenario: Admin mutation proxy
- **WHEN** ROLE_ADMIN submits an allowed broker or asset mutation from the browser
- **THEN** the BFF validates origin/body and forwards the exact existing backend contract with server-side authentication

### Requirement: Functional overview and catalog management
The overview SHALL remain simple and may show asset/broker totals from existing list envelopes plus links. Broker management SHALL list, create, edit permitted local fields, activate and deactivate. Asset management SHALL list, create, edit name, activate and deactivate. All feedback SHALL be friendly pt-BR, responsive in Light/Dark and accessible by keyboard/touch; lifecycle changes SHALL require clear confirmation when destructive in meaning. No charts, physical DELETE, invented field or cosmetic backend endpoint SHALL be added.

#### Scenario: Admin overview
- **WHEN** catalog envelopes are available
- **THEN** overview shows simple Ativos/Corretoras counts and links without analytics backend expansion

#### Scenario: Admin performs catalog lifecycle change
- **WHEN** ROLE_ADMIN confirms activation or deactivation
- **THEN** the list reflects the authoritative response and announces a pt-BR result without raw JSON

### Requirement: Provider-assisted administrative asset registration
The primary ADMIN asset registration UI SHALL use provider discovery in a compact dialog and confirm provider-derived canonical metadata. Existing ADMIN backend contracts MAY remain available for compatibility, but the principal UI SHALL NOT encourage invented ticker, name, type, currency, logo or price data. Asset and broker lists SHALL use compact rows, dialogs and contextual lifecycle actions without physical deletion.

#### Scenario: Administrator adds an asset
- **WHEN** an administrator selects a supported provider result in Adicionar ativo
- **THEN** the compact dialog confirms its provider-derived identity before registration
- **AND** the catalog list retains contextual edit and lifecycle actions.
