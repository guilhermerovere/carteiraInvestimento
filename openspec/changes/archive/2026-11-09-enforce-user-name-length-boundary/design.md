## Context

See `proposal.md` for the motivation. The identity specification already defines a trimmed, nonblank name with 1..255 characters. `V1__create_usuarios.sql` persists `usuarios.nome` as `VARCHAR(255)`. `Usuario.comNome` checks the boundary after trimming, but the compact constructor and `Usuario.novoUsuario` do not. Consequently, registration and first-admin provisioning can construct a 256-character user name. `RegisterRequest` originally had `@NotBlank` but no maximum-length constraint. After the first correction added `@Size(max = 255)`, review found that Bean Validation saw the raw JSON value while the domain checked the trimmed value: a valid 255-character name with outer spaces was rejected as 257 characters. The bootstrap service also returns an existing admin before it constructs a `Usuario`, so invalid configured names must be checked explicitly before the existing-user branch.

## Goals / Non-Goals

**Goals:**

- Make the existing 1..255-character rule authoritative in the domain creation path after trimming.
- Reject a registration name of 256 characters as the existing generic HTTP invalid-request response (`400`) before the application service is invoked.
- Make an overlong `ADMIN_NAME` fail before lookup, hashing, persistence, or audit creation, including when the configured e-mail already belongs to an administrator.
- Prove the behavior with one coherent TDD cycle spanning HTTP, domain, and bootstrap transaction boundaries.

**Non-Goals:**

- Change the existing identity requirement, `usuarios` schema, error payload contract, password/e-mail validation, or administrative-bootstrap semantics unrelated to the name boundary.
- Introduce a Flyway migration: the existing `VARCHAR(255)` schema is already the persistence boundary.

## Decisions

### Normalize and validate the name in `Usuario`

Extract the existing `comNome` logic into one domain helper that trims, rejects blank values, and rejects a trimmed length greater than 255. The `Usuario` construction path and `comNome` will both use it, so every new or changed aggregate observes the same rule and persisted valid values retain their normalized representation.

The alternative of enforcing only `@Size` on `RegisterRequest` would leave bootstrap and direct domain callers unprotected. Enforcing only the database column would make a client validation error dependent on persistence and cannot protect pre-persistence bootstrap work.

### Validate the registration DTO at the HTTP boundary

Keep the maximum length constraint on `RegisterRequest.nome`, preserving `@NotBlank`, and normalize a non-null `nome` in the record compact constructor before Bean Validation runs. The existing `@Valid` controller then returns the established `400` problem response and does not call `RegistrationService` for a name with 256 characters after `trim`, while it forwards a valid padded 255-character name in normalized form.

The DTO is a fast boundary check; the domain remains the authoritative check because not every creator is HTTP-driven. The compact constructor is necessary because applying `@Size` directly to the raw JSON string would change the trim-first semantics and reject otherwise valid padded input.

### Validate bootstrap input before all side effects

`InitialAdminBootstrapService.provision` will invoke the domain name normalizer before e-mail lookup, password hashing, saving, or auditing, and will pass its normalized result to `Usuario.novoUsuario`. This makes invalid environment configuration fail consistently whether the configured e-mail is new or already an admin.

Adding only a Bean Validation maximum to `AdminProperties` was rejected because the service can be invoked outside Spring property binding and the authoritative trim-first domain rule must still apply.

### One TDD loop with evidence and a mutation check

Create the three regression tests before production edits and run their focused Maven command. Save the unmodified failing console output as `openspec/changes/enforce-user-name-length-boundary/evidence/tdd-red.txt`, with the command and current commit/branch header, so the failure is reviewable. The tests are one behavior slice, not separate TDD cycles:

1. `RegistrationControllerTest` posts 256 `a` characters and expects `400`/`application/problem+json`, while verifying no registration-service invocation.
2. `IdentityDomainTest` creates a new user with 256 characters and expects `IllegalArgumentException`; a 255-character trimmed name remains valid.
3. `InitialAdminBootstrapIntegrationTest` provisions a unique configured e-mail with a 256-character name, expects the exception, and proves user count and audit count are unchanged (therefore no partial administrator, wallet, or audit is persisted).

Apply the smallest production change described above and rerun the same focused command, saving `tdd-green.txt`. Then temporarily relax the domain maximum to 256, rerun the domain/bootstrap subset and save `tdd-mutation-red.txt`; those tests must fail. Revert that temporary mutation with no residual diff, rerun the full focused command, and save `tdd-green-after-mutation.txt`. The final implementation must contain no mutation-only code or test weakening.

During final review, add an HTTP regression for 255 non-whitespace characters with outer spaces. It must receive `201`, return the normalized name, and verify that `RegistrationService` receives that normalized value. First save its expected red failure (`201` expected, `400` received) in `tdd-space-red.txt`. Then normalize `RegisterRequest.nome` in its compact constructor and save the focused green result in `tdd-space-green.txt`. The existing HTTP 256-character test and the domain/bootstrap tests retain the after-trim rejection coverage.

## Risks / Trade-offs

- [Bean Validation and the domain could drift later] → keep the numeric boundary explicit in both entry validation and the single domain helper; the shared focused regression suite detects divergence for the current boundary.
- [A test can observe pre-existing bootstrap records] → compare before/after counts for the unique test e-mail and audit table within the already initialized PostgreSQL test container.
- [Captured red output may include local Maven environment details] → retain only the command header and ordinary test console output; do not include environment secrets.

## Migration Plan

No database migration or data backfill is needed. Deploy with the application change; existing valid rows already fit `VARCHAR(255)`. Rollback is the normal application rollback because the schema and persisted data do not change.
