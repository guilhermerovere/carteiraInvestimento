package com.carteira.carteiraInvestimento.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class MarketQuoteConfigurationTest {
	@Test
	void cacheExpiresTenMinutesAfterWriteRegardlessOfMarketTimestamp() {
		var cache = new MarketQuoteConfiguration().marketQuoteCache();
		var expiration = cache.policy().expireAfterWrite().orElseThrow();
		assertThat(expiration.getExpiresAfter()).isEqualTo(Duration.ofMinutes(10));
	}

	@Test
	void blankCredentialsAreOptionalAtBindingLevel() {
		var properties = new MarketQuoteProperties(new MarketQuoteProperties.Provider("https://brapi.dev", ""),
				new MarketQuoteProperties.Provider("https://alphavantage.co", null),
				new MarketQuoteProperties.Provider("https://twelvedata.com", " "));
		assertThat(properties.brapi().configured()).isFalse();
		assertThat(properties.alphaVantage().configured()).isFalse();
		assertThat(properties.twelveData().configured()).isFalse();
	}
}
