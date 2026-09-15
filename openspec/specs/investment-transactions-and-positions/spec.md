# investment-transactions-and-positions Specification

## Purpose

Estabelece compras e vendas privadas, imutáveis e idempotentes para ativos B3 e US, mantendo caixa BRL, posições, custo e resultado realizado coerentes sob concorrência e sem depender de valuation ou providers durante a confirmação.

## Requirements

### Requirement: Transação imutável e íntegra
O sistema SHALL persistir cada `Transacao` confirmada com `id`, `carteira_id`, `usuario_id`, `acao_id`, `corretora_id`, `exchange_rate_id` nullable, `tipo` BUY ou SELL, `quantidade`, `moeda`, `preco_unitario`, `taxas`, `taxa_cambio_brl`, `valor_total_brl`, `resultado_realizado_brl` nullable, `data_negociacao` e `data_registro`. UUIDs SHALL identificar entidades; quantidade, preço, taxas e taxa de câmbio SHALL usar `NUMERIC(18,8)`; valores BRL e resultado SHALL usar `NUMERIC(18,2)`; timestamps SHALL usar `TIMESTAMPTZ`. Quantidade e preço MUST ser positivos, taxas MUST ser não negativas e taxa de câmbio MUST ser positiva. BUY MUST ter resultado nulo e SELL MUST ter resultado não nulo. Todas as referências SHALL usar `ON DELETE RESTRICT`, e o banco MUST impedir associação de carteira de um usuário com outro e moeda de transação divergente da moeda estrutural do Ativo. Uma transação confirmada MUST NOT ser alterada ou excluída e a capability MUST NOT expor PUT, PATCH ou DELETE.

#### Scenario: Fato financeiro confirmado
- **WHEN** uma BUY ou SELL válida é confirmada
- **THEN** uma única Transacao imutável é persistida com referências, precisão, moeda, câmbio, totais e timestamps coerentes

#### Scenario: Escrita relacional incoerente
- **WHEN** uma escrita direta combina carteira e usuário diferentes, ativo e moeda diferentes, referência inexistente ou campos incompatíveis com BUY/SELL
- **THEN** PostgreSQL rejeita a escrita sem alterar fatos financeiros existentes

### Requirement: Precisão estrita dos inputs em escala 8
Os campos de request `quantidade`, `precoUnitario` e `taxas` SHALL ser parseados diretamente em `BigDecimal`, sem passagem por `double` ou `float`. Cada valor MUST ser exatamente representável em `NUMERIC(18,8)` sem arredondamento que altere seu valor. Representações como `10`, `10.0`, `10.00000000` e `10.0000000000` SHALL ser equivalentes e válidas porque as casas excedentes são zeros; `10.123456789` MUST ser inválido porque a nona casa é significativa. Quantidade e preço MUST ser maiores que zero e taxas MUST ser maiores ou iguais a zero. Precisão significativa excessiva ou overflow do próprio input SHALL responder 400. `HALF_EVEN` MUST NOT corrigir input inválido; SHALL ser usado somente nos cálculos derivados para os quais este contrato define materialização BRL em escala 2, preço médio em escala 8 ou outro ponto explícito. Um resultado derivado de inputs válidos que não caiba na capacidade persistente ou viole invariante financeira SHALL responder 409 e causar rollback integral.

#### Scenario: Zeros excedentes não alteram o input
- **WHEN** quantidade, preço ou taxas são enviados com casas além da oitava contendo somente zeros
- **THEN** o valor é aceito e canonizado sem alteração financeira

#### Scenario: Precisão significativa ou overflow do input
- **WHEN** um campo escala 8 possui casa significativa além da oitava ou seu próprio valor não cabe em `NUMERIC(18,8)`
- **THEN** o request responde 400 sem reservar idempotência nem produzir efeito

### Requirement: Posição persistente e custo materializado
O sistema SHALL manter uma única `Posicao` por `(carteira_id, acao_id)` com UUID, quantidade `NUMERIC(18,8)`, preço médio BRL `NUMERIC(18,8)`, total investido BRL `NUMERIC(18,2)`, lucro realizado acumulado BRL `NUMERIC(18,2)` e última atualização. Uma posição aberta MUST ter quantidade, preço médio e total investido positivos. Uma posição zerada SHALL permanecer persistida com os três campos iguais a zero; seu lucro realizado acumulado MAY ser positivo, zero ou negativo. Recompra SHALL reutilizar a mesma posição, reiniciar quantidade/preço médio/total investido a partir da nova compra e preservar o lucro realizado histórico. `totalInvestidoBrl` SHALL ser a fonte de verdade do custo materializado; `precoMedioBrl` SHALL ser projeção derivada em escala 8.

