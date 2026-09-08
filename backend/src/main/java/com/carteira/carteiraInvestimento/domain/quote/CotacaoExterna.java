package com.carteira.carteiraInvestimento.domain.quote;

import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record CotacaoExterna(BigDecimal preco, Moeda moeda, Instant instanteCotacao, QuoteProvider provider) {
	public CotacaoExterna {
		Objects.requireNonNull(preco, "price must not be null");
		Objects.requireNonNull(moeda, "currency must not be null");
		Objects.requireNonNull(instanteCotacao, "quote instant must not be null");
		Objects.requireNonNull(provider, "provider must not be null");
	}
}
