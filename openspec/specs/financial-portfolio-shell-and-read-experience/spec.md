# financial-portfolio-shell-and-read-experience Specification

## Purpose
Define experiência financeira autenticada sem mutações operacionais para a carteira individual, com consultas precisas, refresh técnico explícito de valuation, responsividade, acessibilidade e sessão protegida.

## Requirements

### Requirement: Shell autenticado, rotas e navegação financeira
The system SHALL make `/carteira`, `/carteira/posicoes`, `/carteira/transacoes`, and `/carteira/movimentacoes` exclusive to a confirmed ROLE_USER. Desktop SHALL retain persistent Valore navigation for Carteira, Posicoes, Transacoes, and Movimentacoes, with a visible accessible collapse/expand control and no clipping/overflow. The ROLE_USER `Operar` launcher SHALL be in the lower sidebar immediately above Recolher/Expandir, aligned to the same grid/gutters; expanded mode shows icon and label, while collapsed mode keeps a centered icon, tooltip, accessible name and functional Comprar/Depositar/Sacar menu. It MUST NOT offer global SELL, which remains contextual to an open position. Mobile SHALL retain the compact header and bottom navigation for Carteira, Posicoes, Transacoes, and Mais, through which Movimentacoes and Operar remain reachable without a duplicate header launcher. The desktop/mobile header SHALL retain only page context, theme, profile and account actions. If sessionStorage contains an ambiguous financial POST, the shell SHALL show a persistent accessible recovery indicator and a `Revisar operacao` action that reopens a read-only intent with safe details and same-key/same-payload retry. It MUST NOT offer cancellation or editing. ROLE_ADMIN SHALL remain outside the personal wallet experience and SHALL NOT receive personal financial operations.

#### Scenario: Navegação de usuário autenticado
- **WHEN** a confirmed ROLE_USER opens a wallet route
- **THEN** the responsive read shell remains available and lower-sidebar/mobile-Mais Operar starts BUY, deposit, or withdrawal without replacing navigation

#### Scenario: SELL is contextual
- **WHEN** the shell is rendered for ROLE_USER
- **THEN** SELL is not shown as a global launcher option and can be started from an open position

#### Scenario: Admin acessa rota de carteira
- **WHEN** ROLE_ADMIN accesses a wallet route or the application shell
- **THEN** the existing access-denied behavior remains, the session is retained, and no personal wallet launcher is shown

#### Scenario: Operar em viewport mobile
- **WHEN** ROLE_USER opens Mais on mobile
- **THEN** it exposes Comprar, Depositar and Sacar and opens an accessible responsive operation container without a duplicate header action or hidden bottom navigation content

#### Scenario: Operar em sidebar recolhida
- **WHEN** ROLE_USER collapses the desktop sidebar
- **THEN** Operar remains immediately above Expandir as a centered named icon with tooltip and a keyboard/touch functional compact anchored popover; its labels, icons, spacing and borders remain aligned without clipping or overflow in Light and Dark

#### Scenario: Recovery of ambiguous financial intent
- **WHEN** ROLE_USER returns to the shell while an ambiguous POST intent remains in sessionStorage
- **THEN** a persistent accessible indicator opens safe read-only operation details and manual retry with the original key and payload, without claiming cancellation

### Requirement: Tema e tokens semânticos
O sistema SHALL suportar tema global Light, Dark e System, persistir a preferência e respeitar a preferência do sistema quando selecionado System, sem flash perceptível ou mismatch relevante de hidratação. A supressão de hydration SHALL existir somente no elemento raiz necessário. Light SHALL usar superfícies claras com acento verde financeiro sóbrio; Dark SHALL usar hierarquia intencional de quase-preto/carvão com acento roxo profundo. Tokens SHALL cobrir superfícies e semânticas; componentes financeiros MUST NOT espalhar cores raw. Resultado financeiro SHALL comunicar sinal e texto, e nunca depender somente da cor. Login, cadastro e `/admin` SHALL permanecer funcionais e legíveis nos três temas; login e cadastro SHALL receber composição responsiva própria da Valore, com Light claro e verde sóbrio e Dark carvão com roxo profundo.

