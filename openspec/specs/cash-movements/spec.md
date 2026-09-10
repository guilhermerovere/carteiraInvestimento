## Purpose

Permite caixa BRL privado, atômico e auditável para a carteira principal de ROLE_USER, com histórico e idempotência seguros.

## Requirements

### Requirement: Ledger BRL imutável e integridade relacional
O sistema SHALL manter saldo BRL materializado e ledger imutável com UUID, carteira, usuário, tipo DEPOSITO ou SAQUE, valor positivo, descrição normalizada e data/hora. O banco MUST impedir saldo negativo, valor/tipo inválido, duplicidade diária de snapshot e associação de carteira de A com usuário B. Não existem PUT, PATCH ou DELETE para movimentações.

#### Scenario: Associação cruzada é rejeitada
- **WHEN** persistência direta usa carteira de A com usuario_id de B
- **THEN** PostgreSQL rejeita por integridade relacional

### Requirement: Comandos estritos e chaves idempotentes
POSTs SHALL aceitar somente valor e descricao; campo desconhecido MUST retornar 400 sanitizado. Valor MUST ser positivo, escala até dois e caber em NUMERIC(18,2) sem arredondar. Descrição SHALL ser trim, blank para null e máximo 160. Idempotency-Key é obrigatória, não sofre trim, tem 1–128 caracteres e formato ^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$; ela MUST NOT estar em resposta, logs, auditoria ou ProblemDetail. A estrita rejeição de campos MUST ser localizada ao caixa e não mudar JSON global.

#### Scenario: Request estrito sem regressão global
- **WHEN** POST de caixa contém campo desconhecido e capability existente recebe seu contrato atual
- **THEN** caixa responde 400 e a capability existente preserva comportamento JSON

#### Scenario: Chave inválida não é normalizada
- **WHEN** chave está ausente, vazia, contém espaço inicial/final ou viola formato
- **THEN** a operação responde 400 sem efeito persistido

### Requirement: Operação financeira atômica e saldo resultante
Depósito e saque novos SHALL alterar saldo, movimento, snapshot, auditoria de sucesso e resultado idempotente na mesma transação. Saque MUST usar débito condicionado atômico; saldo insuficiente SHALL retornar 409. Depósito cujo saldo resultante excederia NUMERIC(18,2) SHALL retornar 409 sanitizado. Toda falha/rollback MUST reverter reserva e MUST NOT persistir resposta de sucesso.

#### Scenario: Saques concorrentes
- **WHEN** dois saques simultâneos de 80 ocorrem com saldo 100
- **THEN** um recebe 201, outro 409, saldo é 20 e há um saque, snapshot, auditoria e nenhuma reserva residual da falha

#### Scenario: Overflow do saldo
- **WHEN** depósito válido individualmente ultrapassa o limite no saldo resultante
- **THEN** responde 409 sanitizado e saldo, reserva, movimento, snapshot e auditoria permanecem inalterados

#### Scenario: Falha não consome chave
- **WHEN** saldo insuficiente, movimento, snapshot ou auditoria falha e ocorre rollback
- **THEN** a mesma chave pode executar nova tentativa legítima

### Requirement: Idempotência PostgreSQL segura e replay determinístico
PostgreSQL SHALL ser autoridade final da reserva única por carteira, tipo e chave. Reserva MUST ser atômica e segura sem continuar transação após UNIQUE; deve distinguir nova/existente, esperar reserva concorrente, repetir após commit vencedor e permitir nova tentativa após rollback. A comparação SHALL usar SHA-256 de registro UTF-8 canônico, versionado, de ordem fixa, tipos explícitos e length-prefix. Valor normalizado é decimal não exponencial; 100, 100.0 e 100.00 são equivalentes; delimitadores na descrição MUST NOT causar colisão estrutural.

Para mesma carteira, tipo, chave e payload normalizado commitado, SHALL retornar 201 com movimento original e saldoResultante original. Replay MUST NOT alterar saldo, criar movimento, snapshot ou auditoria e MUST NOT recalcular com saldo atual. Chave igual e payload diferente SHALL retornar 409 sem efeito parcial.

