## Why

O frontend possui sessão segura, mas não uma experiência financeira autenticada de carteira. Esta etapa estabelece shell, qualidade visual e consultas financeiras precisas sem introduzir mutações operacionais; o refresh técnico explícito de valuation permanece permitido por materializar o snapshot existente.

## What Changes

- Cria área ROLE_USER da carteira com shell, resumo, posições, transações, movimentações, responsividade, acessibilidade e testes focados.
- Cria fundação global Light/Dark/System com tokens semânticos e `next-themes`; aplica o branding Valore e uma experiência premium e responsiva a login, cadastro e shell, preservando o admin funcional e legível.
- Cria BFF financeiro same-origin, estrito, `Cache-Control: no-store`, com correlação segura e normalização lossless de BigDecimal para strings decimais.
- Usa refresh técnico explícito de valuation, sem depósito, saque, BUY, SELL, formulários, seletores, Idempotency-Key operacional, gráficos ou dashboard analítico.
- Define `/` como redirect server-side para `/login`, `/carteira` ou `/admin` conforme a sessão confirmada, usa a landing natural de cada role após login sem `returnTo`, e mantém `/inicio` como compatibilidade server-side sem mudar a fronteira de sessão.
- Adiciona `next-themes` e `lossless-json`; não adiciona `decimal.js`, que fica para `establish-financial-operations-frontend`.

## Capabilities

### New Capabilities

- `financial-portfolio-shell-and-read-experience`: experiência financeira autenticada sem mutações operacionais, com leitura precisa e refresh técnico explícito.

### Modified Capabilities

- `frontend-authentication-session`: muda a landing protegida de ROLE_USER para `/carteira` e mantém `/inicio` como compatibilidade, sem alterar a fronteira de sessão.

## Impact

- Afeta somente artefatos futuros do frontend: App Router, BFF, estilos/tokens, componentes, TanStack Query e testes.
- Consome APIs backend existentes; não requer mudança de backend, PRD, archive ou Graphify.
