package com.carteira.carteiraInvestimento.presentation.asset;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;

public record AssetDiscoveryResponse(String ticker, Mercado mercado) { }
