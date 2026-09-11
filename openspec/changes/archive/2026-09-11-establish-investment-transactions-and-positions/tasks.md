## 1. Migration V9 e integridade PostgreSQL

- [x] 1.1 Criar somente `V9__establish_investment_transactions_and_positions.sql`, sem alterar V1–V8, com `transacoes`, tipos/escalas/timestamps/checks e FKs `ON DELETE RESTRICT`; verificar Flyway em PostgreSQL Testcontainers.
- [x] 1.2 Proteger `transacoes` com FK composta `(carteira_id,usuario_id)` e coerência declarativa `(acao_id,moeda)` via chave auxiliar em `acoes`; verificar escritas diretas cruzadas e B3/USD ou US/BRL rejeitadas.
- [x] 1.3 Criar `posicoes` com `UNIQUE(carteira_id,acao_id)`, escalas, FKs restritivas e CHECK dos estados totalmente zerado ou totalmente aberto; verificar lucro acumulado positivo/zero/negativo e estados híbridos rejeitados.
- [x] 1.4 Criar `transacoes_idempotencia` com UNIQUE `(carteira_id,idempotency_key)`, key/fingerprint, estado reserva/conclusão e, no mínimo, `transacao_id`, `valor_origem NUMERIC(36,16)`, `saldo_caixa_brl_resultante`, `posicao_id`, `posicao_quantidade_resultante`, `posicao_preco_medio_brl_resultante`, `posicao_total_investido_brl_resultante`, `posicao_lucro_realizado_acumulado_brl_resultante`, `posicao_ultima_atualizacao_resultante` e timestamps técnicos; não adicionar `valor_origem` a `transacoes` nem usar JSON arbitrário, e verificar reserva incompleta válida e conclusão parcialmente preenchida rejeitada.
- [x] 1.5 Adicionar índices coerentes com transações `data_registro DESC,id DESC`, posições por carteira/ativo e custódia aberta, e idempotência; verificar nomes, colunas, direção e ausência de índices/filtros especulativos.
- [x] 1.6 Evoluir nullability e CHECKs de `carteira_snapshots` para exigir `valor_posicoes_brl`, `lucro_nao_realizado_brl` e `patrimonio_total_brl` todos NULL ou todos preenchidos, com patrimônio conhecido coerente, preservando `saldo_caixa_brl`/`total_investido_brl` NOT NULL e dados V6 existentes; verificar migration sobre baseline V1–V8, rejeição de estados parciais e estado sem posição com zeros/patrimônio igual ao saldo.
- [x] 1.7 Mapear novas tabelas/colunas no ORM e executar Hibernate `ddl-auto=validate`; verificar startup contra PostgreSQL migrado sem H2.

## 2. Domínio, normalização e precisão

- [x] 2.1 Implementar modelos puros `Transacao`, `Posicao` e tipo BUY/SELL com invariantes de imutabilidade, moeda, resultado e estados aberto/zerado; verificar testes unitários de construções válidas e inválidas.
- [x] 2.2 Parsear diretamente `quantidade`, `precoUnitario` e `taxas` como BigDecimal, nunca double/float, e aceitar somente valores exatamente representáveis em `NUMERIC(18,8)` sem arredondamento; verificar equivalência de `10`, `10.0`, `10.00000000` e `10.0000000000`, rejeição 400 de `10.123456789`, overflow do input, quantidade/preço não positivos e taxas negativas.
- [x] 2.3 Implementar `HALF_EVEN` somente nos resultados derivados previstos — materialização BRL escala 2, preço médio escala 8 e demais pontos explícitos — sem usá-lo para corrigir input de precisão excessiva; verificar empates pares/ímpares, limites e distinção entre input inválido 400 e resultado derivado impossível 409.
- [x] 2.4 Implementar BUY: valor origem/bruto/custo, primeira compra, soma de quantidade/custo e preço médio derivado do total investido; verificar B3, US com FX diferentes, taxas, frações e preço médio ponderado.
- [x] 2.5 Implementar SELL parcial com `novaQuantidade > 0`, `novoTotalInvestidoBrl = totalInvestidoBrlAnterior - custoBaseBrl`, resultado realizado signed e preço médio remanescente inalterado; verificar lucro, prejuízo, resultado zero e rejeição 409/rollback da venda extrema que deixaria custo não positivo ou outro estado aberto inválido, sem clamp, exclusão, recálculo corretivo ou custo inventado.
- [x] 2.6 Implementar SELL total usando todo custo remanescente e zeramento de custódia; verificar absorção de resíduos de arredondamento e lucro realizado acumulado.
- [x] 2.7 Implementar recompra na mesma Posicao zerada, reiniciando custódia/custo/preço médio e preservando lucro acumulado; verificar identidade da posição e múltiplos ciclos.
- [x] 2.8 Rejeitar short selling, SELL sem Posicao, oversell, taxas maiores que bruto e estado parcial financeiramente inválido como conflito; verificar taxas iguais ao bruto permitem líquido zero, BUY com resultado realizado NULL, SELL com resultado obrigatório signed e rollback.
- [x] 2.9 Somar `lucroRealizadoAcumuladoAnterior + resultadoRealizadoBrl` como valor signed e rejeitar 409 se o resultado não couber em `NUMERIC(18,2)`; aplicar a mesma regra a qualquer derivado fora da capacidade persistente e verificar rollback integral, distinguindo overflow do input como 400.
- [x] 2.10 Capturar/normalizar `dataNegociacao` de OffsetDateTime com offset obrigatório para Instant em microssegundos e não impor limites de futuro/passado; verificar offsets equivalentes, nanos e datas antigas/futuras aceitas.

