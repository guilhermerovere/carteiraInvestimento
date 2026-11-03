## 1. Red — Reproduzir a regressão e fixar contratos

- [x] 1.5 Adicionar teste de adapter com o envelope atual observado de `/api/v2/tickers` (`results[]`, `symbol`, `subType`, B3/BRL) para SANB11; verificar a falha anterior de desserialização/`payload` antes de aceitar o contrato normalizado.
- [x] 1.6 Adicionar teste da rota administrativa BFF para o POST simplificado de SANB11 e confirmar o encaminhamento estrito para `/api/v1/acoes` com apenas ticker e mercado.

- [x] 1.1 Adicionar fixtures JSON com identidade de `/api/v2/tickers` e cotação de `/api/v2/stocks/quote` sem `type` para SANB11/unit, HGLG11/fii, BOVA11/etf e PETR4/stock; verificar que os testes do adapter falham antes da implementação especificamente por usar o payload de quote/heurística atual para classificar.
- [x] 1.2 Adicionar testes de adapter para subtipo ausente/desconhecido, match canônico exato, quote BRL, mismatch de símbolo e divergência de moeda; verificar que os casos inválidos esperam erro sanitizável e nunca produzem metadata/preview combinada.
- [x] 1.3 Adicionar testes de aplicação e controller para preview enriquecida não persistente e registro sem `save` em divergência, subtipo não suportado ou payload obrigatório ausente; verificar que os novos testes falham pelos campos/comportamentos ainda inexistentes e que POST continua aceitando somente ticker/mercado.
- [x] 1.4 Adicionar testes BFF e frontend para o contrato allowlisted/lossless e para a prévia explícita antes de “Cadastrar ativo”, incluindo branding ausente e falha recuperável; verificar que os testes falham porque discovery ainda contém apenas ticker/mercado e o cadastro ainda não exige prévia.

## 2. Green — Corrigir resolução B3 no backend

- [x] 2.1 Separar no cliente/DTOs Brapi o contrato de identidade de `/api/v2/tickers` do contrato de cotação de `/api/v2/stocks/quote`, preservando busca pública sem credencial obrigatória e autenticação de quote conforme configuração; verificar com testes de deserialização e de chamadas ao endpoint correto.
- [x] 2.2 Implementar o mapeamento exclusivo `unit|stock` → `ACAO`, `fii` → `FII`, `etf` → `ETF` a partir da identidade e rejeitar todo subtipo nulo, blank ou não suportado; verificar que as quatro fixtures reais passam e que nenhuma classificação consulta `type` do quote ou sufixo do ticker.
- [x] 2.3 Implementar a resolução compartilhada identidade → cotação, cotando o ticker canônico e exigindo igualdade de símbolo, B3/BRL e preço válido, com join por ticker em respostas multi-symbol; verificar que mismatch, payload malformado e moeda divergente retornam a falha esperada sem metadata combinada.
- [x] 2.4 Evoluir a porta de discovery e o valor de aplicação para carregar ticker, nome, tipo, mercado, moeda, branding opcional e cotação atual, reutilizando a resolução B3 tanto na prévia quanto na revalidação do registro; verificar nos testes de serviço que discovery não salva e POST revalida antes do único `save` possível.
- [x] 2.5 Enriquecer `GET /api/v1/acoes/discovery` e seu response DTO/OpenAPI sem alterar o request estrito de `POST /api/v1/acoes`; verificar nos testes de controller os campos allowlisted, autorização existente, parâmetros estritos, ausência de mutação e erros sanitizados.

## 3. Green — Entregar a prévia no BFF e frontend

- [x] 3.1 Atualizar `AssetDiscovery`, o mapper BFF e a rota de discovery para validar/encaminhar somente ticker, nome, tipo, mercado, moeda, branding opcional e `cotacaoAtual` lossless, sem payload bruto ou credenciais; verificar com testes da rota/mapper os casos válido, decimal exato e resposta backend malformada.
- [x] 3.2 Atualizar cliente e `AssetSelector` para renderizar logo/fallback, ticker, nome, tipo, B3/BRL e preço atual em uma prévia não persistente, habilitando confirmação apenas para preview válida; verificar com testes de interação que selecionar/abandonar a prévia não chama registro.
- [x] 3.3 Fazer a confirmação enviar somente ticker canônico e mercado ao POST existente, mantendo seleção direta de ativo já catalogado, tratamento de 409 e retry sanitizado; verificar com testes de interação que confirmação chama uma única vez `registerAsset(ticker, mercado)` e invalida o catálogo apenas após sucesso.

## 4. Refactor — Remover caminhos ambíguos e consolidar limites

- [x] 4.1 Remover a inferência de tipo por regex/sufixo, `type` do DTO de quote quando não necessário e estruturas ticker-only obsoletas, consolidando canonicalização/join sem duplicar regras; verificar que busca no código não encontra fallback de classificação B3 e que todos os testes focados continuam verdes.
- [x] 4.2 Revisar nomes, logs e categorias de erro para não expor payload/credencial e distinguir not-found, subtipo inválido e indisponibilidade/divergência upstream; verificar que os testes de ProblemDetail/correlation id e os asserts de ausência de segredo passam.
- [x] 4.3 Revisar o fluxo completo contra proposal, design, delta spec e PRD, confirmando que não houve migration, reclassificação histórica, mudança em BUY/SELL, US ou admin; verificar por diff que somente arquivos previstos da correção e seus testes foram alterados.

## 5. Verificação final

- [x] 5.1 Executar os testes backend focados de adapter, aplicação e controller e depois `.\mvnw.cmd clean verify`; verificar conclusão sem falhas e sem escrita inesperada em dados/artefatos fora do build.
- [x] 5.2 Executar no frontend os testes focados da rota/mapper/seletor e depois os scripts completos disponíveis de test, lint, typecheck e build; verificar conclusão sem falhas e a prévia acessível por nome/role/status.
- [x] 5.3 Executar `npx.cmd openspec validate fix-brapi-b3-asset-metadata --strict` e revisar `npx.cmd openspec status --change fix-brapi-b3-asset-metadata`; verificar validação estrita aprovada e todos os artefatos de planejamento coerentes.
- [x] 5.4 Comparar o estado final do Git com o baseline, preservar integralmente as modificações Graphify preexistentes e verificar que nenhum arquivo em `graphify-out/` foi criado, restaurado ou atualizado pela implementação.
- [x] 5.5 Reconstruir e reiniciar as imagens backend/frontend a partir desta árvore após a última correção, confirmar os serviços locais acessíveis e validar em Catálogos globais → Ativos → Adicionar ativo o POST administrativo e o resultado no catálogo; essa tela não oferece discovery ou prévia, que permanecem cobertos no fluxo de usuário pelas tarefas 3.2, 3.3 e 5.2.
  - Evidência manual informada: `docker compose up --build` foi executado após a correção do envelope `results[]` da Brapi, com os containers reconstruídos e a aplicação acessível.
  - Resultado observado: SANB11 foi cadastrada e aparece ativa no catálogo como `ACAO`, `B3`, `BRL`; BOVA11 apresentou indisponibilidade em uma tentativa e foi cadastrada normalmente ao repetir, sem exigir novo cadastro dos registros já existentes.
  - Limitação aceita: os logs registraram timeouts intermitentes da Brapi, sem causa específica determinada; o prazo atual foi mantido por decisão explícita.