#### Scenario: Preferência de tema persistida
- **WHEN** o usuário escolhe Dark e recarrega uma rota de carteira
- **THEN** a rota reaparece em Dark antes da interação visual principal, preservando contraste e hierarquia próprios do tema

### Requirement: Branding e autenticação visual Valore
O produto SHALL exibir o nome Valore na metadata, login, cadastro e shell sem renomear contratos ou identificadores técnicos. Login e cadastro SHALL usar hero institucional à esquerda e formulário à direita em desktop, reduzindo o hero e priorizando o formulário em mobile. Campos SHALL possuir labels reais, autocomplete, foco visível, associação de erro e estados disabled/loading. O cadastro SHALL apresentar o rótulo `Nome Completo`, mantendo `nome` no payload existente. A autenticação MUST NOT oferecer providers sociais, recuperação de senha ou capacidades inexistentes.

#### Scenario: Cadastro preserva contrato
- **WHEN** a pessoa preenche `Nome Completo` e envia o cadastro
- **THEN** o frontend envia o valor no campo técnico `nome` do contrato existente

### Requirement: Fronteira financeira same-origin e precisa
O browser SHALL consumir somente `/api/finance/*` para dados financeiros. O BFF SHALL ler `auth_session` exclusivamente no servidor, chamar o backend com Bearer e nunca devolver JWT, Authorization, URL interna ou payload sensível. Todo handler SHALL validar entrada antes de chamar Spring e responder `Cache-Control: no-store`. Listagens aceitarão somente page e size, rejeitarão parâmetros desconhecidos e validarão page >= 0 e size nos limites backend; counters poderão ser Number somente após validação integer, finito, >= 0 e safe integer. Refresh SHALL rejeitar body ou query params inesperados. O BFF SHALL preservar tokens financeiros e normalizar dinheiro, quantidade, preço, taxas, FX e percentual para strings decimais antes do JSON frontend; nenhum valor financeiro SHALL ser convertido primeiro para Number. SHALL preservar X-Correlation-ID seguro no round-trip quando aceito/retornado pela infraestrutura.

#### Scenario: Decimal de alta precisão
- **WHEN** o backend devolve `0.10000001`, `0.00000001`, `123456789.12345678` ou `-123456789.12345678` em campo financeiro
- **THEN** o DTO recebido pelo browser contém exatamente a mesma representação decimal em string

### Requirement: Resumo patrimonial e refresh explícito
`/carteira` SHALL apresentar `patrimonioTotalBrl` como informação primária e contextualizar rentabilidade não realizada e lucro não realizado, com saldo em caixa, total investido, valor das posições e lucro realizado como métricas secundárias hierarquizadas. SHALL apresentar preview de posições abertas e caminho para a lista completa. O refresh SHALL ocorrer apenas por ação explícita, manter o último estado seguro, impedir duplicidade e, após sucesso, invalidar/refetch somente `portfolio.summary`, sem invalidar automaticamente posições ou resetar o cache inteiro.

#### Scenario: Atualização de mercado
- **WHEN** o usuário aciona Atualizar mercado
- **THEN** a interface mostra estado localizado de atualização sem inventar novo patrimônio e apresenta a resposta autoritativa após o refresh

### Requirement: Freshness e proveniência temporal
A experiência SHALL distinguir `valuationInstant`, `instanteCotacao` e `instanteCambio`. O resumo SHALL informar o instante de cálculo de carteira como contexto principal; os instantes de cotação e câmbio SHALL ficar em disclosure acessível secundário, sem alegar simultaneidade entre eles.

#### Scenario: Dados B3 e US com instantes distintos
- **WHEN** o resumo contém cotação e FX com horários diferentes
- **THEN** a interface identifica cada proveniência sem dizer que os dados foram cotados no mesmo instante

### Requirement: Leitura de posições responsiva
A página de posições SHALL tratar GET posições como autoridade para custódia, quantidade, preço médio, total investido, estado persistido e paginação; GET resumo SHALL ser autoridade para quote atual, valor atual BRL, lucro não realizado, rentabilidade, provider, instante de cotação e freshness. Poderá enriquecer custódia pelo último summary somente por `ativoId`, nunca ticker, índice ou ordem. Se uma posição não existir no summary, valuation SHALL mostrar indisponível/não atualizado e MUST NOT ser recalculado, inventado, vinculado a outro ativo ou exibido como snapshot antigo sem indicação. A página SHALL ser resiliente a instantes diferentes entre endpoints.

