ALTER TABLE acoes
    ADD CONSTRAINT uk_acoes_id_moeda UNIQUE (id, moeda);

CREATE TABLE transacoes (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    acao_id UUID NOT NULL,
    corretora_id UUID NOT NULL,
    exchange_rate_id UUID NULL,
    tipo VARCHAR(4) NOT NULL,
    quantidade NUMERIC(18,8) NOT NULL,
    moeda VARCHAR(3) NOT NULL,
    preco_unitario NUMERIC(18,8) NOT NULL,
    taxas NUMERIC(18,8) NOT NULL,
    taxa_cambio_brl NUMERIC(18,8) NOT NULL,
    valor_total_brl NUMERIC(18,2) NOT NULL,
    resultado_realizado_brl NUMERIC(18,2) NULL,
    data_negociacao TIMESTAMPTZ NOT NULL,
    data_registro TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_transacoes_tipo CHECK (tipo IN ('BUY', 'SELL')),
    CONSTRAINT ck_transacoes_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_transacoes_preco_unitario CHECK (preco_unitario > 0),
    CONSTRAINT ck_transacoes_taxas CHECK (taxas >= 0),
    CONSTRAINT ck_transacoes_taxa_cambio CHECK (taxa_cambio_brl > 0),
    CONSTRAINT ck_transacoes_resultado_tipo CHECK (
        (tipo = 'BUY' AND resultado_realizado_brl IS NULL)
        OR (tipo = 'SELL' AND resultado_realizado_brl IS NOT NULL)
    ),
    CONSTRAINT ck_transacoes_moeda_fx CHECK (
        (moeda = 'BRL' AND exchange_rate_id IS NULL AND taxa_cambio_brl = 1.00000000)
        OR (moeda = 'USD' AND exchange_rate_id IS NOT NULL)
    ),
    CONSTRAINT fk_transacoes_carteira_usuario FOREIGN KEY (carteira_id, usuario_id)
        REFERENCES carteiras (id, usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_acao_moeda FOREIGN KEY (acao_id, moeda)
        REFERENCES acoes (id, moeda) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_corretora FOREIGN KEY (corretora_id)
        REFERENCES corretoras (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_exchange_rate FOREIGN KEY (exchange_rate_id)
        REFERENCES historico_cambio (id) ON DELETE RESTRICT
);

CREATE INDEX ix_transacoes_historico
    ON transacoes (carteira_id, data_registro DESC, id DESC);
CREATE TABLE posicoes (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    acao_id UUID NOT NULL,
    quantidade NUMERIC(18,8) NOT NULL,
    preco_medio_brl NUMERIC(18,8) NOT NULL,
    total_investido_brl NUMERIC(18,2) NOT NULL,
    lucro_realizado_acumulado_brl NUMERIC(18,2) NOT NULL,
    ultima_atualizacao TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_posicoes_carteira_acao UNIQUE (carteira_id, acao_id),
    CONSTRAINT ck_posicoes_estado CHECK (
        (quantidade = 0 AND preco_medio_brl = 0 AND total_investido_brl = 0)
        OR (quantidade > 0 AND preco_medio_brl > 0 AND total_investido_brl > 0)
    ),
    CONSTRAINT fk_posicoes_carteira FOREIGN KEY (carteira_id)
        REFERENCES carteiras (id) ON DELETE RESTRICT,
    CONSTRAINT fk_posicoes_acao FOREIGN KEY (acao_id)
        REFERENCES acoes (id) ON DELETE RESTRICT
);

CREATE INDEX ix_posicoes_abertas
    ON posicoes (carteira_id, acao_id) WHERE quantidade > 0;

CREATE TABLE transacoes_idempotencia (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    fingerprint CHAR(64) NOT NULL,
    estado VARCHAR(12) NOT NULL,
    transacao_id UUID NULL,
    ticker_resultante VARCHAR(6) NULL,
    valor_origem NUMERIC(36,16) NULL,
    saldo_caixa_brl_resultante NUMERIC(18,2) NULL,
    posicao_id UUID NULL,
    posicao_quantidade_resultante NUMERIC(18,8) NULL,
    posicao_preco_medio_brl_resultante NUMERIC(18,8) NULL,
    posicao_total_investido_brl_resultante NUMERIC(18,2) NULL,
    posicao_lucro_realizado_acumulado_brl_resultante NUMERIC(18,2) NULL,
    posicao_ultima_atualizacao_resultante TIMESTAMPTZ NULL,
    reservada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    concluida_em TIMESTAMPTZ NULL,
    CONSTRAINT uk_transacoes_idempotencia UNIQUE (carteira_id, idempotency_key),
    CONSTRAINT ck_transacoes_idempotencia_key CHECK (
        idempotency_key ~ '^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$'
    ),
    CONSTRAINT ck_transacoes_idempotencia_fingerprint CHECK (fingerprint ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_transacoes_idempotencia_estado CHECK (estado IN ('RESERVADA', 'CONCLUIDA')),
    CONSTRAINT ck_transacoes_idempotencia_resultado CHECK (
        (estado = 'RESERVADA'
            AND transacao_id IS NULL AND ticker_resultante IS NULL AND valor_origem IS NULL
            AND saldo_caixa_brl_resultante IS NULL AND posicao_id IS NULL
            AND posicao_quantidade_resultante IS NULL
            AND posicao_preco_medio_brl_resultante IS NULL
            AND posicao_total_investido_brl_resultante IS NULL
            AND posicao_lucro_realizado_acumulado_brl_resultante IS NULL
            AND posicao_ultima_atualizacao_resultante IS NULL AND concluida_em IS NULL)
        OR
        (estado = 'CONCLUIDA'
            AND transacao_id IS NOT NULL AND ticker_resultante IS NOT NULL AND valor_origem IS NOT NULL
            AND saldo_caixa_brl_resultante IS NOT NULL AND posicao_id IS NOT NULL
            AND posicao_quantidade_resultante IS NOT NULL
            AND posicao_preco_medio_brl_resultante IS NOT NULL
            AND posicao_total_investido_brl_resultante IS NOT NULL
            AND posicao_lucro_realizado_acumulado_brl_resultante IS NOT NULL
            AND posicao_ultima_atualizacao_resultante IS NOT NULL AND concluida_em IS NOT NULL)
    ),
    CONSTRAINT fk_transacoes_idempotencia_carteira_usuario FOREIGN KEY (carteira_id, usuario_id)
        REFERENCES carteiras (id, usuario_id) ON DELETE RESTRICT DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT fk_transacoes_idempotencia_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_idempotencia_transacao FOREIGN KEY (transacao_id)
        REFERENCES transacoes (id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacoes_idempotencia_posicao FOREIGN KEY (posicao_id)
        REFERENCES posicoes (id) ON DELETE RESTRICT
);

ALTER TABLE carteira_snapshots
    ALTER COLUMN valor_posicoes_brl DROP NOT NULL,
    ALTER COLUMN lucro_nao_realizado_brl DROP NOT NULL,
    ALTER COLUMN patrimonio_total_brl DROP NOT NULL,
    DROP CONSTRAINT ck_carteira_snapshots_valores_minimos,
    ADD CONSTRAINT ck_carteira_snapshots_valores_minimos CHECK (
        saldo_caixa_brl >= 0 AND total_investido_brl >= 0
        AND (valor_posicoes_brl IS NULL OR valor_posicoes_brl >= 0)
        AND (patrimonio_total_brl IS NULL OR patrimonio_total_brl >= 0)
    ),
    ADD CONSTRAINT ck_carteira_snapshots_valuation_coerente CHECK (
        (valor_posicoes_brl IS NULL
            AND lucro_nao_realizado_brl IS NULL
            AND patrimonio_total_brl IS NULL)
        OR
        (valor_posicoes_brl IS NOT NULL
            AND lucro_nao_realizado_brl IS NOT NULL
            AND patrimonio_total_brl IS NOT NULL
            AND patrimonio_total_brl = saldo_caixa_brl + valor_posicoes_brl)
    );