#### Scenario: Posição zerada e recompra
- **WHEN** uma venda total zera a posição e depois ocorre nova BUY do mesmo ativo
- **THEN** o mesmo id de Posicao é reutilizado, os campos de custódia refletem somente o novo ciclo e o lucro realizado acumulado anterior é preservado

#### Scenario: Estados estruturais inválidos
- **WHEN** uma escrita tenta persistir quantidade zero com custo ou preço médio positivo, ou quantidade positiva sem custo e preço médio positivos
- **THEN** PostgreSQL rejeita a escrita

### Requirement: Cálculo e liquidação de BUY
Para BUY, o sistema SHALL calcular sem arredondamentos intermediários `valorOrigemExato = quantidade * precoUnitario`, `brutoBrlExato = valorOrigemExato * taxaCambioBrl` e `custoBrl = round2_HALF_EVEN(brutoBrlExato + taxasBrl)`. SHALL calcular `novaQuantidade = quantidadeAnterior + quantidadeComprada`, `novoTotalInvestidoBrl = totalInvestidoAnterior + custoBrl` e `novoPrecoMedioBrl = round8_HALF_EVEN(novoTotalInvestidoBrl / novaQuantidade)`. A operação MUST exigir Ativo ativo, Corretora ativa e saldo suficiente; SHALL debitar caixa BRL, criar/reabrir/atualizar Posicao, criar Transacao com `resultadoRealizadoBrl` nulo, atualizar snapshot e auditar COMPRA atomicamente. Saldo nunca pode ficar negativo. Resultado não representável nas escalas contratadas MUST ser rejeitado como conflito, sem efeito parcial.

#### Scenario: Primeira compra e preço médio ponderado
- **WHEN** BUY válida ocorre sem posição anterior ou sobre posição aberta
- **THEN** custo total incorpora bruto convertido e taxas uma única vez, quantidade é somada e preço médio é derivado do novo total investido com HALF_EVEN em escala 8

#### Scenario: Compra sem saldo ou com overflow
- **WHEN** custo final excede o saldo disponível ou qualquer resultado financeiro não cabe na precisão contratada
- **THEN** a operação responde 409 e nenhum saldo, posição, transação, snapshot, auditoria ou resultado idempotente é persistido

### Requirement: Cálculo e liquidação de SELL
Para SELL, o sistema SHALL calcular sem arredondamentos intermediários `valorOrigemExato = quantidadeVendida * precoUnitario`, `brutoBrlExato = valorOrigemExato * taxaCambioBrl` e `valorLiquidoBrl = round2_HALF_EVEN(brutoBrlExato - taxasBrl)`. Taxas maiores que o bruto exato MUST causar 409; taxas iguais ao bruto SHALL permitir líquido zero. Venda a descoberto MUST ser rejeitada. Em venda parcial, `novaQuantidade = quantidadeAnterior - quantidadeVendida` MUST ser positiva, `custoBaseBrl = round2_HALF_EVEN(quantidadeVendida * precoMedioBrl)`, `resultadoRealizadoBrl = valorLiquidoBrl - custoBaseBrl` e `novoTotalInvestidoBrl = totalInvestidoBrlAnterior - custoBaseBrl`; `precoMedioBrl` SHALL permanecer inalterado. Se a materialização deixar `novaQuantidade > 0` com `novoTotalInvestidoBrl <= 0` ou qualquer combinação que viole o estado aberto, a operação MUST responder 409 e reverter integralmente, sem clamp, exclusão da Posicao, recálculo de preço médio para esconder resíduo ou custo inventado. Em venda total, e somente nela, o custo-base SHALL ser todo o `totalInvestidoBrl` remanescente para absorver resíduos e quantidade, preço médio e total investido SHALL ficar zero. BUY MUST persistir `resultadoRealizadoBrl = NULL`; SELL MUST persistir `resultadoRealizadoBrl` obrigatório e signed. `lucroRealizadoAcumuladoBrl` SHALL ser signed. Toda SELL SHALL somar `lucroRealizadoAcumuladoAnterior + resultadoRealizadoBrl`; se a soma ou qualquer outro resultado derivado não couber em `NUMERIC(18,2)` ou em sua capacidade persistente, a operação MUST responder 409 e reverter integralmente. Uma SELL válida SHALL creditar o líquido no caixa BRL e criar Transacao, snapshot e auditoria VENDA atomicamente.

