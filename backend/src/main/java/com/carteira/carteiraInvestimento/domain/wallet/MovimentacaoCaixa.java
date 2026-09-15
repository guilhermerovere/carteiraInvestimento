package com.carteira.carteiraInvestimento.domain.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record MovimentacaoCaixa(UUID id, UUID carteiraId, UUID usuarioId, TipoMovimentacaoCaixa tipo,
		BigDecimal valorBrl, String descricao, Instant dataHora) {

	public MovimentacaoCaixa {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(carteiraId, "carteiraId must not be null");
		Objects.requireNonNull(usuarioId, "usuarioId must not be null");
		Objects.requireNonNull(tipo, "tipo must not be null");
		Objects.requireNonNull(valorBrl, "valorBrl must not be null");
		Objects.requireNonNull(dataHora, "dataHora must not be null");
		if (valorBrl.signum() <= 0 || valorBrl.scale() > 2 || valorBrl.precision() - valorBrl.scale() > 16) {
			throw new IllegalArgumentException("invalid BRL amount");
		}
		if (descricao != null && descricao.length() > 160) {
			throw new IllegalArgumentException("description is too long");
		}
	}
}
