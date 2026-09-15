package com.carteira.carteiraInvestimento.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.logo-dev")
public record LogoDevProperties(String url, String secretKey) {
	public boolean configured() { return secretKey != null && !secretKey.isBlank(); }
}
