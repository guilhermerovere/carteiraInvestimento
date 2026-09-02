CREATE TABLE logs_auditoria (
    id UUID PRIMARY KEY,
    usuario_id UUID NULL,
    tipo_evento VARCHAR(64) NOT NULL,
    resultado VARCHAR(32) NOT NULL,
    severidade VARCHAR(32) NOT NULL,
    endpoint VARCHAR(512) NULL,
    correlation_id UUID NOT NULL,
    data_hora TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX ix_logs_auditoria_usuario_id ON logs_auditoria (usuario_id);
CREATE INDEX ix_logs_auditoria_correlation_id ON logs_auditoria (correlation_id);
