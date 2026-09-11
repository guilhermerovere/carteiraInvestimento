## Context

See proposal.md. A carteira já é lock financeiro comum; snapshots suportam trio desconhecido; quotes e câmbio possuem cache/fallback próprios. A revisão fixa precisão de instant, ordenação estrita e tratamento de carteira vazia.

## Goals / Non-Goals

**Goals:** valuation sem parcial, fotografia local consistente, providers fora de lock, versões exatas e snapshots determinísticos.

**Non-Goals:** redesenho arquitetural, cache próprio, scheduler, providers novos, frontend, V1–V9 e valuation histórica.

## Decisions

### Tentativa completa

REPEATABLE_READ read-only captura carteira/saldo/versão, abertas/custos/ativos e realizado de todas as posições e encerra. Só então captura `valuationInstant` truncado a microssegundos, resolve quotes e uma FX se US, calcula, e valida versão sob FOR UPDATE. Divergência descarta tudo e executa uma única tentativa nova; não reutiliza dados externos da primeira.

### V10 e snapshots

V10 adicionará estado_versao BIGINT NOT NULL e valuation_instant TIMESTAMPTZ NULL. Checks implementáveis por linha aceitam os três estados: desconhecido; local vazio com instant opcional; materializado com trio e instant. POST vazio é materializado (zeros + instant); legado/local vazio pode ter instant nulo.

### Upsert ordenado

Após normalização em microssegundos, refresh só atualiza valuation se o instant persistido for nulo ou o novo for estritamente maior. Menor e empate preservam a linha existente, impedindo conclusão tardia antiga de sobrescrever refresh mais novo.

### Mutações e integrações

DEPOSITO/SAQUE e BUY/SELL incrementam versão uma vez no próprio commit; replay/rollback não. Caixa preserva valuation conhecida e recompõe patrimônio; negociação invalida com abertas e grava estado local ao fechar última. Quote interna de inativo é limitada à custódia aberta; falha é normal de market-quotes e valuation traduz a necessidade em 502.

## Risks / Trade-offs

- [Provider lento] → sem transação/lock durante chamada.
- [Duas mutações durante tentativa] → segundo conflito explícito 409.
- [Micros iguais] → preservação determinística do snapshot existente.
- [Legado] → checks não exigem instant de todo trio histórico local.

## Migration Plan

V10 será criada somente no apply, testada de V1 a V10 e com Hibernate validate. Rollback de aplicação mantém schema forward-compatible; migration não é revertida após uso.

## Open Questions

Nenhuma.
