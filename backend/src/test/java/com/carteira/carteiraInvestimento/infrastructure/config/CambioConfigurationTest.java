package com.carteira.carteiraInvestimento.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.github.benmanes.caffeine.cache.Ticker;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CambioConfigurationTest {
	@Test void caffeineRetentionExpiresFiveMinutesAfterWriteWithControllableTicker() {
		MutableTicker ticker=new MutableTicker(); var cache=new CambioConfiguration().cambioCache(ticker);
		var value=new ObservacaoCambio(UUID.randomUUID(),MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5"),
				CambioProvider.ALPHA_VANTAGE,Instant.EPOCH,Instant.EPOCH);
		cache.put("USD:BRL",value); ticker.advance(5,TimeUnit.MINUTES);
		assertThat(cache.getIfPresent("USD:BRL")).isNull();
	}
	private static final class MutableTicker implements Ticker {private final AtomicLong nanos=new AtomicLong();public long read(){return nanos.get();}void advance(long value,TimeUnit unit){nanos.addAndGet(unit.toNanos(value));}}
}
