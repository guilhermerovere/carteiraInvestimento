package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PortfolioValuationSchemaIT extends PostgreSqlContainerSupport {
    private static final String SCHEMA = "portfolio_valuation_v10";

    @BeforeAll
    static void migrateV1ThroughV10() {
        Flyway flyway = Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas(SCHEMA).defaultSchema(SCHEMA).target("10").cleanDisabled(false).load();
        flyway.clean();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(10);
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("10");
    }

    @Test
    void v10AddsRequiredVersionAndNullableMicrosecondInstant() throws Exception {
        try (Connection connection = connection()) {
            UUID wallet = wallet(connection);
            assertThat(value(connection, "SELECT estado_versao FROM carteiras WHERE id=?", wallet, Long.class)).isZero();
            assertThat(columnNullable(connection, "carteiras", "estado_versao")).isEqualTo("NO");
            assertThat(columnNullable(connection, "carteira_snapshots", "valuation_instant")).isEqualTo("YES");
            assertThatThrownBy(() -> execute(connection, "UPDATE carteiras SET estado_versao=-1 WHERE id=?", wallet))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void acceptsExactlyUnknownLocalEmptyAndMaterializedStatesAndRejectsPartialOnes() throws Exception {
        try (Connection connection = connection()) {
            UUID wallet = wallet(connection);
            insertSnapshot(connection, wallet, LocalDate.of(2026, 9, 1), "10.00", "5.00", null, null, null, null);
            insertSnapshot(connection, wallet, LocalDate.of(2026, 9, 2), "10.00", "0.00", "0.00", "0.00", "10.00", null);
            Instant instant = Instant.parse("2026-09-12T12:00:00.123456Z");
            insertSnapshot(connection, wallet, LocalDate.of(2026, 9, 3), "10.00", "5.00", "8.00", "3.00", "18.00", instant);
            assertThat(value(connection, "SELECT valuation_instant FROM carteira_snapshots WHERE carteira_id=? AND data_referencia='2026-09-03'", wallet, Timestamp.class).toInstant()).isEqualTo(instant);
            assertThatThrownBy(() -> insertSnapshot(connection, wallet, LocalDate.of(2026, 9, 4),
                    "10.00", "5.00", "8.00", null, null, instant)).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> insertSnapshot(connection, wallet, LocalDate.of(2026, 9, 5),
                    "10.00", "5.00", "8.00", "3.00", "18.00", null)).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> insertSnapshot(connection, wallet, LocalDate.of(2026, 9, 6),
                    "10.00", "0.00", "0.00", "0.00", "11.00", null)).isInstanceOf(SQLException.class);
        }
    }

    @Test
    void validV9MaterializedRowsAreSafelyInitializedWhenMigratingToV10() throws Exception {
        String schema = "portfolio_v9_to_v10";
        Flyway v9 = Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas(schema).defaultSchema(schema).target("9").cleanDisabled(false).load();
        v9.clean(); v9.migrate();
        UUID wallet;
        try (Connection connection = POSTGRES.createConnection("")) {
            connection.setSchema(schema); wallet = wallet(connection);
            execute(connection, """
                    INSERT INTO carteira_snapshots(id,carteira_id,data_referencia,saldo_caixa_brl,
                        valor_posicoes_brl,total_investido_brl,patrimonio_total_brl,lucro_nao_realizado_brl)
                    VALUES (?,?,DATE '2026-09-01',10.00,8.00,5.00,18.00,3.00)
                    """, UUID.randomUUID(), wallet);
        }
        Flyway latest = Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas(schema).defaultSchema(schema).target("10").cleanDisabled(false).load();
        assertThat(latest.migrate().migrationsExecuted).isEqualTo(1);
        try (Connection connection = POSTGRES.createConnection("")) {
            connection.setSchema(schema);
            assertThat(value(connection, "SELECT valuation_instant FROM carteira_snapshots WHERE carteira_id=?",
                    wallet, Timestamp.class)).isNotNull();
            assertThat(value(connection, "SELECT valor_posicoes_brl FROM carteira_snapshots WHERE carteira_id=?",
                    wallet, BigDecimal.class)).isEqualByComparingTo("8.00");
        }
    }

    private Connection connection() throws SQLException {
        Connection connection = POSTGRES.createConnection(""); connection.setSchema(SCHEMA); return connection;
    }
    private UUID wallet(Connection connection) throws SQLException {
        UUID user = UUID.randomUUID(), wallet = UUID.randomUUID();
        execute(connection, "INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,'ROLE_USER',true)",
                user, "V10 User", "v10-" + user + "@test", "hash");
        execute(connection, "INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,10.00)",
                wallet, user, "Principal");
        return wallet;
    }
    private void insertSnapshot(Connection connection, UUID wallet, LocalDate date, String cash, String invested,
            String value, String profit, String equity, Instant instant) throws SQLException {
        execute(connection, """
                INSERT INTO carteira_snapshots(id,carteira_id,data_referencia,saldo_caixa_brl,total_investido_brl,
                    valor_posicoes_brl,lucro_nao_realizado_brl,patrimonio_total_brl,valuation_instant)
                VALUES (?,?,?,?,?,?,?,?,?)
                """, UUID.randomUUID(), wallet, Date.valueOf(date), decimal(cash), decimal(invested), decimal(value),
                decimal(profit), decimal(equity), instant == null ? null : Timestamp.from(instant));
    }
    private BigDecimal decimal(String value) { return value == null ? null : new BigDecimal(value); }
    private String columnNullable(Connection connection, String table, String column) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT is_nullable FROM information_schema.columns WHERE table_schema=? AND table_name=? AND column_name=?")) {
            statement.setString(1, SCHEMA); statement.setString(2, table); statement.setString(3, column);
            try (var result = statement.executeQuery()) { result.next(); return result.getString(1); }
        }
    }
    private <T> T value(Connection connection, String sql, UUID id, Class<T> type) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) { statement.setObject(1, id);
            try (var result = statement.executeQuery()) { result.next(); return result.getObject(1, type); }
        }
    }
    private void execute(Connection connection, String sql, Object... values) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) statement.setObject(index + 1, values[index]);
            statement.executeUpdate();
        }
    }
}
