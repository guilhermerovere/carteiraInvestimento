## ADDED Requirements

### Requirement: Versão financeira e snapshot de caixa compatível com valuation
Cada DEPOSITO/SAQUE novo confirmado SHALL aplicar `novoEstadoVersao = estadoVersaoAnterior + 1` exatamente uma vez, na mesma transação que altera saldo e snapshot. Replay idempotente SHALL retornar o resultado original sem alterar saldo, snapshot ou versão; falha/rollback SHALL não incrementá-la. Com valuation materializada no mesmo dia, o movimento SHALL preservar `valorPosicoesBrl`, `lucroNaoRealizadoBrl` e `valuationInstant`, atualizar saldo, recompor total investido local e recalcular patrimônio. Com valuation desconhecida, trio e instant permanecerão nulos; sem posição aberta, SHALL gravar zeros locais e patrimônio igual ao novo saldo.

#### Scenario: Depósito após valuation materializada
- **WHEN** depósito novo confirma no mesmo dia de valuation materializada
- **THEN** a versão aumenta uma vez e a valuation/instant são preservados enquanto patrimônio é recomposto

#### Scenario: Movimento com valuation desconhecida
- **WHEN** depósito ou saque novo ocorre com posição aberta e valuation desconhecida
- **THEN** versão aumenta uma vez e trio/instant continuam nulos

#### Scenario: Movimento sem posição aberta
- **WHEN** depósito ou saque novo ocorre sem posição aberta
- **THEN** versão aumenta uma vez e snapshot contém zeros locais e patrimônio igual ao saldo

#### Scenario: Replay ou rollback
- **WHEN** a mesma key é repetida ou a nova movimentação sofre rollback
- **THEN** estadoVersao não muda
