ALTER TABLE carteiras
    ADD CONSTRAINT ck_carteiras_saldo_caixa_brl_non_negative CHECK (saldo_caixa_brl >= 0),
    ADD CONSTRAINT uk_carteiras_id_usuario UNIQUE (id, usuario_id);

CREATE TABLE movimentacoes_caixa (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    tipo VARCHAR(16) NOT NULL,
    valor_brl NUMERIC(18,2) NOT NULL,
    descricao VARCHAR(160) NULL,
    data_hora TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_movimentacoes_caixa_tipo CHECK (tipo IN ('DEPOSITO', 'SAQUE')),
    CONSTRAINT ck_movimentacoes_caixa_valor_positivo CHECK (valor_brl > 0),
    CONSTRAINT fk_movimentacoes_caixa_carteira_usuario
        FOREIGN KEY (carteira_id, usuario_id) REFERENCES carteiras (id, usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_movimentacoes_caixa_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE INDEX ix_movimentacoes_caixa_historico
    ON movimentacoes_caixa (carteira_id, data_hora DESC, id DESC);

CREATE TABLE carteira_snapshots (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    data_referencia DATE NOT NULL,
    saldo_caixa_brl NUMERIC(18,2) NOT NULL,
    valor_posicoes_brl NUMERIC(18,2) NOT NULL,
    total_investido_brl NUMERIC(18,2) NOT NULL,
    patrimonio_total_brl NUMERIC(18,2) NOT NULL,
    lucro_nao_realizado_brl NUMERIC(18,2) NOT NULL,
    CONSTRAINT uk_carteira_snapshots_carteira_data UNIQUE (carteira_id, data_referencia),
    CONSTRAINT fk_carteira_snapshots_carteira
        FOREIGN KEY (carteira_id) REFERENCES carteiras (id) ON DELETE RESTRICT,
    CONSTRAINT ck_carteira_snapshots_valores_minimos CHECK (
        saldo_caixa_brl >= 0
        AND valor_posicoes_brl >= 0
        AND total_investido_brl >= 0
        AND patrimonio_total_brl >= 0
    )
);

CREATE TABLE movimentacoes_caixa_idempotencia (
    id UUID PRIMARY KEY,
    carteira_id UUID NOT NULL,
    tipo VARCHAR(16) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    fingerprint CHAR(64) NOT NULL,
    movimentacao_id UUID NULL,
    saldo_resultante NUMERIC(18,2) NULL,
    criada_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_movimentacoes_caixa_idempotencia UNIQUE (carteira_id, tipo, idempotency_key),
    CONSTRAINT ck_movimentacoes_caixa_idempotencia_tipo CHECK (tipo IN ('DEPOSITO', 'SAQUE')),
    CONSTRAINT ck_movimentacoes_caixa_idempotencia_key CHECK (
        idempotency_key ~ '^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$'
    ),
    CONSTRAINT ck_movimentacoes_caixa_idempotencia_fingerprint CHECK (
        fingerprint ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_movimentacoes_caixa_idempotencia_resultado CHECK (
        (movimentacao_id IS NULL AND saldo_resultante IS NULL)
        OR (movimentacao_id IS NOT NULL AND saldo_resultante IS NOT NULL)
    ),
    CONSTRAINT fk_movimentacoes_caixa_idempotencia_carteira
        FOREIGN KEY (carteira_id) REFERENCES carteiras (id) ON DELETE RESTRICT,
    CONSTRAINT fk_movimentacoes_caixa_idempotencia_movimentacao
        FOREIGN KEY (movimentacao_id) REFERENCES movimentacoes_caixa (id) ON DELETE RESTRICT
);
