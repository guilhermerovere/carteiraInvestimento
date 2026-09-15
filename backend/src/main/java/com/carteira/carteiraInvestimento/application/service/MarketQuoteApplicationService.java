package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.port.CotacaoProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage;
import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPort;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.carteira.carteiraInvestimento.domain.quote.CotacaoExterna;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MarketQuoteApplicationService implements MarketQuoteUseCase {
	private final AtivoPort ativos;
	private final HistoricoCotacaoPort historico;
	private final Cache<UUID, Cotacao> cache;
	private final Clock clock;
	private final Map<QuoteProvider, CotacaoProviderPort> providers;
	private final ConcurrentHashMap<UUID, Object> locks = new ConcurrentHashMap<>();

	public MarketQuoteApplicationService(AtivoPort ativos, HistoricoCotacaoPort historico,
			List<CotacaoProviderPort> providers, Cache<UUID, Cotacao> cache, Clock clock) {
		this.ativos = ativos;
		this.historico = historico;
		this.cache = cache;
		this.clock = clock;
		this.providers = new EnumMap<>(QuoteProvider.class);
		providers.forEach(provider -> this.providers.put(provider.provider(), provider));
	}

	@Override
	public Cotacao cotacaoAtual(UUID ativoId) {
		Ativo ativo = ativoVisivel(ativoId);
		Cotacao cached = cache.getIfPresent(ativoId);
		if (cached != null) return cached;
		return locked(ativoId, () -> {
			Cotacao inside = cache.getIfPresent(ativoId);
			return inside != null ? inside : obterPersistirCachear(ativo);
		});
	}

	@Override
	public Cotacao atualizarCotacao(UUID ativoId) {
		Ativo ativo = ativos.findById(ativoId).orElseThrow(AtivoNotFoundException::new);
		if (!ativo.ativo()) throw new InactiveAssetRefreshException();
		return locked(ativoId, () -> obterPersistirCachear(ativo));
	}

	@Override
	public Cotacao cotacaoAtualParaCustodia(UUID ativoId, boolean posicaoAberta) {
		Ativo ativo = ativos.findById(ativoId).orElseThrow(AtivoNotFoundException::new);
		if (!ativo.ativo() && !posicaoAberta) throw new AtivoNotFoundException();
		Cotacao cached = cache.getIfPresent(ativoId);
		if (cached != null) return cached;
		return locked(ativoId, () -> {
			Cotacao inside = cache.getIfPresent(ativoId);
			return inside != null ? inside : obterPersistirCachear(ativo);
		});
	}

	@Override
	public HistoricoCotacaoPage historico(UUID ativoId, int page, int size, boolean admin) {
		if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("invalid pagination");
		Ativo ativo = ativos.findById(ativoId).orElseThrow(AtivoNotFoundException::new);
		if (!ativo.ativo() && !admin) throw new AtivoNotFoundException();
		return historico.findByAtivoId(ativoId, page, size);
	}

	private Ativo ativoVisivel(UUID id) {
		Ativo ativo = ativos.findById(id).orElseThrow(AtivoNotFoundException::new);
		if (!ativo.ativo()) throw new AtivoNotFoundException();
		return ativo;
	}

	private Cotacao obterPersistirCachear(Ativo ativo) {
		CotacaoExterna externa = obterExterna(ativo);
		try {
			Cotacao accepted = Cotacao.aceitar(ativo, externa, Instant.now(clock));
			Cotacao persisted = historico.save(accepted);
			cache.put(ativo.id(), persisted);
			return persisted;
		} catch (IllegalArgumentException exception) {
			throw new QuoteIntegrationException("provider returned invalid quote data", exception);
		}
	}

	private CotacaoExterna obterExterna(Ativo ativo) {
		if (ativo.mercado() == Mercado.B3) return provider(QuoteProvider.BRAPI).obter(ativo.ticker());
		return provider(QuoteProvider.TWELVE_DATA).obter(ativo.ticker());
	}

	private CotacaoProviderPort provider(QuoteProvider provider) {
		CotacaoProviderPort result = providers.get(provider);
		if (result == null) throw new QuoteIntegrationException("quote provider is unavailable");
		return result;
	}

	private <T> T locked(UUID id, java.util.function.Supplier<T> action) {
		Object lock = locks.computeIfAbsent(id, ignored -> new Object());
		synchronized (lock) { return action.get(); }
	}
}