## 3. Fingerprint e idempotência transacional

- [x] 3.1 Extrair o escritor SHA-256 UTF-8 versionado, tipado e length-prefixed para componente genérico sem mudar fingerprints/contratos do caixa; verificar regressão dos testes existentes de 100/100.0/100.00, null e delimitadores.
- [x] 3.2 Implementar fingerprint de transação com ordem e campos exatos, marcador FX null/non-null, decimal canônico e Instant normalizado; verificar equivalência numérica/temporal e divergência de cada campo participante.
- [x] 3.3 Implementar reserva PostgreSQL por carteira/key com `INSERT ... ON CONFLICT DO NOTHING RETURNING`, sem recuperar UNIQUE em transação abortada; verificar nova/existente, espera por concorrente e leitura após commit.
- [x] 3.4 Implementar comparação antes de qualquer validação volátil: replay igual retorna imediatamente e divergente responde 409; verificar que replay confirmado não captura novo `dataRegistro`, não valida FX e não consulta Ativo, Corretora, Carteira financeira ou Posicao.
- [x] 3.5 Persistir conclusão idempotente tipada com Transacao e todas as colunas enumeradas em 1.4; verificar que `valorOrigem` é materializado do produto exato apenas nessa projeção e que replay não o recalcula, não lê saldo/Posicao atuais e preserva os dados/timestamps originais.
- [x] 3.6 Garantir reserva e efeito na mesma transação, com rollback liberando a key em toda falha de negócio/persistência/auditoria; verificar retry legítimo após cada estágio falho.
- [x] 3.7 Garantir que Idempotency-Key/fingerprint nunca aparecem em resposta, logs, auditoria ou ProblemDetail; verificar key ausente, vazia, espaços, caracteres inválidos e 129 caracteres como 400 sem eco.

## 4. Ports, persistência local e locking

- [x] 4.1 Adicionar leituras protegidas específicas de Ativo com `FOR SHARE`/equivalente e validar BUY ativo versus SELL ativo/inativo; verificar PostgreSQL bloqueia desativação concorrente até commit.
- [x] 4.2 Adicionar leitura protegida específica de Corretora ativa com `FOR SHARE`/equivalente para BUY/SELL; verificar desativação concorrente serializada e Corretora inexistente/inativa mapeada corretamente.
- [x] 4.3 Estender histórico FX com leitura local por UUID sem cache/provider e validar existência, par USD→BRL e deadline inclusiva `dataRegistro <= registradoEm + 5 minutos` usando o mesmo `dataRegistro` único da operação nova, sem segunda leitura do Clock nem `instanteCotacao`; verificar validade exatamente na deadline e 409 no primeiro instante representável posterior.
- [x] 4.4 Adicionar lock explícito `FOR UPDATE` da Carteira e reutilizar débito/crédito `UPDATE ... RETURNING` com checks de saldo/overflow; verificar saldo nunca negativo e crédito SELL revertido em falha posterior.
- [x] 4.5 Implementar consulta/lock `FOR UPDATE`, criação e atualização de Posicao sempre depois da Carteira; verificar UNIQUE como defesa final e ausência de lost update.
- [x] 4.6 Implementar persistência imutável e consultas privadas/paginadas de Transacao na ordem `data_registro DESC,id DESC`; verificar nenhuma operação de update/delete e desempate por UUID.
- [x] 4.7 Implementar consultas paginadas de Posicao com join de ticker, lista somente quantidade positiva e detalhe por ativo incluindo zero; verificar ordem `ticker ASC,ativoId ASC` e ausência de valuation.
- [x] 4.8 Instrumentar/provar a ordem global reserva → Ativo → Corretora → Carteira → Posicao → Snapshot → auditoria/conclusão em BUY e SELL; verificar que nenhum fluxo adquire Posicao antes da Carteira.

