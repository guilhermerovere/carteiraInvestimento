package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CotacaoProviderPort;
import com.carteira.carteiraInvestimento.application.port.AssetDiscoveryProviderPort;
import com.carteira.carteiraInvestimento.application.port.AssetMetadataProviderPort;
import com.carteira.carteiraInvestimento.application.service.AssetValidationUnavailableException;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.asset.TickerCanonicalizer;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
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
import java.util.Locale;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class TwelveDataQuoteAdapter implements CotacaoProviderPort, AssetMetadataProviderPort, AssetDiscoveryProviderPort {
	private static final Logger log = LoggerFactory.getLogger(TwelveDataQuoteAdapter.class);
	private static final Set<String> US_EXCHANGES = Set.of("NASDAQ", "NYSE", "NYSE ARCA", "CBOE", "IEX");
	private static final Set<String> US_MICS = Set.of("XNAS", "XNGS", "XNCM", "XNMS", "XNIM", "XNYS", "ARCX", "BATS", "IEXG");
	private final TwelveDataClient client;
	private final MarketQuoteProperties properties;

	public TwelveDataQuoteAdapter(TwelveDataClient client, MarketQuoteProperties properties) {
		this.client = client; this.properties = properties;
	}

	@Override public QuoteProvider provider() { return QuoteProvider.TWELVE_DATA; }
	@Override public Mercado market() { return Mercado.US; }

	@Override public List<String> discover(String search) {
		if (!properties.twelveData().configured()) throw validation("configuration", null);
		try {
			TwelveStocksResponse response = client.stocks(search, properties.twelveData().credential());
			ensureValidDiscoveryResponse(response);
			return response.data().stream().filter(this::isUsStock).map(TwelveStockItem::symbol)
					.map(TickerCanonicalizer::canonicalize).filter(Mercado.US::aceitaTicker).distinct().toList();
		} catch (AssetValidationUnavailableException exception) { throw exception;
		} catch (IllegalArgumentException exception) { throw exception;
		} catch (RetryableException exception) { throw validation("timeout", exception);
		} catch (FeignException exception) {
			if (exception.status() == 404) throw new IllegalArgumentException("asset was not found");
			throw validation(httpCategory(exception), exception);
		} catch (RuntimeException exception) { throw validation("payload", exception); }
	}

	@Override public AssetMetadata validate(String requestedTicker) {
		if (!properties.twelveData().configured()) throw validation("configuration", null);
		try {
			TwelveResponse response = client.quote(requestedTicker, properties.twelveData().credential());
			ensureValidValidationQuote(response);
			String ticker = canonicalTicker(response.symbol());
			if (!requestedTicker.equals(ticker)) throw new IllegalArgumentException("asset was not found");
			if (!isUsVenue(response.exchange(), response.micCode()) || !"USD".equalsIgnoreCase(trim(response.currency()))) {
				throw new IllegalArgumentException("asset is not a supported US instrument");
			}
			return new AssetMetadata(requestedTicker, ticker, requiredValidationName(response.name()), typeOrCommonStock(response.type()),
					Mercado.US, LogoProvider.LOGO_DEV, "ticker/" + ticker);
		} catch (AssetValidationUnavailableException | IllegalArgumentException exception) { throw exception;
		} catch (RetryableException exception) { throw validation("timeout", exception);
		} catch (FeignException exception) {
			if (exception.status() == 404) throw new IllegalArgumentException("asset was not found");
			throw validation(httpCategory(exception), exception);
		} catch (RuntimeException exception) { throw validation("payload", exception); }
	}

	@Override
	public CotacaoExterna obter(String ticker) {
		if (!properties.twelveData().configured()) throw integration("configuration");
		try {
			TwelveResponse response = client.quote(ticker, properties.twelveData().credential());
			ensureValidQuoteResponse(response);
			if (response.timestamp() == null || response.timestamp() <= 0) throw integration("payload");
			return new CotacaoExterna(new BigDecimal(required(response.close())),
					Moeda.valueOf(required(response.currency())), Instant.ofEpochSecond(response.timestamp()), provider());
		} catch (QuoteIntegrationException | QuoteNotFoundException exception) {
			throw exception;
		} catch (RetryableException exception) {
			throw integration("timeout", exception);
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
	private String requiredValidationName(String value) {
		if (value == null || value.isBlank()) throw validation("payload", null);
		return value.trim();
	}
	private boolean isUsStock(TwelveStockItem item) {
		if (item == null || !"USD".equalsIgnoreCase(trim(item.currency())) || !"United States".equalsIgnoreCase(trim(item.country()))) return false;
		if (!isUsVenue(item.exchange(), item.micCode())) return false;
		try { return Mercado.US.aceitaTicker(TickerCanonicalizer.canonicalize(item.symbol())) && type(item.type()) != null; }
		catch (IllegalArgumentException ignored) { return false; }
	}
	private boolean isUsVenue(String exchange, String micCode) {
		return US_EXCHANGES.contains(trim(exchange).toUpperCase(Locale.ROOT))
				|| US_MICS.contains(trim(micCode).toUpperCase(Locale.ROOT));
	}
	private String canonicalTicker(String value) {
		if (value == null || value.isBlank()) throw validation("payload", null);
		try { return TickerCanonicalizer.canonicalize(value); }
		catch (IllegalArgumentException invalid) { throw validation("payload", invalid); }
	}
	private TipoAtivo typeOrCommonStock(String raw) {
		return trim(raw).isEmpty() ? TipoAtivo.ACAO : type(raw);
	}
	private TipoAtivo type(String raw) {
		return switch (trim(raw).toLowerCase(Locale.ROOT)) {
			case "common stock", "stock", "equity" -> TipoAtivo.ACAO;
			case "etf", "exchange traded fund" -> TipoAtivo.ETF;
			default -> throw new IllegalArgumentException("unsupported asset type");
		};
	}
	private String trim(String value) { return value == null ? "" : value.trim(); }
	private void ensureValidDiscoveryResponse(TwelveStocksResponse response) {
		if (response == null) throw validation("payload", null);
		if (hasProviderError(response.status(), response.code())) throwValidationProviderError(response.code(), response.message());
		if (response.data() == null) throw validation("payload", null);
	}
	private void ensureValidValidationQuote(TwelveResponse response) {
		if (response == null) throw validation("payload", null);
		if (hasProviderError(response.status(), response.code())) throwValidationProviderError(response.code(), response.message());
	}
	private void ensureValidQuoteResponse(TwelveResponse response) {
		if (response == null) throw integration("payload");
		if (!hasProviderError(response.status(), response.code())) return;
		if (isNotFound(response.code(), response.message())) throw new QuoteNotFoundException();
		throw integration("provider-message");
	}
	private boolean hasProviderError(String status, Integer code) {
		return "error".equalsIgnoreCase(trim(status)) || (code != null && code >= 400);
	}
	private void throwValidationProviderError(Integer code, String message) {
		if (isNotFound(code, message)) throw new IllegalArgumentException("asset was not found");
		String category = code == null ? "provider-message" : switch (code) {
			case 401, 403 -> "authorization";
			case 429 -> "throttled";
			default -> code >= 500 ? "upstream" : "provider-message";
		};
		throw validation(category, null);
	}
	private boolean isNotFound(Integer code, String message) {
		if (code != null) return code == 404;
		String normalized = trim(message).toLowerCase(Locale.ROOT);
		return normalized.contains("not found") || normalized.contains("invalid symbol") || normalized.contains("not supported");
	}
	private String httpCategory(FeignException exception) { return httpCategory((Exception) exception); }
	private String httpCategory(Exception exception) {
		if (exception instanceof FeignException feign) return switch (feign.status()) { case 401, 403 -> "authorization"; case 429 -> "throttled"; default -> feign.status() >= 500 ? "upstream" : "http"; };
		return "timeout";
	}
	private AssetValidationUnavailableException validation(String category, Throwable cause) {
		log.warn("asset metadata provider failure provider={} failureCategory={}", provider(), category);
		return cause == null ? new AssetValidationUnavailableException("Twelve Data asset validation failed") : new AssetValidationUnavailableException("Twelve Data asset validation failed", cause);
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
	@GetMapping("/stocks") TwelveStocksResponse stocks(@RequestParam("symbol") String symbol, @RequestParam("apikey") String apiKey);
}

record TwelveResponse(String symbol, String name, String exchange, @com.fasterxml.jackson.annotation.JsonProperty("mic_code") String micCode,
		String currency, String type, String close, Long timestamp, String status, Integer code, String message) {}
record TwelveStocksResponse(List<TwelveStockItem> data, String status, Integer code, String message) {}
record TwelveStockItem(String symbol, String name, String country, String currency, String exchange,
		@com.fasterxml.jackson.annotation.JsonProperty("mic_code") String micCode, String type) {}