#### Scenario: Venda parcial
- **WHEN** quantidade vendida é menor que a custódia
- **THEN** quantidade e custo materializado diminuem, preço médio das unidades restantes não muda e resultado realizado é líquido menos custo-base arredondado

#### Scenario: Venda parcial extrema produz estado materializado inválido
- **WHEN** uma SELL parcial deixaria quantidade positiva, mas total investido não positivo ou outra combinação incompatível com o estado aberto
- **THEN** a operação responde 409 e sofre rollback integral, sem clamp, exclusão, recálculo corretivo de preço médio ou custo inventado

#### Scenario: Venda total absorve resíduos
- **WHEN** quantidade vendida é exatamente toda a custódia
- **THEN** todo total investido remanescente é usado como custo-base final, a posição fica zerada e o lucro realizado acumulado recebe o resultado

#### Scenario: Taxas consomem o bruto
- **WHEN** taxas SELL são iguais ao bruto convertido exato
- **THEN** a venda pode concluir com crédito zero e resultado calculado normalmente

#### Scenario: Overflow do lucro realizado acumulado
- **WHEN** `lucroRealizadoAcumuladoAnterior + resultadoRealizadoBrl` não cabe em `NUMERIC(18,2)`
- **THEN** a SELL responde 409 e reverte saldo, posição, transação, snapshot, auditoria e conclusão idempotente

### Requirement: Mercado, moeda e FX local
Ativo B3 SHALL gerar Transacao em BRL, exigir `exchangeRateId` ausente ou nulo, usar internamente `taxaCambioBrl = 1.00000000` e MUST NOT consultar exchange-rates. Para uma operação nova US, `dataRegistro` SHALL ser capturado uma única vez pelo `Clock` depois que a idempotência determinar que não é replay. O Ativo US SHALL gerar Transacao em USD e exigir `exchangeRateId`; o sistema SHALL resolver exclusivamente a linha local de `historico_cambio`, validar id existente, par USD→BRL e a deadline inclusiva `dataRegistro <= historicoCambio.registradoEm + 5 minutos`, copiar sua taxa para a Transacao e persistir o id como proveniência. A validação MUST usar exatamente esse `dataRegistro`, sem segunda leitura do `Clock` e sem usar `instanteCotacao`. No exato instante da deadline o FX SHALL ser válido; no primeiro instante representável posterior SHALL responder 409. Replay confirmado MUST NOT capturar novo `dataRegistro` nem revalidar FX. `taxaCambioBrl` persistida SHALL ser o snapshot financeiro autoritativo e imutável. O request MUST NOT aceitar moeda nem taxa numérica do cliente. O POST MUST NOT chamar provider FX ou market-quotes, nem alterar `HistoricoCotacao`; o preço confirmado pelo usuário MAY divergir da cotação sugerida por fluxo anterior.

#### Scenario: BUY ou SELL B3
- **WHEN** Ativo B3 recebe request sem exchangeRateId
- **THEN** a operação usa BRL e taxa 1.00000000 sem chamada FX

#### Scenario: BUY ou SELL US com observação válida
- **WHEN** exchangeRateId identifica USD→BRL local ainda válido na confirmação
- **THEN** a operação usa preço em USD, copia a taxa observada e persiste o id de proveniência sem chamar provider

#### Scenario: FX inadequado
- **WHEN** exchangeRateId US é inexistente, de par inadequado ou expirado
- **THEN** a operação responde 409 sem efeito financeiro

#### Scenario: Borda inclusiva da validade FX
- **WHEN** `dataRegistro` é exatamente `registradoEm + 5 minutos`
- **THEN** o FX é válido