## 5. Orquestração BUY/SELL e atomicidade

- [x] 5.1 Criar caso de uso transacional único que resolve carteira do SecurityContext, executa validação estática/fingerprint/reserva e captura um único `dataRegistro` via Clock somente depois de determinar que a operação é nova; reutilizá-lo em Transacao, Posicao, snapshot, auditoria, FX e verificações temporais, e verificar carteira ausente como 500 sanitizado e ausência de nova captura no replay.
- [x] 5.2 Implementar fluxo B3 com moeda BRL, FX null e taxa interna 1.00000000, sem chamar exchange-rates; verificar BUY/SELL e rejeição 400 de exchangeRateId presente.
- [x] 5.3 Implementar fluxo US com moeda USD e exchangeRateId obrigatório resolvido localmente, copiando taxa/id imutáveis; verificar id ausente como 400 e inexistente/inadequado/expirado como 409.
- [x] 5.4 Orquestrar BUY atômico com lifecycle, saldo, posição, Transacao, snapshot, COMPRA e conclusão idempotente; verificar saldo insuficiente e overflow revertem todos os artefatos.
- [x] 5.5 Orquestrar SELL atômico com lifecycle, custódia, crédito, posição, Transacao, snapshot, VENDA e conclusão idempotente; verificar falha posterior ao crédito reverte todos os artefatos.
- [x] 5.6 Remover qualquer dependência do POST em market-quotes, FX provider, Receita, CVM ou ViaCEP; verificar fakes/probes falham o teste se qualquer provider for invocado em sucesso ou erro.

## 6. Snapshots locais e compatibilidade de caixa

- [x] 6.1 Substituir o writer estreito do caixa por composer local compartilhado que lê posições abertas e soma custo após lock da Carteira; verificar soma em centavos e nenhuma chamada externa.
- [x] 6.2 Implementar composição BUY/SELL com saldo/total investido locais e os três campos dependentes de valuation (`valor_posicoes_brl`, `lucro_nao_realizado_brl`, `patrimonio_total_brl`) todos NULL quando resta posição aberta; verificar BUY e SELL invalidam valuation conhecido anterior e nunca produzem estado parcial.
- [x] 6.3 Implementar estado sem posições abertas com `valor_posicoes_brl = 0`, `lucro_nao_realizado_brl = 0`, `total_investido_brl = 0` e patrimônio igual ao caixa, mantendo saldo/total investido não nulos; verificar venda da última posição e carteira que nunca teve posição aberta.
- [x] 6.4 Evoluir depósito/saque para recompor investimento sem zerá-lo e preservar valuation conhecido, recalculando patrimônio; verificar ambos os tipos sobre snapshot conhecido.
- [x] 6.5 Preservar o trio de valuation NULL em depósito/saque quando valuation não existe; verificar total investido continua conhecido e não vira zero artificial.
- [x] 6.6 Tratar primeiro DEPOSITO/SAQUE do novo dia com Posicao aberta e snapshot ausente: gravar saldo real, recomputar total investido local e deixar o trio de valuation NULL, sem copiar snapshot anterior nem chamar provider; verificar explicitamente a virada de dia.
- [x] 6.7 Preservar snapshot anterior e upsert único do dia em `America/Sao_Paulo`; verificar virada de dia, timezone do host, replay, rollback e snapshot concorrente.

## 7. Auditoria e tempo determinístico

