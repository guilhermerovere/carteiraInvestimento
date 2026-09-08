CREATE TABLE acoes (
    id UUID PRIMARY KEY,
    ticker VARCHAR(6) NOT NULL,
    nome VARCHAR(160) NOT NULL,
    tipo VARCHAR(16) NOT NULL,
    mercado VARCHAR(8) NOT NULL,
    moeda VARCHAR(3) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_acoes_ticker UNIQUE (ticker),
    CONSTRAINT ck_acoes_ticker_canonico CHECK (ticker = upper(btrim(ticker))),
    CONSTRAINT ck_acoes_ticker_mercado CHECK (
        (mercado = 'B3' AND ticker ~ '^[A-Z]{4}[0-9]{1,2}$') OR
        (mercado = 'US' AND ticker ~ '^[A-Z]{1,5}$')
    ),
    CONSTRAINT ck_acoes_tipo CHECK (tipo IN ('ACAO', 'FII', 'ETF')),
    CONSTRAINT ck_acoes_mercado CHECK (mercado IN ('B3', 'US')),
    CONSTRAINT ck_acoes_moeda CHECK (moeda IN ('BRL', 'USD')),
    CONSTRAINT ck_acoes_mercado_moeda CHECK (
        (mercado = 'B3' AND moeda = 'BRL') OR
        (mercado = 'US' AND moeda = 'USD')
    ),
    CONSTRAINT ck_acoes_nome CHECK (btrim(nome) <> '' AND nome = btrim(nome))
);