#### Scenario: Primeiro instante após a validade FX
- **WHEN** `dataRegistro` é um instante representável depois de `registradoEm + 5 minutos`
- **THEN** a operação responde 409 sem efeito financeiro

### Requirement: Lifecycle protegido e ordem global de locks
Uma operação nova SHALL impedir alteração do lifecycle validado de Ativo e Corretora até o commit financeiro. BUY MUST validar Ativo ativo; SELL MAY usar Ativo ativo ou inativo quando houver custódia suficiente; BUY e SELL MUST validar Corretora ativa. Todos os fluxos BUY/SELL SHALL adquirir recursos na ordem determinística: (1) reserva idempotente, (2) Ativo, (3) Corretora, (4) Carteira, (5) Posicao, (6) Snapshot e (7) auditoria/conclusão idempotente. A Carteira MUST sempre ser adquirida antes da Posicao e SHALL ser o lock comum de todas as operações financeiras da mesma carteira, compatível com depósito/saque. Falha após crédito provisório de SELL MUST reverter integralmente a transação.

#### Scenario: Desativação concorrente
- **WHEN** ADMIN tenta desativar Ativo durante BUY ou Corretora durante BUY/SELL
- **THEN** a ordem serializa a alteração com a operação, impedindo que a nova Transacao confirme contra lifecycle diferente do estado protegido

#### Scenario: Operações financeiras concorrentes
- **WHEN** BUY/BUY, BUY/saque, BUY/SELL, SELL/SELL, operações na mesma posição, em ativos diferentes ou com keys diferentes concorrem na mesma carteira
- **THEN** a carteira serializa os efeitos financeiros, saldo não fica negativo, posição não sofre lost update e todos os fluxos preservam Carteira antes de Posicao

### Requirement: Idempotência, fingerprint e precedência de replay
POST SHALL exigir `Idempotency-Key` sem trim, de 1 a 128 caracteres e regex `^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$`; a chave MUST NOT aparecer em logs, auditoria, respostas ou ProblemDetail. PostgreSQL SHALL manter reserva única por `(carteira_id, idempotency_key)` em `transacoes_idempotencia`, e reserva e efeito SHALL compartilhar a mesma transação, de modo que rollback não consuma a chave. O fingerprint SHALL usar SHA-256 sobre registro UTF-8, versionado, ordenado, com tags/tipos e length-prefix, contendo `ativoId`, `corretoraId`, tipo, quantidade/preço/taxas em forma decimal canônica, `dataNegociacao` normalizada para Instant e marcador explícito null/non-null mais valor de `exchangeRateId`. Representações numéricas equivalentes e offsets que representem o mesmo Instant SHALL produzir o mesmo fingerprint. Taxa resolvida, ticker, nome, saldo, posição, cotação e lifecycle MUST NOT participar.

A ordem SHALL ser validação estática, normalização, fingerprint, reserva/consulta idempotente e comparação. Replay confirmado com mesmo fingerprint MUST retornar imediatamente o resultado original, sem revalidar FX, Ativo, Corretora, saldo ou Posicao. Mesma key com fingerprint diferente MUST responder 409 antes de validações voláteis.

#### Scenario: Replay após estado volátil mudar
- **WHEN** a mesma key e payload confirmado são repetidos depois de FX expirar, lifecycle mudar, saldo mudar ou posição mudar
- **THEN** o sistema responde 201 com o resultado original sem revalidar estado volátil e sem novo efeito

#### Scenario: Key reutilizada com payload diferente
- **WHEN** a mesma carteira recebe a mesma key com fingerprint diferente, inclusive concorrentemente
- **THEN** somente o payload vencedor pode produzir efeito e o divergente recebe 409 antes de validações voláteis

#### Scenario: Rollback libera key
- **WHEN** qualquer etapa da nova operação falha antes do commit
- **THEN** a reserva é revertida e a mesma key pode ser usada em nova tentativa legítima

