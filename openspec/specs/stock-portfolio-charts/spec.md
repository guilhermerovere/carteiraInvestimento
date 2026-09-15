# stock-portfolio-charts Specification

## Purpose
Define leituras gráficas privadas e rastreáveis para apresentar a evolução patrimonial histórica válida da carteira e a composição atual exclusivamente por ações, sem criar ou inferir dados financeiros.

## Requirements

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
