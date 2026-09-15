package com.carteira.carteiraInvestimento.presentation.quote;

import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CotacaoResponse(UUID id, UUID ativoId, BigDecimal preco, Moeda moeda,
		Instant instanteCotacao, QuoteProvider provider, Instant recebidoEm) {
	public static CotacaoResponse from(Cotacao quote) {
		return new CotacaoResponse(quote.id(), quote.ativoId(), quote.preco(), quote.moeda(),
				quote.instanteCotacao(), quote.provider(), quote.recebidoEm());
	}
}
