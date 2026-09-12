## Why

O frontend possui sessão segura, mas não uma experiência financeira autenticada de carteira. Esta etapa estabelece shell, qualidade visual e consultas financeiras precisas sem introduzir mutações operacionais; o refresh técnico explícito de valuation permanece permitido por materializar o snapshot existente.

## What Changes

- Cria área ROLE_USER da carteira com shell, resumo, posições, transações, movimentações, responsividade, acessibilidade e testes focados.
- Cria fundação global Light/Dark/System com tokens semânticos e `next-themes`; a experiência visual completa é aplicada à carteira, enquanto login e admin permanecem funcionais, legíveis e não redesenhados.
- Cria BFF financeiro same-origin, estrito, `Cache-Control: no-store`, com correlação segura e normalização lossless de BigDecimal para strings decimais.
- Usa refresh técnico explícito de valuation, sem depósito, saque, BUY, SELL, formulários, seletores, Idempotency-Key operacional, gráficos ou dashboard analítico.
- Define `/carteira` como landing de ROLE_USER e `/inicio` como compatibilidade server-side, sem mudar a fronteira de sessão ou ROLE_ADMIN.
- Adiciona `next-themes` e `lossless-json`; não adiciona `decimal.js`, que fica para `establish-financial-operations-frontend`.

## Capabilities

### New Capabilities

- `financial-portfolio-shell-and-read-experience`: experiência financeira autenticada sem mutações operacionais, com leitura precisa e refresh técnico explícito.

### Modified Capabilities

- `frontend-authentication-session`: muda a landing protegida de ROLE_USER para `/carteira` e mantém `/inicio` como compatibilidade, sem alterar a fronteira de sessão.

## Impact

- Afeta somente artefatos futuros do frontend: App Router, BFF, estilos/tokens, componentes, TanStack Query e testes.
- Consome APIs backend existentes; não requer mudança de backend, PRD, archive ou Graphify.
