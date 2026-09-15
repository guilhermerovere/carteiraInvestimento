## 1. Contratos e leitura histórica

- [x] 1.1 Localizar a entidade/repositório de `carteira_snapshots` e criar a projeção privada de evolução com data, total investido e resultado não realizado; verificar consulta cronológica ascendente por carteira.
- [x] 1.2 Aplicar na consulta o critério objetivo de valuation materializado (`valuationInstant`, valor de posições, resultado não realizado e patrimônio não nulos); verificar que snapshots sem valuation materializado não retornam e que não há interpolação.
- [x] 1.3 Implementar GET `/api/v1/carteira/graficos/evolucao` derivando usuário/carteira do SecurityContext; verificar 401 anônimo, 403 ADMIN, isolamento entre usuários e coleção vazia quando não houver pontos elegíveis.
- [x] 1.4 Documentar no OpenAPI autenticação, estrutura da série, coleção vazia, critério de valuation materializado, ordenação cronológica, 401 e 403; verificar ProblemDetail sanitizado e preservação de `X-Correlation-ID` em falha tratável.

## 2. Resumo e fronteira same-origin

- [x] 2.1 Estender a posição do resumo com `tipo` canônico persistido sem alterar fórmulas, snapshots ou providers; verificar ACAO/FII/ETF e `valorAtualBrl` existente no contrato de resumo.
- [x] 2.2 Adicionar o BFF GET `/api/finance/portfolio/charts/evolution` com bearer apenas no servidor, no-store e mapeamento decimal como string; verificar forwarding, erros seguros e ausência de JWT no browser.
- [x] 2.3 Criar chave/query de evolução e invalidá-la somente após refresh de resumo bem-sucedido; verificar que falha ou carregamento de evolução não invalida cards, posições ou cache global.

## 3. Gráficos do resumo

- [x] 3.1 Implementar o componente de Evolução do Patrimônio com séries Valor investido e Resultado não realizado, eixos/tooltip/legenda formatados em BRL e sem criar ponto para data sem valuation; verificar pontos, valores negativos e estado vazio.
- [x] 3.2 Implementar o donut de Composição filtrando estritamente `tipo === 'ACAO'` e `valorAtualBrl` autoritativo; verificar fatias individuais BBAS3/PETR4/ITUB4/AAPL e exclusão completa de FII/ETF.
- [x] 3.3 Preservar valores financeiros como strings até a fronteira de renderização do gráfico; verificar que nenhum cálculo, percentual ou fallback usa Number como estado canônico nem estima posição indisponível.
- [x] 3.4 Inserir a faixa de gráficos após os cards e antes de Minhas posições, com Evolução dominante e donut complementar; verificar que cards, tabela e hierarquia do mock principal permanecem preservados.
- [x] 3.5 Implementar loading, vazio, indisponibilidade e erro isolados para cada gráfico com texto acessível; verificar que Minhas posições e os cards permanecem utilizáveis em todos os estados.

## 4. Responsividade, tema e acessibilidade

- [x] 4.1 Aplicar tokens Light/Dark aos cards, eixos, séries, donut, tooltip e legendas; verificar contraste, sinal textual e estados que não dependem somente de cor.
- [x] 4.2 Ajustar desktop, tablet e mobile conforme os mockups em `docs/ui-reference/`; verificar banda 2/3–1/3 em desktop, empilhamento legível em espaço reduzido e ausência de clipping/overflow horizontal involuntário.
- [x] 4.3 Adicionar descrições, tabelas/alternativas textuais ou equivalentes acessíveis para valores e séries; verificar navegação por teclado, leitor de tela e tooltips não bloqueantes.

## 5. Verificação

- [x] 5.1 Adicionar testes backend para critério de snapshots com valuation materializado, ordenação, vazio, autorização, isolamento, falha tratável e contrato OpenAPI; verificar a suíte focada verde.
- [x] 5.2 Adicionar testes frontend/BFF para tipo no resumo, filtro ACAO, exclusão FII/ETF, valores indisponíveis, query isolation e estados dos gráficos; verificar Vitest/RTL verde.
- [x] 5.3 Adicionar smoke Playwright e QA visual em Light/Dark nos viewports desktop, tablet e mobile; verificar posição relativa aos cards/Minhas posições, dados reais, responsividade e acessibilidade.
- [x] 5.4 Executar backend/frontend pertinentes e `npx.cmd openspec validate establish-stock-portfolio-charts --strict`; verificar comandos verdes e `git diff --check` sem alteração de produção fora do escopo.

## 6. Refinamento visual dos cards

- [x] 6.1 Estender a projeção de evolução com `valorPosicoesBrl` e `patrimonioTotalBrl` do mesmo snapshot materializado; verificar contrato backend/BFF sem nova fórmula ou leitura externa.
- [x] 6.2 Substituir micrográficos históricos dos cards por visuais atuais de composição, proporção, comparação e estado; verificar zero/um snapshot, Light/Dark e mobile sem overflow.
- [x] 6.3 Aplicar tokens ao plot principal em Light/Dark e executar verificações focadas, build, verify, validação strict e diff check.
