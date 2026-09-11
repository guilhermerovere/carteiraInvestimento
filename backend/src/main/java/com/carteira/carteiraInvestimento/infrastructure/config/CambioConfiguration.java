package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.port.CambioProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCambioPort;
import com.carteira.carteiraInvestimento.application.service.CambioApplicationService;
import com.carteira.carteiraInvestimento.application.service.CambioUseCase;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CambioConfiguration {
	@Bean("cambioTicker") Ticker cambioTicker() { return Ticker.systemTicker(); }
	@Bean("cambioCache") Cache<String, ObservacaoCambio> cambioCache(@Qualifier("cambioTicker") Ticker ticker) {
		return Caffeine.newBuilder().ticker(ticker).expireAfterWrite(Duration.ofMinutes(5)).build();
	}
	@Bean CambioUseCase cambioUseCase(HistoricoCambioPort historico, AuditoriaIsoladaPort auditoria,
			List<CambioProviderPort> providers, @Qualifier("cambioCache") Cache<String, ObservacaoCambio> cache,
			Clock clock) {
		return new CambioApplicationService(historico, auditoria, providers, cache, clock);
	}
}
