## 1. Backend discovery US — Red → Green → Refactor

- [x] 1.1 **Red:** estender `QuoteProviderAdaptersTest` e `AtivoControllerTest` para exigir discovery `mercado=US` para AAPL com `/stocks` seguido de `/quote`, `cotacaoAtual` USD positiva e canônica, e tratar quote não confirmável ou provider indisponível sem persistência nem credencial exposta.
- [x] 1.2 **Green:** ajustar `TwelveDataQuoteAdapter` para qualificar cada candidato US elegível com `GET /quote?symbol=<ticker>&apikey=<server-secret>`, construir `AssetPreview` somente com quote válido e reutilizar exceções sanitizadas; manter `/stocks`, a revalidação de `registrar` e as regras existentes.
- [x] 1.3 **Refactor:** extrair a qualificação de prévia/quote para eliminar duplicação sem ampliar tipos ou mercados, nem mudar câmbio, autorização, lifecycle ou transação; reexecutar os testes focalizados do backend.

## 2. BFF e contrato do cliente — Red → Green → Refactor

- [x] 2.1 **Red:** ampliar `frontend/src/app/api/finance/finance-routes.test.ts` para requerer `mercado=B3|US`, rejeitar ausência, valor inválido ou campo extra, encaminhar `q` e mercado e remover campos do provider da prévia US.
- [x] 2.2 **Green:** alterar a rota BFF de discovery, `financeApi` e tipos compartilhados para aceitar mercado tipado e forward allowlisted, sem B3 implícito, derivação de mercado ou exposição de payload/chave do Twelve Data.
- [x] 2.3 **Refactor:** centralizar a validação/serialização de mercado preservando correlation ID, `no-store` e ProblemDetail pt-BR; reexecutar o teste BFF focalizado.

## 3. Seletor de ativo — Red → Green → Refactor

- [x] 3.1 **Red:** adicionar em `selectors.test.tsx` cobertura para seletor acessível B3/US, limpeza de prévia/erro e cache na troca, busca AAPL em US, prévia cotada e confirmação apenas com `registerAsset("AAPL", "US")` após 404 do catálogo.
- [x] 3.2 **Green:** implementar o controle explícito de mercado e propagá-lo pela query key, discovery, prévia e confirmação; renderizar US em USD e manter recuperação de indisponibilidade.
- [x] 3.3 **Refactor:** simplificar o estado local para não reutilizar prévia/falha entre mercados, preservando cadastro ticker+mercado desacoplado do formulário financeiro; reexecutar os testes do seletor.

## 4. BUY end-to-end — Red → Green → Refactor

- [x] 4.1 **Red:** criar caso Playwright com AAPL inicialmente ausente do catálogo e mock de discovery US cotada: selecionar US, conferir prévia, cadastrar por `POST /api/v1/acoes` somente com `{ticker:"AAPL",mercado:"US"}`, confirmar que ainda não houve POST financeiro e então concluir BUY com o `exchangeRateId` existente.
- [x] 4.2 **Green:** atualizar apenas mock/fixtures e integração necessária para o cenário, assegurando que indisponibilidade não oferece cadastro e B3/US continuam selecionáveis.
- [x] 4.3 **Refactor:** centralizar rotas/fixtures de catálogo no mock E2E, removendo duplicação e mantendo o teste independente de preço ou credencial reais do provider; reexecutar o spec focalizado.

## 5. Verificação final

- [x] 5.1 Executar no diretório `backend` `.\\mvnw.cmd clean verify` e confirmar que testes unitários e de integração, incluindo segurança/persistência do catálogo, permanecem verdes.
- [x] 5.2 Executar no diretório `frontend` `npm.cmd run lint`, `npm.cmd run typecheck`, `npm.cmd run test` e `npm.cmd run build`; confirmar que BFF, seletor e contratos existentes continuam verdes.
- [x] 5.3 Executar no diretório `frontend` `npm.cmd run test:e2e -- e2e/operations.spec.ts` e confirmar AAPL ausente → prévia US cotada → cadastro → BUY, sem regressão de B3, câmbio, autorização ou separação entre cadastro e POST financeiro.

## 6. Correções da revisão antes do archive

- [x] 6.1 **Red:** adicionar em `selectors.test.tsx` cobertura para filtrar o catálogo pelo mercado selecionado, impedir que busca exata selecione ativo de outro mercado e ignorar respostas atrasadas de `precheck` que tentem restaurar seleção, prévia ou erro após troca B3/US.
- [x] 6.2 **Green/Refactor:** filtrar candidatos cadastrados e de discovery pelo mercado explícito, conferir `found.mercado` antes de `onChange` e invalidar a geração assíncrona de `precheck` na troca de mercado, preservando a seleção normal do mercado correto.
- [x] 6.3 Reexecutar testes focados e as verificações finais pertinentes (`test`, `lint`, `typecheck`, `build` e E2E), validar a change estritamente e parar para revisão antes do archive.

## 7. Proteção do cadastro pendente antes do archive

- [x] 7.1 **Red:** adicionar em `selectors.test.tsx` cobertura para ignorar o sucesso tardio de `registerAsset` após troca US → B3 e para impedir que o lookup assíncrono após conflito `409` selecione o ativo do mercado anterior.
- [x] 7.2 **Green/Refactor:** estender a geração assíncrona do seletor ao cadastro, à invalidação de cache e à recuperação de conflito, conferindo também o mercado retornado antes de `onChange` e ignorando estado tardio da operação invalidada.
- [x] 7.3 Reexecutar testes focados e as verificações finais pertinentes (`test`, `lint`, `typecheck`, `build` e E2E), reutilizar a evidência recente do backend sem novo diff backend, validar a change estritamente e parar para revisão antes do archive.
