package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPort;
import com.carteira.carteiraInvestimento.application.service.AtivoUseCase;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MarketQuotePersistenceIT extends PostgreSqlContainerSupport {
	@Autowired DataSource dataSource;
	@Autowired AtivoUseCase ativos;
	@Autowired HistoricoCotacaoPort history;
	private UUID ativoId;

	@BeforeEach
	void setUp() throws Exception {
		clean();
		ativoId = ativos.criar("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3).id();
	}

	@AfterEach void tearDown() throws Exception { clean(); }

	@Test
	void schemaHasExactTypesChecksIndexForeignKeyAndNoObservationUnique() throws Exception {
		assertThat(count("SELECT count(*) FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='historico_cotacoes' AND column_name IN ('instante_cotacao','recebido_em') AND data_type='timestamp with time zone'"))
				.isEqualTo(2);
		assertThat(count("SELECT count(*) FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='historico_cotacoes' AND column_name='preco' AND numeric_precision=15 AND numeric_scale=4"))
				.isEqualTo(1);
		assertThat(count("SELECT count(*) FROM pg_indexes WHERE schemaname=current_schema() AND tablename='historico_cotacoes' AND indexname='idx_historico_cotacoes_recente' AND indexdef LIKE '%ativo_id, instante_cotacao DESC, recebido_em DESC, id DESC%'"))
				.isEqualTo(1);
		assertThat(count("SELECT count(*) FROM pg_constraint WHERE conrelid='historico_cotacoes'::regclass AND contype='u'"))
				.isZero();
		assertThat(count("SELECT count(*) FROM information_schema.tables WHERE table_schema=current_schema() AND table_name='cotacao_atual'" )).isZero();
		assertThat(count("SELECT count(*) FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='acoes' AND column_name='preco'" )).isZero();
	}

	@Test
	void databaseRejectsInvalidValuesAndForeignKeyDeletion() throws Exception {
		for (Object[] invalid : java.util.List.of(
				new Object[]{"0.0000", "BRL", "BRAPI"}, new Object[]{"1.0000", "EUR", "BRAPI"},
				new Object[]{"1.0000", "BTC", "BRAPI"},
				new Object[]{"1.0000", "BRL", "OTHER"})) {
			assertThatThrownBy(() -> directInsert(UUID.randomUUID(), (String) invalid[0], (String) invalid[1], (String) invalid[2]))
					.isInstanceOf(PSQLException.class);
		}
		directInsert(UUID.randomUUID(), "1.0000", "BRL", "BRAPI");
		assertThatThrownBy(() -> execute("DELETE FROM acoes WHERE id='" + ativoId + "'"))
				.isInstanceOf(PSQLException.class);
	}

	@Test
	void equalObservationsCoexistAndOrderingUsesAllTieBreakers() {
		Instant market = Instant.parse("2026-09-08T18:00:00Z");
		Instant received = Instant.parse("2026-09-08T18:00:01Z");
		UUID low = new UUID(0, 1); UUID high = new UUID(0, 2);
		history.save(new Cotacao(low, ativoId, new BigDecimal("10"), Moeda.BRL, market, QuoteProvider.BRAPI, received));
		history.save(new Cotacao(high, ativoId, new BigDecimal("10"), Moeda.BRL, market, QuoteProvider.BRAPI, received));
		var page = history.findByAtivoId(ativoId, 0, 20);
		assertThat(page.items()).extracting(Cotacao::id).containsExactly(high, low);
		assertThat(page.totalElements()).isEqualTo(2);
	}

	private void directInsert(UUID id, String price, String currency, String provider) throws Exception {
		try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(
				"INSERT INTO historico_cotacoes(id,ativo_id,preco,moeda,instante_cotacao,origem_provider,recebido_em) VALUES (?,?,?::numeric,?,now(),?,now())")) {
			s.setObject(1, id); s.setObject(2, ativoId); s.setString(3, price); s.setString(4, currency); s.setString(5, provider); s.executeUpdate();
		}
	}

	private long count(String sql) throws Exception {
		try (Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement(sql); var r=s.executeQuery()) { r.next(); return r.getLong(1); }
	}
	private void execute(String sql) throws Exception {
		try (Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement(sql)) { s.executeUpdate(); }
	}
	private void clean() throws Exception { execute("DELETE FROM historico_cotacoes"); execute("DELETE FROM acoes"); }
}
