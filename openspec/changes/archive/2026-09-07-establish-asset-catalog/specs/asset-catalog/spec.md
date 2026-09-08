## Purpose

Define o catálogo global de instrumentos financeiros com identidade canônica, lifecycle preservável e contratos backend estáveis para os módulos financeiros futuros.

## ADDED Requirements

### Requirement: Ativo global canônico e protegido
O sistema SHALL manter `Ativo` global sem usuário, carteira, quantidade, saldo, preço, cotação, corretora ou patrimônio. Cada ativo SHALL expor id UUID, ticker, nome, tipo, mercado, moeda, ativo, criadoEm e atualizadoEm. A aplicação SHALL executar trim, uppercase e validação do ticker antes de busca ou persistência. PostgreSQL SHALL rejeitar ticker diferente de `UPPER(TRIM(ticker))`, formato inválido para seu mercado, nome blank e violação da constraint única nomeada `uk_acoes_ticker`.

#### Scenario: Ticker canônico criado
- **WHEN** um administrador cria um ativo com ticker válido em minúsculas ou com espaços externos
- **THEN** o ativo é persistido exatamente com ticker trimado e em uppercase

#### Scenario: Escrita direta não canônica
- **WHEN** uma escrita direta tenta persistir ticker minúsculo ou com espaços externos
- **THEN** PostgreSQL rejeita a escrita

#### Scenario: Duplicidade concorrente
- **WHEN** duas operações concorrentes criam o mesmo ticker canônico
- **THEN** somente um ativo é persistido e a violação de `uk_acoes_ticker` é traduzida para `409 Conflict`

### Requirement: Valores controlados, formato e nome
O sistema SHALL admitir somente tipo `ACAO`, `FII` ou `ETF`, mercado `B3` ou `US`, e moeda `BRL` ou `USD`. B3 SHALL derivar BRL e ticker `^[A-Z]{4}[0-9]{1,2}$`; US SHALL derivar USD e ticker `^[A-Z]{1,5}$`. Os formatos são deliberadamente limitados a esta capability. Nome SHALL ser trimado, não blank e ter de 1 a 160 caracteres após normalização.

#### Scenario: Mercado determina moeda
- **WHEN** um administrador cria ativo B3 ou US válido
- **THEN** o ativo recebe respectivamente BRL ou USD sem moeda fornecida pelo cliente

#### Scenario: Formato ou nome inválido
- **WHEN** ticker não satisfaz o padrão do mercado ou nome é vazio, apenas espaços ou maior que 160 após trim
- **THEN** a operação é rejeitada com `400 Bad Request` sem persistência

#### Scenario: Formato inválido direto no banco
- **WHEN** escrita direta combina mercado B3 ou US com ticker fora de seu padrão
- **THEN** PostgreSQL rejeita a escrita

### Requirement: Requests estritos, identidade imutável e lifecycle
POST `/api/v1/acoes` SHALL aceitar exatamente `ticker`, `nome`, `tipo`, `mercado`. PATCH `/api/v1/acoes/{id}` SHALL aceitar exatamente `nome`. PATCH `/api/v1/acoes/{id}/ativo` SHALL aceitar exatamente `{ "ativo": true|false }`, definir o estado solicitado, não fazer toggle e ser idempotente. Ticker, tipo, mercado e moeda SHALL ser imutáveis; DELETE físico MUST NOT existir. Campos desconhecidos ou estruturais indevidos SHALL retornar `400 Bad Request` com ProblemDetail sanitizado.

#### Scenario: Campo estrutural em PATCH de nome
- **WHEN** PATCH de nome inclui `ticker` ou qualquer campo não permitido
- **THEN** o sistema responde 400 e não altera o ativo

#### Scenario: Lifecycle idempotente
- **WHEN** administrador envia o mesmo valor `ativo` já persistido
- **THEN** o sistema responde 200 com a representação atual, mantendo o estado

### Requirement: Respostas de mutação
POST SHALL responder 201 e ambos PATCH SHALL responder 200, sempre com `AtivoResponse` contendo somente id, ticker, nome, tipo, mercado, moeda, ativo, criadoEm e atualizadoEm.

#### Scenario: Mutação bem-sucedida
- **WHEN** administrador cria, altera nome ou define lifecycle válido
- **THEN** a resposta tem o status previsto e somente os campos de `AtivoResponse`

### Requirement: Listagem paginada controlada
GET `/api/v1/acoes` SHALL retornar envelope estável `{items, page, size, totalElements, totalPages}`. Defaults são page 0 e size 20; `page >= 0` e `1 <= size <= 100`. Aceita somente `q`, `tipo`, `ativo` administrativo, `sort` ticker/nome e `direction` asc/desc, com ticker asc padrão. `q` SHALL ser trimado, case-insensitive e contains em ticker ou nome; vazio após trim equivale a ausência de filtro.

#### Scenario: Busca simples por q
- **WHEN** lista recebe `q` parcial, com caixa ou espaços externos
- **THEN** localiza ticker ou nome correspondentes por contains case-insensitive

#### Scenario: Paginação ou ordenação inválida
- **WHEN** page é negativo, size é 0 ou maior que 100, ou sort/direction não é permitido
- **THEN** o sistema responde 400 com ProblemDetail sanitizado

### Requirement: Visibilidade e consulta por ticker
GET lista e GET ticker SHALL permitir ROLE_USER e ROLE_ADMIN. User SHALL receber somente ativos ativos na lista, ignorando tentativa de `ativo=false`, e 404 para ticker inativo. Admin sem filtro ativo SHALL ver ativos e inativos; com `ativo=true|false` SHALL receber o filtro; e poderá consultar ticker inativo. A visibilidade SHALL ser decidida antes da persistência sem introduzir dependência de Spring Security no domínio.

#### Scenario: User não amplia visibilidade
- **WHEN** ROLE_USER lista com `ativo=false` ou busca ticker inativo
- **THEN** a lista contém somente ativos e a busca responde 404

#### Scenario: Admin consulta catálogo completo
- **WHEN** ROLE_ADMIN lista sem filtro ou consulta ticker inativo
- **THEN** obtém ativos ativos e inativos conforme o contrato

### Requirement: Autorização, erros e persistência
POST e PATCH SHALL permitir somente ROLE_ADMIN; anônimo SHALL receber 401 e user sem permissão 403. Inexistente retorna 404; entrada inválida 400; somente `uk_acoes_ticker` duplicada retorna 409. Todos SHALL usar ProblemDetail sanitizado e `X-Correlation-ID`. A tabela `acoes` SHALL ser criada por Flyway forward-only e validada pelo ORM; outras constraints não SHALL ser traduzidas como duplicidade. CRUD autorizado não SHALL gerar auditoria operacional, preservando a auditoria existente de 403.

#### Scenario: Outra constraint não vira conflito
- **WHEN** persistência falha por CHECK ou constraint diferente de `uk_acoes_ticker`
- **THEN** a falha não é traduzida como 409 de ticker duplicado
