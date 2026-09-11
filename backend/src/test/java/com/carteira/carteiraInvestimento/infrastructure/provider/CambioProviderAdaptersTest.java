package com.carteira.carteiraInvestimento.infrastructure.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.service.CambioProviderException;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.infrastructure.config.CambioProperties;
import feign.Feign;
import feign.FeignException;
import feign.RetryableException;
import feign.jackson.JacksonDecoder;
import java.time.Instant;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

class CambioProviderAdaptersTest {
	@Test void alphaUsesExactRequestAndParsesRatePairTimestampAndTimezone() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(json("{\"Realtime Currency Exchange Rate\":{" +
					"\"1. From_Currency Code\":\"USD\",\"3. To_Currency Code\":\"BRL\"," +
					"\"5. Exchange Rate\":\"5.123456789\",\"6. Last Refreshed\":\"2026-09-10 09:30:00\"," +
					"\"7. Time Zone\":\"America/New_York\"}}"));
			var client = feign(AlphaVantageCambioClient.class, server);
			var result = new AlphaVantageCambioAdapter(client, properties(server, "alpha", "twelve")).obterUsdBrl();
			var request = server.takeRequest();
			assertThat(request.getPath()).contains("function=CURRENCY_EXCHANGE_RATE", "from_currency=USD",
					"to_currency=BRL", "apikey=alpha");
			assertThat(result.taxa().toPlainString()).isEqualTo("5.123456789");
			assertThat(result.instanteCotacao()).isEqualTo(Instant.parse("2026-09-10T13:30:00Z"));
			assertThat(result.provider()).isEqualTo(CambioProvider.ALPHA_VANTAGE);
		}
	}

	@Test void twelveUsesOnlyExchangeRateAndUnixTimestamp() throws Exception {
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(json("{\"symbol\":\"USD/BRL\",\"rate\":\"5.01020304\",\"timestamp\":1789047000}"));
			var client = feign(TwelveDataCambioClient.class, server);
			var result = new TwelveDataCambioAdapter(client, properties(server, "alpha", "twelve")).obterUsdBrl();
			var request = server.takeRequest();
			assertThat(request.getPath()).startsWith("/exchange_rate?").contains("symbol=USD/BRL", "apikey=twelve")
					.doesNotContain("currency_conversion");
			assertThat(result.instanteCotacao()).isEqualTo(Instant.ofEpochSecond(1789047000));
			assertThat(result.provider()).isEqualTo(CambioProvider.TWELVE_DATA);
		}
	}

	@Test void semanticHttp200FailuresDistinguishRateLimitCredentialAndMissingRate() {
		AlphaVantageCambioClient client = mock(AlphaVantageCambioClient.class);
		var adapter = new AlphaVantageCambioAdapter(client, properties("alpha", "twelve"));
		when(client.exchangeRate(anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new AlphaCambioResponse(null, null, "rate limit reached", null));
		assertFailure(adapter::obterUsdBrl, true);
		when(client.exchangeRate(anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new AlphaCambioResponse(null, null, null, "Invalid API key"));
		assertFailure(adapter::obterUsdBrl, false);
		when(client.exchangeRate(anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new AlphaCambioResponse(null, null, null, null));
		assertFailure(adapter::obterUsdBrl, true);
	}

	@Test void invalidPairRateTimestampTimezoneAndDecodeAreEligible() {
		AlphaVantageCambioClient alpha = mock(AlphaVantageCambioClient.class);
		var adapter = new AlphaVantageCambioAdapter(alpha, properties("alpha", "twelve"));
		for (AlphaCambioRate rate : List.of(
				new AlphaCambioRate("EUR", "BRL", "5", "2026-09-10 12:00:00", "UTC"),
				new AlphaCambioRate("USD", "BRL", "bad", "2026-09-10 12:00:00", "UTC"),
				new AlphaCambioRate("USD", "BRL", "5", "bad", "UTC"),
				new AlphaCambioRate("USD", "BRL", "5", "2026-09-10 12:00:00", "Not/AZone"))) {
			when(alpha.exchangeRate(anyString(), anyString(), anyString(), anyString()))
					.thenReturn(new AlphaCambioResponse(rate, null, null, null));
			assertFailure(adapter::obterUsdBrl, true);
		}
	}

	@Test void timeoutConnection429AndServerErrorsAreEligibleWhileCredentialHttpIsNot() {
		AlphaVantageCambioClient client = mock(AlphaVantageCambioClient.class);
		var adapter = new AlphaVantageCambioAdapter(client, properties("alpha", "twelve"));
		RetryableException retry = mock(RetryableException.class);
		doThrow(retry).when(client).exchangeRate(anyString(), anyString(), anyString(), anyString());
		assertFailure(adapter::obterUsdBrl, true);
		for (int status : List.of(429, 500, 503, 401, 403)) {
			FeignException failure = mock(FeignException.class);
			when(failure.status()).thenReturn(status); when(failure.contentUTF8()).thenReturn("");
			doThrow(failure).when(client).exchangeRate(anyString(), anyString(), anyString(), anyString());
			assertFailure(adapter::obterUsdBrl, status != 401 && status != 403);
		}
	}

	@Test void twelveClassifiesSemanticRateLimitCredentialMissingRateAndInvalidTimestamp() {
		TwelveDataCambioClient client = mock(TwelveDataCambioClient.class);
		var adapter = new TwelveDataCambioAdapter(client, properties("alpha", "twelve"));
		when(client.exchangeRate(anyString(), anyString())).thenReturn(new TwelveCambioResponse(null, null, null, "error", 429, "rate limit"));
		assertFailure(adapter::obterUsdBrl, true);
		when(client.exchangeRate(anyString(), anyString())).thenReturn(new TwelveCambioResponse(null, null, null, "error", 401, "Invalid API key"));
		assertFailure(adapter::obterUsdBrl, false);
		when(client.exchangeRate(anyString(), anyString())).thenReturn(new TwelveCambioResponse("USD/BRL", null, 1L, null, null, null));
		assertFailure(adapter::obterUsdBrl, true);
		when(client.exchangeRate(anyString(), anyString())).thenReturn(new TwelveCambioResponse("USD/BRL", "5", 0L, null, null, null));
		assertFailure(adapter::obterUsdBrl, true);
	}

	@Test void missingLocalKeysAreNonEligibleWithoutCallingHttp() {
		AlphaVantageCambioClient alpha = mock(AlphaVantageCambioClient.class);
		assertFailure(() -> new AlphaVantageCambioAdapter(alpha, properties("", "")).obterUsdBrl(), false);
		TwelveDataCambioClient twelve = mock(TwelveDataCambioClient.class);
		assertFailure(() -> new TwelveDataCambioAdapter(twelve, properties("", "")).obterUsdBrl(), false);
	}

	@Test void twelveTimeoutConnection429ServerAndDecodeFailuresAreEligible() throws Exception {
		TwelveDataCambioClient client = mock(TwelveDataCambioClient.class);
		var adapter = new TwelveDataCambioAdapter(client, properties("alpha", "twelve"));
		doThrow(mock(RetryableException.class)).when(client).exchangeRate(anyString(), anyString());
		assertFailure(adapter::obterUsdBrl, true);
		for (int status : List.of(429, 500, 503, 401, 403)) {
			FeignException failure = mock(FeignException.class); when(failure.status()).thenReturn(status);
			when(failure.contentUTF8()).thenReturn(""); doThrow(failure).when(client).exchangeRate(anyString(), anyString());
			assertFailure(adapter::obterUsdBrl, status != 401 && status != 403);
		}
		try (MockWebServer server = new MockWebServer()) {
			server.enqueue(json("{invalid-json"));
			var actual = new TwelveDataCambioAdapter(feign(TwelveDataCambioClient.class, server), properties(server,"alpha","twelve"));
			assertFailure(actual::obterUsdBrl, true);
		}
	}

	@Test void providerRatesThatAreNonPositiveRoundToZeroOrOverflowAreEligible() {
		TwelveDataCambioClient client = mock(TwelveDataCambioClient.class);
		var adapter = new TwelveDataCambioAdapter(client, properties("alpha", "twelve"));
		for (String rate : List.of("0", "-1", "0.000000004", "10000000000.00000000")) {
			when(client.exchangeRate(anyString(), anyString())).thenReturn(new TwelveCambioResponse("USD/BRL",rate,1L,null,null,null));
			assertFailure(adapter::obterUsdBrl, true);
		}
	}

	private <T> T feign(Class<T> type, MockWebServer server) {
		return Feign.builder().contract(new SpringMvcContract()).decoder(new JacksonDecoder())
				.target(type, server.url("/").toString());
	}
	private MockResponse json(String body) { return new MockResponse().setHeader("Content-Type", "application/json").setBody(body); }
	private CambioProperties properties(MockWebServer server, String alpha, String twelve) {
		return new CambioProperties(new CambioProperties.Provider(server.url("/").toString(), alpha),
				new CambioProperties.Provider(server.url("/").toString(), twelve));
	}
	private CambioProperties properties(String alpha, String twelve) {
		return new CambioProperties(new CambioProperties.Provider("http://localhost", alpha),
				new CambioProperties.Provider("http://localhost", twelve));
	}
	private void assertFailure(Runnable action, boolean eligible) {
		assertThatThrownBy(action::run).isInstanceOfSatisfying(CambioProviderException.class,
				exception -> assertThat(exception.fallbackEligible()).isEqualTo(eligible));
	}
}
