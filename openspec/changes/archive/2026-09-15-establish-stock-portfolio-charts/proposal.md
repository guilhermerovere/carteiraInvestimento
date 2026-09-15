## Why

O resumo atual informa o estado patrimonial e uma prévia de posições, mas não permite acompanhar a evolução histórica válida da carteira nem a participação real de cada ação. Esta change acrescenta somente essas duas leituras, com dados persistidos e valuation rastreável.

## What Changes

- Adiciona a leitura autenticada de evolução patrimonial baseada em `carteira_snapshots` com valuation materializado, sem interpolação, dados sintéticos ou cotações inventadas.
- Adiciona a composição da carteira como donut por ticker de `TipoAtivo.ACAO`, usando o `valorAtualBrl` autoritativo já entregue no resumo.
- Expõe `tipo` nas posições do resumo quando necessário para que o browser exclua FII e ETF sem inferir a classificação pelo ticker.
- Integra os dois gráficos ao resumo da carteira seguindo a composição, proporções, cards, espaçamento e hierarquia dos mockups aprovados, em Light e Dark e com comportamento responsivo.
- Substitui quaisquer micrográficos históricos dos cards por visualizações compactas do estado atual do resumo, sem ler `carteira_snapshots` nessa área.
- Mantém fora do escopo FII, ETF, proventos, recomendações, metas, DARF, IRPF e analytics adicionais.

## Capabilities

### New Capabilities

- `stock-portfolio-charts`: evolução histórica do patrimônio da carteira e composição atual exclusivamente por ação para a carteira do principal autenticado.

### Modified Capabilities

- `portfolio-valuation-and-net-worth`: expor a classificação de ativo nas posições do resumo e preservar a autoridade do resumo para valor atual BRL.
- `financial-portfolio-shell-and-read-experience`: apresentar os dois gráficos no resumo da carteira, de forma acessível e responsiva, sem alterar os demais fluxos de leitura.

## Impact

- Backend: consulta privada de snapshots e novo GET `/api/v1/carteira/graficos/evolucao`; DTO/OpenAPI e testes de autorização, ordenação e critério de valuation materializado.
- Frontend/BFF: proxy same-origin e query de evolução restritos ao gráfico principal; cards superiores usam apenas valores atuais de `/api/v1/carteira/resumo`, e a composição reutiliza o mesmo resumo.
- Persistência: apenas leitura de `carteira_snapshots`; nenhuma migration, novo provider, cotação, interpolação ou cálculo financeiro é introduzido.
