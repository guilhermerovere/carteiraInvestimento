## Purpose

Define leituras gráficas privadas e rastreáveis para apresentar a evolução patrimonial histórica válida da carteira e a composição atual exclusivamente por ações, sem criar ou inferir dados financeiros.

## ADDED Requirements

### Requirement: Evolução patrimonial por snapshots com valuation materializado
GET `/api/v1/carteira/graficos/evolucao` SHALL exigir ROLE_USER e derivar a carteira exclusivamente do SecurityContext. A resposta SHALL conter pontos em ordem cronológica ascendente, cada um com `dataReferencia`, `totalInvestidoBrl`, `resultadoNaoRealizadoBrl`, `valorPosicoesBrl` e `patrimonioTotalBrl`, lidos diretamente do mesmo `carteira_snapshots` da carteira autenticada cujo valuation esteja materializado: `valuationInstant`, `valorPosicoesBrl`, `lucroNaoRealizadoBrl` e `patrimonioTotalBrl` não nulos. Snapshots sem valuation materializado SHALL ser ignorados; o endpoint não SHALL interpolar datas, preencher lacunas, fazer backfill, consultar providers, recalcular snapshots históricos, usar preço médio como preço atual ou inventar cotação histórica. A ausência de pontos elegíveis SHALL retornar uma coleção vazia, não erro nem ponto sintético.

#### Scenario: Série com lacuna de valuation
- **WHEN** snapshots cronológicos incluem um snapshot sem valuation materializado entre dois snapshots com valuation materializado
- **THEN** a resposta contém somente os dois pontos materializados, sem criar ponto para a data sem valuation

#### Scenario: Isolamento e autorização
- **WHEN** usuário anônimo, ROLE_ADMIN ou ROLE_USER acessa a rota
- **THEN** anônimo recebe 401, ROLE_ADMIN recebe 403 e ROLE_USER recebe somente os snapshots com valuation materializado da própria carteira

### Requirement: Composição atual exclusivamente por ação
A composição SHALL ser derivada no cliente somente das posições abertas do `/api/v1/carteira/resumo` cuja classificação seja `TipoAtivo.ACAO` e cujo `valorAtualBrl` seja autoritativo e não nulo. Cada fatia SHALL representar um ticker individual, usar seu valor atual BRL real e não agrupar por tipo, mercado ou setor. FII e ETF SHALL ser excluídos, mesmo que presentes no resumo; ausência de ações elegíveis SHALL produzir estado vazio, não percentuais ou valores inventados.

#### Scenario: Carteira mista
- **WHEN** o resumo contém BBAS3, PETR4, AAPL, um FII e um ETF com valores atuais
- **THEN** o donut contém uma fatia individual para BBAS3, PETR4 e AAPL e não contém fatia para o FII ou ETF

#### Scenario: Valor atual indisponível
- **WHEN** uma posição ACAO não possui `valorAtualBrl` autoritativo no resumo
- **THEN** ela não integra o donut e a interface comunica a indisponibilidade sem estimar seu valor

### Requirement: Contrato e erros seguros dos gráficos
O endpoint de evolução SHALL retornar somente dados da carteira do principal autenticado. Erros tratáveis SHALL usar ProblemDetail sanitizado e preservar `X-Correlation-ID`, sem expor detalhes internos nem dados de outra carteira. OpenAPI SHALL documentar autenticação, estrutura da série, coleção vazia, critério de valuation materializado, ordenação cronológica e respostas 401/403.

#### Scenario: Falha tratável durante a leitura da evolução
- **WHEN** ocorre falha tratável durante a leitura da evolução
- **THEN** a resposta usa ProblemDetail sanitizado e preserva `X-Correlation-ID` sem expor detalhes internos nem dados de outra carteira

## ADDED Requirements

### Requirement: Visualizações atuais dos cards do resumo
Os cards superiores SHALL usar exclusivamente os valores atuais já recebidos de `/api/v1/carteira/resumo`; não SHALL consultar, receber ou depender de `carteira_snapshots`, da série de evolução, de `hasUsefulHistory` ou de qualquer gráfico histórico. Patrimônio total SHALL apresentar composição de `saldoCaixaBrl` e `valorPosicoesBrl`. Saldo em caixa e Valor das posições SHALL apresentar sua proporção no patrimônio somente quando `patrimonioTotalBrl` for maior que zero. Total investido SHALL apresentar sempre as duas quantidades reais `totalInvestidoBrl` e `valorPosicoesBrl` como comparação Investido/Atual. Resultado realizado SHALL comunicar “Resultado acumulado com vendas”; resultado não realizado SHALL comunicar seu estado positivo, negativo ou neutro sem inventar escala ou série. Proporções e comparações SHALL usar decimal canônico e `Decimal` antes da fronteira de renderização.

#### Scenario: Um ou nenhum snapshot histórico
- **WHEN** o resumo possui valores atuais, mas a evolução tem zero ou um snapshot
- **THEN** todos os cards superiores exibem suas visualizações atuais completas, sem ponto isolado, barra histórica única, interpolação ou área vazia

#### Scenario: Resultado não realizado negativo
- **WHEN** `lucroNaoRealizadoBrl` atual é negativo
- **THEN** o card comunica o estado negativo por sinal textual além do tratamento cromático, sem linha histórica

#### Scenario: Tema escuro
- **WHEN** o usuário usa tema Dark
- **THEN** as visualizações atuais, o plot de Evolução Patrimonial, grid, ticks, tooltip, legenda e barras usam tokens do tema e não exibem superfície clara fixa