#### Scenario: Replay preserva resultado histórico
- **WHEN** depósito A resultou 100, saldo posterior é 500 e A é repetido
- **THEN** resposta 201 contém movimento A e saldoResultante 100 sem novo efeito

#### Scenario: Requisições idênticas concorrentes
- **WHEN** duas requisições concorrentes usam mesma carteira, tipo, chave e payload normalizado
- **THEN** ambas recebem 201 e mesmo resultado original, com um efeito, movimento, snapshot e auditoria

#### Scenario: Reserva concorrente faz rollback
- **WHEN** reserva concorrente falha antes do commit e requisição idêntica aguarda
- **THEN** a aguardando cria reserva e executa uma operação legítima

#### Scenario: Payloads concorrentes divergentes
- **WHEN** duas requisições concorrentes usam a mesma chave e payloads diferentes
- **THEN** só vencedor aplica efeito e perdedor recebe 409 sem efeito parcial

### Requirement: Instante e snapshot mínimo coerentes
Cada operação nova SHALL capturar único instante e usá-lo para movimento, auditoria quando aplicável e data de snapshot. Data SHALL derivar desse instante na zona configurada, padrão America/Sao_Paulo, independente da timezone da máquina. Snapshot tem UUID próprio e uma linha por carteira/data; saldo e patrimônio são saldo resultante e posições, investido e lucro são 0.00. Snapshot anterior MUST NOT mudar; replay/rollback MUST NOT atualizá-lo.

#### Scenario: Virada de dia e replay
- **WHEN** operações ocorrem antes/depois da meia-noite configurada e uma é repetida
- **THEN** snapshots usam data do único instante e replay preserva timestamp sem atualizar snapshot

### Requirement: Ownership, auditoria e respostas privadas
This requirement SHALL enforce private ownership, sanitized errors, and the documented response contracts.
Os endpoints são POST /api/v1/carteira/caixa/deposito, POST /api/v1/carteira/caixa/saque, GET /api/v1/carteira/caixa e GET /api/v1/carteira/caixa/movimentacoes. Só ROLE_USER usa carteira do principal; anônimo recebe 401 e ROLE_ADMIN recebe 403. ROLE_USER sem carteira recebe 500 sanitizado com X-Correlation-ID, sem fallback/IDs/SQL/stack trace.

POST novo ou replay responde 201 com movimentacao contendo id, tipo, valorBrl, descricao e dataHora, e saldoResultante. GET saldo retorna saldoCaixaBrl; histórico usa envelope próprio, defaults page=0/size=20, limites page>=0 e size 1–100, ordem dataHora DESC, id DESC. Auditoria de sucesso é transacional normal, nunca isolada/REQUIRES_NEW, e não contém dados privados, payload, fingerprint, chave ou credenciais.

#### Scenario: Carteira ausente
- **WHEN** ROLE_USER autenticado não possui carteira principal
- **THEN** recebe 500 sanitizado com X-Correlation-ID, sem 403/404 ou consulta alheia

#### Scenario: Auditoria participa do commit
- **WHEN** operação autorizada falha após iniciar fluxo financeiro
- **THEN** nenhum evento de sucesso DEPOSITO ou SAQUE permanece

#### Scenario: Histórico vazio e paginação inválida
- **WHEN** histórico está vazio ou page/size é inválido
- **THEN** retorna respectivamente 200 com items=[] ou 400 ProblemDetail sanitizado

#### Scenario: Retry after snapshot or audit rollback
- **WHEN** snapshot or transactional audit fails and the operation rolls back, then the same key is submitted again
- **THEN** the retry can reserve and commit legitimately, with no success result persisted from the failed attempt

#### Scenario: Host timezone does not affect operation
- **WHEN** the same injected operation instant is evaluated on hosts with different timezones
- **THEN** movement timestamp and snapshot date remain determined by the injected Clock and configured zone (default America/Sao_Paulo)

#### Scenario: Sanitized errors preserve correlation
- **WHEN** any cash endpoint fails
- **THEN** status follows the 400/401/403/409/500 contract, X-Correlation-ID is preserved, and SQL, stack trace, keys, fingerprints, private IDs and balances are not exposed
