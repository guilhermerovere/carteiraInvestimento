## 1. Schema e migrações PostgreSQL

- [ ] 1.1 Criar migração forward-only do ledger, NUMERIC(18,2), checks financeiros, índices e tipos; verificar com Flyway e PostgreSQL real
- [ ] 1.2 Adicionar UNIQUE auxiliar e FK composta carteira_id/usuario_id; verificar rejeição direta de carteira A + usuário B
- [ ] 1.3 Fechar PK UUID de carteira_snapshots e UNIQUE(carteira_id,data_referencia); verificar uma linha por carteira/data e upsert concorrente
- [ ] 1.4 Criar tabela/constraints de idempotência por carteira, tipo e key com fingerprint e resultado original; verificar rollback da reserva

## 2. Domínio e aplicação

- [ ] 2.1 Implementar BigDecimal normalizado, descrição trim/blank-null e SHA-256 canônico UTF-8 versionado com tipos/length-prefix; verificar equivalência 100/100.0/100.00 e ausência de colisão por delimitadores
- [ ] 2.2 Capturar um único operationInstant do Clock para movimento, auditoria permitida e snapshot na zona configurada; verificar meia-noite e timezone do host
- [ ] 2.3 Validar overflow do saldo resultante como 409 sanitizado; verificar unitário e integração com saldo próximo ao limite
- [ ] 2.4 Resolver carteira primária pelo SecurityContext/JWT; verificar ROLE_USER sem carteira como 500 sem fallback

## 3. Idempotência e transação

- [ ] 3.1 Implementar reserva ON CONFLICT DO NOTHING RETURNING (ou equivalente), sem continuar após unique violation; verificar espera por commit concorrente e retry após rollback com Testcontainers
- [ ] 3.2 Ordenar fluxo validar/capturar/reservar/mutar/persistir movimento/snapshot/auditoria/resultado e responder só após commit; verificar propagação transacional
- [ ] 3.3 Reverter reserva em saldo insuficiente, overflow e falhas de movimento/snapshot/auditoria; verificar retries sequenciais com mesma key
- [ ] 3.4 Implementar replay 201 determinístico com timestamp e saldoResultante originais, sem novos efeitos; verificar saldo posterior não é reutilizado
- [ ] 3.5 Testar concorrência idêntica (dois 201, um efeito) e concorrência divergente (vencedor aplica, perdedor 409 sem parcial)
- [ ] 3.6 Testar dois saques de 80 sobre saldo 100: um 201, um 409, saldo 20, um movimento/snapshot/auditoria e nenhuma reserva residual rejeitada

## 4. Persistência, snapshot e auditoria

- [ ] 4.1 Persistir ledger imutável e atualizar somente snapshot do dia com saldo/patrimônio resultantes e demais campos 0.00; verificar replay/rollback e datas anteriores
- [ ] 4.2 Registrar DEPOSITO/SAQUE na mesma transação, sem REQUIRES_NEW/AuditoriaIsoladaPort; verificar rollback remove evento e metadados não contêm dados financeiros, key ou fingerprint
- [ ] 4.3 Garantir histórico dataHora DESC,id DESC, paginação 0/20 e 1..100; verificar vazio 200 items=[] e inválidos 400

## 5. API, segurança e contratos

- [ ] 5.1 Implementar quatro endpoints sem usuarioId/carteiraId, com ROLE_ADMIN=403 e anônimo=401; verificar ownership
- [ ] 5.2 Restringir DTOs POST a valor/descricao localmente e rejeitar desconhecidos sem alterar JSON global; verificar capabilities existentes
- [ ] 5.3 Validar Idempotency-Key ausente/vazia/espaços/formato como 400 sem trim, eco ou logging; verificar regex 1..128
- [ ] 5.4 Fixar corpos 201/200 sem carteira/usuário/key/fingerprint/JPA/auditoria/snapshot; verificar replay original
- [ ] 5.5 Mapear 400/401/403/409/500 a ProblemDetail sanitizado com X-Correlation-ID; verificar ausência de SQL/stack trace/IDs privados

## 6. Validação final

- [ ] 6.1 Executar `./mvnw.cmd verify` no backend e corrigir falhas sem ampliar escopo
- [ ] 6.2 Executar `npx.cmd openspec validate establish-cash-movements --strict` na raiz
- [ ] 6.3 Executar `git diff --check` e `git status`; revisar backend-only e ausência de frontend/BFF, COMPRA/VENDA, Transacao, Posicao, FX, market-quotes e PUT/PATCH/DELETE
