## MODIFIED Requirements

### Requirement: Eventos minimos de seguranca
In addition to existing events, successful email change, password change and account closure SHALL be audited transactionally for the current principal. Audit fields SHALL contain only event type/result/severity/endpoint/correlation/time and principal linkage needed for integrity. They MUST NOT contain old/new email, name values, password/hash, JWT, Authorization, request body, confirmation text, cash, positions or transaction values. Closure audit SHALL remain referentially linked while the identity is inactivated/anonymized.

#### Scenario: Cadastro concluido
- **WHEN** um usuario e cadastrado com sucesso
- **THEN** um evento de cadastro bem sucedido e persistido para esse usuario na mesma transacao do usuario e da carteira

#### Scenario: Login concluido ou rejeitado
- **WHEN** uma tentativa de login tem sucesso, falha por credenciais ou e rejeitada por usuario inativo
- **THEN** o evento correspondente e persistido com resultado e severidade coerentes, antes do token no sucesso e em transacao isolada na rejeicao

#### Scenario: Acesso negado
- **WHEN** um usuario autenticado tenta, pelo fluxo HTTP real, um recurso para o qual nao possui permissao e recebe `403 Forbidden`
- **THEN** um unico evento `ACESSO_NEGADO` e persistido isoladamente para o UUID do usuario, com resultado `NEGADO`, severidade `ALERTA`, endpoint da requisicao e o mesmo correlation ID da resposta

#### Scenario: Falha de auditoria durante acesso negado
- **WHEN** a persistencia isolada do evento de acesso negado falha
- **THEN** a requisicao ainda responde `403 Forbidden`, sem erro de infraestrutura ao cliente, com o mesmo status HTTP, media type `application/problem+json` e `ProblemDetail` sanitizado originalmente previstos, e somente diagnostico operacional sanitizado e produzido

#### Scenario: Administrador inicial criado
- **WHEN** o provisionamento cria o administrador inicial
- **THEN** um evento de criacao do administrador e persistido para ele na mesma transacao, com endpoint nulo e correlation ID gerado

#### Scenario: Sensitive account mutation is audited
- **WHEN** email/password change or account closure succeeds
- **THEN** exactly one sanitized event is committed with the mutation and contains no sensitive payload or financial state
