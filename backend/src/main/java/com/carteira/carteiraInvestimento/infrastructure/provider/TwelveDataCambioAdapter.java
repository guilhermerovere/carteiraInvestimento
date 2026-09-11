package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CambioProviderPort;
import com.carteira.carteiraInvestimento.application.service.CambioProviderException;
import com.carteira.carteiraInvestimento.domain.fx.CambioExterno;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.infrastructure.config.CambioProperties;
import feign.FeignException;
import feign.RetryableException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class TwelveDataCambioAdapter implements CambioProviderPort {
	private static final Logger log = LoggerFactory.getLogger(TwelveDataCambioAdapter.class);
	private final TwelveDataCambioClient client;
	private final CambioProperties properties;

	public TwelveDataCambioAdapter(TwelveDataCambioClient client, CambioProperties properties) {
		this.client = client; this.properties = properties;
	}
	@Override public CambioProvider provider() { return CambioProvider.TWELVE_DATA; }

	@Override
	public CambioExterno obterUsdBrl() {
		if (!properties.twelveData().configured()) throw failure(false, "configuration", null);
		try {
			TwelveCambioResponse response = client.exchangeRate("USD/BRL", properties.twelveData().credential());
			if (response == null) throw failure(true, "empty", null);
			if ("error".equalsIgnoreCase(response.status()) || response.message() != null && response.rate() == null) {
				String semantic = response.message() == null ? "" : response.message();
				throw failure(!credentialError(semantic, response.code()), semanticCategory(semantic), null);
			}
			if (!"USD/BRL".equals(required(response.symbol()))) throw failure(true, "pair", null);
			if (response.timestamp() == null || response.timestamp() <= 0) throw failure(true, "timestamp", null);
			return new CambioExterno(MoedaCambio.USD, MoedaCambio.BRL,
					parseRate(response.rate()), provider(), Instant.ofEpochSecond(response.timestamp()));
		} catch (CambioProviderException exception) {
			throw exception;
		} catch (RetryableException exception) {
			throw failure(true, "transport", exception);
		} catch (FeignException exception) {
			String body = exception.contentUTF8();
			boolean credential = exception.status() == 401 || exception.status() == 403 || credentialError(body, exception.status());
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
		if (original.signum() <= 0) throw failure(true, "rate", null);
		BigDecimal normalized = original.setScale(8, RoundingMode.HALF_EVEN);
		if (normalized.signum() <= 0 || normalized.precision() - normalized.scale() > 10) {
			throw failure(true, "rate", null);
		}
		return original;
	}
	private boolean credentialError(String value, Integer code) {
		String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
		return code != null && (code == 401 || code == 403)
				|| normalized.contains("invalid api key") || normalized.contains("apikey is invalid")
				|| normalized.contains("api key is invalid") || normalized.contains("missing api key")
				|| normalized.contains("unauthorized") || normalized.contains("forbidden");
	}
	private String semanticCategory(String value) {
		String normalized = value.toLowerCase(Locale.ROOT);
		return normalized.contains("rate limit") || normalized.contains("credits") ? "rate-limit" : "provider-message";
	}
	private CambioProviderException failure(boolean eligible, String category, Throwable cause) {
		log.warn("exchange-rate provider failure provider={} failureCategory={} fallbackEligible={}", provider(), category, eligible);
		String message = "Twelve Data exchange-rate integration failed";
		return cause == null ? new CambioProviderException(eligible, message) : new CambioProviderException(eligible, message, cause);
	}
}

@FeignClient(name = "twelveDataCambioClient", url = "${application.exchange-rates.twelve-data.url}")
interface TwelveDataCambioClient {
	@GetMapping("/exchange_rate")
	TwelveCambioResponse exchangeRate(@RequestParam("symbol") String symbol, @RequestParam("apikey") String apiKey);
}
record TwelveCambioResponse(String symbol, String rate, Long timestamp, String status, Integer code, String message) {}
