package com.carteira.carteiraInvestimento.infrastructure.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties;
import feign.FeignException;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

class QuoteProviderAdaptersTest {
	@Test
	void brapiParsesIsoInstantDecimalCurrencyAndClassifiesFailures() {
		BrapiClient client = mock(BrapiClient.class);
		when(client.quote(anyString(), anyString())).thenReturn(new BrapiResponse(List.of(
				new BrapiResult("PETR4", new BigDecimal("123.456789"), "BRL", "2026-09-08T18:30:00Z"))));
		var adapter = new BrapiQuoteAdapter(client, properties("key", "key", "key"));
		var quote = adapter.obter("PETR4");
		assertThat(quote.preco()).isEqualByComparingTo("123.456789");
		assertThat(quote.moeda()).isEqualTo(Moeda.BRL);
		assertThat(quote.instanteCotacao()).isEqualTo(Instant.parse("2026-09-08T18:30:00Z"));

		when(client.quote(anyString(), anyString())).thenReturn(new BrapiResponse(List.of()));
		assertThatThrownBy(() -> adapter.obter("PETR4")).isInstanceOf(QuoteIntegrationException.class);
		FeignException notFound = mock(FeignException.class); when(notFound.status()).thenReturn(404);
		doThrow(notFound).when(client).quote(anyString(), anyString());
		assertThatThrownBy(() -> adapter.obter("PETR4")).isInstanceOf(QuoteNotFoundException.class);
	}

	@Test
	void externalHttpAuthenticationRateLimitServerAndTimeoutAreIntegrationFailures() {
		BrapiClient client = mock(BrapiClient.class);
		var adapter = new BrapiQuoteAdapter(client, properties("key", "key", "key"));
		for (int status : List.of(400, 401, 403, 429, 500, 503)) {
			FeignException failure = mock(FeignException.class);
			when(failure.status()).thenReturn(status);
			doThrow(failure).when(client).quote(anyString(), anyString());
			assertThatThrownBy(() -> adapter.obter("PETR4")).isInstanceOf(QuoteIntegrationException.class);
		}
		doThrow(new RuntimeException("timeout")).when(client).quote(anyString(), anyString());
		assertThatThrownBy(() -> adapter.obter("PETR4")).isInstanceOf(QuoteIntegrationException.class);
	}

	@Test
	void alphaUsesProviderTimezoneAndNeverJvmDefault() {
		AlphaVantageClient client = mock(AlphaVantageClient.class);
		Map<String, AlphaBar> series = new LinkedHashMap<>();
		series.put("2026-07-01 12:30:00", new AlphaBar("10.12555"));
		when(client.intraday(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new AlphaResponse(new AlphaMeta("America/New_York"), series, null, null, null));
		var adapter = new AlphaVantageQuoteAdapter(client, properties("key", "key", "key"));
		TimeZone previous = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
			assertThat(adapter.obter("AAPL").instanteCotacao()).isEqualTo(Instant.parse("2026-07-01T16:30:00Z"));
			TimeZone.setDefault(TimeZone.getTimeZone("Europe/Lisbon"));
			assertThat(adapter.obter("AAPL").instanteCotacao()).isEqualTo(Instant.parse("2026-07-01T16:30:00Z"));
		} finally { TimeZone.setDefault(previous); }

		when(client.intraday(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new AlphaResponse(new AlphaMeta(null), series, null, null, null));
		assertThatThrownBy(() -> adapter.obter("AAPL")).isInstanceOf(QuoteIntegrationException.class);
	}

	@Test
	void alphaSeparatesSemanticNotFoundFromRateLimitAndMissingConfiguration() {
		AlphaVantageClient client = mock(AlphaVantageClient.class);
		when(client.intraday(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new AlphaResponse(null, null, "Invalid API call", null, null));
		assertThatThrownBy(() -> new AlphaVantageQuoteAdapter(client, properties("x", "key", "x")).obter("BAD"))
				.isInstanceOf(QuoteNotFoundException.class);
		when(client.intraday(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new AlphaResponse(null, null, null, "rate limit", null));
		assertThatThrownBy(() -> new AlphaVantageQuoteAdapter(client, properties("x", "key", "x")).obter("AAPL"))
				.isInstanceOf(QuoteIntegrationException.class);
		assertThatThrownBy(() -> new AlphaVantageQuoteAdapter(client, properties("x", "", "x")).obter("AAPL"))
				.isInstanceOf(QuoteIntegrationException.class);
	}

	@Test
	void twelveDataUsesUnixTimestampAndClassifiesProviderErrors() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.quote(anyString(), anyString())).thenReturn(
				new TwelveResponse("201.535", "USD", 1782923400L, "ok", null, null));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));
		assertThat(adapter.obter("AAPL").instanteCotacao()).isEqualTo(Instant.ofEpochSecond(1782923400L));
		when(client.quote(anyString(), anyString())).thenReturn(
				new TwelveResponse(null, null, null, "error", 404, "symbol not found"));
		assertThatThrownBy(() -> adapter.obter("BAD")).isInstanceOf(QuoteNotFoundException.class);
		when(client.quote(anyString(), anyString())).thenReturn(
				new TwelveResponse(null, null, null, "error", 429, "rate limit"));
		assertThatThrownBy(() -> adapter.obter("AAPL")).isInstanceOf(QuoteIntegrationException.class);
	}

	private MarketQuoteProperties properties(String brapi, String alpha, String twelve) {
		return new MarketQuoteProperties(new MarketQuoteProperties.Provider("http://localhost", brapi),
				new MarketQuoteProperties.Provider("http://localhost", alpha),
				new MarketQuoteProperties.Provider("http://localhost", twelve));
	}
}
