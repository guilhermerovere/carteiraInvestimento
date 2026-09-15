CREATE TABLE carteiras (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    saldo_caixa_brl NUMERIC(18,2) NOT NULL,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carteiras_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT uk_carteiras_usuario UNIQUE (usuario_id)
);
