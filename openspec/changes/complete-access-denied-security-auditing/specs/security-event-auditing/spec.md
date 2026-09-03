## MODIFIED Requirements

### Requirement: Eventos minimos de seguranca
O sistema SHALL auditar cadastro de usuario, login bem sucedido, login falho, tentativa de usuario inativo, acesso negado e criacao do administrador inicial, registrando resultado, severidade, endpoint e data/hora coerentes com o evento. Cadastro e criacao do administrador MUST persistir a auditoria na mesma transacao da criacao; login bem sucedido MUST persistir a auditoria antes de retornar sucesso; login falho, usuario inativo e acesso negado MUST usar transacao isolada. Uma negacao de autorizacao que produza `403 Forbidden` para um principal autenticado MUST registrar automaticamente `ACESSO_NEGADO`, com resultado `NEGADO`, severidade `ALERTA`, UUID do usuario autenticado, endpoint da requisicao e o correlation ID ja associado a ela. A falha dessa persistencia isolada MUST NOT substituir ou alterar a resposta `403` original, propagar erro de infraestrutura ao cliente ou produzir efeito externo alem de diagnostico operacional sanitizado.

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
