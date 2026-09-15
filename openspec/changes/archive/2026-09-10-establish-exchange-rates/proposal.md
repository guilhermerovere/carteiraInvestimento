## Why

Operações futuras de ativos dos Estados Unidos precisam mostrar e registrar uma taxa USD/BRL real, rastreável e financeiramente precisa antes da confirmação. `market-quotes` cobre preços de ativos, mas não câmbio global nem o identificador de observação que a futura transação deve confirmar.

## What Changes

- Adiciona a capability backend global e somente de leitura `GET /api/v1/cambio/usd-brl`, protegida para `ROLE_USER` e `ROLE_ADMIN`.
- Introduz domínio, ports, adapters OpenFeign, persistência e cache Caffeine próprios de FX, sem acoplamento a Cotacao de ativos.
- Obtém USD/BRL pelo AlphaVantage `CURRENCY_EXCHANGE_RATE` e usa TwelveData `/exchange_rate` com `symbol=USD/BRL` apenas após falha elegível; classifica HTTP e payload semântico antes de decidir fallback.
- Persiste observações externas válidas em `historico_cambio` pela futura migration `V8__create_historico_cambio.sql`, com `provider`, BigDecimal `NUMERIC(18,8)`, instante de proveniência e instante de registro.
- Define a deadline funcional única `registradoEm + 5 minutos` para cache e para o futuro `exchangeRateId`: cache nunca estende a vida da observação além dela.
- Integra erros sanitizados, correlation ID, OpenAPI e auditoria best-effort quando uma resolução termina sem FX válido.
- Documenta a integração futura de transação US por `exchangeRateId`, sem implementar Transacao, BUY, SELL ou valuation.

## Capabilities

### New Capabilities

- `exchange-rates`: fornece observações USD/BRL atuais, persistidas e rastreáveis, com proveniência temporal, fallback, deadline de frescor, segurança, erros e contrato futuro de confirmação.

### Modified Capabilities

- Nenhuma.

## Impact

- Backend: futura área FX em domain/application/infrastructure/presentation, migration V8, configuração externa e OpenAPI.
- Integrações: preserva URLs, API keys, OpenFeign e timeout de quatro segundos já configurados para AlphaVantage e TwelveData; DTOs/classificadores permanecem privados.
- Dados: adiciona histórico global imutável de câmbio, sem alterar V1–V7, cotações de ativos ou dados de carteira.
- Testes futuros: MockWebServer/servidor HTTP local, Clock/Ticker controlável e PostgreSQL Testcontainers, sem acesso à internet pública.
