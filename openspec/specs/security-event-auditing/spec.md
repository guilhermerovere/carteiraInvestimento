# Security Event Auditing Specification

## Purpose

Estabelece uma trilha persistida, correlacionavel e sanitizada para eventos de seguranca, permitindo investigacao operacional sem expor credenciais ou informacoes financeiras privadas.

## Requirements

### Requirement: Registro persistido de auditoria de seguranca
O sistema SHALL persistir cada evento de seguranca em `logs_auditoria` com `id` UUID, `usuario_id` nullable, `tipo_evento`, `resultado`, `severidade`, `endpoint` nullable, `correlation_id` e `data_hora`. Quando nenhum usuario persistido puder ser identificado, `usuario_id` SHALL ser `NULL`. Quando o evento nao tiver origem HTTP, `endpoint` SHALL ser `NULL`.

#### Scenario: Evento associado a usuario conhecido
- **WHEN** um evento auditavel identifica com seguranca um usuario persistido
- **THEN** o registro contem seu UUID em `usuario_id` e os demais metadados obrigatorios

#### Scenario: Evento sem usuario identificado
- **WHEN** uma tentativa de login usa e-mail inexistente ou nenhum usuario persistido pode ser identificado
- **THEN** o evento e persistido com `usuario_id` igual a `NULL`

#### Scenario: Correlacao de evento
- **WHEN** um evento auditavel ocorre durante uma requisicao
- **THEN** o registro possui `correlation_id` que permite correlaciona-lo com a requisicao sem armazenar seu corpo completo, usando `X-Correlation-ID` somente quando for UUID valido e limitado ou UUID gerado nos demais casos

#### Scenario: Evento de sistema sem requisicao HTTP
- **WHEN** a criacao do administrador inicial gera auditoria
- **THEN** o registro possui `endpoint` nulo e `correlation_id` UUID gerado

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

### Requirement: Sanitizacao obrigatoria
Logs da aplicacao e auditoria persistida MUST NOT registrar senha, `senha_hash`, JWT, header `Authorization`, credenciais, corpo HTTP completo, saldo, posicoes, quantidades, valores de transacoes ou outros dados financeiros privados.

#### Scenario: Inspecao de eventos e logs
- **WHEN** registros gerados pelos fluxos de cadastro, login, inatividade, acesso negado e bootstrap sao inspecionados
- **THEN** nenhum dado proibido esta presente nos campos, mensagens ou metadados

#### Scenario: Login falho
- **WHEN** um login falha
- **THEN** a auditoria registra somente metadados necessarios ao evento e nao inclui a senha recebida, token, corpo completo ou credenciais equivalentes

### Requirement: Ausencia de consulta de auditoria nesta etapa
O sistema MUST NOT expor endpoint de consulta de `logs_auditoria` como parte desta capability inicial.

#### Scenario: Inspecao da superficie HTTP
- **WHEN** as rotas introduzidas por esta change sao enumeradas
- **THEN** nao existe rota nova para consulta de auditoria por usuario ou administrador
