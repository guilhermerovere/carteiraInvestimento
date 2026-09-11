package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CambioProviderPort;
import com.carteira.carteiraInvestimento.application.service.CambioProviderException;
import com.carteira.carteiraInvestimento.domain.fx.CambioExterno;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import feign.FeignException;
import feign.RetryableException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class AlphaVantageCambioAdapter implements CambioProviderPort {
	private static final Logger log = LoggerFactory.getLogger(AlphaVantageCambioAdapter.class);
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final AlphaVantageCambioClient client;
	private final MarketQuoteProperties properties;

	public AlphaVantageCambioAdapter(AlphaVantageCambioClient client, MarketQuoteProperties properties) {
		this.client = client; this.properties = properties;
	}

	@Override public CambioProvider provider() { return CambioProvider.ALPHA_VANTAGE; }

	@Override
	public CambioExterno obterUsdBrl() {
		if (!properties.alphaVantage().configured()) throw failure(false, "configuration", null);
		try {
			AlphaCambioResponse response = client.exchangeRate("CURRENCY_EXCHANGE_RATE", "USD", "BRL",
					properties.alphaVantage().credential());
			if (response == null) throw failure(true, "empty", null);
			String semantic = first(response.errorMessage(), response.note(), response.information());
			if (semantic != null) throw failure(!credentialError(semantic), semanticCategory(semantic), null);
			AlphaCambioRate rate = response.rate();
			if (rate == null) throw failure(true, "missing-rate", null);
			if (!"USD".equals(required(rate.fromCurrency())) || !"BRL".equals(required(rate.toCurrency()))) {
				throw failure(true, "pair", null);
			}
			var instant = LocalDateTime.parse(required(rate.lastRefreshed()), DATE_TIME)
					.atZone(ZoneId.of(required(rate.timeZone()))).toInstant();
			return new CambioExterno(MoedaCambio.USD, MoedaCambio.BRL,
					parseRate(rate.exchangeRate()), provider(), instant);
		} catch (CambioProviderException exception) {
			throw exception;
		} catch (RetryableException exception) {
			throw failure(true, "transport", exception);
		} catch (FeignException exception) {
			String body = exception.contentUTF8();
			boolean credential = exception.status() == 400 || exception.status() == 401
					|| exception.status() == 403 || credentialError(body);
			throw failure(!credential, "http-" + exception.status(), exception);
		} catch (RuntimeException exception) {
			throw failure(true, "payload", exception);
		}
	}

	private String required(String value) {
		if (value == null || value.isBlank()) throw failure(true, "payload", null);
		return value.trim();
	}
	private BigDecimal parseRate(String value) {
		BigDecimal original = new BigDecimal(required(value));
		return ObservacaoCambio.normalizarTaxa(original);
	}
	private String first(String... values) {
		for (String value : values) if (value != null && !value.isBlank()) return value;
		return null;
	}
	private boolean credentialError(String value) {
		if (value == null) return false;
		String normalized = value.toLowerCase(Locale.ROOT);
		return normalized.contains("invalid api key") || normalized.contains("apikey is invalid")
				|| normalized.contains("api key is invalid") || normalized.contains("missing api key")
				|| normalized.contains("claim your free api key") || normalized.contains("demo api key")
				|| normalized.contains("premium endpoint")
				|| normalized.contains("unauthorized") || normalized.contains("forbidden");
	}
	private String semanticCategory(String value) {
		String normalized = value.toLowerCase(Locale.ROOT);
		return normalized.contains("rate limit") || normalized.contains("frequency") ? "rate-limit" : "provider-message";
	}
	private CambioProviderException failure(boolean eligible, String category, Throwable cause) {
		log.warn("exchange-rate provider failure provider={} failureCategory={} fallbackEligible={}", provider(), category, eligible);
		String message = "Alpha Vantage exchange-rate integration failed";
		return cause == null ? new CambioProviderException(eligible, message) : new CambioProviderException(eligible, message, cause);
	}
}

@FeignClient(name = "alphaVantageCambioClient", url = "${application.market-quotes.alpha-vantage.url}")
interface AlphaVantageCambioClient {
	@GetMapping("/query")
	AlphaCambioResponse exchangeRate(@RequestParam("function") String function,
			@RequestParam("from_currency") String fromCurrency, @RequestParam("to_currency") String toCurrency,
			@RequestParam("apikey") String apiKey);
}

record AlphaCambioResponse(@JsonProperty("Realtime Currency Exchange Rate") AlphaCambioRate rate,
		@JsonProperty("Error Message") String errorMessage, @JsonProperty("Note") String note,
		@JsonProperty("Information") String information) {}
record AlphaCambioRate(@JsonProperty("1. From_Currency Code") String fromCurrency,
		@JsonProperty("3. To_Currency Code") String toCurrency,
		@JsonProperty("5. Exchange Rate") String exchangeRate,
		@JsonProperty("6. Last Refreshed") String lastRefreshed,
		@JsonProperty("7. Time Zone") String timeZone) {}
