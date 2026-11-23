package com.carteira.carteiraInvestimento.presentation.asset;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
import com.carteira.carteiraInvestimento.application.port.AssetDiscoveryProviderPort.AssetPreview;
import java.math.BigDecimal;

public record AssetDiscoveryResponse(String ticker, String nome, TipoAtivo tipo, Mercado mercado, Moeda moeda,
		LogoProvider logoProvider, String logoReference, BigDecimal cotacaoAtual) {
	public static AssetDiscoveryResponse from(AssetPreview preview) {
		return new AssetDiscoveryResponse(preview.ticker(), preview.name(), preview.type(), preview.market(),
				preview.currency(), preview.logoProvider(), preview.logoReference(), preview.currentPrice());
	}
}
