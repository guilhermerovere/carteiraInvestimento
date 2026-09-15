package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.quote.CotacaoExterna;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;

public interface CotacaoProviderPort {
	QuoteProvider provider();
	CotacaoExterna obter(String ticker);
}
