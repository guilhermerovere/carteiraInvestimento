# portfolio-valuation-and-net-worth Specification

## Purpose

Define valuation atual completa da carteira em BRL e sua materialização diária explícita, com precisão financeira, proveniência externa, consistência local e concorrência verificável.

## Requirements

### Requirement: Fórmulas, posições participantes e totais
O sistema SHALL usar exclusivamente BigDecimal. B3: `valorAtualOrigemExato=quantidade*cotacaoAtualBrl` e `valorAtualBrl=round2_HALF_EVEN(valorAtualOrigemExato)`; US: `valorAtualOrigemExato=quantidade*cotacaoAtualUsd` e `valorAtualBrl=round2_HALF_EVEN(valorAtualOrigemExato*taxaAtualUsdBrl)`. Por posição aberta, lucro não realizado é valor BRL menos total investido e rentabilidade é `round4_HALF_EVEN(lucro*100/totalInvestido)`. Agregados somam valores BRL materializados; rentabilidade agregada é null quando investimento é zero. Overflow derivado SHALL retornar 409. Posições zeradas não participam de lista, valor, investimento, lucro não realizado ou rentabilidade; seu lucro realizado acumulado SHALL participar do agregado histórico de todas as posições persistidas e nunca será somado ao patrimônio.

#### Scenario: Realizado de posição aberta e zerada
- **WHEN** uma posição zerada e uma aberta possuem lucro realizado acumulado
- **THEN** o resumo soma ambos, mas só a aberta integra valuation atual

### Requirement: Leitura consistente e valuationInstant preciso
Cada tentativa SHALL abrir uma transação read-only REPEATABLE_READ que captura na mesma fotografia carteira, saldo, estadoVersao, posições abertas, custos, dados locais de ativos e lucro realizado agregado de todas as posições; ela SHALL terminar antes da primeira chamada a MarketQuoteUseCase ou CambioUseCase. Após seu fechamento e antes de resolução externa, SHALL capturar uma única vez `valuationInstant`, normalizá-lo a microssegundos antes de response, comparação e persistência, e usá-lo como instante lógico da tentativa, não como timestamp de provider.

#### Scenario: Fronteira de provider
- **WHEN** provider é invocado durante valuation
- **THEN** a transação read-only inicial já terminou e não há lock financeiro mantido

### Requirement: Quotes, FX e falha integral
Para cada posição aberta, valuation SHALL invocar market-quotes atual; HistóricoCotacao não é stale fallback. Ativo inativo com custódia aberta permanece avaliável pela leitura interna restrita. Se houver US, SHALL invocar CambioUseCase exatamente uma vez por tentativa e expor uma única resolução raiz; B3 e carteira vazia não acessam FX. Falha necessária de quote ou FX SHALL abortar toda valuation com 502, sem response parcial ou snapshot.

#### Scenario: Várias US
- **WHEN** existem várias posições US abertas
- **THEN** todas usam uma única resolução FX na tentativa e `cambioAtual` não é repetido nas posições

### Requirement: Retry completo e validação final
A tentativa fará leitura consistente, valuationInstant, quotes, FX, cálculo e então transação curta com Carteira FOR UPDATE para comparar estadoVersao. Se divergir, SHALL descartar integralmente estado local, instant, quotes, FX e cálculos, liberar a transação e realizar exatamente uma segunda tentativa completa, invocando novamente os use cases mesmo que caches respondam eficientemente. Se a segunda divergência ocorrer, SHALL retornar 409; nunca recalculará com providers antigos sob FOR UPDATE. Valuation GET/POST não incrementa versão.

#### Scenario: Retry perde versão
- **WHEN** estado muda durante a primeira tentativa
- **THEN** retry usa novo valuationInstant e novas invocações de quote/FX; nova perda retorna 409

### Requirement: Snapshot, empty e ordenação estrita
V10 SHALL admitir sem consulta cross-table: (1) desconhecido: trio valor/lucro/patrimônio e instant nulos; (2) local sem posições: investimento/valor/lucro zero, patrimônio=saldo e instant nulo permitido; (3) materializado: trio não nulo e instant não nulo, inclusive zeros de POST explícito vazio. Estado parcial MUST ser rejeitado e V6–V9 preservado. GET vazio retorna zeros, rentabilidade null, câmbio null, posições vazias e valuationInstant da tentativa sem persistir. POST vazio faz o mesmo sem providers e materializa zeros com instant. POST só substitui campos de valuation se snapshot não tiver instant ou `novoInstant > persistido`; se menor ou igual após normalização micros, mantém a valuation persistida.

#### Scenario: Empate temporal
- **WHEN** POST posterior conclui com valuationInstant igual ao persistido após normalização em microssegundos
- **THEN** não substitui os campos de valuation existentes

#### Scenario: Instant maior ou menor
- **WHEN** novo instant é maior ou menor que o persistido
- **THEN** maior substitui e menor não substitui

### Requirement: API strict, response, segurança e documentação
O sistema SHALL expor exatamente GET `/api/v1/carteira/resumo` e POST `/api/v1/carteira/resumo/atualizar`; POST não possui corpo e ambos rejeitam body malformado ou query params inesperados com 400. Ambos usam somente SecurityContext, exigem ROLE_USER, retornam 401 anônimo e 403 ROLE_ADMIN, e usam ProblemDetail sanitizado/X-Correlation-ID para 400/401/403/409/502. GET não persiste; POST materializa. A raiz contém exatamente valuationInstant, saldoCaixaBrl, totalInvestidoBrl, valorPosicoesBrl, lucroNaoRealizadoBrl, lucroRealizadoAcumuladoBrl, patrimonioTotalBrl, rentabilidadeNaoRealizadaPercentual, cambioAtual e posicoes. Posição aberta contém exatamente ativoId, ticker, mercado, moeda, quantidade, precoMedioBrl, totalInvestidoBrl, cotacaoAtual, providerCotacao, instanteCotacao, valorAtualOrigem, valorAtualBrl, lucroNaoRealizadoBrl e rentabilidadePercentual. Cambio é null sem US ou `{taxaCambioBrl,provider,instanteCambio}`. OpenAPI SHALL documentar contrato, precisão, B3/US, empty, persistência e erros; GET e POST não geram auditoria operacional.

#### Scenario: Proveniência
- **WHEN** valuation usa quote e FX
- **THEN** valuationInstant, instanteCotacao e instanteCambio são conceitualmente distintos sem alegação de simultaneidade

### Requirement: Classificação autoritativa para composição por ação
Cada posição aberta retornada por GET e POST `/api/v1/carteira/resumo` SHALL incluir a classificação canônica `tipo` do ativo, além dos campos já definidos. A classificação SHALL ser lida do ativo persistido participante da valuation e não inferida de ticker, mercado ou cotação. `valorAtualBrl` existente continua sendo a única fonte autoritativa para o valor atual em BRL usado pela composição; esta extensão não altera fórmulas, snapshots, providers nem a semântica resiliente/parcial existente da valuation: se cotação ou câmbio estiver indisponível, não inventa `valorAtualBrl`, preserva os demais dados persistidos da posição e não reintroduz falha integral do resumo.

#### Scenario: Posição com classificação persistida
- **WHEN** o resumo materializa posições abertas ACAO, FII e ETF
- **THEN** cada posição retorna seu `tipo` canônico e mantém o `valorAtualBrl` calculado pela valuation existente
