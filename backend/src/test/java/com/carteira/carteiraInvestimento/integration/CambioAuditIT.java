package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.port.CambioProviderPort;
import com.carteira.carteiraInvestimento.application.port.HistoricoCambioPort;
import com.carteira.carteiraInvestimento.application.service.CambioApplicationService;
import com.carteira.carteiraInvestimento.application.service.CambioProviderException;
import com.carteira.carteiraInvestimento.application.service.CambioUnavailableException;
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
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CambioAuditIT extends PostgreSqlContainerSupport {
	@Autowired AuditoriaIsoladaPort audit; @Autowired HistoricoCambioPort history; @Autowired DataSource dataSource;
	@BeforeEach void clean()throws Exception{execute("DELETE FROM logs_auditoria WHERE tipo_evento='CAMBIO_INDISPONIVEL'");execute("DELETE FROM historico_cambio");MDC.clear();}
	@Test void finalIntegrationFailurePersistsSanitizedIsolatedAuditWithCorrelation() {
		UUID correlation=UUID.randomUUID();MDC.put("correlationId",correlation.toString());
		var service=service(new Fake(CambioProvider.ALPHA_VANTAGE,new CambioProviderException(true,"secret")),new Fake(CambioProvider.TWELVE_DATA,new CambioProviderException(true,"secret")));
		assertThatThrownBy(service::obterUsdBrl).isInstanceOf(CambioUnavailableException.class);
		assertThat(count("SELECT count(*) FROM logs_auditoria WHERE tipo_evento='CAMBIO_INDISPONIVEL' AND correlation_id='"+correlation+"' AND endpoint='/api/v1/cambio/usd-brl'")).isOne();
		assertThat(count("SELECT count(*) FROM historico_cambio")).isZero();
	}
	@Test void successfulFallbackPersistsObservationWithoutAvailabilityAudit() {
		var service=service(new Fake(CambioProvider.ALPHA_VANTAGE,new CambioProviderException(true,"timeout")),new Fake(CambioProvider.TWELVE_DATA,null));
		assertThat(service.obterUsdBrl().provider()).isEqualTo(CambioProvider.TWELVE_DATA);
		assertThat(count("SELECT count(*) FROM logs_auditoria WHERE tipo_evento='CAMBIO_INDISPONIVEL'")).isZero();
		assertThat(count("SELECT count(*) FROM historico_cambio")).isOne();
	}
	private CambioApplicationService service(CambioProviderPort... providers){com.github.benmanes.caffeine.cache.Cache<String,ObservacaoCambio> cache=Caffeine.newBuilder().build();return new CambioApplicationService(history,audit,List.of(providers),cache,Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"),ZoneOffset.UTC));}
	private record Fake(CambioProvider provider,RuntimeException failure) implements CambioProviderPort {public CambioExterno obterUsdBrl(){if(failure!=null)throw failure;return new CambioExterno(MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5.1"),provider,Instant.parse("2026-09-10T11:59:00Z"));}}
	private long count(String sql){try(var c=dataSource.getConnection();var s=c.prepareStatement(sql);var r=s.executeQuery()){r.next();return r.getLong(1);}catch(Exception e){throw new RuntimeException(e);}}
	private void execute(String sql)throws Exception{try(var c=dataSource.getConnection();var s=c.prepareStatement(sql)){s.executeUpdate();}}
}
