## Why

O produto precisa de uma fonte global e estável para identificar instrumentos financeiros antes de introduzir cotações, transações e posições. Sem esse catálogo, os módulos financeiros futuros não terão uma referência canônica, única e com lifecycle preservável.

## What Changes

- Introduz o catálogo global backend-only de `Ativo`, persistido na tabela PostgreSQL `acoes` para aderência ao PRD.
- Suporta inicialmente os tipos `ACAO`, `FII` e `ETF`, nos mercados `B3` e `US`, com moeda derivada e coerente (`BRL` para B3; `USD` para US).
- Define ticker canônico globalmente único, com normalização trim/uppercase e formato inicial limitado por mercado.
- Expõe `/api/v1/acoes` para consulta paginada e por ticker a usuários autenticados, e criação, edição limitada de nome e lifecycle a administradores.
- Preserva a identidade estrutural do instrumento: ticker, tipo, mercado e moeda não podem ser alterados; lifecycle usa ativação/desativação, sem DELETE físico.
- Inclui migration Flyway, constraints PostgreSQL, integração com segurança e ProblemDetail existentes, e cobertura de domínio, aplicação, MVC, persistência/Testcontainers e concorrência.
- Exclui frontend, BFF, cotações, provedores externos, FX, transações, posições, patrimônio e auditoria operacional de CRUD.

## Capabilities

### New Capabilities

- `asset-catalog`: catálogo global de ativos, sua identidade, lifecycle, acesso autorizado e contratos HTTP backend.

### Modified Capabilities

- Nenhuma.

## Impact

- Backend: novo domínio, ports, serviços, adaptador JPA, controller e contratos de `Ativo`; extensão pontual de autorização e tradução de erros, preservando a infraestrutura JWT, `ProblemDetail` e correlação existentes.
- Banco: migration forward-only `V4__create_acoes.sql` (ou nome sequencial equivalente confirmado no momento da implementação) para a tabela `acoes` e suas constraints.
- API: novos endpoints autenticados sob `/api/v1/acoes`; não há mudança de frontend, BFF, dependências externas ou do PRD.
