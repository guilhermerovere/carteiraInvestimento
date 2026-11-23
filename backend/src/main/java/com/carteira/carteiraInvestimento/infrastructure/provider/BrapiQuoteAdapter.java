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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	public List<AssetPreview> discover(String search) {
		try {
			List<ResolvedIdentity> identities = identities(search, false);
			if (identities.isEmpty()) return List.of();
			String symbols = identities.stream().map(ResolvedIdentity::ticker).distinct()
					.collect(java.util.stream.Collectors.joining(","));
			Map<String, BrapiQuoteData> quotes = quotesByTicker(client.quote(symbols, authorization()));
			return identities.stream().map(identity -> preview(identity, requiredQuote(identity.ticker(), quotes))).toList();
		} catch (IllegalArgumentException | AssetValidationUnavailableException exception) {
			throw exception;
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
			ResolvedIdentity identity = selectIdentity(requestedTicker);
			BrapiQuoteData quote = requiredQuote(identity.ticker(), quotesByTicker(
					client.quote(identity.ticker(), authorization())));
			String logo = trustedLogo(identity.logo());
			return new AssetMetadata(requestedTicker, identity.ticker(), identity.name(), identity.type(), Mercado.B3,
					logo == null ? null : LogoProvider.BRAPI, logo);
		} catch (IllegalArgumentException | AssetValidationUnavailableException exception) {
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
	private TipoAtivo parseType(String value) {
		if (value == null || value.isBlank()) throw new IllegalArgumentException("asset type is unavailable");
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "unit", "stock" -> TipoAtivo.ACAO;
			case "fii" -> TipoAtivo.FII;
			case "etf" -> TipoAtivo.ETF;
			default -> throw new IllegalArgumentException("unsupported asset type");
		};
	}
	private ResolvedIdentity selectIdentity(String requestedTicker) {
		String requested = TickerCanonicalizer.canonicalize(requestedTicker);
		List<ResolvedIdentity> candidates = identities(requested, true);
		return candidates.stream().filter(candidate -> candidate.ticker().equals(requested)).findFirst()
				.orElseGet(() -> candidates.size() == 1 ? candidates.getFirst() : notFound());
	}
	private List<ResolvedIdentity> identities(String search, boolean strict) {
		BrapiTickersResponse response = client.tickers(search);
		if (response == null || response.identities() == null) throw validation("payload");
		if (response.identities().isEmpty()) {
			if (strict) throw new IllegalArgumentException("asset was not found");
			return List.of();
		}
		var resolved = new LinkedHashMap<String, ResolvedIdentity>();
		for (BrapiTickerIdentity identity : response.identities()) {
			String ticker = identity == null ? null : canonicalB3Ticker(identity.stock());
			if (ticker == null) continue;
			String name = identity.name() == null ? "" : identity.name().trim();
			if (name.isEmpty()) {
				if (strict && ticker.equals(TickerCanonicalizer.canonicalize(search))) throw validation("payload");
				continue;
			}
			try {
				resolved.putIfAbsent(ticker, new ResolvedIdentity(ticker, name, parseType(identity.type()), identity.logo()));
			} catch (IllegalArgumentException invalid) {
				if (strict && ticker.equals(TickerCanonicalizer.canonicalize(search))) throw invalid;
			}
		}
		if (strict && resolved.isEmpty()) throw new IllegalArgumentException("asset was not found");
		return List.copyOf(resolved.values());
	}
	private Map<String, BrapiQuoteData> quotesByTicker(BrapiResponse response) {
		if (response == null || response.results() == null || response.results().isEmpty()) throw validation("empty");
		Map<String, BrapiQuoteData> quotes = new LinkedHashMap<>();
		for (BrapiResult result : response.results()) {
			String ticker = result == null ? null : canonicalB3Ticker(result.symbol());
			if (ticker == null || result.data() == null || quotes.putIfAbsent(ticker, result.data()) != null) {
				throw validation("payload");
			}
		}
		return quotes;
	}
	private BrapiQuoteData requiredQuote(String ticker, Map<String, BrapiQuoteData> quotes) {
		BrapiQuoteData quote = quotes.get(ticker);
		if (quote == null) throw validation("symbol-mismatch");
		if (quote.currency() == null || !"BRL".equalsIgnoreCase(quote.currency().trim())) throw validation("currency-mismatch");
		if (quote.regularMarketPrice() == null || quote.regularMarketPrice().signum() <= 0) throw validation("price");
		return quote;
	}
	private AssetPreview preview(ResolvedIdentity identity, BrapiQuoteData quote) {
		String logo = trustedLogo(identity.logo());
		return new AssetPreview(identity.ticker(), identity.name(), identity.type(), Mercado.B3, Moeda.BRL,
				logo == null ? null : LogoProvider.BRAPI, logo, quote.regularMarketPrice());
	}
	private ResolvedIdentity notFound() { throw new IllegalArgumentException("asset was not found"); }
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

	@GetMapping("/api/v2/tickers")
	BrapiTickersResponse tickers(@RequestParam("search") String search);
}

record BrapiResponse(List<BrapiResult> results) {}
record BrapiResult(String requestedSymbol, String symbol, Boolean changed, BrapiQuoteData data) {}
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
record BrapiQuoteData(BigDecimal regularMarketPrice, String currency, String regularMarketTime) {}
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
record BrapiTickersResponse(List<BrapiTickerIdentity> stocks, List<BrapiTickerSearchResult> results) {
	BrapiTickersResponse(List<BrapiTickerIdentity> stocks) { this(stocks, null); }
	List<BrapiTickerIdentity> identities() {
		if (stocks != null) return stocks;
		if (results == null) return null;
		return results.stream().map(BrapiTickerSearchResult::identity).toList();
	}
}
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
record BrapiTickerSearchResult(String symbol, String name, String subType, String exchange, String currency, String logoUrl) {
	BrapiTickerIdentity identity() { return new BrapiTickerIdentity(symbol, name, subType, logoUrl); }
}
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
record BrapiTickerIdentity(String stock, String name, String type, String logo) {}
record ResolvedIdentity(String ticker, String name, TipoAtivo type, String logo) {}
