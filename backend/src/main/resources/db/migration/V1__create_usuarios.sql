CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_usuarios_email_canonico CHECK (email = lower(btrim(email))),
    CONSTRAINT ck_usuarios_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

CREATE UNIQUE INDEX ux_usuarios_email_canonico ON usuarios (lower(email));