### Requirement: Resultado idempotente materializado
`valorOrigem` MUST NOT ser coluna de `Transacao`; ele SHALL continuar derivado do fato imutável `quantidade * precoUnitario`. Para tornar o replay semanticamente e byte a byte equivalente ao response original, `transacoes_idempotencia` SHALL materializar uma projeção de conclusão em colunas tipadas contendo no mínimo `transacao_id`, `valor_origem NUMERIC(36,16)`, `saldo_caixa_brl_resultante`, `posicao_id`, `posicao_quantidade_resultante`, `posicao_preco_medio_brl_resultante`, `posicao_total_investido_brl_resultante`, `posicao_lucro_realizado_acumulado_brl_resultante` e `posicao_ultima_atualizacao_resultante`, além dos timestamps técnicos necessários da reserva/conclusão. `NUMERIC(36,16)` SHALL acomodar o produto exato de dois `NUMERIC(18,8)`. A projeção MUST NOT usar JSON arbitrário sem justificativa. Replay MUST retornar a mesma Transacao, o `valorOrigem`, o saldo resultante e o snapshot resultante da Posicao materializados na primeira execução e MUST NOT recalcular `valorOrigem`, consultar saldo atual ou consultar Posicao atual. Primeira execução e replay SHALL responder `201 Created` com exatamente `transacao`, `valorOrigem`, `saldoCaixaBrl` e `posicao`. `transacao` SHALL conter exatamente `id`, `ativoId`, `ticker`, `corretoraId`, `exchangeRateId`, `tipo`, `quantidade`, `moeda`, `precoUnitario`, `taxas`, `taxaCambioBrl`, `valorTotalBrl`, `resultadoRealizadoBrl`, `dataNegociacao` e `dataRegistro`; `posicao` SHALL conter exatamente `id`, `ativoId`, `ticker`, `quantidade`, `precoMedioBrl`, `totalInvestidoBrl`, `lucroRealizadoAcumuladoBrl` e `ultimaAtualizacao`.

#### Scenario: Timeout do cliente após commit
- **WHEN** a primeira execução commita mas o cliente não recebe a resposta e repete a mesma key/payload
- **THEN** o replay retorna 201 com os campos e valores originais materializados, sem usar projeções atuais

### Requirement: Atomicidade, snapshot local e auditoria
Reserva, locks, saldo, Posicao, Transacao, snapshot diário, auditoria de sucesso e conclusão idempotente MUST ocorrer na mesma transação PostgreSQL. Falha de qualquer etapa, inclusive auditoria, MUST causar rollback integral. Somente após excluir replay, a operação nova SHALL capturar `dataRegistro` uma vez do `Clock`; esse mesmo instante SHALL ser usado por `Transacao.dataRegistro`, `Posicao.ultimaAtualizacao`, verificações temporais que dependam do instante atual, data do snapshot derivada em `America/Sao_Paulo` e `auditoria.dataHora` de COMPRA/VENDA. Nenhum desses eventos financeiros SHALL chamar `Instant.now()` independentemente. Replay confirmado MUST NOT capturar novo `dataRegistro`. `dataNegociacao` SHALL ser recebida como ISO-8601 com offset obrigatório, normalizada para Instant e preservada no replay; não SHALL existir restrição de data futura ou antiga nesta change. Auditoria SHALL conter somente usuário, evento COMPRA/VENDA, resultado, severidade, endpoint, correlation ID e instante, sem valores ou identificadores financeiros privados listados no contrato.

Após BUY/SELL, o snapshot do dia SHALL atualizar saldo, recompor `totalInvestidoBrl` das posições abertas e invalidar `valorPosicoesBrl`, `lucroNaoRealizadoBrl` e `patrimonioTotalBrl` para `NULL`, sem provider. Os três campos dependentes de valuation MUST ter estado coerente: quando o valuation for desconhecido com posição aberta, todos SHALL ser `NULL`, nunca parcialmente preenchidos; a V9 SHALL impor CHECKs contra estado parcial. `saldoCaixaBrl` e `totalInvestidoBrl` SHALL permanecer sempre não nulos. Quando não houver posição aberta, SHALL gravar `valorPosicoesBrl = 0`, `lucroNaoRealizadoBrl = 0`, `totalInvestidoBrl = 0` e patrimônio igual ao saldo. O composer compartilhado SHALL aplicar ao primeiro DEPOSITO/SAQUE do novo dia com posição aberta o saldo real, total investido recomposto e o trio de valuation `NULL`, sem copiar valuation de snapshot anterior nem chamar provider; valuation conhecido do mesmo dia SHALL continuar preservado em mutações somente de caixa. Snapshot anterior MUST permanecer imutável.

