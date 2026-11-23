## Why

O fluxo de BUY anuncia descoberta para B3 e US, mas o seletor e a BFF fixam a consulta na B3. Além disso, a descoberta US usa apenas o catálogo de símbolos do Twelve Data e devolve uma prévia sem cotação, permitindo que uma prévia não financeiramente completa chegue à confirmação de cadastro.

## What Changes

- Adicionar seleção explícita de mercado B3 ou US ao seletor de ativos usado antes de BUY e propagar o mercado escolhido, sem inferi-lo do ticker, pelo cliente e pela BFF até `GET /api/v1/acoes/discovery`.
- Completar a descoberta US no adapter Twelve Data: para cada candidato US elegível, consultar `/quote` e só devolver a prévia se ela tiver símbolo canônico correspondente, venue US, moeda USD, timestamp válido e `close` decimal positivo.
- Tratar falhas de cotação US (resposta inválida, erro do provider, timeout, limitação ou indisponibilidade) como falha recuperável de descoberta, sem oferecer cadastro, sem persistir ativo e sem revelar chave ou payload do provider.
- Manter a confirmação enviando exclusivamente `ticker` e `mercado`; o backend continuará revalidando o ativo antes da persistência e o cadastro continuará separado do POST financeiro de BUY.

## Capabilities

### New Capabilities

_Nenhuma._

### Modified Capabilities

_Nenhuma. A capability `asset-catalog` já exige descoberta e prévia para B3 e US, cotação atual obrigatória para a prévia, confirmação somente com ticker/mercado, revalidação e ausência de efeitos financeiros no cadastro._

## Impact

- Backend: `TwelveDataQuoteAdapter`, contrato de discovery e seus testes de adapter/controller.
- Frontend/BFF: rota same-origin de discovery, cliente `financeApi`, `AssetSelector`, testes Vitest e mock de E2E.
- E2E: fluxo de BUY com AAPL inicialmente ausente do catálogo, prévia US cotada, confirmação de cadastro e posterior POST de transação.
- Sem mudança de API externa pública além de a BFF passar a aceitar/validar `mercado` em sua rota de discovery; sem alteração das regras de câmbio, autorização, transações ou credenciais.
