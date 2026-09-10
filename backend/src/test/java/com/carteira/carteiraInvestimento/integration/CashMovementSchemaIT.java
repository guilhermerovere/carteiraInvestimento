package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CashMovementSchemaIT extends PostgreSqlContainerSupport {
	private static final String SCHEMA = "cash_schema_test";

	@BeforeAll
	static void migrate() {
		Flyway flyway = Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.schemas(SCHEMA).defaultSchema(SCHEMA).cleanDisabled(false).load();
		flyway.clean();
		flyway.migrate();
	}

	@Test
	void enforcesFinancialChecksCompositeOwnershipAndRestrictedDeletes() throws Exception {
		try (Connection connection = connection()) {
			UUID userA = user(connection);
			UUID userB = user(connection);
			UUID walletA = wallet(connection, userA, new BigDecimal("100.00"));
			UUID walletB = wallet(connection, userB, BigDecimal.ZERO);
			assertThatThrownBy(() -> movement(connection, walletA, userB, "DEPOSITO", new BigDecimal("1.00")))
					.isInstanceOf(SQLException.class);
			assertThatThrownBy(() -> movement(connection, walletA, userA, "DEPOSITO", BigDecimal.ZERO))
					.isInstanceOf(SQLException.class);
			assertThatThrownBy(() -> movement(connection, walletA, userA, "INVALIDO", BigDecimal.ONE))
					.isInstanceOf(SQLException.class);
			assertThatThrownBy(() -> update(connection, "UPDATE carteiras SET saldo_caixa_brl=-1 WHERE id=?", walletA))
					.isInstanceOf(SQLException.class);
			UUID movement = movement(connection, walletA, userA, "DEPOSITO", BigDecimal.ONE);
			assertThatThrownBy(() -> update(connection, "DELETE FROM carteiras WHERE id=?", walletA))
					.isInstanceOf(SQLException.class);
			assertThat(walletB).isNotNull();
			assertThat(movement).isNotNull();
		}
	}

	@Test
	void enforcesSnapshotAndIdempotencyIdentities() throws Exception {
		try (Connection connection = connection()) {
			UUID user = user(connection);
			UUID wallet = wallet(connection, user, BigDecimal.ZERO);
			UUID movement = movement(connection, wallet, user, "DEPOSITO", BigDecimal.ONE);
			insertSnapshot(connection, wallet, LocalDate.of(2026, 1, 1));
			assertThatThrownBy(() -> insertSnapshot(connection, wallet, LocalDate.of(2026, 1, 1)))
					.isInstanceOf(SQLException.class);
			insertIdempotency(connection, wallet, movement, "same-key");
			assertThatThrownBy(() -> insertIdempotency(connection, wallet, movement, "same-key"))
					.isInstanceOf(SQLException.class);
			assertThatThrownBy(() -> insertIdempotency(connection, wallet, movement, " invalid"))
					.isInstanceOf(SQLException.class);
		}
	}

	private Connection connection() throws SQLException {
		Connection connection = POSTGRES.createConnection("");
		connection.setSchema(SCHEMA);
		return connection;
	}

	private UUID user(Connection connection) throws SQLException {
		UUID id = UUID.randomUUID();
		try (var statement = connection.prepareStatement("INSERT INTO usuarios (id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,?,true)")) {
			statement.setObject(1, id); statement.setString(2, "User");
			statement.setString(3, "cash-" + id + "@example.test"); statement.setString(4, "hash");
			statement.setString(5, "ROLE_USER"); statement.executeUpdate();
		}
		return id;
	}

	private UUID wallet(Connection connection, UUID user, BigDecimal balance) throws SQLException {
		UUID id = UUID.randomUUID();
		try (var statement = connection.prepareStatement("INSERT INTO carteiras (id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,?)")) {
			statement.setObject(1, id); statement.setObject(2, user); statement.setString(3, "Carteira Principal");
			statement.setBigDecimal(4, balance); statement.executeUpdate();
		}
		return id;
	}

	private UUID movement(Connection connection, UUID wallet, UUID user, String type, BigDecimal amount)
			throws SQLException {
		UUID id = UUID.randomUUID();
		try (var statement = connection.prepareStatement("INSERT INTO movimentacoes_caixa (id,carteira_id,usuario_id,tipo,valor_brl,data_hora) VALUES (?,?,?,?,?,?)")) {
			statement.setObject(1, id); statement.setObject(2, wallet); statement.setObject(3, user);
			statement.setString(4, type); statement.setBigDecimal(5, amount);
			statement.setTimestamp(6, Timestamp.from(Instant.now())); statement.executeUpdate();
		}
		return id;
	}

	private void insertSnapshot(Connection connection, UUID wallet, LocalDate date) throws SQLException {
		try (var statement = connection.prepareStatement("INSERT INTO carteira_snapshots VALUES (?,?,?,?,?,?,?,?)")) {
			statement.setObject(1, UUID.randomUUID()); statement.setObject(2, wallet); statement.setObject(3, date);
			for (int index = 4; index <= 8; index++) statement.setBigDecimal(index, BigDecimal.ZERO);
			statement.executeUpdate();
		}
	}

	private void insertIdempotency(Connection connection, UUID wallet, UUID movement, String key) throws SQLException {
		try (var statement = connection.prepareStatement("INSERT INTO movimentacoes_caixa_idempotencia (id,carteira_id,tipo,idempotency_key,fingerprint,movimentacao_id,saldo_resultante,criada_em) VALUES (?,?,?,?,?,?,?,?)")) {
			statement.setObject(1, UUID.randomUUID()); statement.setObject(2, wallet); statement.setString(3, "DEPOSITO");
			statement.setString(4, key); statement.setString(5, "a".repeat(64)); statement.setObject(6, movement);
			statement.setBigDecimal(7, BigDecimal.ONE); statement.setTimestamp(8, Timestamp.from(Instant.now()));
			statement.executeUpdate();
		}
	}

	private void update(Connection connection, String sql, UUID id) throws SQLException {
		try (var statement = connection.prepareStatement(sql)) { statement.setObject(1, id); statement.executeUpdate(); }
	}
}
