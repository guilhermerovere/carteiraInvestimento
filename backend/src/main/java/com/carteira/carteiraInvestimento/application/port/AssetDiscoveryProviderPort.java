package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import java.util.List;

public interface AssetDiscoveryProviderPort {
	Mercado market();
	List<String> discover(String search);
}