#### Scenario: Posições em mobile
- **WHEN** a página de posições é aberta em viewport mobile
- **THEN** não usa tabela horizontal gigante e mantém os detalhes acessíveis por disclosure

### Requirement: Transações e movimentações de caixa somente para leitura
As páginas SHALL consumir as APIs paginadas existentes e mostrar somente campos devolvidos. Transações SHALL priorizar BUY/SELL textual, ticker, data, valor BRL e resultado realizado; `corretoraId` SHALL aparecer só como Identificador da corretora ou ser omitido, nunca como nome ou por join frágil. Movimentações SHALL apresentar saldo de caixa e histórico com tipo, valor, descrição e data; saldo resultante por linha SHALL ser mostrado somente se fizer parte da resposta daquele item.

#### Scenario: Histórico vazio
- **WHEN** a API retorna coleção vazia de transações ou movimentações
- **THEN** a página apresenta estado vazio neutro sem CTA operacional morto

### Requirement: Falhas rastreáveis e estados isolados
O BFF e cliente SHALL preservar ProblemDetail seguro, status e `X-Correlation-ID`, expondo o código de suporte secundariamente com opção de cópia. 401 SHALL seguir o fluxo de sessão existente; 403 SHALL preservar sessão e informar acesso negado; 409 no refresh SHALL informar concorrência e permitir nova tentativa/refetch; 502 de mercado SHALL preservar dados já renderizados quando seguros e informar indisponibilidade sem afirmar perda de carteira. Erros de uma lista SHALL ser isolados do resumo carregado.

#### Scenario: Falha de refresh com dados existentes
- **WHEN** refresh retorna 502 após o resumo ter sido carregado
- **THEN** o último resumo permanece visível, a mensagem informa indisponibilidade de mercado e oferece retry

### Requirement: Estados de qualidade, responsividade e acessibilidade
As páginas SHALL fornecer skeletons dimensionados para resumo e listagens, paginação acessível, estados de erro e vazio, foco visível, contraste WCAG AA, semântica apropriada de tabelas/dialogs/disclosures, alvos de toque adequados, navegação por teclado, `aria-live` para refresh/erro e respeito a reduced motion. Desktop, tablet e mobile SHALL preservar legibilidade e informação prioritária sem depender apenas de cor.

#### Scenario: Navegação por teclado
- **WHEN** usuário navega o shell, toggle de tema e disclosures por teclado
- **THEN** todos recebem foco visível, nome acessível e ordem de foco utilizável

### Requirement: Cobertura de validação da experiência de leitura
A suíte SHALL cobrir tema, persistência, shell, regressão legível de login/admin, BFF estrito/no-store/correlation ID, normalização decimal, formatadores string-aware, paginação segura, resumo, autoridades e join por ativoId de posições, valuation ausente, transações, movimentações, loading, vazio, 401, 403, 409, 502 e responsividade. Smoke E2E SHALL cobrir login até carteira, persistência de tema, carteira→posições, transações, movimentações, refresh e regressão mínima de login/admin. A validação SHALL incluir build de produção e QA visual documentado Light/Dark em desktop, tablet e mobile.

#### Scenario: Regressão de precisão é detectada
- **WHEN** uma resposta financeira contém decimal fracionário, percentual negativo ou valor BRL de grande magnitude
- **THEN** testes comprovam que a fronteira backend-BFF-DTO não perde precisão

### Requirement: User asset discovery route and compact operation controls
The personal shell SHALL expose `/carteira/ativos` to every `ROLE_USER` in coherent desktop and mobile navigation. The page SHALL be a compact investment discovery surface rather than a CRUD form. A collapsed desktop Operar control SHALL use a small anchored popover for Comprar, Depositar and Sacar.

#### Scenario: Desktop user navigation
- **WHEN** a user opens the personal shell
- **THEN** Carteira, Posicoes, Ativos, Transacoes and Movimentacoes are reachable
- **AND** collapsed Operar opens only a compact anchored action popover.

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
