package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import com.carteira.carteiraInvestimento.application.port.AtivoPort;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.application.port.CotacaoProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage;
import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPort;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.carteira.carteiraInvestimento.domain.quote.CotacaoExterna;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class MarketQuoteApplicationServiceTest {
	private static final Instant NOW = Instant.parse("2026-09-08T19:00:00Z");

	@Test
	void cacheHitDoesNotCallProviderOrPersistenceAndMissPersistsBeforeCaching() {
		Fixture f = new Fixture(Mercado.B3);
		Cotacao first = f.service.cotacaoAtual(f.asset.id());
		Cotacao second = f.service.cotacaoAtual(f.asset.id());
		assertThat(second).isEqualTo(first);
		assertThat(f.brapi.calls()).isEqualTo(1);
		assertThat(f.history.saved).containsExactly(first);
	}

	@Test
	void failedRefreshPreservesExistingCache() {
		Fixture f = new Fixture(Mercado.B3);
		Cotacao current = f.service.cotacaoAtual(f.asset.id());
		f.brapi.failure = new QuoteIntegrationException("down");
		assertThatThrownBy(() -> f.service.atualizarCotacao(f.asset.id())).isInstanceOf(QuoteIntegrationException.class);
		assertThat(f.cache.getIfPresent(f.asset.id())).isEqualTo(current);
		assertThat(f.history.saved).hasSize(1);
	}

	@Test
	void persistenceFailureNeverReplacesCache() {
		Fixture f = new Fixture(Mercado.B3);
		Cotacao current = f.service.cotacaoAtual(f.asset.id());
		f.history.fail = true;
		assertThatThrownBy(() -> f.service.atualizarCotacao(f.asset.id())).isInstanceOf(IllegalStateException.class);
		assertThat(f.cache.getIfPresent(f.asset.id())).isEqualTo(current);
	}

	@Test
	void appliesCompleteUsFallbackMatrix() {
		assertMatrix(new QuoteNotFoundException(), new QuoteNotFoundException(), QuoteNotFoundException.class);
		assertMatrix(new QuoteNotFoundException(), new QuoteIntegrationException("x"), QuoteIntegrationException.class);
		assertMatrix(new QuoteIntegrationException("x"), new QuoteNotFoundException(), QuoteIntegrationException.class);
		assertMatrix(new QuoteIntegrationException("x"), new QuoteIntegrationException("y"), QuoteIntegrationException.class);
		Fixture success = new Fixture(Mercado.US);
		success.alpha.failure = new QuoteIntegrationException("x");
		assertThat(success.service.cotacaoAtual(success.asset.id()).provider()).isEqualTo(QuoteProvider.TWELVE_DATA);
	}

	@Test
	void coordinatesConcurrentGetMissesPerAsset() throws Exception {
		Fixture f = new Fixture(Mercado.B3);
		f.brapi.block = new CountDownLatch(1);
		try (var executor = Executors.newFixedThreadPool(2)) {
			var a = executor.submit(() -> f.service.cotacaoAtual(f.asset.id()));
			var b = executor.submit(() -> f.service.cotacaoAtual(f.asset.id()));
			while (f.brapi.calls() == 0) Thread.onSpinWait();
			f.brapi.block.countDown();
			assertThat(a.get()).isEqualTo(b.get());
		}
		assertThat(f.brapi.calls()).isEqualTo(1);
		assertThat(f.history.saved).hasSize(1);
	}

	@Test
	void coordinatesRefreshWithConcurrentGetWithoutOlderCacheOverwrite() throws Exception {
		Fixture f = new Fixture(Mercado.B3);
		f.brapi.block = new CountDownLatch(1);
		try (var executor = Executors.newFixedThreadPool(2)) {
			var refresh = executor.submit(() -> f.service.atualizarCotacao(f.asset.id()));
			while (f.brapi.calls() == 0) Thread.onSpinWait();
			var get = executor.submit(() -> f.service.cotacaoAtual(f.asset.id()));
			f.brapi.block.countDown();
			assertThat(get.get()).isEqualTo(refresh.get());
		}
		assertThat(f.brapi.calls()).isEqualTo(1);
		assertThat(f.history.saved).hasSize(1);
	}

	@Test
	void lifecycleIsAppliedBeforeCacheAndProvider() {
		Fixture f = new Fixture(Mercado.B3, false);
		assertThatThrownBy(() -> f.service.cotacaoAtual(f.asset.id())).isInstanceOf(AtivoNotFoundException.class);
		assertThatThrownBy(() -> f.service.historico(f.asset.id(), 0, 20, false)).isInstanceOf(AtivoNotFoundException.class);
		assertThat(f.service.historico(f.asset.id(), 0, 20, true).items()).isEmpty();
		assertThatThrownBy(() -> f.service.atualizarCotacao(f.asset.id())).isInstanceOf(InactiveAssetRefreshException.class);
		assertThat(f.brapi.calls()).isZero();
	}

	@Test
	void inactiveAssetIsQuotedOnlyForExplicitOpenCustodyAndPublicRouteRemainsBlocked() {
		Fixture f = new Fixture(Mercado.B3, false);
		assertThatThrownBy(() -> f.service.cotacaoAtualParaCustodia(f.asset.id(), false))
				.isInstanceOf(AtivoNotFoundException.class);
		Cotacao custodyQuote = f.service.cotacaoAtualParaCustodia(f.asset.id(), true);
		assertThat(custodyQuote.ativoId()).isEqualTo(f.asset.id());
		assertThat(f.brapi.calls()).isEqualTo(1);
		assertThatThrownBy(() -> f.service.cotacaoAtual(f.asset.id())).isInstanceOf(AtivoNotFoundException.class);
		assertThat(f.brapi.calls()).isEqualTo(1);
	}

	@Test
	void neverUsesHistoryAsStaleFallbackAndProviderRunsOutsideTransaction() {
		Fixture f = new Fixture(Mercado.B3);
		f.history.saved.add(new Cotacao(UUID.randomUUID(), f.asset.id(), new BigDecimal("99"), Moeda.BRL,
				NOW.minusSeconds(7200), QuoteProvider.BRAPI, NOW.minusSeconds(7100)));
		f.brapi.failure = new QuoteIntegrationException("down");
		assertThatThrownBy(() -> f.service.cotacaoAtual(f.asset.id())).isInstanceOf(QuoteIntegrationException.class);
		assertThat(f.cache.getIfPresent(f.asset.id())).isNull();
		assertThat(f.brapi.transactionActive).isFalse();
	}

	private void assertMatrix(RuntimeException alpha, RuntimeException twelve, Class<? extends Throwable> expected) {
		Fixture f = new Fixture(Mercado.US);
		f.alpha.failure = alpha; f.twelve.failure = twelve;
		assertThatThrownBy(() -> f.service.cotacaoAtual(f.asset.id())).isInstanceOf(expected);
	}

	private static final class Fixture {
		final Ativo asset;
		final FakeHistory history = new FakeHistory();
		final FakeProvider brapi = new FakeProvider(QuoteProvider.BRAPI, Moeda.BRL);
		final FakeProvider alpha = new FakeProvider(QuoteProvider.ALPHA_VANTAGE, Moeda.USD);
		final FakeProvider twelve = new FakeProvider(QuoteProvider.TWELVE_DATA, Moeda.USD);
		final Cache<UUID, Cotacao> cache = Caffeine.newBuilder().build();
		final MarketQuoteApplicationService service;
		Fixture(Mercado market) { this(market, true); }
		Fixture(Mercado market, boolean active) {
			Ativo created = Ativo.novo(market == Mercado.B3 ? "PETR4" : "AAPL", "Asset", TipoAtivo.ACAO, market);
			asset = active ? created : created.definirAtivo(false);
			service = new MarketQuoteApplicationService(new FakeAssets(asset), history,
					List.of(brapi, alpha, twelve), cache, Clock.fixed(NOW, ZoneOffset.UTC));
		}
	}

	private static final class FakeProvider implements CotacaoProviderPort {
		final QuoteProvider provider; final Moeda currency; final AtomicInteger calls = new AtomicInteger();
		volatile RuntimeException failure; volatile CountDownLatch block;
		volatile boolean transactionActive;
		FakeProvider(QuoteProvider provider, Moeda currency) { this.provider = provider; this.currency = currency; }
		@Override public QuoteProvider provider() { return provider; }
		@Override public CotacaoExterna obter(String ticker) {
			calls.incrementAndGet();
			transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
			if (block != null) try { block.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
			if (failure != null) throw failure;
			return new CotacaoExterna(new BigDecimal("123.456789"), currency, NOW.minusSeconds(86400), provider);
		}
		int calls() { return calls.get(); }
	}

	private static final class FakeHistory implements HistoricoCotacaoPort {
		final List<Cotacao> saved = new ArrayList<>(); boolean fail;
		@Override public Cotacao save(Cotacao quote) { if (fail) throw new IllegalStateException("db"); saved.add(quote); return quote; }
		@Override public HistoricoCotacaoPage findByAtivoId(UUID id, int page, int size) {
			return new HistoricoCotacaoPage(List.copyOf(saved), page, size, saved.size(), saved.isEmpty() ? 0 : 1);
		}
	}

	private record FakeAssets(Ativo asset) implements AtivoPort {
		@Override public Ativo save(Ativo ativo) { return ativo; }
		@Override public Optional<Ativo> findById(UUID id) { return asset.id().equals(id) ? Optional.of(asset) : Optional.empty(); }
		@Override public Optional<Ativo> findByTicker(String ticker, AtivoReadScope scope) { return Optional.empty(); }
		@Override public AtivoPage list(AtivoQuery query) { throw new UnsupportedOperationException(); }
	}
}
