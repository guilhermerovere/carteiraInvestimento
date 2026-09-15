package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.CambioProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCambioPort;
import com.carteira.carteiraInvestimento.application.service.CambioApplicationService;
import com.carteira.carteiraInvestimento.domain.fx.CambioExterno;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class CambioTransactionBoundaryIT extends PostgreSqlContainerSupport {
	@Autowired HistoricoCambioPort history; @Autowired DataSource dataSource; @Autowired PlatformTransactionManager transactionManager;
	@BeforeEach void clean()throws Exception{try(var c=dataSource.getConnection();var s=c.prepareStatement("DELETE FROM historico_cambio")){s.executeUpdate();}}
	@Test void providerRunsOutsideTransactionAndSaveIsCommittedBeforeCaching()throws Exception{
		CambioProviderPort twelveData=new CambioProviderPort(){public CambioProvider provider(){return CambioProvider.TWELVE_DATA;}public CambioExterno obterUsdBrl(){assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();return new CambioExterno(MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5.2"),provider(),Instant.parse("2026-09-10T11:59:00Z"));}};
		com.github.benmanes.caffeine.cache.Cache<String,ObservacaoCambio> cache=Caffeine.newBuilder().build();
		var service=new CambioApplicationService(history,event->{},List.of(twelveData),cache,Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"),ZoneOffset.UTC));
		ObservacaoCambio result=service.obterUsdBrl();
		assertThat(count()).isOne(); assertThat(cache.getIfPresent(CambioApplicationService.CACHE_KEY)).isEqualTo(result);
	}
	@Test void transactionalPersistenceRollsBackAndFailedSaveNeverPopulatesServiceCache(){
		var value=new ObservacaoCambio(java.util.UUID.randomUUID(),MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5.2"),CambioProvider.TWELVE_DATA,Instant.parse("2026-09-10T11:59:00Z"),Instant.parse("2026-09-10T12:00:00Z"));
		assertThatThrownBy(()->new TransactionTemplate(transactionManager).executeWithoutResult(status->{history.save(value);throw new IllegalStateException("force rollback");})).isInstanceOf(IllegalStateException.class);
		assertThat(count()).isZero();
	}
	private long count(){try(var c=dataSource.getConnection();var s=c.prepareStatement("SELECT count(*) FROM historico_cambio");var r=s.executeQuery()){r.next();return r.getLong(1);}catch(Exception e){throw new RuntimeException(e);}}
}
