package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CotacaoProviderPort;
import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.quote.CotacaoExterna;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class AlphaVantageQuoteAdapter implements CotacaoProviderPort {
	private static final Logger log = LoggerFactory.getLogger(AlphaVantageQuoteAdapter.class);
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final AlphaVantageClient client;
	private final MarketQuoteProperties properties;

	public AlphaVantageQuoteAdapter(AlphaVantageClient client, MarketQuoteProperties properties) {
		this.client = client; this.properties = properties;
	}

	@Override public QuoteProvider provider() { return QuoteProvider.ALPHA_VANTAGE; }

	@Override
	public CotacaoExterna obter(String ticker) {
		if (!properties.alphaVantage().configured()) throw integration("configuration");
		try {
			AlphaResponse response = client.intraday("TIME_SERIES_INTRADAY", ticker, "5min", "compact",
					properties.alphaVantage().credential());
			if (response == null) throw integration("empty");
			if (response.errorMessage() != null) throw new QuoteNotFoundException();
			if (response.note() != null || response.information() != null) throw integration("provider-message");
			if (response.meta() == null || response.series() == null || response.series().isEmpty()) throw integration("empty");
			String timestamp = response.series().keySet().stream().max(String::compareTo).orElseThrow();
			AlphaBar bar = response.series().get(timestamp);
			String timezone = required(response.meta().timezone());
			return new CotacaoExterna(new BigDecimal(required(bar == null ? null : bar.close())), Moeda.USD,
					LocalDateTime.parse(timestamp, DATE_TIME).atZone(ZoneId.of(timezone)).toInstant(), provider());
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
		return new QuoteIntegrationException("Alpha Vantage integration failed");
	}
	private QuoteIntegrationException integration(String category, Throwable cause) {
		log.warn("quote provider failure provider={} failureCategory={} fallback=true", provider(), category);
		return new QuoteIntegrationException("Alpha Vantage integration failed", cause);
	}
}

@FeignClient(name = "alphaVantageQuoteClient", url = "${application.market-quotes.alpha-vantage.url}")
interface AlphaVantageClient {
	@GetMapping("/query")
	AlphaResponse intraday(@RequestParam("function") String function, @RequestParam("symbol") String symbol,
			@RequestParam("interval") String interval, @RequestParam("outputsize") String outputSize,
			@RequestParam("apikey") String apiKey);
}

record AlphaResponse(@JsonProperty("Meta Data") AlphaMeta meta,
		@JsonProperty("Time Series (5min)") Map<String, AlphaBar> series,
		@JsonProperty("Error Message") String errorMessage, @JsonProperty("Note") String note,
		@JsonProperty("Information") String information) {}
record AlphaMeta(@JsonProperty("6. Time Zone") String timezone) {}
record AlphaBar(@JsonProperty("4. close") String close) {}
