package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CotacaoProviderPort;
import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.quote.CotacaoExterna;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class TwelveDataQuoteAdapter implements CotacaoProviderPort {
	private static final Logger log = LoggerFactory.getLogger(TwelveDataQuoteAdapter.class);
	private final TwelveDataClient client;
	private final MarketQuoteProperties properties;

	public TwelveDataQuoteAdapter(TwelveDataClient client, MarketQuoteProperties properties) {
		this.client = client; this.properties = properties;
	}

	@Override public QuoteProvider provider() { return QuoteProvider.TWELVE_DATA; }

	@Override
	public CotacaoExterna obter(String ticker) {
		if (!properties.twelveData().configured()) throw integration("configuration");
		try {
			TwelveResponse response = client.quote(ticker, properties.twelveData().credential());
			if (response == null) throw integration("empty");
			if ("error".equalsIgnoreCase(response.status())) {
				String message = response.message() == null ? "" : response.message().toLowerCase(Locale.ROOT);
				if (response.code() != null && response.code() == 404 || message.contains("not found") || message.contains("not supported")) {
					throw new QuoteNotFoundException();
				}
				throw integration("provider-message");
			}
			if (response.timestamp() == null || response.timestamp() <= 0) throw integration("payload");
			return new CotacaoExterna(new BigDecimal(required(response.close())),
					Moeda.valueOf(required(response.currency())), Instant.ofEpochSecond(response.timestamp()), provider());
		} catch (QuoteIntegrationException | QuoteNotFoundException exception) {
			throw exception;
		} catch (FeignException exception) {
			if (exception.status() == 404) throw new QuoteNotFoundException(exception);
			throw integration("http", exception);
		} catch (RuntimeException exception) {
			throw integration("payload", exception);
		}
	}

	private String required(String value) {
		if (value == null || value.isBlank()) throw integration("payload");
		return value.trim();
	}
	private QuoteIntegrationException integration(String category) {
		log.warn("quote provider failure provider={} failureCategory={} fallback=true", provider(), category);
		return new QuoteIntegrationException("Twelve Data integration failed");
	}
	private QuoteIntegrationException integration(String category, Throwable cause) {
		log.warn("quote provider failure provider={} failureCategory={} fallback=true", provider(), category);
		return new QuoteIntegrationException("Twelve Data integration failed", cause);
	}
}

@FeignClient(name = "twelveDataQuoteClient", url = "${application.market-quotes.twelve-data.url}")
interface TwelveDataClient {
	@GetMapping("/quote")
	TwelveResponse quote(@RequestParam("symbol") String symbol, @RequestParam("apikey") String apiKey);
}

record TwelveResponse(String close, String currency, Long timestamp, String status, Integer code, String message) {}
