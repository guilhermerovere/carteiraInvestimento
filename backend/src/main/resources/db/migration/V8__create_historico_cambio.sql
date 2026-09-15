CREATE TABLE historico_cambio (
    id UUID PRIMARY KEY,
    moeda_origem CHAR(3) NOT NULL,
    moeda_destino CHAR(3) NOT NULL,
    taxa NUMERIC(18,8) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    instante_cotacao TIMESTAMPTZ NOT NULL,
    registrado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_historico_cambio_moeda_origem CHECK (moeda_origem = 'USD'),
    CONSTRAINT ck_historico_cambio_moeda_destino CHECK (moeda_destino = 'BRL'),
    CONSTRAINT ck_historico_cambio_taxa CHECK (taxa > 0),
    CONSTRAINT ck_historico_cambio_provider CHECK (provider IN ('ALPHA_VANTAGE', 'TWELVE_DATA'))
);

CREATE INDEX idx_historico_cambio_recente
    ON historico_cambio (moeda_origem, moeda_destino, instante_cotacao DESC, registrado_em DESC, id DESC);
