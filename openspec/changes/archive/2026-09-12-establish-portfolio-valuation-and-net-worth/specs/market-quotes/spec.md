## ADDED Requirements

### Requirement: Leitura interna de quote para custódia inativa
Market-quotes SHALL disponibilizar leitura interna que resolve quote atual de ativo inativo somente quando ele compõe posição aberta da carteira em valuation. A leitura SHALL reutilizar cache, providers, fallback e HistóricoCotacao existentes, sem tornar HistóricoCotacao stale fallback. Ela MUST NOT ser bypass geral para ativo inativo e não altera a rota pública.

#### Scenario: Custódia aberta inativa
- **WHEN** valuation solicita internamente quote de ativo inativo com posição aberta
- **THEN** a capability resolve a quote atual ou propaga sua falha normal

#### Scenario: Ativo inativo sem custódia aberta
- **WHEN** uma leitura interna não é motivada por posição aberta em valuation
- **THEN** ela não recebe privilégio para resolver ativo inativo

#### Scenario: Rota pública inalterada
- **WHEN** usuário consulta cotação pública de ativo inativo
- **THEN** o comportamento público existente permanece aplicável
