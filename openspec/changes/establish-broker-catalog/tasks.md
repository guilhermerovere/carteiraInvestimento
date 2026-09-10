## 1. Providers e contratos privados

- [ ] 1.1 Configurar BrasilAPI CNPJ/CVM e ViaCEP com timeout 4s e verificar binding em teste.
- [ ] 1.2 Criar portas/modelos e adapters privados, verificando DTO/provider fora de domain/application.
- [ ] 1.3 Testar adapters em HTTP local sem internet: sucesso, 404 semantico, 5xx, timeout, decoding, ausente e excedido.

## 2. Schema, dominio e persistencia

- [ ] 2.1 Criar V7 com tabela, UUID, CHECK/UNIQUE/limites/timestamps e verificar Flyway/Hibernate/Testcontainers.
- [ ] 2.2 Implementar CNPJ/checksum, CEP/UF, manuais, imutabilidade, lifecycle e Clock/timestamps, verificando null/blank/limites/no-op.
- [ ] 2.3 Implementar port/adapter com flush/scope/ordem, traduzir so UNIQUE e verificar concorrencia uma linha/409/uma auditoria.

## 3. Compliance e fronteira transacional

- [ ] 3.1 Implementar pipeline local -> BrasilAPI CNPJ -> Receita -> CVM -> ViaCEP obrigatorio, verificando precedencia, CEP inconsistente e 502.
- [ ] 3.2 Classificar CNPJ ausente/Receita nao ativa/CVM sem ativo em 422 e tecnicas/limites/ViaCEP em 502, sem persistencia parcial.
- [ ] 3.3 Separar orquestrador nao transacional da persistencia+auditoria curta, verificando providers fora de transacao ativa e flush UNIQUE.

## 4. API, lifecycle e responses

- [ ] 4.1 Implementar PATCH ausencia/null/blank/vazio/no-op e lifecycle no-op, verificando sem UPDATE/timestamp/auditoria em no-op.
- [ ] 4.2 Criar seis endpoints estritos e verificar GET CNPJ somente `^[0-9]{14}$` com checksum, sem query alternativo/DELETE.
- [ ] 4.3 Implementar SelectionResponse USER/AdminResponse ADMIN, paginacao/ordem fixa e respostas de mutacao administrativas.

## 5. Seguranca, erros e auditoria

- [ ] 5.1 Verificar 401 anonimo, 403 USER, 404 inativa USER e sem acesso financeiro ampliado.
- [ ] 5.2 Implementar ProblemDetail sanitizado 400/404/409/422/502/500 e correlation ID, sem vazamento.
- [ ] 5.3 Implementar auditoria atomica de sucesso e isolada de rejeicao; verificar rollback e 500 se auditoria obrigatoria falhar.

## 6. OpenAPI e validacao

- [ ] 6.1 Documentar endpoints, roles, CNPJ path, PATCH/no-op, responses e erros 400/401/403/404/409/422/502/500.
- [ ] 6.2 Executar suites unitarias, HTTP local, API/security/Testcontainers sem internet publica.
- [ ] 6.3 Executar `npx.cmd openspec validate establish-broker-catalog --strict` e `git diff --check`.
