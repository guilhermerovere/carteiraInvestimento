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

### Requirement: Provider-backed CVM validation
Broker registration SHALL normalize a canonical 14-digit CNPJ and validate the regulated institution against the public official CVM intermediary registry. Resolution SHALL first use an exact 14-digit match. Only when no exact record exists, it SHALL search the snapshot for active supported intermediary/corretora records with the same eight-digit CNPJ root and accept only one unambiguous candidate. Zero candidates, inactive-only candidates, or multiple compatible root candidates SHALL be rejected safely; an exact inactive record SHALL NOT fall through to root matching. The CVM-returned CNPJ SHALL be the canonical regulatory identity persisted and used for duplication, while Receita/CEP data may describe the submitted establishment. Headquarters and branches of the same root SHALL NOT create separate broker records. The implementation SHALL retain a bounded in-memory last-known-good snapshot with a six-hour refresh TTL. A successful official `cad_intermed.zip` refresh SHALL atomically replace that snapshot; a failed refresh SHALL keep it. Cold start SHALL load a documented, versioned local baseline derived from the official registry format before attempting a best-effort remote refresh. The baseline and a successful remote snapshot are the only fallback data; the implementation SHALL NOT use another provider or invent entries. A matching active broker SHALL be admitted; a confirmed absent or ambiguous CNPJ SHALL return `422` with code `BROKER_CVM_NOT_REGISTERED` and detail `Este CNPJ não está cadastrado na CVM.`; only a missing snapshot combined with timeout, 5xx, download, archive, or parsing failure SHALL return safe `502` with code `BROKER_VALIDATION_UNAVAILABLE`.

#### Scenario: Official registry contains the CNPJ while a proxy is unavailable
- **WHEN** a canonical CNPJ is active in the official CVM registry and a third-party CVM proxy is unavailable
- **THEN** the compliant broker is created without depending on that proxy

#### Scenario: Official registry is temporarily unavailable
- **WHEN** the official registry cannot be obtained or parsed after a baseline or prior successful snapshot is available
- **THEN** registration continues from that snapshot without treating the CNPJ as absent

#### Scenario: Cold start is temporarily offline
- **WHEN** the application starts while the official registry is unavailable
- **THEN** the versioned baseline is loaded, remote refresh is best-effort, and a CNPJ known by the baseline can be registered

#### Scenario: No snapshot exists while the official registry is unavailable
- **WHEN** neither the baseline nor a successful remote snapshot is available
- **THEN** no broker is persisted and the UI receives only the friendly temporary-validation message with `BROKER_VALIDATION_UNAVAILABLE`

#### Scenario: Establishment resolves to canonical CVM headquarters
- **WHEN** `02.332.886/0016-82` has no exact registry row and the snapshot contains only the active supported XP intermediary `02.332.886/0001-04` for root `02.332.886`
- **THEN** validation succeeds and the broker identity is persisted as canonical CNPJ `02332886000104`

#### Scenario: Headquarters already exists when a branch is submitted
- **WHEN** canonical XP `02332886000104` is already registered and an administrator submits `02332886001682`
- **THEN** no second broker is created and the response is `409` with `BROKER_ALREADY_REGISTERED`

#### Scenario: Root match is ambiguous
- **WHEN** no exact CNPJ exists and more than one active supported registry record shares the submitted eight-digit root
- **THEN** root fallback is rejected with `422` and no broker is persisted

### Requirement: Administrative broker presentation reuses compliance contracts
The ROLE_ADMIN frontend SHALL list/consult brokers, create with exactly CNPJ plus optional numero/complemento, edit only numero/complemento, and set lifecycle through the existing idempotent `ativo` request. Official identity/address fields SHALL be presented as read-only, physical DELETE and manual branding inputs MUST NOT appear, and EntityLogo SHALL consume only system-owned branding with fallback. Provider/compliance errors SHALL be mapped to safe pt-BR presentation without raw detail.

#### Scenario: Admin maintains a broker
- **WHEN** ROLE_ADMIN creates, edits, activates or deactivates a broker in the administrative UI
- **THEN** the UI submits only existing allowed fields and presents a friendly pt-BR result without raw provider or JSON payload
