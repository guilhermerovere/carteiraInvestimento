## MODIFIED Requirements

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
