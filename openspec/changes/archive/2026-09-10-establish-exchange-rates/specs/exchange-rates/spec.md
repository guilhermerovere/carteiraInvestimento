## Purpose

Fornece observações atuais, reais, persistidas e rastreáveis de USD para BRL para exibição e confirmação futura de operações US, sem misturar câmbio com cotação de ativos.

## ADDED Requirements

### Requirement: Observação USD/BRL válida, precisa e temporalmente rastreável
O sistema SHALL suportar exclusivamente o par canônico USD→BRL. A observação aceita SHALL ter UUID, moedas, taxa, `provider`, `instanteCotacao` e `registradoEm`. Taxa SHALL ser parseada sem double/float, positiva antes e depois de uma única normalização `setScale(8, HALF_EVEN)`, e caber em `NUMERIC(18,8)`; excesso de casas não a invalida se normalizável, mas round-to-zero e overflow a invalidam. `instanteCotacao` SHALL ser Instant confiável fornecido pelo contrato do provider, e `registradoEm` SHALL ser gerado pelo backend via Clock ao aceitar a observação. O sistema MUST NOT consultar BRL→BRL, inventar tempo ou taxa 1:1, nem acoplar-se a Cotacao de ativos.

#### Scenario: Taxa normalizável é aceita
- **WHEN** provider devolve USD/BRL positivo com mais de oito casas que normaliza positivamente e cabe em NUMERIC(18,8)
- **THEN** a observação é persistida com taxa de oito casas HALF_EVEN e ambos os timestamps obrigatórios

#### Scenario: Taxa ou tempo inválido é rejeitado
- **WHEN** taxa é zero, negativa, overflow, arredonda para zero ou o provider não fornece instante confiável
- **THEN** não há histórico/cache e a observação não é retornada

### Requirement: Cadeia de providers classificada semanticamente
Em miss, AlphaVantage SHALL ser chamado com `CURRENCY_EXCHANGE_RATE`, `from_currency=USD` e `to_currency=BRL`. Falha elegível SHALL chamar TwelveData `/exchange_rate` com `symbol=USD/BRL`; `currency_conversion` MUST NOT ser usado. Cada adapter SHALL classificar status HTTP e, antes de concluir sucesso, JSON, campos semânticos conhecidos de erro/rate limit/credencial, par retornado, taxa, timestamp e decode. HTTP 200 com rate limit, taxa/timestamp ausente ou inválido, ou payload inutilizável SHALL ser elegível; HTTP 200 com erro inequívoco de API key, assim como key ausente ou 400/401/403 inequivocamente de configuração, SHALL encerrar a cadeia sem fallback contínuo. Twelve vencedor SHALL ser `TWELVE_DATA`; conclusão sem observação válida SHALL ser 502 sanitizado.

#### Scenario: HTTP 200 semântico é classificado corretamente
- **WHEN** Alpha retorna 200 com rate limit, erro conhecido de credencial ou resposta normal sem taxa válida
- **THEN** o primeiro usa fallback, o segundo encerra por configuração e o terceiro usa fallback, sem tratar todos indistintamente como payload

#### Scenario: TwelveData fornece timestamp contratual
- **WHEN** TwelveData `/exchange_rate` retorna `symbol=USD/BRL`, `rate` e `timestamp` Unix válidos
- **THEN** o timestamp é convertido deterministicamente para `instanteCotacao`

### Requirement: Deadline funcional, cache e coordenação local
A validade de uma observação SHALL terminar em `deadline = registradoEm + 5 minutos`, válida se `now <= deadline` e expirada se `now > deadline`, usando Clock server-side. O futuro `exchangeRateId` usa exclusivamente essa regra, nunca `instanteCotacao`. Cache `USD:BRL` SHALL reter no máximo cinco minutos e MUST validar a mesma deadline absoluta antes de devolver hit; cache.put após commit MUST NOT estender a vida funcional. Hit SHALL preservar id, moedas, taxa, provider, instanteCotacao e registradoEm, sem nova chamada ao provider e sem histórico. Miss/expiração SHALL coordenar callers localmente por chave e reverificar cache no lock. Sem observação válida, MUST NOT usar stale cache, histórico ou taxa inventada; Redis e lock distribuído são excluídos.

