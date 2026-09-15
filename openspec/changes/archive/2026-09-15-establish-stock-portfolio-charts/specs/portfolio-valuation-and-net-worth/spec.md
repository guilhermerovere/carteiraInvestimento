## ADDED Requirements

### Requirement: Classificação autoritativa para composição por ação
Cada posição aberta retornada por GET e POST `/api/v1/carteira/resumo` SHALL incluir a classificação canônica `tipo` do ativo, além dos campos já definidos. A classificação SHALL ser lida do ativo persistido participante da valuation e não inferida de ticker, mercado ou cotação. `valorAtualBrl` existente continua sendo a única fonte autoritativa para o valor atual em BRL usado pela composição; esta extensão não altera fórmulas, snapshots, providers nem a semântica resiliente/parcial existente da valuation: se cotação ou câmbio estiver indisponível, não inventa `valorAtualBrl`, preserva os demais dados persistidos da posição e não reintroduz falha integral do resumo.

#### Scenario: Posição com classificação persistida
- **WHEN** o resumo materializa posições abertas ACAO, FII e ETF
- **THEN** cada posição retorna seu `tipo` canônico e mantém o `valorAtualBrl` calculado pela valuation existente
