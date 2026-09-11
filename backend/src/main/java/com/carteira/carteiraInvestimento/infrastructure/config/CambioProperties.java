package com.carteira.carteiraInvestimento.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.exchange-rates")
public record CambioProperties(Provider alphaVantage, Provider twelveData) {
	public record Provider(String url, String credential) {
		public boolean configured() { return credential != null && !credential.isBlank(); }
	}
}
