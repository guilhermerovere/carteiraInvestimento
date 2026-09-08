## Why

O catálogo global de ativos já define a identidade e a moeda estrutural dos instrumentos B3 e US, mas ainda não oferece cotações. Esta change estabelece a base backend isolada para obter, validar, armazenar historicamente e servir cotações frescas sem acoplar preço ao Ativo ou antecipar funcionalidades de carteira, câmbio ou frontend.

## What Changes

- Adicionar a capability backend-only de cotações de mercado para ativos B3 e US, separada do catálogo de Ativo.
- Obter cotações B3 via Brapi e cotações US via AlphaVantage com fallback para TwelveData, por adapters OpenFeign isolados atrás de uma port de aplicação.
- Persistir cada obtenção externa bem-sucedida em `historico_cotacoes`, com preço positivo, moeda, timestamps de mercado e ingestão, provider controlado e FK restritiva para `acoes`.
- Servir cotação atual com cache Caffeine por UUID do Ativo, TTL de 10 minutos, sem persistir em cache hit e sem usar histórico como stale fallback.
- Expor endpoints autenticados para cotação atual e histórico paginado, além de refresh manual administrativo que ignora o cache.
- Aplicar lifecycle, autorização, ProblemDetail, correlation ID e semântica sanitizada de erros externos coerentes com as capabilities existentes.
- Excluir USD/BRL, scheduler, fallback para histórico, transações, posições, frontend/BFF e auditoria operacional de provider.

## Capabilities

### New Capabilities

- `market-quotes`: obtenção, cache, histórico, APIs e tratamento de falhas de cotações B3 e US para ativos do catálogo global.

### Modified Capabilities

- Nenhuma.

## Impact

- Backend: novo domínio e ports de cotação, adapters OpenFeign/configuração de providers, persistência JPA/Flyway, cache Caffeine, serviços de aplicação e contratos HTTP sob `/api/v1/acoes/{id}`.
- Banco: nova tabela `historico_cotacoes` referenciando `acoes(id)`, sem alterar a tabela `acoes` para armazenar preço.
- Configuração: somente as variáveis necessárias para Brapi, AlphaVantage e TwelveData, com timeout externo único entre 3 e 5 segundos; `.env.example` e `docker-compose.yml` serão alinhados no momento da implementação.
- Dependências: reutiliza OpenFeign e Caffeine já presentes; não introduz WebClient, RestTemplate, SDK externo, Resilience4j, Redis ou chamadas reais em testes.