#### Scenario: Falha de auditoria ou snapshot
- **WHEN** persistência do snapshot, auditoria COMPRA/VENDA ou conclusão idempotente falha
- **THEN** saldo, posição, transação, reserva, snapshot e auditoria de sucesso são integralmente revertidos

#### Scenario: Instante único de COMPRA e VENDA
- **WHEN** uma operação nova BUY ou SELL é confirmada
- **THEN** `Transacao.dataRegistro`, `Posicao.ultimaAtualizacao` e `auditoria.dataHora` são iguais ao único `dataRegistro` capturado, e a data do snapshot deriva dele em `America/Sao_Paulo`

#### Scenario: Mutação com posição aberta
- **WHEN** BUY/SELL termina com ao menos uma posição aberta
- **THEN** snapshot contém saldo e total investido locais e marca os três campos dependentes de valuation como NULL

#### Scenario: Nenhuma posição aberta
- **WHEN** SELL termina sem qualquer posição aberta na carteira
- **THEN** snapshot contém zeros conhecidos para posições/investimento/lucro não realizado e patrimônio igual ao saldo BRL

#### Scenario: Primeiro movimento de caixa do novo dia com posição aberta
- **WHEN** existe Posicao aberta, ocorre DEPOSITO ou SAQUE e ainda não existe snapshot no dia derivado do instante da operação
- **THEN** o snapshot usa saldo real e total investido local, grava os três campos dependentes de valuation como NULL, não copia o dia anterior e não chama provider

### Requirement: APIs privadas, listagens e posições zeradas
The system SHALL expose exactly POST `/api/v1/carteira/transacoes`, GET `/api/v1/carteira/transacoes`, GET `/api/v1/carteira/transacoes/{id}`, GET `/api/v1/carteira/posicoes`, and GET `/api/v1/carteira/posicoes/{ativoId}`. All SHALL be exclusive to ROLE_USER and derive user/wallet from `SecurityContext`; anonymous receives 401 and ROLE_ADMIN 403. The public Transaction model SHALL retain exactly `id`, `ativoId`, `ticker`, persisted `nome`, optional system-owned `logoProvider`/`logoReference`, `corretoraId`, persisted `corretoraNome`, `exchangeRateId`, `tipo`, `quantidade`, `moeda`, `precoUnitario`, `taxas`, `taxaCambioBrl`, `valorTotalBrl`, `resultadoRealizadoBrl`, `dataNegociacao`, and `dataRegistro`, reused in POST response, GET detail, and list. Transaction history SHALL use only persisted joins and SHALL NOT invoke quote, FX, valuation, or external providers. The public Position model SHALL contain `id`, `ativoId`, `ticker`, `nome`, `mercado`, `moeda`, `ativo`, optional system-owned `logoProvider`/`logoReference`, `quantidade`, `precoMedioBrl`, `totalInvestidoBrl`, `lucroRealizadoAcumuladoBrl`, and `ultimaAtualizacao`; it SHALL be reused in POST response, GET detail, and list. Position responses SHALL NOT contain valuation data. The added market/currency/lifecycle/branding values SHALL come from the persisted canonical asset record, including when inactive, and MUST NOT be resolved through the active public catalog. Transaction pagination remains `{items,page,size,totalElements,totalPages}`, page 0/size 20 defaults, 1..100 size, fixed `dataRegistro DESC,id DESC`; positions retain the same envelope, no filters, `ticker ASC,ativoId ASC`, and only quantity greater than zero in the list. Unknown list query parameters SHALL return 400. Position detail MAY return a persisted zero-quantity position.

#### Scenario: Listagem de custódia
- **WHEN** a user lists open and zero-quantity positions in storage
- **THEN** the response contains only open positions, in ticker/asset-id order, with canonical asset metadata and no valuation

#### Scenario: Consulta de posição zerada
- **WHEN** a user queries an owned persisted zero-quantity position
- **THEN** the response includes its zeroed current cost, historical realized profit, and current canonical asset metadata

#### Scenario: Position references inactive asset
- **WHEN** an open position references an inactive catalog asset
- **THEN** its response still includes ticker, name, market, currency, lifecycle status, and optional branding reference for contextual SELL

