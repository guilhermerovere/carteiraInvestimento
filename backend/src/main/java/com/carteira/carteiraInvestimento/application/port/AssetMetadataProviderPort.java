package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;

public interface AssetMetadataProviderPort {
	Mercado market();
	AssetMetadata validate(String requestedTicker);

	record AssetMetadata(String requestedTicker, String ticker, String name, TipoAtivo type, Mercado market,
			LogoProvider logoProvider, String logoReference) { }
}