#### Scenario: Hit próximo da deadline não prolonga validade
- **WHEN** observação cacheada é consultada após `registradoEm + 5 minutos`
- **THEN** ela não é devolvida, ocorre nova resolução externa e nenhuma vida adicional é criada por cache.put tardio

#### Scenario: Miss concorrente compartilha observação
- **WHEN** callers simultâneos na mesma instância encontram USD:BRL expirado
- **THEN** a carga coordenada realiza idealmente uma resolução e persistência e devolve o mesmo UUID aos callers

### Requirement: Histórico e schema FX governados por Flyway
O sistema SHALL planejar `V8__create_historico_cambio.sql`, sem alterar V1–V7, para criar `historico_cambio` com `id UUID` PK, `moeda_origem CHAR(3)`, `moeda_destino CHAR(3)`, `taxa NUMERIC(18,8)`, `provider`, `instante_cotacao` e `registrado_em`, todos NOT NULL. Checks SHALL exigir USD, BRL, taxa > 0 e provider conhecido; índice SHALL ser `(moeda_origem, moeda_destino, instante_cotacao DESC, registrado_em DESC, id DESC)`. Toda obtenção externa válida SHALL criar nova linha, inclusive taxa igual; hit e falha SHALL criar nenhuma. Não haverá UNIQUE, purge, stale fallback ou endpoint público de histórico.

#### Scenario: Observações externas iguais permanecem eventos distintos
- **WHEN** duas resoluções externas válidas retornam a mesma taxa
- **THEN** ambas são linhas históricas distintas e possuem provider coerente

### Requirement: API segura, transação e auditoria de indisponibilidade
O sistema SHALL expor exclusivamente GET `/api/v1/cambio/usd-brl`, sem body, mutação, refresh ou histórico público. USER e ADMIN SHALL receber 200 com id, moedas, taxa de escala 8, instanteCotacao, provider e registradoEm; anônimo SHALL receber 401. Provider HTTP SHALL executar fora de transação PostgreSQL; validação seguirá para bean transacional separado ou mecanismo equivalente que faça save, flush e commit antes do cache.put. Falha de persistência SHALL não cachear e será 500 quando independente do upstream; impossibilidade upstream/configuração será 502. ProblemDetail SHALL ser sanitizado e correlacionado. Auditoria isolada best-effort SHALL ocorrer quando a resolução termina sem FX válido por integração, mas não em hit, Alpha sucesso ou fallback bem-sucedido.

#### Scenario: Falha final é auditada sem substituir resposta
- **WHEN** Alpha e fallback elegível falham, ou Alpha falha não elegivelmente por credencial/configuração e nenhuma taxa é entregue
- **THEN** a capability tenta auditoria sanitizada isolada e responde 502 mesmo se a auditoria falhar

### Requirement: Contrato futuro de exchangeRateId
Para futura BUY/SELL US, frontend SHALL consultar GET FX, mostrar a observação devolvida e enviar somente `exchangeRateId`; taxa numérica não é autoridade. O id será obrigatório para US e dispensado para B3. A futura Transacao SHALL validar existência, USD→BRL e `now <= registradoEm + 5 minutos` com Clock server-side; id inexistente, inadequado ou expirado SHALL resultar futuramente em 409. Transacao SHALL copiar `taxaCambioBrl` ao snapshot financeiro e histórico FX não o substitui.

#### Scenario: Identificador futuro expirado
- **WHEN** futura confirmação US ocorre após `registradoEm + 5 minutos`
- **THEN** a futura capability rejeita com 409 e solicita nova obtenção FX
