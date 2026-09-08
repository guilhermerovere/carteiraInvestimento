## Purpose

Fornecer cotações de mercado frescas e historicamente rastreáveis para ativos B3 e US, preservando a separação entre o catálogo global de ativos e os dados de preço.

## ADDED Requirements

### Requirement: Integridade numérica e temporal de resposta externa
O sistema SHALL converter preço externo para BigDecimal, validar que é positivo, normalizar para escala 4 com HALF_UP e validar que cabe em NUMERIC(15,4) antes de persistir. Valor não numérico/não válido, zero, negativo ou overflow SHALL resultar em 502 sanitizado e MUST NOT ser persistido ou colocado no cache. A tabela SHALL aceitar somente BRL e USD, além da validação contra moeda estrutural do Ativo. Adapter SHALL converter timestamp usando timezone/offset explicitamente definido pelo contrato externo em Instant e MUST NOT usar timezone local ou fabricar instante; timestamp impossível, malformado ou sem informação suficiente SHALL ser falha de integração. recebidoEm SHALL ser gerado pelo sistema ao aceitar a resposta.

#### Scenario: Escala, HALF_UP e overflow
- **WHEN** provider retorna preço com escala menor, igual ou maior que quatro casas, inclusive arredondamento HALF_UP, ou valor fora de NUMERIC(15,4)
- **THEN** valor válido é persistido na escala normalizada e valor inválido resulta 502 sem histórico ou cache

#### Scenario: Timestamp independente do timezone da JVM
- **WHEN** adapter recebe timestamp com timezone/offset contratual ou sem informação temporal suficiente
- **THEN** converte o primeiro para Instant determinístico e rejeita o segundo sem usar timezone local

### Requirement: Freshness, persistência e coordenação do cache
TTL SHALL ser dez minutos desde recebidoEm/momento de inserção no cache, nunca desde instanteCotacao. Em cache miss, o sistema SHALL obter, validar, persistir e confirmar sucesso da persistência antes de cachear e responder. Falha de persistência SHALL impedir cache e sucesso. Refresh SHALL ignorar cache apenas para decidir chamar provider, preservar entrada existente em qualquer falha e substituí-la somente após persistência bem-sucedida. Coordenação local por UUID SHALL cobrir GET miss e refresh concorrentes, reduzir chamadas duplicadas e impedir que uma observação mais antiga sobrescreva uma mais nova. OpenFeign MUST NOT ocorrer dentro de transação longa de banco.

#### Scenario: TTL mede ingestão e não instante de mercado
- **WHEN** cotação antiga de mercado é recebida e inserida agora no cache
- **THEN** ela permanece fresca por dez minutos desde a inserção

#### Scenario: Refresh falho preserva cache
- **WHEN** cache contém A e refresh falha no provider ou na persistência de B
- **THEN** PUT falha, A permanece até expiração natural e B não é retornada, persistida parcialmente ou cacheada

### Requirement: Classificação determinística da cadeia e observabilidade sanitizada
Credenciais ausentes SHALL permitir startup e, quando provider for necessário, serão falha de integração sem retornar 401. B3 SHALL retornar 404 apenas para BRAPI NOT_FOUND e 502 para falha de integração. US SHALL retornar 404 somente para AV NOT_FOUND e TD NOT_FOUND; AV NOT_FOUND + TD integração, AV integração + TD NOT_FOUND e ambas integrações SHALL retornar 502; qualquer falha elegível seguida de sucesso de TD SHALL retornar 200. O campo provider exposto SHALL indicar somente o provider vencedor. Logging técnico pode conter provider, ativoId, failureCategory, fallback e correlationId, mas MUST NOT conter segredo, API key, Authorization, payload completo ou gerar `logs_auditoria`.

#### Scenario: Matriz US inconclusiva
- **WHEN** pelo menos um provider US falha tecnicamente e nenhum provider entrega cotação
- **THEN** a resposta é 502, não 404

#### Scenario: Credencial ausente
- **WHEN** aplicação inicia sem credencial de provider e uma operação requer esse provider
- **THEN** endpoints não relacionados continuam disponíveis e a operação aplica fallback aplicável ou responde 502 sanitizado

