package com.carteira.carteiraInvestimento.domain.fx;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record CambioExterno(MoedaCambio moedaOrigem, MoedaCambio moedaDestino, BigDecimal taxa,
		CambioProvider provider, Instant instanteCotacao) {
	public CambioExterno {
		Objects.requireNonNull(moedaOrigem, "source currency must not be null");
		Objects.requireNonNull(moedaDestino, "target currency must not be null");
		Objects.requireNonNull(taxa, "rate must not be null");
		Objects.requireNonNull(provider, "provider must not be null");
		Objects.requireNonNull(instanteCotacao, "quote instant must not be null");
		if (moedaOrigem != MoedaCambio.USD || moedaDestino != MoedaCambio.BRL) {
			throw new IllegalArgumentException("only USD/BRL is supported");
		}
	}
}
