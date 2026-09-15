package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.HistoricoCambioPort;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CambioSchemaIT extends PostgreSqlContainerSupport {
	@Autowired DataSource dataSource; @Autowired HistoricoCambioPort history;
	@BeforeEach void clean() throws Exception { execute("DELETE FROM historico_cambio"); }
	@Test void v8HasExactTypesChecksOrderedIndexNoUniqueAndHibernateValidates() throws Exception {
		assertThat(count("SELECT count(*) FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='historico_cambio' AND column_name='taxa' AND numeric_precision=18 AND numeric_scale=8")).isOne();
		assertThat(count("SELECT count(*) FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='historico_cambio' AND column_name IN ('instante_cotacao','registrado_em') AND data_type='timestamp with time zone'")).isEqualTo(2);
		assertThat(count("SELECT count(*) FROM pg_indexes WHERE schemaname=current_schema() AND tablename='historico_cambio' AND indexname='idx_historico_cambio_recente' AND indexdef LIKE '%moeda_origem, moeda_destino, instante_cotacao DESC, registrado_em DESC, id DESC%'")).isOne();
		assertThat(count("SELECT count(*) FROM pg_constraint WHERE conrelid='historico_cambio'::regclass AND contype='u'")).isZero();
	}
	@Test void checksRejectInvalidPairRateProviderAndEqualObservationsCoexist() throws Exception {
		for(String[] invalid:new String[][]{{"EUR","BRL","1","ALPHA_VANTAGE"},{"USD","USD","1","ALPHA_VANTAGE"},{"USD","BRL","0","ALPHA_VANTAGE"},{"USD","BRL","1","OTHER"}})
			assertThatThrownBy(()->insert(invalid)).isInstanceOf(PSQLException.class);
		Instant quote=Instant.parse("2026-09-10T11:00:00Z"), recorded=Instant.parse("2026-09-10T12:00:00Z");
		history.save(new ObservacaoCambio(UUID.randomUUID(),MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5.1"),CambioProvider.ALPHA_VANTAGE,quote,recorded));
		history.save(new ObservacaoCambio(UUID.randomUUID(),MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5.1"),CambioProvider.ALPHA_VANTAGE,quote,recorded));
		assertThat(count("SELECT count(*) FROM historico_cambio")).isEqualTo(2);
	}
	private void insert(String[] values)throws Exception{try(Connection c=dataSource.getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO historico_cambio(id,moeda_origem,moeda_destino,taxa,provider,instante_cotacao,registrado_em) VALUES (?,?,?,?::numeric,?,now(),now())")){s.setObject(1,UUID.randomUUID());for(int i=0;i<4;i++)s.setString(i+2,values[i]);s.executeUpdate();}}
	private long count(String sql)throws Exception{try(Connection c=dataSource.getConnection();PreparedStatement s=c.prepareStatement(sql);var r=s.executeQuery()){r.next();return r.getLong(1);}}
	private void execute(String sql)throws Exception{try(Connection c=dataSource.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.executeUpdate();}}
}