- [x] 7.1 Adicionar COMPRA e VENDA ao enum de eventos e evoluir `AuditoriaCommand`/adapter com instante explícito retrocompatível; manter fallback apenas para eventos não financeiros existentes de segurança/login/compliance e demais callers sem instante.
- [x] 7.2 Fazer DEPOSITO/SAQUE passar o `operationInstant` já capturado e COMPRA/VENDA passar exatamente o `dataRegistro`, sem `Instant.now()` independente; verificar `movimento.dataHora == auditoria.dataHora == operationInstant` para depósito e saque, e `Transacao.dataRegistro == Posicao.ultimaAtualizacao == auditoria.dataHora == dataRegistro` para compra e venda, com data do snapshot derivada do mesmo instante em `America/Sao_Paulo`.
- [x] 7.3 Manter auditoria de sucesso na mesma transação financeira, nunca isolada, e falha de auditoria causando rollback; verificar nenhum evento permanece após falha.
- [x] 7.4 Verificar sanitização de COMPRA/VENDA: somente usuário, evento, resultado, severidade, endpoint, correlation ID e instante, sem ativo/corretora/tipo detalhado, quantidade, preço, FX, taxas, saldo, posição, lucro, key ou fingerprint.
- [x] 7.5 Reutilizar ACESSO_NEGADO para ownership cruzado e ROLE_ADMIN 403 sem expor finanças; verificar falha da auditoria isolada não altera o 403.

## 8. API, responses e segurança

- [x] 8.1 Criar request estrito do POST com exatamente ativoId, corretoraId, tipo, quantidade, precoUnitario, taxas, dataNegociacao e exchangeRateId, sem usuarioId/carteiraId/moeda/taxaCambioBrl; verificar desconhecidos/JSON/UUID/tipo inválidos como 400.
- [x] 8.2 Criar resposta 201 exata com `transacao`, `valorOrigem`, `saldoCaixaBrl` e `posicao`; usar o modelo público único de Transacao com exatamente `id`, `ativoId`, `ticker`, `corretoraId`, `exchangeRateId`, `tipo`, `quantidade`, `moeda`, `precoUnitario`, `taxas`, `taxaCambioBrl`, `valorTotalBrl`, `resultadoRealizadoBrl`, `dataNegociacao`, `dataRegistro`, e o modelo público único de Posicao com exatamente `id`, `ativoId`, `ticker`, `quantidade`, `precoMedioBrl`, `totalInvestidoBrl`, `lucroRealizadoAcumuladoBrl`, `ultimaAtualizacao`; verificar escalas, nulls BUY/SELL e igualdade entre primeira execução e replay.
- [x] 8.3 Expor exatamente POST/GET `/api/v1/carteira/transacoes` e GET por id, sem PUT/PATCH/DELETE ou filtros, reutilizando o mesmo modelo público de Transacao no POST, detalhe e items; verificar inventário MVC/OpenAPI de rotas e schemas idênticos.
- [x] 8.4 Expor exatamente GET `/api/v1/carteira/posicoes` e GET por ativoId, reutilizando o mesmo modelo público de Posicao do POST, sem campos/endpoints de valuation ou mutação; verificar lista somente quantidade positiva, detalhe incluindo zero e schemas idênticos.
- [x] 8.5 Implementar envelopes com exatamente `items`, `page`, `size`, `totalElements`, `totalPages`, defaults page=0/size=20, page>=0, size 1..100 e rejeição 400 de qualquer query param diferente de page/size; verificar transações vazias, posições vazias e limites.
- [x] 8.6 Aplicar ROLE_USER aos cinco endpoints, identidade exclusiva do SecurityContext e carteira principal; verificar anônimo 401, ROLE_ADMIN 403 e isolamento entre dois usuários.
- [x] 8.7 Implementar GET Transacao: própria 200, UUID global ausente 404 e existente de outro usuário 403+ACESSO_NEGADO; verificar resposta alheia não contém nenhum campo financeiro.
- [x] 8.8 Implementar GET Posicao por ativo: própria aberta/zerada 200, inexistente 404 e ativo global inexistente 404 conforme contrato; verificar nenhuma posição de outro usuário é retornada.
- [x] 8.9 Mapear 400/404/409/500 previstos para ProblemDetail sanitizado com `X-Correlation-ID`, sem 422/502 no POST; verificar SQL, stack trace, ids privados, key e valores financeiros ausentes.
- [x] 8.10 Documentar OpenAPI dos cinco endpoints com Bearer/ROLE_USER, Idempotency-Key, request/response exatos, B3/US/FX, escalas, paginação, ownership, posições zeradas e ausência de valuation; verificar schema OpenAPI automatizado.

## 9. Testes financeiros e de validação

