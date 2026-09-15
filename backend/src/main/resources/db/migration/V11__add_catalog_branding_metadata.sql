ALTER TABLE acoes
    ADD COLUMN logo_provider VARCHAR(16) NULL,
    ADD COLUMN logo_reference VARCHAR(512) NULL,
    ADD CONSTRAINT ck_acoes_branding_pair CHECK (
        (logo_provider IS NULL AND logo_reference IS NULL)
        OR (logo_provider IN ('BRAPI', 'LOGO_DEV') AND logo_reference IS NOT NULL AND btrim(logo_reference) <> '')
    );

ALTER TABLE corretoras
    ADD COLUMN logo_provider VARCHAR(16) NULL,
    ADD COLUMN logo_reference VARCHAR(253) NULL,
    ADD CONSTRAINT ck_corretoras_branding_pair CHECK (
        (logo_provider IS NULL AND logo_reference IS NULL)
        OR (logo_provider = 'LOGO_DEV' AND logo_reference IS NOT NULL AND btrim(logo_reference) <> '')
    );
