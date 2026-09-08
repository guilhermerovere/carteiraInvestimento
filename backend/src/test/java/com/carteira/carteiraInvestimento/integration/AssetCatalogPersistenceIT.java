package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.service.AtivoUseCase;
import com.carteira.carteiraInvestimento.application.service.DuplicateTickerException;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.application.port.AtivoSort;
import com.carteira.carteiraInvestimento.application.port.SortDirection;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;

@SpringBootTest
class AssetCatalogPersistenceIT extends PostgreSqlContainerSupport {
	@Autowired private DataSource dataSource;
	@Autowired private AtivoUseCase ativos;

	@BeforeEach
	void cleanCatalog() throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM acoes")) {
			statement.executeUpdate();
		}
	}

	@Test
	void migrationCreatesNamedUniqueConstraintAndAcceptsValidRows() throws Exception {
		insert("PETR4", "Petrobras", "ACAO", "B3", "BRL");
		insert("AAPL", "Apple", "ETF", "US", "USD");
		assertThat(count("SELECT count(*) FROM acoes")).isEqualTo(2);
		assertThat(count("SELECT count(*) FROM pg_constraint WHERE conname = 'uk_acoes_ticker'"))
				.isPositive();
	}

	@Test
	void databaseRejectsNonCanonicalAndMarketInvalidTickers() {
		assertConstraint(() -> insert("petr4", "Petrobras", "ACAO", "B3", "BRL"), "ck_acoes_ticker_canonico");
		assertConstraint(() -> insert("PETR4 ", "Petrobras", "ACAO", "B3", "BRL"), "ck_acoes_ticker_canonico");
		assertConstraint(() -> insert("PETR", "Petrobras", "ACAO", "B3", "BRL"), "ck_acoes_ticker_mercado");
		assertConstraint(() -> insert("ABC123", "US Asset", "ACAO", "US", "USD"), "ck_acoes_ticker_mercado");
	}

	@Test
	void databaseRejectsInvalidControlledValuesCurrencyAndName() {
		assertConstraint(() -> insert("PETR4", "Petrobras", "CRIPTO", "B3", "BRL"), "ck_acoes_tipo");
		assertConstraint(() -> insert("PETR4", "Petrobras", "ACAO", "B3", "USD"), "ck_acoes_mercado_moeda");
		assertConstraint(() -> insert("PETR4", "   ", "ACAO", "B3", "BRL"), "ck_acoes_nome");
	}

	@Test
	void databaseEnforcesGlobalTickerUniqueness() throws Exception {
		insert("PETR4", "Petrobras", "ACAO", "B3", "BRL");
		assertConstraint(() -> insert("PETR4", "Outra", "ETF", "B3", "BRL"), "uk_acoes_ticker");
		assertThat(count("SELECT count(*) FROM acoes WHERE ticker = 'PETR4'" )).isEqualTo(1);
	}

	@Test
	void specificListingSupportsCaseInsensitiveContainsTypeAndEmptyQ() {
		ativos.criar("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3);
		ativos.criar("HGLG11", "Fundo Logistico", TipoAtivo.FII, Mercado.B3);
		ativos.criar("AAPL", "Apple", TipoAtivo.ACAO, Mercado.US);
		var byTicker = ativos.listar(query("  eTr  ", null));
		var byNameAndType = ativos.listar(query("  LOGIS  ", TipoAtivo.FII));
		var emptyQ = ativos.listar(query("   ", null));
		assertThat(byTicker.items()).extracting("ticker").containsExactly("PETR4");
		assertThat(byNameAndType.items()).extracting("ticker").containsExactly("HGLG11");
		assertThat(emptyQ.totalElements()).isEqualTo(3);
	}

	@Test
	void concurrentCanonicalCreationProducesOneSuccessOneConflictAndOneRow() throws Exception {
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try (var executor = Executors.newFixedThreadPool(2)) {
			var first = executor.submit(() -> createConcurrently(ready, start, " petr4 "));
			var second = executor.submit(() -> createConcurrently(ready, start, "PETR4"));
			ready.await();
			start.countDown();
			Object firstResult = first.get();
			Object secondResult = second.get();
			assertThat(java.util.List.of(firstResult, secondResult).stream()
					.filter(result -> result instanceof DuplicateTickerException).count()).isEqualTo(1);
			assertThat(java.util.List.of(firstResult, secondResult).stream()
					.filter(result -> "success".equals(result)).count()).isEqualTo(1);
		}
		assertThat(count("SELECT count(*) FROM acoes WHERE ticker = 'PETR4'" )).isEqualTo(1);
	}

	private Object createConcurrently(CountDownLatch ready, CountDownLatch start, String ticker) {
		try {
			ready.countDown();
			start.await();
			ativos.criar(ticker, "Petrobras", TipoAtivo.ACAO, Mercado.B3);
			return "success";
		} catch (DuplicateTickerException exception) {
			return exception;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new AssertionError(exception);
		}
	}

	private AtivoQuery query(String q, TipoAtivo tipo) {
		return new AtivoQuery(q, tipo, AtivoReadScope.ALL, 0, 20, AtivoSort.TICKER, SortDirection.ASC);
	}

	private void insert(String ticker, String nome, String tipo, String mercado, String moeda) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO acoes (id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,?,?,?,?,true)")) {
			statement.setObject(1, UUID.randomUUID());
			statement.setString(2, ticker);
			statement.setString(3, nome);
			statement.setString(4, tipo);
			statement.setString(5, mercado);
			statement.setString(6, moeda);
			statement.executeUpdate();
		}
	}

	private long count(String sql) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
				var result = statement.executeQuery()) {
			result.next();
			return result.getLong(1);
		}
	}

	private void assertConstraint(SqlAction action, String expectedConstraint) {
		assertThatThrownBy(action::run).isInstanceOf(PSQLException.class)
				.satisfies(error -> assertThat(((PSQLException) error).getServerErrorMessage().getConstraint())
						.isEqualTo(expectedConstraint));
	}

	@FunctionalInterface
	private interface SqlAction {
		void run() throws SQLException;
	}
}