### Requirement: Cotação e histórico separados do Ativo
O sistema SHALL representar Cotação/HistoricoCotacao separadamente de Ativo. Cada observação aceita SHALL conter id UUID, ativoId UUID, preço positivo com precisão NUMERIC(15,4), moeda, instanteCotacao informado pelo provider, provider de origem e recebidoEm; os dois timestamps SHALL usar Instant. A moeda SHALL ser coerente com a moeda estrutural do Ativo, e o provider SHALL ser BRAPI, ALPHA_VANTAGE ou TWELVE_DATA. A persistência SHALL manter `historico_cotacoes.ativo_id` como FK para `acoes(id)` com deleção restrita ou equivalente, sem coluna de preço em `acoes` e sem expiração automática de histórico.

#### Scenario: Observação externa válida é historizada
- **WHEN** uma cotação externa válida e compatível com o Ativo é aceita
- **THEN** o sistema persiste uma observação com preço positivo, moeda, provider e ambos os timestamps exigidos

#### Scenario: Preço ou moeda inválidos
- **WHEN** uma resposta externa possui preço não positivo ou moeda divergente da moeda estrutural do Ativo
- **THEN** ela não é persistida nem retornada como cotação atual

#### Scenario: Observações iguais permanecem distintas
- **WHEN** duas obtenções externas bem-sucedidas possuem o mesmo ativo, provider, instante e preço
- **THEN** ambas permanecem como observações históricas independentes

### Requirement: Obtenção aplicável por mercado e cadeia de providers
O sistema SHALL obter cotação B3 somente por Brapi. Para Ativo US, SHALL consultar AlphaVantage como provider primário e TwelveData como fallback quando o primário falhar por timeout, rate limit, resposta 5xx, resposta vazia ou malformada, indisponibilidade ou símbolo não encontrado coerente com os contratos dos providers. Credencial ou configuração inválida SHALL ser classificada como falha de integração, distinta de ticker inexistente. O sistema MUST NOT consultar provider para USD/BRL nesta capability.

#### Scenario: Ativo B3 usa Brapi
- **WHEN** o sistema precisa obter uma cotação para Ativo B3 ativo
- **THEN** consulta Brapi e não tenta provider US

#### Scenario: Falha do primário US usa fallback
- **WHEN** AlphaVantage falha por condição elegível de fallback para Ativo US
- **THEN** o sistema tenta TwelveData e identifica o provider que eventualmente forneceu a cotação

#### Scenario: Nenhum ticker é fornecido pelos providers aplicáveis
- **WHEN** todos os providers aplicáveis indicam ticker inexistente ou não suportado
- **THEN** o sistema responde 404 com ProblemDetail sanitizado de cotação indisponível

#### Scenario: Falha da cadeia externa
- **WHEN** a cadeia aplicável falha por timeout, rate limit, 5xx, credencial/configuração inválida, resposta malformada ou indisponibilidade
- **THEN** o sistema responde 502 Bad Gateway com ProblemDetail sanitizado e sem detalhe interno ou credencial

### Requirement: Cotação atual fresca em cache
GET `/api/v1/acoes/{id}/cotacao` SHALL localizar e validar o Ativo antes de consultar cache por UUID do Ativo. Para Ativo ativo elegível, o cache SHALL ter TTL de 10 minutos; um cache hit SHALL retornar a mesma cotação sem chamar provider ou persistir observação. Em cache miss, o sistema SHALL obter cotação externa aplicável, validá-la, persistir uma nova observação, popular o cache e respondê-la. O sistema MUST NOT usar histórico persistido como fallback após expiração do cache e SHALL evitar chamadas externas duplicadas concorrentes para o mesmo UUID quando a carga atômica por chave estiver disponível.

#### Scenario: Cache hit não cria histórico
- **WHEN** GET atual encontra cotação não expirada no cache para o UUID do Ativo
- **THEN** retorna a cotação sem chamar provider e sem inserir histórico

#### Scenario: Cache miss obtém e registra cotação
- **WHEN** GET atual encontra cache ausente ou expirado para Ativo ativo
- **THEN** obtém uma cotação externa válida, persiste exatamente uma nova observação, atualiza o cache e retorna a cotação

#### Scenario: Histórico não é stale fallback
- **WHEN** o cache está ausente ou expirado e a cadeia externa falha
- **THEN** o sistema não retorna automaticamente uma observação histórica anterior como cotação atual

