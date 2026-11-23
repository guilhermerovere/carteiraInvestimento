## Why

The registration flow currently accepts a user name longer than the `usuarios.nome VARCHAR(255)` persistence contract. The identity specification already requires a trimmed, nonblank name of 1..255 characters, so this is an enforcement gap that can defer rejection to persistence rather than returning the required client error.

## What Changes

- Enforce the existing 255-character name boundary at HTTP request validation and in the `Usuario` domain creation path.
- Make administrative bootstrap validate an overlong configured name before it can hash, save, or audit an administrator.
- Add focused regression coverage for HTTP `400`, domain rejection, and bootstrap fail-fast with no persisted user, wallet, or audit event.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. `skip_specs: true` is intentional: `identity-and-primary-wallet` already requires a trimmed, nonblank name of 1..255 characters, and this change only makes every existing entry path honor that rule.

## Impact

Affected backend areas are the registration request DTO, `Usuario` name validation, the initial-admin bootstrap path, and their focused controller, domain, application/bootstrap, and transaction tests. The public endpoint behavior for an invalid overlong name becomes the existing invalid-request response (`400`); no schema or migration change is required because `V1__create_usuarios.sql` already defines `nome VARCHAR(255)`.
