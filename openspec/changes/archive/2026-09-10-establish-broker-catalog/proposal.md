## Why

As compras e vendas futuras exigem uma corretora global, previamente validada e ativa; o sistema ainda nao possui esse catalogo nem a verificacao regulatoria que impede o uso de instituicoes nao conformes. Esta change estabelece essa base sem introduzir transacoes financeiras ou ownership de carteira.

## What Changes

- Adicionar catalogo global de corretoras administrado por `ROLE_ADMIN`, com CNPJ canonico, unico e imutavel.
- Consultar BrasilAPI CNPJ, BrasilAPI CVM e ViaCEP antes de persistir dados oficiais e endereco enriquecido; distinguir bloqueio regulatorio (`422`) de indisponibilidade tecnica upstream (`502`).
- Adicionar lifecycle local idempotente, leitura restrita de inativas para `ROLE_USER`, RBAC, auditoria sanitizada, API paginada, persistencia PostgreSQL/Flyway e contrato OpenAPI.
- Preparar a integridade referencial conceitual para a futura `transacoes.corretora_id`, sem criar Transacao, BUY, SELL, Posicao, frontend, BFF ou DELETE de corretora.

## Capabilities

### New Capabilities

- `broker-catalog`: Catalogo global de corretoras com compliance externo, lifecycle, consultas seguras e administracao auditada.

### Modified Capabilities

- Nenhuma.

## Impact

- Backend nas camadas domain, application, infrastructure e presentation; migration Flyway forward-only apos V6 e Hibernate em modo validate.
- Novos adapters OpenFeign privados para BrasilAPI CNPJ, BrasilAPI CVM e ViaCEP, seguindo o timeout global atual de quatro segundos.
- Seis endpoints `/api/v1/corretoras`, seus DTOs, ProblemDetail e documentacao OpenAPI; a seguranca existente continua respondendo 401/403 e auditando acesso negado.
- Testes unitarios, de adapters, API, seguranca e PostgreSQL/Testcontainers para o novo catalogo, sem modificar contratos das capabilities existentes.
