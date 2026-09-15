package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.port.CambioProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCambioPort;
import com.carteira.carteiraInvestimento.domain.fx.CambioExterno;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class CambioApplicationServiceTest {
	private static final Instant NOW = Instant.parse("2026-09-10T12:00:00Z");

	@Test void twelveDataObservationCacheHitPreservesUuidProviderAndTimestampsWithoutNewCallOrHistory() {
		Fixture f = new Fixture();
		ObservacaoCambio first = f.service.obterUsdBrl();
		ObservacaoCambio second = f.service.obterUsdBrl();
		assertThat(first.provider()).isEqualTo(CambioProvider.TWELVE_DATA);
		assertThat(second).isSameAs(first);
		assertThat(second.id()).isEqualTo(first.id());
		assertThat(second.moedaOrigem()).isEqualTo(first.moedaOrigem());
		assertThat(second.moedaDestino()).isEqualTo(first.moedaDestino());
		assertThat(second.taxa()).isEqualByComparingTo(first.taxa());
		assertThat(second.provider()).isEqualTo(first.provider());
		assertThat(second.instanteCotacao()).isEqualTo(first.instanteCotacao());
		assertThat(second.registradoEm()).isEqualTo(first.registradoEm());
		assertThat(f.twelve.calls()).isOne(); assertThat(f.alpha.calls()).isZero();
		assertThat(f.history.saved).containsExactly(first); assertThat(f.audit.events).isEmpty();
	}

	@Test void deadlineIsInclusiveThenExpiresWithoutStaleOrHistoryFallback() {
		Fixture f = new Fixture();
		ObservacaoCambio first = f.service.obterUsdBrl();
		f.clock.set(NOW.plusSeconds(300));
		assertThat(f.service.obterUsdBrl().id()).isEqualTo(first.id());
		f.clock.set(NOW.plusSeconds(300).plusNanos(1));
		ObservacaoCambio renewed = f.service.obterUsdBrl();
		assertThat(renewed.id()).isNotEqualTo(first.id());
		assertThat(f.twelve.calls()).isEqualTo(2); assertThat(f.alpha.calls()).isZero(); assertThat(f.history.saved).hasSize(2);

		f.clock.set(NOW.plusSeconds(601));
		f.twelve.failure = new CambioProviderException(true, "down");
		assertThatThrownBy(f.service::obterUsdBrl).isInstanceOf(CambioUnavailableException.class);
		assertThat(f.cache.getIfPresent(CambioApplicationService.CACHE_KEY)).isNull();
	}

	@Test void cachePutAfterDelayedCommitDoesNotRestartFiveMinuteDeadline() {
		Fixture f = new Fixture();
		f.history.afterSave = () -> f.clock.set(NOW.plusSeconds(299));
		ObservacaoCambio first = f.service.obterUsdBrl();
		assertThat(first.registradoEm()).isEqualTo(NOW);

		f.history.afterSave = null;
		f.clock.set(NOW.plusSeconds(300));
		assertThat(f.service.obterUsdBrl().id()).isEqualTo(first.id());
		f.clock.set(NOW.plusSeconds(300).plusNanos(1));
		assertThat(f.service.obterUsdBrl().id()).isNotEqualTo(first.id());
		assertThat(f.twelve.calls()).isEqualTo(2); assertThat(f.alpha.calls()).isZero();
		assertThat(f.history.saved).hasSize(2);
	}

	@Test void twelveDataFailureIsReportedWithoutCallingLegacyAlphaVantage() {
		Fixture f = new Fixture(); f.twelve.failure = new CambioProviderException(true, "temporary");
		assertThatThrownBy(f.service::obterUsdBrl).isInstanceOf(CambioUnavailableException.class);
		assertThat(f.twelve.calls()).isOne(); assertThat(f.alpha.calls()).isZero();
		assertThat(f.audit.events).hasSize(1); assertThat(f.history.saved).isEmpty();
	}

	@Test void twelveDataFinalFailureIsAuditedBestEffortWithoutFallback() {
		Fixture f = new Fixture(); f.twelve.failure = new CambioProviderException(false, "credential"); f.audit.fail = true;
		assertThatThrownBy(f.service::obterUsdBrl).isInstanceOf(CambioUnavailableException.class);
		assertThat(f.twelve.calls()).isOne(); assertThat(f.alpha.calls()).isZero();
		assertThat(f.history.saved).isEmpty(); assertThat(f.cache.asMap()).isEmpty();
	}

	@Test void normalizationFailureIs502AndPersistenceFailureIs500WithNoCache() {
		Fixture invalid = new Fixture(); invalid.twelve.rate = new BigDecimal("0.000000004");
		assertThatThrownBy(invalid.service::obterUsdBrl).isInstanceOf(CambioUnavailableException.class);
		assertThat(invalid.audit.events).hasSize(1); assertThat(invalid.cache.asMap()).isEmpty();

		Fixture database = new Fixture(); database.history.fail = true;
		assertThatThrownBy(database.service::obterUsdBrl).isInstanceOf(IllegalStateException.class);
		assertThat(database.cache.asMap()).isEmpty(); assertThat(database.audit.events).isEmpty();
	}

	@Test void concurrentMissUsesOneExternalResolutionPersistenceAndUuid() throws Exception {
		Fixture f = new Fixture(); CountDownLatch entered = new CountDownLatch(1); CountDownLatch release = new CountDownLatch(1);
		f.twelve.entered=entered; f.twelve.release=release;
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<java.util.concurrent.Future<ObservacaoCambio>> futures = new ArrayList<>();
			for (int i=0;i<20;i++) futures.add(executor.submit(f.service::obterUsdBrl));
			assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue(); release.countDown();
			var ids = new java.util.HashSet<java.util.UUID>();
			for (var future : futures) ids.add(future.get(5, TimeUnit.SECONDS).id());
			assertThat(ids).hasSize(1); assertThat(f.twelve.calls()).isOne(); assertThat(f.alpha.calls()).isZero(); assertThat(f.history.saved).hasSize(1);
		}
	}

	private static final class Fixture {
		final MutableClock clock = new MutableClock(NOW);
		final FakeProvider alpha = new FakeProvider(CambioProvider.ALPHA_VANTAGE);
		final FakeProvider twelve = new FakeProvider(CambioProvider.TWELVE_DATA);
		final FakeHistory history = new FakeHistory(); final FakeAudit audit = new FakeAudit();
		final Cache<String, ObservacaoCambio> cache = Caffeine.newBuilder().build();
		final CambioApplicationService service = new CambioApplicationService(history, audit, List.of(alpha,twelve),cache,clock);
	}
	private static final class FakeProvider implements CambioProviderPort {
		final CambioProvider provider; final AtomicInteger calls = new AtomicInteger(); volatile RuntimeException failure;
		volatile BigDecimal rate = new BigDecimal("5.123456789"); volatile CountDownLatch entered, release;
		FakeProvider(CambioProvider provider){this.provider=provider;}
		@Override public CambioProvider provider(){return provider;}
		@Override public CambioExterno obterUsdBrl(){calls.incrementAndGet();
			assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
			if(entered!=null) entered.countDown(); if(release!=null) try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}
			if(failure!=null) throw failure;
			return new CambioExterno(MoedaCambio.USD,MoedaCambio.BRL,rate,provider,NOW.minusSeconds(30));}
		int calls(){return calls.get();}
	}
	private static final class FakeHistory implements HistoricoCambioPort {
		final List<ObservacaoCambio> saved = new ArrayList<>(); boolean fail; Runnable afterSave;
		@Override public synchronized ObservacaoCambio save(ObservacaoCambio value){if(fail)throw new IllegalStateException("database secret");saved.add(value);if(afterSave!=null)afterSave.run();return value;}
	}
	private static final class FakeAudit implements AuditoriaIsoladaPort {
		final List<AuditoriaCommand> events = new ArrayList<>(); boolean fail;
		@Override public void recordIsoladamente(AuditoriaCommand event){if(fail)throw new IllegalStateException("audit secret");events.add(event);}
	}
	private static final class MutableClock extends Clock {
		private volatile Instant value; MutableClock(Instant value){this.value=value;} void set(Instant value){this.value=value;}
		@Override public ZoneId getZone(){return ZoneOffset.UTC;} @Override public Clock withZone(ZoneId zone){return this;}
		@Override public Instant instant(){return value;}
	}
}