- [x] 9.1 Cobrir domínio BUY: primeira compra, múltiplas compras, B3, US com FX diferentes, taxas, frações, resíduos, HALF_EVEN e overflow; verificar resultados exatos sem double/float.
- [x] 9.2 Cobrir domínio SELL: parcial, total, lucro, prejuízo, zero, taxas igual/maior que bruto, resultado/acumulado signed, preço médio preservado e custo final residual; incluir venda parcial extrema que produziria estado materializado inválido e overflow da soma do lucro acumulado, verificando 409 e rollback integral.
- [x] 9.3 Cobrir ciclo posição zerada/recompra e lucro acumulado histórico positivo/zero/negativo; verificar mesmo UUID e reinício apenas dos campos de custódia.
- [x] 9.4 Cobrir validações de quantidade, preço, taxas, lifecycle, saldo, SELL sem posição, oversell e FX; verificar precedência 400 versus 404/409 e ausência de efeitos.
- [x] 9.5 Cobrir B3 sem qualquer acesso FX e US sem qualquer provider, usando observações locais; verificar POST não altera histórico de cotação/câmbio.

## 10. Testes de idempotência, concorrência e locking

- [x] 10.1 Testar mesmo payload sequencial e payload divergente com mesma key; verificar dois 201/um efeito no replay e 409 sem efeito no divergente.
- [x] 10.2 Testar retry após FX expirar e após Ativo/Corretora mudar lifecycle; verificar replay original imediato sem validações voláteis.
- [x] 10.3 Testar mesma key concorrente com payload igual e divergente; verificar espera PostgreSQL, resultado original compartilhado e somente vencedor com efeito.
- [x] 10.4 Testar rollback da reserva e retry após falhas de saldo, posição, Transacao, snapshot, auditoria e conclusão; verificar nenhuma chave permanentemente consumida.
- [x] 10.5 Simular timeout do cliente após commit e mudanças posteriores de saldo/posição; verificar replay preserva Transacao, valorOrigem, saldo e snapshot da posição originais.
- [x] 10.6 Testar BUY+BUY, duas BUY no mesmo ativo, BUY+SELL e SELL+SELL; verificar serialização, sem lost update/oversell e artefatos atômicos.
- [x] 10.7 Testar BUY+saque e depósitos/saques concorrendo com BUY/SELL; verificar Carteira como lock comum, saldo não negativo e snapshots coerentes.
- [x] 10.8 Testar duas BUY em ativos diferentes e keys diferentes na mesma carteira, além de carteiras diferentes; verificar ordem de locks, serialização somente onde exigida e ausência de deadlock.
- [x] 10.9 Testar snapshot concorrente sob operações mistas; verificar uma linha por carteira/data, estado final coerente e snapshot anterior imutável.
- [x] 10.10 Testar desativação concorrente de Ativo durante BUY e de Corretora durante BUY/SELL com latches; verificar share lock mantém lifecycle validado até commit e operação iniciada depois vê o novo estado.

## 11. Testes PostgreSQL, segurança e regressão

- [x] 11.1 Criar suite de schema V9 para checks, FKs RESTRICT, coerência moeda, UNIQUE Posicao/idempotência, projeção tipada completa, ausência de `valor_origem` em Transacao, trio de snapshot todo NULL/todo preenchido, saldo/total investido NOT NULL e índices; verificar diretamente em PostgreSQL Testcontainers.
- [x] 11.2 Criar testes de rollback PostgreSQL de toda a unidade financeira e probes de transação/locks; verificar saldo, posição, Transacao, snapshot, auditoria e reserva contam zero após falha.
- [x] 11.3 Criar testes API de 401, ADMIN 403, ownership 403+ACESSO_NEGADO, 404 e correlation ID; verificar auditoria sanitizada.
- [x] 11.4 Criar testes de auditoria financeira atômica, falha causando rollback, ausência de dados privados e Clock determinístico; verificar igualdade do instante em DEPOSITO/SAQUE/COMPRA/VENDA e colunas persistidas.
- [x] 11.5 Executar suites existentes de cash-movements, asset-catalog, broker-catalog, exchange-rates, market-quotes e security; verificar compatibilidade sem chamadas externas reais e sem H2.

## 12. Validação final da change

- [x] 12.1 Executar `backend\mvnw.cmd test` e `backend\mvnw.cmd verify` com PostgreSQL Testcontainers; corrigir somente falhas dentro do escopo e registrar resultado.
- [x] 12.2 Executar `npx.cmd openspec validate establish-investment-transactions-and-positions --strict` na raiz; verificar zero erros e warnings relevantes.
- [x] 12.3 Executar `git diff --check` e `git status`; revisar que somente V9 foi criada, V1–V8/PRD/Graphify não mudaram e não há frontend, migration extra, archive, commit, PUT/PATCH/DELETE ou provider no POST.