### Requirement: APIs de cotação e histórico controladas
O sistema SHALL expor GET `/api/v1/acoes/{id}/cotacao`, GET `/api/v1/acoes/{id}/historico` e PUT `/api/v1/acoes/{id}/atualizar-cotacao`, usando UUID de Ativo. Respostas de cotação SHALL conter somente id, ativoId, preco, moeda, instanteCotacao, provider e recebidoEm. O histórico SHALL ser paginado em envelope próprio, sem expor Page do Spring, com defaults page=0 e size=20, validação `page >= 0` e `1 <= size <= 100`, e ordem por instanteCotacao desc, recebidoEm desc, id desc. O refresh SHALL ignorar cache, obter e validar cotação externa, persistir nova observação, substituir o cache e retornar a nova cotação.

#### Scenario: Histórico paginado mais recente primeiro
- **WHEN** cliente autenticado solicita o histórico com parâmetros válidos ou ausentes
- **THEN** recebe envelope próprio com os defaults aplicáveis e observações em ordem mais recente primeiro

#### Scenario: Paginação inválida
- **WHEN** page é negativo, size é menor que 1 ou maior que 100
- **THEN** o sistema responde 400 com ProblemDetail sanitizado

#### Scenario: Histórico vazio é coleção válida
- **WHEN** Ativo existente e visível não possui observações históricas
- **THEN** GET histórico responde 200 com `{items:[], page:0, size:20, totalElements:0, totalPages:0}`

#### Scenario: UUID inválido
- **WHEN** qualquer endpoint de cotação recebe id que não é UUID válido
- **THEN** responde 400 com ProblemDetail sanitizado e X-Correlation-ID

#### Scenario: Refresh administrativo ignora cache
- **WHEN** ROLE_ADMIN chama PUT de refresh e existe cotação não expirada em cache
- **THEN** o sistema consulta provider, persiste nova observação, atualiza cache e retorna a nova cotação

### Requirement: Lifecycle, visibilidade e segurança de dados de mercado
GET de cotação e histórico SHALL exigir ROLE_USER ou ROLE_ADMIN; PUT de refresh SHALL exigir ROLE_ADMIN. Anônimo SHALL receber 401 e principal autorizado insuficiente SHALL receber 403 pelo mecanismo existente. ROLE_USER SHALL receber 404 para qualquer dado de mercado de Ativo inativo. ROLE_ADMIN SHALL poder consultar histórico existente de Ativo inativo, mas GET atual para Ativo inativo SHALL não consultar provider nem retornar cotação atual e responder 404; PUT refresh para Ativo inativo SHALL responder 409 com ProblemDetail seguro. Ativo inexistente SHALL responder 404.

#### Scenario: Usuário não acessa mercado de ativo inativo
- **WHEN** ROLE_USER chama GET atual ou histórico para Ativo inativo
- **THEN** o sistema responde 404 sem expor seus dados de mercado

#### Scenario: Admin consulta histórico inativo sem nova obtenção
- **WHEN** ROLE_ADMIN chama GET histórico de Ativo inativo
- **THEN** recebe somente observações existentes sem nova chamada a provider

#### Scenario: Atual e refresh de ativo inativo são bloqueados
- **WHEN** ROLE_ADMIN chama GET atual ou PUT refresh para Ativo inativo
- **THEN** GET responde 404 e não chama provider, enquanto PUT responde 409 e não chama provider

### Requirement: Contratos sanitizados e ausência de efeitos adicionais
As respostas de erro desta capability SHALL usar ProblemDetail sanitizado e preservar `X-Correlation-ID`. Consultas, obtenções, cache hits e refreshes de cotação MUST NOT criar registros em `logs_auditoria` nem auditoria operacional de provider. A capability SHALL permanecer backend-only e MUST NOT expor entidade JPA, payload bruto de provider, endpoint por ticker, scheduler ou atualização assíncrona.

#### Scenario: Erro externo preserva contrato HTTP seguro
- **WHEN** uma operação de cotação falha por condição externa conhecida
- **THEN** a resposta contém o status mapeado, ProblemDetail sanitizado e correlation ID sem detalhes do provider além do provider de uma cotação bem-sucedida

#### Scenario: Consulta autorizada não cria auditoria operacional
- **WHEN** uma consulta de cotação ou histórico é autorizada e processada
- **THEN** nenhuma nova entrada é criada em `logs_auditoria` por essa consulta
