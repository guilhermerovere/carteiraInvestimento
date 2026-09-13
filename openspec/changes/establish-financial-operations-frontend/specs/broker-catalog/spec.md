## MODIFIED Requirements

### Requirement: Responses deterministicas, RBAC e visibilidade
All broker routes SHALL require authentication. USER may only list/consult active brokers; inactive and nonexistent brokers return 404. ADMIN creates, edits allowed local address fields, activates/deactivates, and may query both states; USER mutation receives 403 and anonymous access 401. USER list/detail SHALL preserve `id`, `cnpj`, `razaoSocial`, `nomeFantasia`, and `ativo`, with optional system-owned `logoProvider` and `logoReference` for presentation. ADMIN responses SHALL preserve existing official/address/lifecycle fields and MAY include those same optional references. POST/PATCH SHALL preserve the current ADMIN response contract. List pagination, fixed ordering, and existing strict query parameters remain unchanged. No response SHALL contain secret credentials, raw provider payloads, or credential-bearing Search API URLs; a Logo.dev publishable key is assembled by the frontend only for the official image CDN.

#### Scenario: Responses por role
- **WHEN** USER or ADMIN queries, lists, or mutates broker data according to the existing permissions
- **THEN** USER receives only selection data plus optional branding reference, ADMIN receives the administrative response plus optional branding reference, and neither receives provider payloads or secrets

#### Scenario: Inactive visibility remains restricted
- **WHEN** USER queries or lists an inactive broker
- **THEN** the existing 404/active-only behavior remains and no inactive broker becomes selectable

## ADDED Requirements

### Requirement: Automatic optional broker branding
Broker registration SHALL remain exclusively ADMIN-only and the existing POST body SHALL NOT accept logo, URL, file, provider, publishable key, or secret key. After authoritative CNPJ/CVM/address validation, the system SHALL ATTEMPT automatic branding resolution when Logo.dev Search is configured. Search SHALL run server-side with `LOGO_DEV_SECRET_KEY`, `strategy=match`, and actual canonical `razaoSocial`/`nomeFantasia`; if the current model supplies an official domain, that domain may be used directly, but the system MUST NOT invent one. Persist only `logoProvider=LOGO_DEV` and one canonical domain in `logoReference` when one sufficiently strong exact normalized identity match is unambiguous. Search ranking/popularity alone is insufficient. Missing credentials, provider outage/timeout, malformed/no results, ambiguous names, or weak match leave branding absent and MUST NOT reject a compliant broker. Search SHALL occur outside the short persistence transaction after authoritative validation; failure SHALL NOT roll back broker creation. Catalog reads SHALL reuse the persisted domain and MUST NOT call Search once per broker row. The frontend MAY use `LOGO_DEV_PUBLISHABLE_KEY` only in direct image requests to `https://img.logo.dev`; no image proxy, downloaded bytes, secret key, raw provider payload, or Search URL is stored or returned.

#### Scenario: Broker branding is attempted after validation
- **WHEN** a compliant broker is authoritatively validated and Logo.dev Search is configured
- **THEN** the backend attempts a server-side `strategy=match` lookup using canonical broker identity after validation and outside the short persistence transaction

#### Scenario: Broker matches uniquely
- **WHEN** exactly one sufficiently strong exact normalized Search result maps to a canonical domain
- **THEN** the backend stores `logoProvider=LOGO_DEV` and that domain as `logoReference`

#### Scenario: Broker match is ambiguous or unavailable
- **WHEN** a broker branding lookup has multiple plausible matches, no match, or provider failure
- **THEN** the broker remains successfully registered with absent branding metadata

#### Scenario: Logo.dev secret is isolated
- **WHEN** backend Search API resolves broker branding
- **THEN** `LOGO_DEV_SECRET_KEY` is sent only server-to-server and is absent from browser responses, logs, stored references, and image URLs

#### Scenario: Publishable key is used only for CDN delivery
- **WHEN** the frontend renders a persisted Logo.dev broker domain
- **THEN** it uses the configured publishable key only in an `img.logo.dev` image request, never in Search API or any other request

#### Scenario: ADMIN submits a manual logo
- **WHEN** an ADMIN includes a logo URL, file, provider, Logo.dev publishable key, or Logo.dev secret key in broker registration
- **THEN** the request is rejected as an unknown field and no user-supplied logo is stored

#### Scenario: Catalog reads reuse branding metadata
- **WHEN** a USER loads multiple active brokers
- **THEN** the catalog response reuses persisted logo references without external search per row

#### Scenario: Branding provider failure does not affect operations
- **WHEN** a broker's stored logo is missing or its image endpoint fails
- **THEN** broker selection falls back visually and financial operation rules remain unchanged
