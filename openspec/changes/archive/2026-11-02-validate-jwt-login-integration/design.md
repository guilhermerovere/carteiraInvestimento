## Context

See [proposal.md](proposal.md) for motivation. The production chain is already present: `FoundationSecurityConfiguration` allows login and protects `/api/v1/auth/me`; `LoginService` authenticates against persisted credentials and delegates issuance to `JwtAccessTokenIssuer`; `PersistedUserJwtAuthenticationConverter` reloads the user from persistence for protected requests. Existing coverage tests these pieces separately, while security ITs create a JWT directly with `JwtEncoder`.

`PostgreSqlContainerSupport` supplies PostgreSQL 16 Testcontainers, datasource settings and the JWT/bootstrap configuration used by Spring Boot/Flyway integration tests. The test must remain in the `integration` package so it can extend that support directly.

## Goals / Non-Goals

**Goals:**

- Exercise the real HTTP and persistence boundaries in one deterministic test: BCrypt hash persisted through the application's user port, `POST /api/v1/auth/login`, response token extraction and `GET /api/v1/auth/me` authenticated by the returned token.
- Assert the login JSON contract is safe without asserting or logging the token value itself.
- Use fresh unique test data and the existing shared PostgreSQL container, leaving the production architecture unchanged.

**Non-Goals:**

- Reimplement JWT claim, malformed-token, role, inactive-user, controller-unit or direct-encoder scenarios already covered elsewhere.
- Modify authentication, security configuration, migrations, Flyway, frontend/BFF, database cleanup strategy or production code.

## Decisions

### 1. Test the existing protected endpoint `/api/v1/auth/me`

The protected request will target `/api/v1/auth/me`, which is the smallest real route requiring authentication and returns the persisted principal. It proves the resource-server filter, JWT decoder and persisted-user converter accept the token issued by the real login flow. This is preferred over a test-only protected probe because it validates an actual public API contract; it also avoids coupling the test to an unrelated catalog or financial capability.

### 2. Persist a BCrypt hash through `UsuarioPort`

The fixture will obtain the configured `PasswordEncoder`, encode a fixed valid plaintext password and save a `ROLE_USER` through `UsuarioPort`. This proves the login's configured BCrypt comparison against data that crossed the real persistence adapter. Supplying a raw synthetic hash or mocking authentication was rejected because either would bypass the behavior under validation.

### 3. Extract and reuse exactly the returned token

The test will parse `accessToken` from the login response and use that unchanged value in `Authorization: Bearer <token>` for `/me`. It will not create a token with `JwtEncoder`, use a hard-coded token or call a service directly. The response assertions will require `accessToken`, `tokenType=Bearer` and positive `expiresIn`, and require both password names (`senha`, `senhaHash`) to be absent.

### 4. Keep this as a behavior-verification test

The test is intentionally added after implementation exists. If it passes immediately, its record and task completion will state that it verified existing behavior rather than claiming a Red → Green TDD cycle. A failing result is evidence of a product defect and requires a separately scoped decision before any production fix.

## Risks / Trade-offs

- [The shared Testcontainers database can retain users from other ITs] → Use a UUID-derived canonical email and assert only the response associated with its own persisted user.
- [A JWT can accidentally appear in diagnostics] → Do not print response content or token values; only pass the parsed value to the protected request.
- [The test overlaps individual login and `/me` tests] → Keep the assertions restricted to the cross-boundary sequence they do not cover, not duplicated negative cases.

## Migration Plan

No deployment or schema migration is required. The only deliverables are the integration test and its OpenSpec artifacts. Rollback consists of reverting the added test/artifacts; production behavior is untouched.
