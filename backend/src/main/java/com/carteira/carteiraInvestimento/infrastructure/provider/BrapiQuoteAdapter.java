package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CotacaoProviderPort;
import com.carteira.carteiraInvestimento.application.port.AssetMetadataProviderPort;
import com.carteira.carteiraInvestimento.application.port.AssetDiscoveryProviderPort;
import com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException;
import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.quote.CotacaoExterna;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import com.carteira.carteiraInvestimento.infrastructure.config.MarketQuoteProperties;
import feign.FeignException;
import feign.RetryableException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.net.URI;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.asset.TickerCanonicalizer;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class BrapiQuoteAdapter implements CotacaoProviderPort, AssetMetadataProviderPort, AssetDiscoveryProviderPort {
	private static final Logger log = LoggerFactory.getLogger(BrapiQuoteAdapter.class);
	private final BrapiClient client;
	private final MarketQuoteProperties properties;

	public BrapiQuoteAdapter(BrapiClient client, MarketQuoteProperties properties) {
		this.client = client; this.properties = properties;
	}

	@Override public QuoteProvider provider() { return QuoteProvider.BRAPI; }
	@Override public Mercado market() { return Mercado.B3; }

	@Override
	public List<String> discover(String search) {
		try {
			BrapiAvailableResponse response = client.available(search);
			if (response == null || response.stocks() == null) return List.of();
			return response.stocks().stream().map(this::canonicalB3Ticker)
					.filter(java.util.Objects::nonNull).distinct().toList();
		} catch (RetryableException exception) {
			throw validation("timeout", exception);
		} catch (FeignException exception) {
			throw validation(httpFailureCategory(exception), exception);
		} catch (RuntimeException exception) {
			throw validation("payload", exception);
		}
	}

	@Override
	public AssetMetadata validate(String requestedTicker) {
		try {
			BrapiResponse response = client.quote(requestedTicker, authorization());
			if (response == null || response.results() == null || response.results().isEmpty()) {
				throw new IllegalArgumentException("asset was not found");
			}
			BrapiResult result = response.results().getFirst();
			String symbol = TickerCanonicalizer.canonicalize(result.symbol() == null ? requestedTicker : result.symbol());
			BrapiQuoteData data = requiredData(result);
			if (data.currency() != null && !"BRL".equalsIgnoreCase(data.currency().trim())) {
				throw new IllegalArgumentException("provider currency does not match B3");
			}
			TipoAtivo type = parseType(data.type(), symbol);
			String logo = trustedLogo(data.logourl());
			String name = preferredName(data, symbol);
			return new AssetMetadata(requestedTicker, symbol, name, type, Mercado.B3,
					logo == null ? null : LogoProvider.BRAPI, logo);
		} catch (IllegalArgumentException exception) {
			throw exception;
		} catch (RetryableException exception) {
			throw validation("timeout", exception);
		} catch (FeignException exception) {
			if (exception.status() == 404) throw new IllegalArgumentException("asset was not found");
			throw validation(httpFailureCategory(exception), exception);
		} catch (RuntimeException exception) {
			throw validation("payload", exception);
		}
	}

	@Override
	public CotacaoExterna obter(String ticker) {
		try {
			BrapiResponse response = client.quote(ticker, authorization());
			if (response == null || response.results() == null || response.results().isEmpty()) throw integration("empty");
			BrapiResult result = response.results().getFirst();
			BrapiQuoteData data = requiredData(result);
			return new CotacaoExterna(parsePrice(data.regularMarketPrice()), parseCurrency(data.currency()),
					marketTime(data.regularMarketTime()), provider());
		} catch (QuoteIntegrationException | QuoteNotFoundException exception) {
			throw exception;
		} catch (RetryableException exception) {
			throw integration("timeout", exception);
		} catch (FeignException exception) {
			if (exception.status() == 404) throw new QuoteNotFoundException(exception);
			throw integration(httpFailureCategory(exception), exception);
		} catch (RuntimeException exception) {
			throw integration("payload", exception);
		}
	}

	private BigDecimal parsePrice(BigDecimal value) {
		if (value == null) throw integration("payload");
		return value;
	}
	private BrapiQuoteData requiredData(BrapiResult result) {
		if (result == null || result.data() == null) throw integration("payload");
		return result.data();
	}
	private TipoAtivo parseType(String value, String ticker) {
		if (value == null || value.isBlank()) {
			if (ticker.matches("^[A-Z]{4}[3-9]F?$")) return TipoAtivo.ACAO;
			throw new IllegalArgumentException("asset type is unavailable");
		}
		return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
			case "stock" -> TipoAtivo.ACAO;
			case "fii" -> TipoAtivo.FII;
			case "etf" -> TipoAtivo.ETF;
			default -> throw new IllegalArgumentException("unsupported asset type");
		};
	}
	private String preferredName(BrapiQuoteData data, String ticker) {
		if (data.longName() != null && !data.longName().isBlank()) return data.longName();
		if (data.shortName() != null && !data.shortName().isBlank()) return data.shortName();
		return ticker;
	}
	private Instant marketTime(String value) {
		return value == null || value.isBlank() ? Instant.now() : Instant.parse(value);
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
	private String authorization() { return properties.brapi().configured() ? "Bearer " + properties.brapi().credential() : null; }
	private AssetValidationUnavailableException validation(String category) {
		log.warn("asset metadata provider failure provider={} failureCategory={}", provider(), category);
		return new AssetValidationUnavailableException("Brapi asset validation failed");
	}
	private AssetValidationUnavailableException validation(String category, Throwable cause) {
		log.warn("asset metadata provider failure provider={} failureCategory={}", provider(), category);
		return new AssetValidationUnavailableException("Brapi asset validation failed", cause);
	}
	private String httpFailureCategory(FeignException exception) {
		return switch (exception.status()) {
			case 401, 403 -> "authorization";
			case 429 -> "throttled";
			default -> exception.status() >= 500 ? "upstream" : "http";
		};
	}
	private String trustedLogo(String value) {
		if (value == null || value.isBlank()) return null;
		try {
			URI uri = URI.create(value.trim());
			return "https".equalsIgnoreCase(uri.getScheme()) && "icons.brapi.dev".equalsIgnoreCase(uri.getHost())
					&& uri.getUserInfo() == null ? uri.toASCIIString() : null;
		} catch (IllegalArgumentException ignored) { return null; }
	}
	private String canonicalB3Ticker(String value) {
		try {
			String ticker = TickerCanonicalizer.canonicalize(value);
			return Mercado.B3.aceitaTicker(ticker) ? ticker : null;
		} catch (IllegalArgumentException ignored) { return null; }
	}
}

@FeignClient(name = "brapiQuoteClient", url = "${application.market-quotes.brapi.url}")
interface BrapiClient {
	@GetMapping("/api/v2/stocks/quote")
	BrapiResponse quote(@RequestParam("symbols") String symbols, @RequestHeader(value = "Authorization", required = false) String authorization);

	@GetMapping("/api/available")
	BrapiAvailableResponse available(@RequestParam("search") String search);
}

record BrapiResponse(List<BrapiResult> results) {}
record BrapiResult(String requestedSymbol, String symbol, Boolean changed, BrapiQuoteData data) {}
record BrapiQuoteData(String shortName, String longName, String type, String logourl,
		BigDecimal regularMarketPrice, String currency, String regularMarketTime) {}
record BrapiAvailableResponse(List<String> stocks, List<String> indexes) {}
