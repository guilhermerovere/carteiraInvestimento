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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class BrapiQuoteAdapter implements CotacaoProviderPort {
	private static final Logger log = LoggerFactory.getLogger(BrapiQuoteAdapter.class);
	private final BrapiClient client;
	private final MarketQuoteProperties properties;

	public BrapiQuoteAdapter(BrapiClient client, MarketQuoteProperties properties) {
		this.client = client; this.properties = properties;
	}

	@Override public QuoteProvider provider() { return QuoteProvider.BRAPI; }

	@Override
	public CotacaoExterna obter(String ticker) {
		if (!properties.brapi().configured()) throw integration("configuration");
		try {
			BrapiResponse response = client.quote(ticker, "Bearer " + properties.brapi().credential());
			if (response == null || response.results() == null || response.results().isEmpty()) throw integration("empty");
			BrapiResult result = response.results().getFirst();
			return new CotacaoExterna(parsePrice(result.regularMarketPrice()), parseCurrency(result.currency()),
					Instant.parse(required(result.regularMarketTime())), provider());
		} catch (QuoteIntegrationException | QuoteNotFoundException exception) {
			throw exception;
		} catch (FeignException exception) {
			if (exception.status() == 404) throw new QuoteNotFoundException(exception);
			throw integration("http", exception);
		} catch (RuntimeException exception) {
			throw integration("payload", exception);
		}
	}

	private BigDecimal parsePrice(BigDecimal value) {
		if (value == null) throw integration("payload");
		return value;
	}
	private Moeda parseCurrency(String value) { return Moeda.valueOf(required(value)); }
	private String required(Object value) {
		String text = value == null ? "" : value.toString().trim();
		if (text.isEmpty()) throw integration("payload");
		return text;
	}
	private QuoteIntegrationException integration(String category) {
		log.warn("quote provider failure provider={} failureCategory={}", provider(), category);
		return new QuoteIntegrationException("Brapi integration failed");
	}
	private QuoteIntegrationException integration(String category, Throwable cause) {
		log.warn("quote provider failure provider={} failureCategory={}", provider(), category);
		return new QuoteIntegrationException("Brapi integration failed", cause);
	}
}

@FeignClient(name = "brapiQuoteClient", url = "${application.market-quotes.brapi.url}")
interface BrapiClient {
	@GetMapping("/api/v2/stocks/quote")
	BrapiResponse quote(@RequestParam("symbols") String symbols, @RequestHeader("Authorization") String authorization);
}

record BrapiResponse(List<BrapiResult> results) {}
record BrapiResult(String requestedSymbol, BigDecimal regularMarketPrice, String currency, String regularMarketTime) {}
