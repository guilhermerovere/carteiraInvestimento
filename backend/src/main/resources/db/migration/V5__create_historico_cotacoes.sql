CREATE TABLE historico_cotacoes (
    id UUID PRIMARY KEY,
    ativo_id UUID NOT NULL,
    preco NUMERIC(15,4) NOT NULL,
    moeda VARCHAR(3) NOT NULL,
    instante_cotacao TIMESTAMPTZ NOT NULL,
    origem_provider VARCHAR(20) NOT NULL,
    recebido_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_historico_cotacoes_ativo FOREIGN KEY (ativo_id)
        REFERENCES acoes(id) ON DELETE RESTRICT,
    CONSTRAINT ck_historico_cotacoes_preco CHECK (preco > 0),
    CONSTRAINT ck_historico_cotacoes_moeda CHECK (moeda IN ('BRL', 'USD')),
    CONSTRAINT ck_historico_cotacoes_provider CHECK (
        origem_provider IN ('BRAPI', 'ALPHA_VANTAGE', 'TWELVE_DATA')
    )
);

CREATE INDEX idx_historico_cotacoes_recente
    ON historico_cotacoes (ativo_id, instante_cotacao DESC, recebido_em DESC, id DESC);
