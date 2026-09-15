package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.fx.CambioExterno;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;

public interface CambioProviderPort {
	CambioProvider provider();
	CambioExterno obterUsdBrl();
}
