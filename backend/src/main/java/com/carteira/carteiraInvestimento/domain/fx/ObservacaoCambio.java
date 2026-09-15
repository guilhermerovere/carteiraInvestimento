package com.carteira.carteiraInvestimento.domain.fx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ObservacaoCambio(UUID id, MoedaCambio moedaOrigem, MoedaCambio moedaDestino,
		BigDecimal taxa, CambioProvider provider, Instant instanteCotacao, Instant registradoEm) {
	private static final int SCALE = 8;
	private static final int INTEGER_DIGITS = 10;

	public ObservacaoCambio {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(moedaOrigem, "source currency must not be null");
		Objects.requireNonNull(moedaDestino, "target currency must not be null");
		Objects.requireNonNull(provider, "provider must not be null");
		Objects.requireNonNull(instanteCotacao, "quote instant must not be null");
		Objects.requireNonNull(registradoEm, "recorded instant must not be null");
		if (moedaOrigem != MoedaCambio.USD || moedaDestino != MoedaCambio.BRL) {
			throw new IllegalArgumentException("only USD/BRL is supported");
		}
		taxa = normalizarTaxa(taxa);
	}

	public static ObservacaoCambio aceitar(CambioExterno externa, Instant registradoEm) {
		Objects.requireNonNull(externa, "external exchange rate must not be null");
		return new ObservacaoCambio(UUID.randomUUID(), externa.moedaOrigem(), externa.moedaDestino(),
				externa.taxa(), externa.provider(), externa.instanteCotacao(), registradoEm);
	}

	public static BigDecimal normalizarTaxa(BigDecimal value) {
		Objects.requireNonNull(value, "rate must not be null");
		if (value.signum() <= 0) throw new IllegalArgumentException("rate must be positive");
		BigDecimal normalized = value.scale() == SCALE
				? value
				: value.setScale(SCALE, RoundingMode.HALF_EVEN);
		if (normalized.signum() <= 0) throw new IllegalArgumentException("rate rounds to zero");
		if (normalized.precision() - normalized.scale() > INTEGER_DIGITS) {
			throw new IllegalArgumentException("rate exceeds NUMERIC(18,8)");
		}
		return normalized;
	}
}
