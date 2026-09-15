## ADDED Requirements

### Requirement: Gráficos de ações integrados ao resumo
`/carteira` SHALL apresentar, abaixo dos cards de resumo e integrado à área de `Minhas posições`, uma visualização de Evolução do Patrimônio e um donut de Composição da Carteira. A evolução SHALL identificar Valor investido e Resultado não realizado sem interpolar pontos ausentes. O donut SHALL mostrar somente ticker e participação de ACAO elegível. Os gráficos SHALL preservar estados de loading, vazio, indisponibilidade e erro de forma explícita, acessível e sem substituir os cards, a tabela ou seus dados autoritativos.

#### Scenario: Dados disponíveis
- **WHEN** o resumo possui ações elegíveis e a evolução possui snapshots com valuation materializado
- **THEN** a página apresenta os dois gráficos com legenda compreensível, valores formatados e a tabela Minhas posições preservada

#### Scenario: Sem dados gráficos elegíveis
- **WHEN** não há snapshots com valuation materializado ou não há ACAO com valor atual disponível
- **THEN** o respectivo gráfico apresenta estado vazio explícito e a página mantém cards e Minhas posições utilizáveis

### Requirement: Fidelidade visual e responsividade dos gráficos
Os mockups reais em `docs/ui-reference/Imagem do Codex 13 de set. de 2026, 21_17_22.png`, `Imagem do Codex 13 de set. de 2026, 21_17_39.png` e `Imagem do Codex 13 de set. de 2026, 21_17_49.png` SHALL ser a referência visual vinculante de layout, proporções, cards, espaçamento, tipografia, integração com Minhas posições e Light/Dark. Em desktop, os gráficos SHALL respeitar a grade e a largura útil do conteúdo principal; em tablet e mobile, SHALL empilhar ou reorganizar sem corte, overflow horizontal involuntário ou perda de legenda/tooltip. Requisitos funcionais de evolução e composição prevalecem onde os mockups não os representam.

#### Scenario: Tema e viewport reduzido
- **WHEN** ROLE_USER abre o resumo em Light ou Dark em viewport desktop, tablet ou mobile
- **THEN** cards, eixos, séries, donut, legenda e estados vazios preservam contraste, hierarquia e leitura sem depender somente de cor
