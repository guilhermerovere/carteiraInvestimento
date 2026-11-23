## Why

O cadastro controlado de ativos B3 usa hoje o payload de cotação da Brapi também para identidade e classificação; payloads reais de `/api/v2/stocks/quote` podem omitir `type`, o que induz classificação por formato do ticker e rejeita ou classifica incorretamente instrumentos como units, FIIs e ETFs. A correção é necessária para cumprir o PRD — metadados derivados e validados no servidor, sem persistência diante de inconsistência — e o contrato vigente de descoberta com prévia enriquecida.

## What Changes

- Accept both observed Brapi identity envelopes for `/api/v2/tickers`: the legacy `stocks[]` fields and the current `results[]` fields (`symbol`, `subType`, B3/BRL context), normalizing them before classification.

- Usar `/api/v2/tickers` como fonte autoritativa de identidade e subtipo B3 e manter `/api/v2/stocks/quote` como fonte autoritativa de cotação.
- Mapear explicitamente os subtipos Brapi `unit` e `stock` para `ACAO`, `fii` para `FII` e `etf` para `ETF`; rejeitar com segurança qualquer subtipo reconhecido mas não suportado.
- Cruzar ticker/mercado/moeda retornados pelas fontes de identidade e cotação e impedir a persistência quando houver divergência ou payload insuficiente.
- Entregar a prévia de discovery exigida pelo catálogo com ticker e nome canônicos, tipo, mercado, moeda, branding opcional e cotação atual, mantendo o POST de registro restrito a `ticker` e `mercado`.
- Alinhar backend, BFF e frontend para apresentar a prévia antes da confirmação do cadastro, sem criar posição, transação ou movimentação de caixa.
- Cobrir a regressão com fixtures representativas dos payloads reais sem `type` em quote para SANB11, HGLG11, BOVA11 e PETR4, seguindo TDD Red → Green → Refactor.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `asset-catalog`: corrige a resolução de identidade e classificação B3 e torna explícito o contrato da prévia enriquecida antes do registro controlado.

## Impact

- Backend: adapter/cliente Brapi, DTOs de provider, portas e serviço de discovery/validação, contrato HTTP de discovery e testes unitários/de integração.
- BFF: rota de discovery do catálogo, validação/mapeamento do payload e contratos TypeScript.
- Frontend: cliente financeiro, seletor/página de ativos e testes de interação da prévia e confirmação.
- Integrações: duas responsabilidades Brapi deliberadamente separadas (`/api/v2/tickers` para identidade; `/api/v2/stocks/quote` para preço), sem nova migration, dependência ou segredo.
