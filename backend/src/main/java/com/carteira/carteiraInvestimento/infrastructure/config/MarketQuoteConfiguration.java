package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.port.CotacaoProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPort;
import com.carteira.carteiraInvestimento.application.service.MarketQuoteApplicationService;
import com.carteira.carteiraInvestimento.application.service.MarketQuoteUseCase;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableFeignClients(basePackages = "com.carteira.carteiraInvestimento.infrastructure.provider")
public class MarketQuoteConfiguration {
	@Bean Cache<UUID, Cotacao> marketQuoteCache() {
		return Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();
	}

	@Bean MarketQuoteUseCase marketQuoteUseCase(AtivoPort ativos, HistoricoCotacaoPort history,
			List<CotacaoProviderPort> providers, Cache<UUID, Cotacao> cache, Clock clock) {
		return new MarketQuoteApplicationService(ativos, history, providers, cache, clock);
	}
}
