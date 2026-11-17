## ADDED Requirements

### Requirement: Twelve Data proof for controlled US registration
Before a USER registration can persist a US asset, the system SHALL obtain an exact reference-data match whose canonical symbol equals the requested canonical ticker, whose country/venue and currency establish a US/USD instrument, and whose explicit instrument type maps to the supported `ACAO` or `ETF` domain type. Only an explicit Equity/Common Stock or ETF classification SHALL be accepted; a missing type, Mutual Fund, or unmapped type MUST NOT be inferred from the ticker or from a quotation payload. The system SHALL then obtain a quotation for that canonical symbol and SHALL require the quotation symbol, accepted US venue, and USD currency to agree with reference data. The quotation SHALL contain a decimal `close` strictly greater than zero and a present Unix `timestamp` strictly greater than zero. Any missing, malformed, unsupported, or divergent required evidence SHALL block registration and persist nothing under the existing sanitized error conventions. The quotation response MUST NOT be required to contain an instrument `type` unless the provider contract later documents that field.

#### Scenario: Exact US Equity identity and quotation agree
- **WHEN** reference data returns one exact US/USD symbol classified explicitly as Equity/Common Stock and its quotation returns the same symbol, an accepted US venue, USD, a positive decimal close, and a positive Unix timestamp
- **THEN** registration maps the type to `ACAO` and may persist the validated canonical metadata

#### Scenario: Exact US ETF identity and quotation agree
- **WHEN** reference data returns one exact US/USD symbol classified explicitly as ETF and its quotation returns the same symbol, an accepted US venue, USD, a positive decimal close, and a positive Unix timestamp
- **THEN** registration maps the type to `ETF` and may persist the validated canonical metadata

#### Scenario: Reference type is absent or unsupported
- **WHEN** the exact reference-data match has no type or is classified as Mutual Fund or another unmapped instrument
- **THEN** registration is rejected without persisting an asset and without inferring `ACAO` from an absent quotation or reference field

#### Scenario: Reference identity and quotation diverge
- **WHEN** reference data and quotation disagree on canonical symbol, accepted US venue, or USD currency
- **THEN** registration is rejected and no catalog record is inserted or changed

#### Scenario: Exact reference matches conflict
- **WHEN** multiple exact US/USD reference matches provide conflicting supported names or instrument types
- **THEN** registration is rejected before quotation and no catalog record is inserted or changed

#### Scenario: Quotation price is unusable
- **WHEN** the agreeing quotation has a blank, non-decimal, zero, or negative close
- **THEN** registration is rejected and no catalog record is inserted or changed

#### Scenario: Quotation timestamp is unusable
- **WHEN** the agreeing quotation has a missing, zero, or negative Unix timestamp
- **THEN** registration is rejected and no catalog record is inserted or changed

#### Scenario: Quotation omits instrument type
- **WHEN** reference data proves a supported type and the agreeing quotation supplies the required symbol, venue, currency, positive close, and positive timestamp but omits `type`
- **THEN** registration uses only the reference-data classification and does not reject or reclassify the asset because quotation `type` is absent
