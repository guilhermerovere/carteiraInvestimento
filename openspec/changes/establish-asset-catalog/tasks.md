## 1. Schema e domínio

- [ ] 1.1 Criar `V4__create_acoes.sql` após V3 com `uk_acoes_ticker`, CHECK de ticker canônico, regex B3/US, tipo/mercado/moeda, coerência mercado/moeda, nome não blank e `VARCHAR(160)`; verificar migration e inserts diretos válidos/inválidos em PostgreSQL real.
- [ ] 1.2 Criar domínio `Ativo`, enums e canonicalizadores para ticker e nome, com moeda derivada, padrões por mercado, nome trimado 1–160, identidade imutável e lifecycle idempotente; verificar testes unitários.
- [ ] 1.3 Criar ports/use cases e escopo controlado de visibilidade aplicado antes do adapter, sem Spring Security no domínio; verificar user ACTIVE_ONLY, admin ALL/FILTERED e q trimado/contains/case-insensitive.

## 2. Persistência e concorrência

- [ ] 2.1 Implementar entidade, repository e adapter `AtivoPort` sem associações financeiras, com Hibernate validate; verificar contexto contra PostgreSQL.
- [ ] 2.2 Implementar `saveAndFlush` e tradução exclusiva de `uk_acoes_ticker` para duplicidade; verificar que CHECK e demais constraints não se tornam 409.
- [ ] 2.3 Criar testes Testcontainers para ticker canônico, minúsculo/espaçado direto rejeitado, regex por mercado, nome blank, coerência moeda, UNIQUE e concorrência; verificar um sucesso, um conflito e um único registro.

## 3. API, contratos e segurança

- [ ] 3.1 Criar requests estritos localizados: POST com ticker/nome/tipo/mercado, PATCH nome e PATCH lifecycle `{ativo}`; verificar campos extras/estruturais e payload inválido retornam 400/ProblemDetail.
- [ ] 3.2 Criar `AtivoResponse` e envelope próprio `{items,page,size,totalElements,totalPages}`; verificar POST=201, PATCHs=200, campos expostos e ausência de Page/JPA no contrato.
- [ ] 3.3 Implementar paginação page>=0, size 1–100, sort ticker/nome, direction asc/desc e q conforme contrato; verificar defaults, limites, busca ticker/nome parcial, caixa, espaços e q vazio.
- [ ] 3.4 Integrar roles existentes: GET para user/admin, mutação somente admin, user sem vazamento de inativos, admin sem ativo vê todos e 403 mantém auditoria existente; verificar 401/403/404 e correlation ID.
- [ ] 3.5 Adicionar testes MVC para lifecycle ativar/desativar/repetir idempotente, inexistente, requests estritos, paginação/envelope, erros 400/404/409 e ProblemDetail sanitizado.

## 4. Validação final

- [ ] 4.1 Executar suíte Maven unitária e Testcontainers/Flyway, incluindo concorrência e contratos HTTP; verificar sucesso.
- [ ] 4.2 Executar `npx.cmd openspec validate establish-asset-catalog --strict`; verificar artefatos coerentes sem alterar PRD, proposal, frontend/BFF ou capabilities existentes.
