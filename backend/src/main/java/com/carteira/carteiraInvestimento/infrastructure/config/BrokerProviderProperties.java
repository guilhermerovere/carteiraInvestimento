package com.carteira.carteiraInvestimento.infrastructure.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("application.broker-catalog")
public record BrokerProviderProperties(Provider brasilApiCnpj, Provider brasilApiCvm, Provider viaCep) {
    public record Provider(String url) { }
}
