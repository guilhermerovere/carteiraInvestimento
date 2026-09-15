package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.port.CambioProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCambioPort;
import com.carteira.carteiraInvestimento.domain.fx.CambioExterno;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.MDC;

public class CambioApplicationService implements CambioUseCase {
	public static final String CACHE_KEY = "USD:BRL";
	private static final Duration VALIDITY = Duration.ofMinutes(5);
	private static final String ENDPOINT = "/api/v1/cambio/usd-brl";
	private final HistoricoCambioPort historico;
	private final AuditoriaIsoladaPort auditoria;
	private final Cache<String, ObservacaoCambio> cache;
	private final Clock clock;
	private final Map<CambioProvider, CambioProviderPort> providers = new EnumMap<>(CambioProvider.class);
	private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

	public CambioApplicationService(HistoricoCambioPort historico, AuditoriaIsoladaPort auditoria,
			List<CambioProviderPort> providers, Cache<String, ObservacaoCambio> cache, Clock clock) {
		this.historico=historico; this.auditoria=auditoria; this.cache=cache; this.clock=clock;
		providers.forEach(provider -> this.providers.put(provider.provider(), provider));
	}

	@Override
	public ObservacaoCambio obterUsdBrl() {
		ObservacaoCambio cached = validCached();
		if (cached != null) return cached;
		Object lock = locks.computeIfAbsent(CACHE_KEY, ignored -> new Object());
		synchronized (lock) {
			ObservacaoCambio inside = validCached();
			if (inside != null) return inside;
			return resolvePersistAndCache();
		}
	}

	private ObservacaoCambio validCached() {
		ObservacaoCambio value = cache.getIfPresent(CACHE_KEY);
		if (value == null) return null;
		Instant deadline = value.registradoEm().plus(VALIDITY);
		if (!Instant.now(clock).isAfter(deadline)) return value;
		cache.invalidate(CACHE_KEY);
		return null;
	}

	private ObservacaoCambio resolvePersistAndCache() {
		CambioExterno external;
		try {
			external = resolveExternal();
		} catch (CambioProviderException exception) {
			auditBestEffort();
			throw new CambioUnavailableException("exchange-rate provider chain failed", exception);
		}
		ObservacaoCambio accepted;
		try {
			accepted = ObservacaoCambio.aceitar(external, Instant.now(clock));
		} catch (IllegalArgumentException exception) {
			auditBestEffort();
			throw new CambioUnavailableException("exchange-rate provider returned invalid data", exception);
		}
		ObservacaoCambio persisted = historico.save(accepted);
		cache.put(CACHE_KEY, persisted);
		return persisted;
	}

	private CambioExterno resolveExternal() {
		return provider(CambioProvider.TWELVE_DATA).obterUsdBrl();
	}

	private CambioProviderPort provider(CambioProvider provider) {
		CambioProviderPort value = providers.get(provider);
		if (value == null) throw new CambioProviderException(false, "exchange-rate provider is not configured");
		return value;
	}

	private void auditBestEffort() {
		try {
			auditoria.recordIsoladamente(new AuditoriaCommand(null, TipoEvento.CAMBIO_INDISPONIVEL,
					ResultadoAuditoria.FALHA, SeveridadeAuditoria.ALERTA, ENDPOINT, correlationId()));
		} catch (RuntimeException ignored) {
			// Availability reporting is best effort and must never replace the primary 502.
		}
	}

	private UUID correlationId() {
		String value = MDC.get("correlationId");
		try { return value == null ? UUID.randomUUID() : UUID.fromString(value); }
		catch (IllegalArgumentException ignored) { return UUID.randomUUID(); }
	}
}