### Requirement: Ownership, erros e OpenAPI
GET Transaction detail for another user SHALL return 403 and use the existing `ACESSO_NEGADO` mechanism; missing Transaction returns 404. Missing own Position returns 404. Invalid/unknown JSON, UUID/type/number, fractional or nonpositive quantity, input not exactly representable in its contract numeric scale, own-input overflow, nonpositive price, negative fees, invalid key, forbidden list query, B3 with exchangeRateId, or US without exchangeRateId SHALL return 400. Insufficient cash, oversell, SELL without Position, inactive asset on BUY, inactive broker, unsuitable/missing/expired FX, fingerprint mismatch, invalid partial position, realized-profit overflow, any nonrepresentable derived result, or SELL fees exceeding gross SHALL return 409. A 409 caused specifically by an FX identifier whose `registradoEm + 5 minutes` deadline has passed SHALL include structured ProblemDetail property `code: "FX_EXPIRED"`; other conflicts SHALL NOT use that code. Missing primary wallet, persistence, transactional audit, or internal invariant failure SHALL return 500. Transaction POST SHALL not return 502 due to a provider and SHALL not use 422. All errors use sanitized ProblemDetail and `X-Correlation-ID`.

OpenAPI SHALL document Bearer, ROLE_USER, endpoints, Idempotency-Key, strict request, exact transaction response, expanded position metadata, B3/US/FX, scale, pagination, ownership, absence of valuation, and 400/401/403/404/409/500 responses including the `FX_EXPIRED` structured code.

#### Scenario: Acesso cruzado
- **WHEN** a ROLE_USER requests a Transaction owned by another user
- **THEN** the system returns correlated sanitized 403 and records `ACESSO_NEGADO` without private financial data

#### Scenario: POST não chama provider
- **WHEN** BUY or SELL is confirmed or rejected
- **THEN** no quote, FX, Receita, CVM, or CEP provider is called and this flow does not produce 502 due to a provider

#### Scenario: FX expirado tem codigo estruturado
- **WHEN** a US transaction uses an observation after `registradoEm + 5 minutes`
- **THEN** it returns 409 with `code=FX_EXPIRED`, without financial effects; unrelated 409 responses omit that code

### Requirement: Versão financeira e invalidação de valuation após negociação
Cada BUY/SELL novo confirmado SHALL aplicar `novoEstadoVersao = estadoVersaoAnterior + 1` exatamente uma vez na mesma transação financeira. Replay idempotente e operação rejeitada/rollback MUST NOT incrementá-la. Se restar posição aberta, SHALL recompor `totalInvestidoBrl` e invalidar valor de posições, lucro não realizado, patrimônio e valuationInstant para nulo. Sem posição aberta, SHALL gravar investimento/valor/lucro não realizado em zero, patrimônio igual ao saldo e instant nulo. Posição zerada SHALL persistir e reter seu lucro realizado acumulado histórico.

#### Scenario: BUY ou SELL parcial
- **WHEN** BUY ou SELL novo termina com posição aberta
- **THEN** versão aumenta uma vez e a valuation do snapshot é invalidada integralmente

#### Scenario: SELL da última posição
- **WHEN** SELL novo zera a última posição
- **THEN** versão aumenta uma vez e o snapshot entra no estado local sem posições

#### Scenario: Replay ou rollback
- **WHEN** uma operação é replay ou é rejeitada/revertida
- **THEN** estadoVersao não muda

#### Scenario: Lucro realizado histórico após zeramento
- **WHEN** SELL total zera uma posição
- **THEN** seu lucro realizado acumulado permanece disponível para agregação histórica

### Requirement: Transaction history survives a committed BUY and market-provider failures
The transaction history SHALL read only the persisted ledger and persisted asset/broker joins. After a successful BUY/SELL, the client SHALL invalidate and refetch the transaction-history query through the same-origin BFF; nullable result, FX, and branding fields SHALL map safely. Quote, FX, valuation, or other market-provider failure MUST NOT block GET /api/v1/carteira/transacoes or the /carteira/transacoes page.

#### Scenario: BUY appears in transaction history after refetch
- **WHEN** a user opens transaction history, completes a BUY, and returns to/refetches transaction history
- **THEN** the persisted BUY appears in the paginated response with HTTP 200 and renders without an error page
- **AND** this remains true when a market-provider request fails
