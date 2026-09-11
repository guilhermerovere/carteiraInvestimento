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
Cada operação nova SHALL capturar um único `operationInstant` e usá-lo para `movimento.dataHora`, `auditoria.dataHora` de DEPOSITO/SAQUE e data de snapshot. DEPOSITO e SAQUE MUST passar esse instante já capturado ao mecanismo compartilhado de auditoria e MUST NOT fazer leitura independente de `Instant.now()`. Data SHALL derivar desse instante na zona configurada, padrão `America/Sao_Paulo`, independente da timezone da máquina. Snapshot tem UUID próprio e uma linha por carteira/data; snapshot anterior MUST NOT mudar e replay/rollback MUST NOT atualizá-lo. Callers não financeiros existentes que não possuam instante explícito MAY manter o fallback legado do adapter.

Depósito e saque SHALL atualizar o saldo e recompor `total_investido_brl` a partir das Posicoes abertas, sem zerar artificialmente investimento. `saldo_caixa_brl` e `total_investido_brl` SHALL permanecer sempre não nulos. Os três campos dependentes de valuation — `valor_posicoes_brl`, `lucro_nao_realizado_brl` e `patrimonio_total_brl` — MUST estar todos `NULL` quando valuation for desconhecido com Posicao aberta, ou todos preenchidos quando conhecido; estado parcial MUST ser rejeitado pelos CHECKs da V9. Quando o snapshot do mesmo dia possuir `valor_posicoes_brl` e `lucro_nao_realizado_brl` conhecidos e a mutação alterar somente caixa, esses valores SHALL ser preservados e `patrimonio_total_brl` SHALL ser recalculado como novo saldo mais valor das posições. Quando valuation estiver desconhecido, o trio SHALL permanecer `NULL`. Quando não existir nenhuma Posicao aberta, `valor_posicoes_brl = 0`, `lucro_nao_realizado_brl = 0`, `total_investido_brl = 0` e `patrimonio_total_brl = saldo_caixa_brl`. Nenhum provider SHALL ser chamado durante essa composição local.

Se existirem Posicoes abertas e ainda não houver snapshot para a data atual, o primeiro DEPOSITO/SAQUE do dia SHALL gravar o saldo real atualizado e `total_investido_brl` recomputado localmente, enquanto `valor_posicoes_brl`, `lucro_nao_realizado_brl` e `patrimonio_total_brl` SHALL ficar `NULL`. O sistema MUST NOT copiar valuation de snapshot de dia anterior nem chamar provider.

#### Scenario: Depósito reutiliza o instante financeiro
- **WHEN** uma operação nova de DEPOSITO é confirmada
- **THEN** `movimento.dataHora` e `auditoria.dataHora` são iguais ao único `operationInstant` capturado e a data do snapshot deriva dele em `America/Sao_Paulo`

#### Scenario: Saque reutiliza o instante financeiro
- **WHEN** uma operação nova de SAQUE é confirmada
- **THEN** `movimento.dataHora` e `auditoria.dataHora` são iguais ao único `operationInstant` capturado e a data do snapshot deriva dele em `America/Sao_Paulo`

#### Scenario: Virada de dia e replay
- **WHEN** operações ocorrem antes/depois da meia-noite configurada e uma é repetida
- **THEN** snapshots usam data do único instante e replay preserva timestamp sem atualizar snapshot

#### Scenario: Movimento de caixa preserva investimento e valuation conhecido
- **WHEN** depósito ou saque ocorre com posição aberta e snapshot do dia possui valuation conhecido ainda estruturalmente aplicável
- **THEN** o snapshot usa saldo novo, recompõe total investido local, preserva valor de posições e lucro não realizado e recalcula patrimônio

#### Scenario: Movimento de caixa preserva valuation ausente
- **WHEN** depósito ou saque ocorre com posição aberta cujo valuation do snapshot está NULL
- **THEN** total investido é recomposto, os três campos dependentes de valuation permanecem NULL e nenhum campo é artificialmente zerado

#### Scenario: Primeiro movimento do novo dia com posição aberta
- **WHEN** existem Posicoes abertas, ocorre depósito ou saque e ainda não existe snapshot para o dia atual
- **THEN** saldo é o valor real atualizado, total investido é recomputado localmente, os três campos dependentes de valuation ficam NULL, nenhum valuation anterior é copiado e nenhum provider é chamado

#### Scenario: Movimento sem posições abertas
- **WHEN** depósito ou saque ocorre sem qualquer Posicao aberta
- **THEN** campos conhecidos de posições/investimento/lucro não realizado são zero e patrimônio é igual ao saldo resultante

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
