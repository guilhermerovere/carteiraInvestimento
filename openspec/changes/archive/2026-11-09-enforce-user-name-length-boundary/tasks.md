## 1. Characterize the existing defect (red)

- [x] 1.1 Add one registration controller regression in `RegistrationControllerTest` that submits a JSON `nome` of exactly 256 non-whitespace characters, expects the existing `400` Problem Details response, and verifies `RegistrationService` has no interactions; add the required static Mockito verification import and verify the test fails before production changes.
- [x] 1.2 Add domain boundary regressions in `IdentityDomainTest`: `Usuario.novoUsuario` rejects a trimmed 256-character name with `IllegalArgumentException`, while a 255-character name remains valid and normalized; verify they fail before production changes.
- [x] 1.3 Add an `InitialAdminBootstrapIntegrationTest` regression that calls `provision` with a unique e-mail and a 256-character name, expects `IllegalArgumentException`, and compares before/after database counts for that e-mail and for audit records so no user, wallet, or bootstrap audit event is partially persisted; verify it fails before production changes.
- [x] 1.4 Run `cd backend; .\mvnw.cmd -Dtest=RegistrationControllerTest,IdentityDomainTest,InitialAdminBootstrapIntegrationTest test` before production edits; create `openspec/changes/enforce-user-name-length-boundary/evidence/` and save the command, branch/commit header, and unmodified failing console output as `tdd-red.txt`.

## 2. Apply the smallest green correction

- [x] 2.1 Add the `RegisterRequest.nome` maximum-length Bean Validation constraint alongside `@NotBlank`; verify task 1.1 receives `400` and the mocked registration service is untouched.
- [x] 2.2 Centralize trim, blank, and maximum-255 validation in `Usuario` and use it for aggregate creation as well as `comNome`; verify task 1.2 passes and existing profile-name behavior stays trim-first.
- [x] 2.3 Invoke the same domain validation at the start of `InitialAdminBootstrapService.provision`, before e-mail lookup, password hashing, saving, or auditing; pass the normalized name into new-admin creation and verify task 1.3 passes with unchanged persistence counts.
- [x] 2.4 Run `cd backend; .\mvnw.cmd -Dtest=RegistrationControllerTest,IdentityDomainTest,InitialAdminBootstrapIntegrationTest test` and save the complete successful output as `openspec/changes/enforce-user-name-length-boundary/evidence/tdd-green.txt`.

## 3. Prove the tests detect a weakened rule

- [x] 3.1 Temporarily change only the domain maximum from 255 to 256, run `cd backend; .\mvnw.cmd -Dtest=IdentityDomainTest,InitialAdminBootstrapIntegrationTest test`, and save the expected failure output as `evidence/tdd-mutation-red.txt`; verify the domain and bootstrap boundary regressions fail.
- [x] 3.2 Revert the temporary mutation without weakening tests, rerun `cd backend; .\mvnw.cmd -Dtest=RegistrationControllerTest,IdentityDomainTest,InitialAdminBootstrapIntegrationTest test`, and save the green output as `evidence/tdd-green-after-mutation.txt`; verify the final source still enforces 255.

## 4. Verify the completed change

- [x] 4.1 Run the relevant backend test suite (`cd backend; .\mvnw.cmd test`) after the focused TDD cycle and record the result in the change evidence; verify no unrelated test regresses.
- [x] 4.2 Run `cd backend; .\mvnw.cmd clean verify` to execute Failsafe and the consolidated JaCoCo gate after the focused TDD cycle; record the result in the change evidence and verify both phases pass.
- [x] 4.3 Review the final diff to confirm production changes are limited to request validation, shared domain validation, and pre-side-effect bootstrap validation; confirm no Flyway migration, Graphify artifact, staging, commit, push, or merge was created.

## 5. Resolve review finding: trim before HTTP length validation

- [x] 5.1 Add the `RegistrationControllerTest` regression for 255 non-whitespace characters with outer spaces; require `201`, a normalized response name, and a normalized `RegistrationService` argument. Run it before production changes and record the expected `201`-versus-`400` failure in `evidence/tdd-space-red.txt`.
- [x] 5.2 Normalize non-null `RegisterRequest.nome` in its compact constructor before `@NotBlank` and `@Size(max = 255)` run. Run the HTTP, domain, and bootstrap boundary tests and record the successful result in `evidence/tdd-space-green.txt`; retain the existing 256-character rejection after `trim`.
- [x] 5.3 Rerun `cd backend; .\mvnw.cmd clean verify`, inspect the final diff and evidence for secrets, run `git diff --check`, and validate the change with `npx.cmd openspec validate enforce-user-name-length-boundary --strict`.
