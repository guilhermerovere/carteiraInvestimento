## MODIFIED Requirements

### Requirement: Usuario persistido e protegido
The authenticated principal SHALL be able to update only its own trimmed nonblank name (1..255), canonical valid unique email, and password through dedicated self-service use cases. Password change SHALL require the current password, reuse the existing password policy and BCrypt strength 12, and never return password/hash. No request SHALL accept another user id, role or active flag.

#### Scenario: Persistencia de novo usuario
- **WHEN** um usuario valido e criado
- **THEN** todos os campos obrigatorios sao persistidos, o identificador e UUID, `ativo` e verdadeiro e a senha armazenada e um hash BCrypt strength 12

#### Scenario: Resposta HTTP segura
- **WHEN** qualquer endpoint retorna dados de usuario
- **THEN** a resposta nao contem senha nem `senha_hash`

#### Scenario: Principal updates profile
- **WHEN** an authenticated principal submits a valid name or email
- **THEN** only that persisted user is updated and the safe current-user representation is returned

#### Scenario: Current password is incorrect
- **WHEN** password change supplies a current password that does not match BCrypt
- **THEN** no hash changes and a safe conflict is returned without credential details

### Requirement: Safe self-service account closure
Account closure SHALL be logical, not physical. It SHALL require current password and explicit confirmation, lock/revalidate the principal wallet in one transaction, require `saldo_caixa_brl = 0`, and require no `posicoes.quantidade > 0`. Historical zero positions SHALL not block. On eligibility, the user SHALL become inactive, name/email SHALL be replaced by deterministic non-personal unique values derived from UUID, the credential SHALL be replaced by an unusable random BCrypt value, and financial/audit history SHALL remain referentially intact. Existing persisted-user authentication checks SHALL reject later login and previously issued JWT use. ROLE_ADMIN without a wallet MAY close its own account after password/confirmation because it has no personal cash or positions.

#### Scenario: Closure blocked by cash
- **WHEN** a ROLE_USER wallet has nonzero cash at transactional revalidation
- **THEN** closure is rejected without identity/history changes and the response carries a safe cash-not-zero code

#### Scenario: Closure blocked by open position
- **WHEN** any position for the wallet has quantity greater than zero
- **THEN** closure is rejected with a safe open-position code while a historical zero position would not block

#### Scenario: Eligible account closes
- **WHEN** current password/confirmation are valid and financial state is zero
- **THEN** the account is inactivated/anonymized atomically, history remains, and subsequent authentication fails
