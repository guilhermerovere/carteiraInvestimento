package com.carteira.carteiraInvestimento.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.market-quotes")
public record MarketQuoteProperties(Provider brapi, Provider alphaVantage, Provider twelveData) {
	public record Provider(String url, String credential) {
		public boolean configured() { return credential != null && !credential.isBlank(); }
	}
}
