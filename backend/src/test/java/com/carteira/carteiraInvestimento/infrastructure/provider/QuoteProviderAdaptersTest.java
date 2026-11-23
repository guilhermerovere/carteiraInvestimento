package com.carteira.carteiraInvestimento.infrastructure.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException;
import com.carteira.carteiraInvestimento.application.port.AssetDiscoveryProviderPort;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	void brapiParsesObservedV2PayloadAndClassifiesFailures() throws Exception {
		BrapiClient client = mock(BrapiClient.class);
		BrapiResponse observed = new ObjectMapper().readValue("""
				{"results":[{"requestedSymbol":"PETR4","symbol":"PETR4","changed":false,"data":{
				"shortName":"PETR4","longName":"Petroleo Brasileiro SA Pfd","currency":"BRL",
				"regularMarketPrice":49,"logourl":"https://icons.brapi.dev/icons/PETR4.svg"}}]}
				""", BrapiResponse.class);
		when(client.quote(anyString(), anyString())).thenReturn(observed);
		var adapter = new BrapiQuoteAdapter(client, properties("key", "key", "key"));
		Instant before = Instant.now();
		var quote = adapter.obter("PETR4");
		assertThat(quote.preco()).isEqualByComparingTo("49");
		assertThat(quote.moeda()).isEqualTo(Moeda.BRL);
		assertThat(quote.instanteCotacao()).isBetween(before, Instant.now());

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
				quote("AAPL", "Apple Inc.", "201.535"));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));
		assertThat(adapter.obter("AAPL").instanteCotacao()).isEqualTo(Instant.ofEpochSecond(1782923400L));
		when(client.quote(anyString(), anyString())).thenReturn(
				new TwelveResponse(null, null, null, null, null, null, null, null, "error", 404, "symbol not found"));
		assertThatThrownBy(() -> adapter.obter("BAD")).isInstanceOf(QuoteNotFoundException.class);
		when(client.quote(anyString(), anyString())).thenReturn(
				new TwelveResponse(null, null, null, null, null, null, null, null, "error", 429, "rate limit"));
		assertThatThrownBy(() -> adapter.obter("AAPL")).isInstanceOf(QuoteIntegrationException.class);
	}

	@Test
	void twelveDataValidationUsesExactStocksEquityAndDoesNotRequireQuoteType() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks("AAPL", "key")).thenReturn(new TwelveStocksResponse(List.of(
				new TwelveStockItem("AAPL", "Apple Austria", "Austria", "EUR", "Vienna", "XWBO", "Common Stock"),
				new TwelveStockItem("MSFT", "Microsoft Corporation", "United States", "USD", "NASDAQ", "XNGS", "Equity"),
				new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Equity")),
				"ok", null, null));
		when(client.quote("AAPL", "key")).thenReturn(quote("AAPL", "Quote name is not authoritative", "201.535"));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

		var metadata = adapter.validate("AAPL");

		assertThat(metadata.ticker()).isEqualTo("AAPL");
		assertThat(metadata.name()).isEqualTo("Apple Inc.");
		assertThat(metadata.market()).isEqualTo(Mercado.US);
		assertThat(metadata.type()).isEqualTo(TipoAtivo.ACAO);
		verify(client).stocks("AAPL", "key");
		verify(client).quote("AAPL", "key");
	}

	@Test
	void twelveDataValidationMapsExplicitStocksEtfWithoutQuoteType() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks("SPY", "key")).thenReturn(stocks(
				new TwelveStockItem("SPY", "SPDR S&P 500 ETF Trust", "United States", "USD", "NYSE ARCA", "ARCX", "ETF")));
		when(client.quote("SPY", "key")).thenReturn(quote("SPY", "SPY", "681.12"));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

		assertThat(adapter.validate("SPY").type()).isEqualTo(TipoAtivo.ETF);
	}

	@Test
	void twelveDataValidationRejectsAbsentOrUnsupportedStocksIdentityBeforeQuote() {
		for (TwelveStocksResponse response : List.of(
				stocks(new TwelveStockItem("MSFT", "Microsoft", "United States", "USD", "NASDAQ", "XNGS", "Equity")),
				stocks(new TwelveStockItem("AAPL", "Apple", "United States", "USD", "NASDAQ", "XNGS", null)),
				stocks(new TwelveStockItem("AAPL", "Apple Fund", "United States", "USD", "NASDAQ", "XNGS", "Mutual Fund")),
				stocks(new TwelveStockItem("AAPL", "Apple Token", "United States", "USD", "NASDAQ", "XNGS", "Digital Asset")))) {
			TwelveDataClient client = mock(TwelveDataClient.class);
			when(client.stocks("AAPL", "key")).thenReturn(response);
			var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

			assertThatThrownBy(() -> adapter.validate("AAPL")).isInstanceOf(IllegalArgumentException.class);
			verify(client, never()).quote(anyString(), anyString());
		}
	}

	@Test
	void twelveDataValidationRejectsConflictingExactStocksIdentityBeforeQuote() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks("AAPL", "key")).thenReturn(stocks(
				new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Equity"),
				new TwelveStockItem("AAPL", "Apple ETF", "United States", "USD", "NYSE ARCA", "ARCX", "ETF")));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

		assertThatThrownBy(() -> adapter.validate("AAPL"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("asset identity is ambiguous");
		verify(client, never()).quote(anyString(), anyString());
	}

	@Test
	void twelveDataUsesExactReferenceAndQuoteForAllQaTickers() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks(anyString(), anyString())).thenAnswer(invocation -> {
			String ticker = invocation.getArgument(0);
			return stocks(new TwelveStockItem(ticker, ticker + " issuer", "United States", "USD", "NASDAQ", "XNGS", "Common Stock"));
		});
		when(client.quote(anyString(), anyString())).thenAnswer(invocation -> {
			String ticker = invocation.getArgument(0);
			return switch (ticker) {
				case "AAPL" -> quote("AAPL", "Apple Inc.", "201.535");
				case "NVDA" -> quote("NVDA", "NVIDIA Corporation", "143.220");
				case "TSLA" -> quote("TSLA", "Tesla, Inc.", "248.980");
				case "MSFT" -> quote("MSFT", "Microsoft Corporation", "503.120");
				default -> throw new AssertionError("unexpected ticker " + ticker);
			};
		});
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

		for (String ticker : List.of("AAPL", "NVDA", "TSLA", "MSFT")) {
			var metadata = adapter.validate(ticker);
			assertThat(metadata.ticker()).isEqualTo(ticker);
			assertThat(metadata.name()).isNotBlank();
			assertThat(metadata.market()).isEqualTo(Mercado.US);
			assertThat(metadata.type()).isEqualTo(TipoAtivo.ACAO);
			assertThat(adapter.obter(ticker).moeda()).isEqualTo(Moeda.USD);
		}
	}

	@Test
	void twelveDataValidationRejectsForeignOrNonUsdInstrumentBeforeRegistration() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks("AAPL", "key")).thenReturn(stocks(
				new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Common Stock")));
		when(client.quote(anyString(), anyString())).thenReturn(
				new TwelveResponse("AAPL", "Apple Inc.", "Vienna", "XWBO", "EUR", "Common Stock",
						"201.535", 1782923400L, "ok", null, null));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

		assertThatThrownBy(() -> adapter.validate("AAPL"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("asset is not a supported US instrument");
	}

	@Test
	void twelveDataValidationRejectsBlankNonDecimalZeroAndNegativeClose() {
		for (String close : List.of(" ", "not-a-decimal", "0", "-0.01")) {
			TwelveDataClient client = mock(TwelveDataClient.class);
			when(client.stocks("AAPL", "key")).thenReturn(stocks(
					new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Equity")));
			when(client.quote("AAPL", "key")).thenReturn(quote("AAPL", "Apple Inc.", close));
			var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

			assertThatThrownBy(() -> adapter.validate("AAPL"))
					.isInstanceOf(AssetValidationUnavailableException.class)
					.hasMessage("Twelve Data asset validation failed");
		}
	}

	@Test
	void twelveDataValidationRejectsMissingZeroAndNegativeTimestamp() {
		for (Long timestamp : new Long[] {null, 0L, -1L}) {
			TwelveDataClient client = mock(TwelveDataClient.class);
			when(client.stocks("AAPL", "key")).thenReturn(stocks(
					new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Equity")));
			when(client.quote("AAPL", "key")).thenReturn(new TwelveResponse("AAPL", "Apple Inc.", "NASDAQ", "XNGS", "USD",
					null, "201.535", timestamp, "ok", null, null));
			var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

			assertThatThrownBy(() -> adapter.validate("AAPL"))
					.isInstanceOf(AssetValidationUnavailableException.class)
					.hasMessage("Twelve Data asset validation failed");
		}
	}

	@Test
	void twelveDataDiscoverySelectsTheUsStockInsteadOfTheFirstSameTickerResult() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks("AAPL", "key")).thenReturn(new TwelveStocksResponse(List.of(
				new TwelveStockItem("AAPL", "Apple Inc.", "Austria", "EUR", "Vienna", "XWBO", "Common Stock"),
				new TwelveStockItem("AAPL", "Apple Inc.", "Colombia", "COP", "BVC", "XBOG", "Common Stock"),
				new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Common Stock"),
				new TwelveStockItem("AAPL", "Apple Fund", "United States", "USD", "NASDAQ", "XNGS", "Mutual Fund")),
				"ok", null, null));
		when(client.quote("AAPL", "key")).thenReturn(quote("AAPL", "Apple Inc.", "201.535"));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));
		assertThat(adapter.discover("AAPL")).extracting(AssetDiscoveryProviderPort.AssetPreview::ticker)
				.containsExactly("AAPL");
	}

	@Test
	void twelveDataDiscoveryBuildsUsPreviewOnlyFromAValidatedUsdQuote() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks("AAPL", "key")).thenReturn(new TwelveStocksResponse(List.of(
				new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Common Stock")),
				"ok", null, null));
		when(client.quote("AAPL", "key")).thenReturn(quote("AAPL", "Apple Inc.", "201.535"));

		var preview = new TwelveDataQuoteAdapter(client, properties("x", "x", "key")).discover("AAPL");

		assertThat(preview).singleElement().satisfies(item -> {
			assertThat(item.ticker()).isEqualTo("AAPL");
			assertThat(item.market()).isEqualTo(Mercado.US);
			assertThat(item.currency()).isEqualTo(Moeda.USD);
			assertThat(item.currentPrice()).isEqualByComparingTo("201.535");
		});
		verify(client).quote("AAPL", "key");
	}

	@Test
	void twelveDataDiscoverySuppressesCandidatesWithoutAUsableUsdQuoteAndSurfacesUnavailableProvider() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		when(client.stocks("AAPL", "key")).thenReturn(new TwelveStocksResponse(List.of(
				new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Common Stock")),
				"ok", null, null));
		when(client.quote("AAPL", "key")).thenReturn(new TwelveResponse("AAPL", "Apple Inc.", "NASDAQ", "XNGS", "USD",
				"Common Stock", "0", 1782923400L, "ok", null, null));
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));

		assertThat(adapter.discover("AAPL")).isEmpty();

		when(client.quote("AAPL", "key")).thenReturn(new TwelveResponse(null, null, null, null, null, null, null, null,
				"error", 429, "rate limit"));
		assertThatThrownBy(() -> adapter.discover("AAPL")).isInstanceOf(AssetValidationUnavailableException.class);
	}

	@Test
	void twelveDataValidationDoesNotReportProviderFailuresAsMissingAssets() {
		TwelveDataClient client = mock(TwelveDataClient.class);
		var adapter = new TwelveDataQuoteAdapter(client, properties("x", "x", "key"));
		when(client.stocks(anyString(), anyString())).thenReturn(stocks(
				new TwelveStockItem("AAPL", "Apple Inc.", "United States", "USD", "NASDAQ", "XNGS", "Common Stock"),
				new TwelveStockItem("MISSING", "Missing", "United States", "USD", "NASDAQ", "XNGS", "Common Stock")));
		for (int status : List.of(401, 403, 429)) {
			when(client.quote(anyString(), anyString())).thenReturn(
					new TwelveResponse(null, null, null, null, null, null, null, null, "error", status, "provider failure"));
			assertThatThrownBy(() -> adapter.validate("AAPL")).isInstanceOf(AssetValidationUnavailableException.class);
		}
		when(client.quote(anyString(), anyString())).thenReturn(
				new TwelveResponse(null, null, null, null, null, null, null, null, "error", 404, "symbol not found"));
		assertThatThrownBy(() -> adapter.validate("MISSING")).isInstanceOf(IllegalArgumentException.class);
		when(client.quote(anyString(), anyString())).thenReturn(null);
		assertThatThrownBy(() -> adapter.validate("AAPL")).isInstanceOf(AssetValidationUnavailableException.class);
		when(client.stocks(anyString(), anyString())).thenReturn(new TwelveStocksResponse(null, "error", 429, "rate limit"));
		assertThatThrownBy(() -> adapter.validate("AAPL")).isInstanceOf(AssetValidationUnavailableException.class);
		assertThatThrownBy(() -> adapter.discover("AAPL")).isInstanceOf(AssetValidationUnavailableException.class);
		when(client.stocks(anyString(), anyString())).thenReturn(new TwelveStocksResponse(null, "error", 404, "symbol not found"));
		assertThatThrownBy(() -> adapter.validate("MISSING")).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> adapter.discover("MISSING")).isInstanceOf(IllegalArgumentException.class);
	}

	private TwelveStocksResponse stocks(TwelveStockItem... items) {
		return new TwelveStocksResponse(List.of(items), "ok", null, null);
	}

	private TwelveResponse quote(String symbol, String name, String close) {
		return new TwelveResponse(symbol, name, "NASDAQ", "XNGS", "USD", null, close, 1782923400L,
				"ok", null, null);
	}

	private MarketQuoteProperties properties(String brapi, String alpha, String twelve) {
		return new MarketQuoteProperties(new MarketQuoteProperties.Provider("http://localhost", brapi),
				new MarketQuoteProperties.Provider("http://localhost", alpha),
				new MarketQuoteProperties.Provider("http://localhost", twelve));
	}
}
