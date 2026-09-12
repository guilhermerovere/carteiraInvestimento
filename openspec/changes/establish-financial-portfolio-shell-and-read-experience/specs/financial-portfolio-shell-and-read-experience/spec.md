## Purpose

Define experiência financeira autenticada sem mutações operacionais para a carteira individual, com consultas precisas, refresh técnico explícito de valuation, responsividade, acessibilidade e sessão protegida.

## ADDED Requirements

### Requirement: Shell autenticado, rotas e navegação financeira
O sistema SHALL disponibilizar `/carteira`, `/carteira/posicoes`, `/carteira/transacoes` e `/carteira/movimentacoes` exclusivamente para ROLE_USER confirmado. Em desktop SHALL apresentar navegação persistente com Carteira, Posições, Transações e Movimentações; em mobile SHALL apresentar cabeçalho compacto e navegação inferior com Carteira, Posições, Transações e Mais, que permita alcançar Movimentações. O cabeçalho SHALL incluir contexto da página, tema, perfil e logout sem oferecer ação financeira inexistente. ROLE_ADMIN SHALL continuar fora da experiência de carteira.

#### Scenario: Navegação de usuário autenticado
- **WHEN** ROLE_USER confirmado acessa qualquer rota de carteira
- **THEN** recebe shell financeiro responsivo e pode navegar entre todas as páginas de leitura sem expor controles de depósito, saque, BUY ou SELL

#### Scenario: Admin acessa rota de carteira
- **WHEN** ROLE_ADMIN confirmado acessa uma rota de carteira
- **THEN** recebe o fluxo existente de acesso negado e sua sessão permanece ativa

### Requirement: Tema e tokens semânticos
O sistema SHALL suportar tema global Light, Dark e System, persistir a preferência e respeitar a preferência do sistema quando selecionado System, sem flash perceptível ou mismatch relevante de hidratação. A supressão de hydration SHALL existir somente no elemento raiz necessário. Light SHALL usar superfícies claras com acento verde financeiro sóbrio; Dark SHALL usar hierarquia intencional de quase-preto/carvão com acento roxo profundo. Tokens SHALL cobrir superfícies e semânticas; componentes financeiros MUST NOT espalhar cores raw. Resultado financeiro SHALL comunicar sinal e texto, e nunca depender somente da cor. Login e `/admin` SHALL permanecer funcionais, legíveis e não redesenhados nos três temas.

#### Scenario: Preferência de tema persistida
- **WHEN** o usuário escolhe Dark e recarrega uma rota de carteira
- **THEN** a rota reaparece em Dark antes da interação visual principal, preservando contraste e hierarquia próprios do tema

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
