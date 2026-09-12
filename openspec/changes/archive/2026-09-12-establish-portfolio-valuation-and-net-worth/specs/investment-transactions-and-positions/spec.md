## ADDED Requirements

### Requirement: Versão financeira e invalidação de valuation após negociação
Cada BUY/SELL novo confirmado SHALL aplicar `novoEstadoVersao = estadoVersaoAnterior + 1` exatamente uma vez na mesma transação financeira. Replay idempotente e operação rejeitada/rollback MUST NOT incrementá-la. Se restar posição aberta, SHALL recompor `totalInvestidoBrl` e invalidar valor de posições, lucro não realizado, patrimônio e valuationInstant para nulo. Sem posição aberta, SHALL gravar investimento/valor/lucro não realizado em zero, patrimônio igual ao saldo e instant nulo. Posição zerada SHALL persistir e reter seu lucro realizado acumulado histórico.

#### Scenario: BUY ou SELL parcial
- **WHEN** BUY ou SELL novo termina com posição aberta
- **THEN** versão aumenta uma vez e a valuation do snapshot é invalidada integralmente

#### Scenario: SELL da última posição
- **WHEN** SELL novo zera a última posição
- **THEN** versão aumenta uma vez e o snapshot entra no estado local sem posições

#### Scenario: Replay ou rollback
- **WHEN** uma operação é replay ou é rejeitada/revertida
- **THEN** estadoVersao não muda

#### Scenario: Lucro realizado histórico após zeramento
- **WHEN** SELL total zera uma posição
- **THEN** seu lucro realizado acumulado permanece disponível para agregação histórica
