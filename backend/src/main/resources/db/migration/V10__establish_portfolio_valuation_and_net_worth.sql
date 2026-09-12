ALTER TABLE carteiras
    ADD COLUMN estado_versao BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_carteiras_estado_versao_non_negative CHECK (estado_versao >= 0);

ALTER TABLE carteira_snapshots
    ADD COLUMN valuation_instant TIMESTAMPTZ NULL;

UPDATE carteira_snapshots
SET valuation_instant = data_referencia::timestamp AT TIME ZONE 'America/Sao_Paulo'
WHERE valor_posicoes_brl IS NOT NULL
  AND lucro_nao_realizado_brl IS NOT NULL
  AND patrimonio_total_brl IS NOT NULL
  AND NOT (
      total_investido_brl = 0
      AND valor_posicoes_brl = 0
      AND lucro_nao_realizado_brl = 0
      AND patrimonio_total_brl = saldo_caixa_brl
  );

ALTER TABLE carteira_snapshots
    DROP CONSTRAINT ck_carteira_snapshots_valuation_coerente,
    ADD CONSTRAINT ck_carteira_snapshots_valuation_coerente CHECK (
        (valor_posicoes_brl IS NULL
            AND lucro_nao_realizado_brl IS NULL
            AND patrimonio_total_brl IS NULL
            AND valuation_instant IS NULL)
        OR
        (total_investido_brl = 0
            AND valor_posicoes_brl = 0
            AND lucro_nao_realizado_brl = 0
            AND patrimonio_total_brl = saldo_caixa_brl)
        OR
        (valor_posicoes_brl IS NOT NULL
            AND lucro_nao_realizado_brl IS NOT NULL
            AND patrimonio_total_brl IS NOT NULL
            AND valuation_instant IS NOT NULL
            AND patrimonio_total_brl = saldo_caixa_brl + valor_posicoes_brl)
    );
