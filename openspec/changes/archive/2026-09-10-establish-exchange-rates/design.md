## Context

O backend já usa OpenFeign com timeout global de quatro segundos, Caffeine, PostgreSQL/Flyway, Clock, ProblemDetail/correlation ID, auditoria isolada e coordenação local em `market-quotes`. FX não reutilizará `Cotacao`: é uma observação global por par, com escala, persistência e confirmação futura próprias. O atual adapter TwelveData de cotações usa `/quote`, mas a capability FX usará exclusivamente `/exchange_rate`.

## Goals / Non-Goals

**Goals:**

- Fornecer observação USD/BRL persistida, com taxa de escala 8 e proveniência temporal verificável.
- Alinhar cache e contrato futuro de `exchangeRateId` à mesma deadline absoluta.
- Isolar adapters/classificadores de provider e preservar os contratos HTTP e de erros existentes.

**Non-Goals:**

- Outros pares, BRL/BRL, transações, posições, caixa USD, valuation, frontend, histórico público, refresh manual, stale cache, Redis ou lock distribuído.

## Decisions

### Modelo FX, timestamps e precisão

O domínio FX terá observação imutável com `id`, `moedaOrigem`, `moedaDestino`, `taxa`, `provider`, `instanteCotacao` e `registradoEm`; seus ports serão próprios e não referenciarão Ativo/Cotacao. `instanteCotacao` é metadado de proveniência: AlphaVantage fornece `6. Last Refreshed` mais `7. Time Zone` no bloco `Realtime Currency Exchange Rate`; o adapter valida ambos e converte deterministicamente, sem timezone da JVM. TwelveData `/exchange_rate` fornece `symbol`, `rate` e `timestamp`, sendo este timestamp Unix da taxa; adapter valida par e converte `timestamp` para Instant. Ausência/invalidade temporal rejeita a resposta; Clock nunca preenche `instanteCotacao`.

`registradoEm` é gerado uma vez pelo backend via Clock quando a observação externa validada é aceita para persistência. A taxa é parseada somente de string/representação decimal segura para BigDecimal, validada positiva, normalizada uma única vez por `setScale(8, HALF_EVEN)` e então novamente validada positiva e representável em `NUMERIC(18,8)`. Mais de oito casas é aceitável quando normaliza; valor positivo que arredonda a `0.00000000` e overflow final são rejeitados. Não há double/float nem arredondamento monetário BRL.

### Deadline única, cache e concorrência

A deadline funcional é `registradoEm + 5 minutos`; é válida com `now <= deadline` e expirada com `now > deadline`, usando Clock server-side. O futuro `exchangeRateId` usa exclusivamente essa regra, nunca `instanteCotacao`. O cache Caffeine `USD:BRL` usa expiração de cinco minutos como retenção auxiliar, mas cada hit é explicitamente validado contra a deadline absoluta antes de retornar. Se expirado funcionalmente, é invalidado e inicia nova resolução: `expireAfterWrite` isoladamente não pode acrescentar cinco minutos após o commit/cache.put. Clock e Ticker controláveis tornam o teste determinístico, sem sleeps.

O service consulta cache, coordena por `USD:BRL` num `ConcurrentHashMap`, e relê cache dentro do lock. Cache hit preserva exatamente id, moedas, taxa, provider, instanteCotacao e registradoEm, sem nova chamada ao provider nem nova linha. Isto coordena uma instância; múltiplas instâncias não são coordenadas e isso é aceitável no escopo atual.

### Providers e taxonomia semântica

Alpha usa `/query?function=CURRENCY_EXCHANGE_RATE&from_currency=USD&to_currency=BRL&apikey=...`; Twelve usa `/exchange_rate?symbol=USD/BRL&apikey=...`, nunca `/currency_conversion`. URLs/keys continuam configuráveis e DTOs/classificadores são privados da infraestrutura.

Cada adapter classifica primeiro campos semânticos conhecidos de erro/rate limit/credencial, depois estrutura, par, taxa e timestamp, e também considera status HTTP, timeout, conexão e decode. Portanto HTTP 200 não significa sucesso: rate limit em payload é elegível; payload inequívoco de API key inválida é não elegível; ausência de taxa, taxa não positiva ou timestamp inútil são elegíveis. API key ausente ou 400/401/403 inequivocamente de credencial/configuração encerra a cadeia sem fallback contínuo. Timeout, conexão, 429, 5xx, decode, par temporariamente indisponível e payload semanticamente inutilizável acionam Twelve. Qualquer conclusão sem observação válida torna-se falha FX sanitizada 502; falha interna independente, inclusive persistência quando mapeada pelo padrão existente, é 500.

### Persistência, transação e auditoria

V8 criará `historico_cambio(id UUID PK, moeda_origem CHAR(3), moeda_destino CHAR(3), taxa NUMERIC(18,8), provider VARCHAR, instante_cotacao TIMESTAMPTZ, registrado_em TIMESTAMPTZ)`, todos NOT NULL, com checks fixos USD/BRL, taxa positiva e providers conhecidos; o índice será `(moeda_origem, moeda_destino, instante_cotacao DESC, registrado_em DESC, id DESC)`. Não haverá UNIQUE: observações externas reais distintas podem coincidir.

Fluxo: HTTP externo fora de transação → classificação/validação/normalização → bean Spring transacional separado ou TransactionTemplate → save → flush → commit → retorno ao orquestrador → cache.put. Não usar self-invocation para presumir proxy transacional. Falha/rollback de persistência não atualiza cache.

Auditoria isolada, curta e best-effort é tentada quando a resolução termina sem nenhuma observação USD/BRL válida por integração, inclusive falha elegível seguida de fallback falho ou Alpha não elegível que encerra a cadeia. Não é criada para consulta, hit, Alpha bem-sucedido ou fallback Twelve bem-sucedido; falha de auditoria não substitui o 502. O evento e logs não incluem key, headers, Authorization, payload bruto, segredo ou dados privados.

### Borda HTTP e integração futura

Há somente GET `/api/v1/cambio/usd-brl`; response contém `id`, moedas, `taxa`, `instanteCotacao`, `provider`, `registradoEm`, preservando semanticamente escala 8. USER/ADMIN têm 200, anônimo 401; 502 e 500 usam ProblemDetail sanitizado e correlation ID. Não há mutação, refresh ou histórico público.

Futura BUY/SELL US consulta essa rota, exibe a observação retornada e envia somente `exchangeRateId`; taxa numérica do browser não é autoridade. O id será obrigatório para US, dispensado para B3. A futura Transacao, com Clock server-side, valida existência, par e `now <= registradoEm + 5 minutos`; inadequado/expirado será 409 e a taxa será copiada a `taxaCambioBrl` em snapshot imutável.

## Risks / Trade-offs

- [Provider altera payload sem mudar HTTP] → classificadores semânticos e MockWebServer cobrem 200 de erro, taxa e timestamp inválidos.
- [Cache vive após deadline por atraso pós-commit] → deadline explícita no hit, com Clock/Ticker controlável.
- [Self-invocation elimina transação] → bean separado/TransactionTemplate e teste de fronteira transacional.
- [Auditoria indisponível] → isolada e best-effort, sem alterar o erro principal.

## Migration Plan

1. Implementar V8 somente por Flyway, sem alterar V1–V7; Hibernate valida o schema.
2. Configurar provider por ambiente sem versionar credenciais.
3. Rollback da aplicação é seguro: schema aditivo e histórico imutável permanecem.
