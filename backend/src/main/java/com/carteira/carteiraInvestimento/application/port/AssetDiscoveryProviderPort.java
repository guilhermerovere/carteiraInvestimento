package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
import java.math.BigDecimal;
import java.util.List;

public interface AssetDiscoveryProviderPort {
	Mercado market();
	List<AssetPreview> discover(String search);

	record AssetPreview(String ticker, String name, TipoAtivo type, Mercado market, Moeda currency,
			LogoProvider logoProvider, String logoReference, BigDecimal currentPrice) { }
}
