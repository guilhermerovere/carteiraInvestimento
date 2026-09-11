package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InvestmentV9MetadataIT extends PostgreSqlContainerSupport {
    private static final String SCHEMA = "investment_v9_metadata_test";

    @BeforeAll
    static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .schemas(SCHEMA).defaultSchema(SCHEMA).cleanDisabled(false).load();
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void exposesOnlyThePlannedIndexesWithExactColumnsOrderDirectionAndPredicate() throws Exception {
        try (Connection connection = connection()) {
            Map<String, String> definitions = strings(connection, """
                    SELECT indexname, indexdef FROM pg_indexes
                    WHERE schemaname=? AND tablename IN ('transacoes','posicoes','transacoes_idempotencia')
                    ORDER BY indexname
                    """);

            assertThat(definitions.keySet()).containsExactlyInAnyOrder(
                    "transacoes_pkey", "ix_transacoes_historico",
                    "posicoes_pkey", "uk_posicoes_carteira_acao", "ix_posicoes_abertas",
                    "transacoes_idempotencia_pkey", "uk_transacoes_idempotencia");
            assertThat(definitions.get("ix_transacoes_historico"))
                    .contains("(carteira_id, data_registro DESC, id DESC)").doesNotContain("WHERE");
            assertThat(definitions.get("uk_posicoes_carteira_acao"))
                    .contains("UNIQUE", "(carteira_id, acao_id)");
            assertThat(definitions.get("ix_posicoes_abertas"))
                    .contains("(carteira_id, acao_id)", "WHERE (quantidade >");
            assertThat(definitions.get("uk_transacoes_idempotencia"))
                    .contains("UNIQUE", "(carteira_id, idempotency_key)");

            assertThat(indexColumns(connection, "ix_transacoes_historico"))
                    .containsExactly("carteira_id:ASC", "data_registro:DESC", "id:DESC");
            assertThat(indexColumns(connection, "ix_posicoes_abertas"))
                    .containsExactly("carteira_id:ASC", "acao_id:ASC");
            assertThat(indexColumns(connection, "uk_transacoes_idempotencia"))
                    .containsExactly("carteira_id:ASC", "idempotency_key:ASC");
        }
    }

    @Test
    void exposesExactFinancialTypesPrecisionScaleAndNullability() throws Exception {
        try (Connection connection = connection()) {
            assertColumns(connection, "transacoes", Map.ofEntries(
                    column("quantidade", "numeric", 18, 8, "NO"),
                    column("preco_unitario", "numeric", 18, 8, "NO"),
                    column("taxas", "numeric", 18, 8, "NO"),
                    column("taxa_cambio_brl", "numeric", 18, 8, "NO"),
                    column("valor_total_brl", "numeric", 18, 2, "NO"),
                    column("resultado_realizado_brl", "numeric", 18, 2, "YES"),
                    column("data_negociacao", "timestamp with time zone", null, null, "NO"),
                    column("data_registro", "timestamp with time zone", null, null, "NO")));
            assertColumns(connection, "posicoes", Map.ofEntries(
                    column("quantidade", "numeric", 18, 8, "NO"),
                    column("preco_medio_brl", "numeric", 18, 8, "NO"),
                    column("total_investido_brl", "numeric", 18, 2, "NO"),
                    column("lucro_realizado_acumulado_brl", "numeric", 18, 2, "NO"),
                    column("ultima_atualizacao", "timestamp with time zone", null, null, "NO")));
            assertColumns(connection, "transacoes_idempotencia", Map.ofEntries(
                    column("valor_origem", "numeric", 36, 16, "YES"),
                    column("saldo_caixa_brl_resultante", "numeric", 18, 2, "YES"),
                    column("posicao_quantidade_resultante", "numeric", 18, 8, "YES"),
                    column("posicao_preco_medio_brl_resultante", "numeric", 18, 8, "YES"),
                    column("posicao_total_investido_brl_resultante", "numeric", 18, 2, "YES"),
                    column("posicao_lucro_realizado_acumulado_brl_resultante", "numeric", 18, 2, "YES")));
            assertColumns(connection, "carteira_snapshots", Map.ofEntries(
                    column("saldo_caixa_brl", "numeric", 18, 2, "NO"),
                    column("total_investido_brl", "numeric", 18, 2, "NO"),
                    column("valor_posicoes_brl", "numeric", 18, 2, "YES"),
                    column("lucro_nao_realizado_brl", "numeric", 18, 2, "YES"),
                    column("patrimonio_total_brl", "numeric", 18, 2, "YES")));
        }
    }

    @Test
    void exposesAllPlannedPrimaryUniqueForeignKeysAndChecks() throws Exception {
        try (Connection connection = connection()) {
            Map<String, String> constraints = strings(connection, """
                    SELECT conname, pg_get_constraintdef(oid)
                    FROM pg_constraint
                    WHERE connamespace=?::regnamespace
                      AND conrelid IN ('acoes'::regclass,'transacoes'::regclass,'posicoes'::regclass,
                                       'transacoes_idempotencia'::regclass,'carteira_snapshots'::regclass)
                    ORDER BY conname
                    """);
            assertThat(constraints.keySet()).contains(
                    "uk_acoes_id_moeda",
                    "transacoes_pkey", "posicoes_pkey", "transacoes_idempotencia_pkey",
                    "uk_posicoes_carteira_acao", "uk_transacoes_idempotencia",
                    "fk_transacoes_carteira_usuario", "fk_transacoes_acao_moeda",
                    "fk_transacoes_usuario", "fk_transacoes_corretora", "fk_transacoes_exchange_rate",
                    "fk_posicoes_carteira", "fk_posicoes_acao",
                    "fk_transacoes_idempotencia_carteira_usuario", "fk_transacoes_idempotencia_usuario",
                    "fk_transacoes_idempotencia_transacao", "fk_transacoes_idempotencia_posicao",
                    "ck_transacoes_tipo", "ck_transacoes_quantidade", "ck_transacoes_preco_unitario",
                    "ck_transacoes_taxas", "ck_transacoes_taxa_cambio", "ck_transacoes_resultado_tipo",
                    "ck_transacoes_moeda_fx", "ck_posicoes_estado", "ck_transacoes_idempotencia_key",
                    "ck_transacoes_idempotencia_fingerprint", "ck_transacoes_idempotencia_estado",
                    "ck_transacoes_idempotencia_resultado", "ck_carteira_snapshots_valores_minimos",
                    "ck_carteira_snapshots_valuation_coerente");
            assertThat(constraints.get("uk_acoes_id_moeda")).contains("UNIQUE (id, moeda)");
            assertThat(constraints.get("fk_transacoes_carteira_usuario"))
                    .contains("FOREIGN KEY (carteira_id, usuario_id)", "ON DELETE RESTRICT");
            assertThat(constraints.get("fk_transacoes_acao_moeda"))
                    .contains("FOREIGN KEY (acao_id, moeda)", "ON DELETE RESTRICT");
            assertThat(constraints.get("fk_transacoes_exchange_rate"))
                    .contains("FOREIGN KEY (exchange_rate_id)", "ON DELETE RESTRICT");
            assertThat(constraints.get("fk_transacoes_idempotencia_carteira_usuario"))
                    .contains("ON DELETE RESTRICT", "DEFERRABLE INITIALLY DEFERRED");
            assertThat(constraints.get("ck_transacoes_resultado_tipo")).contains("BUY", "SELL");
            assertThat(constraints.get("ck_transacoes_moeda_fx")).contains("BRL", "USD", "exchange_rate_id");
            assertThat(constraints.get("ck_posicoes_estado"))
                    .contains("quantidade =", "preco_medio_brl", "total_investido_brl");
            assertThat(constraints.get("ck_transacoes_idempotencia_resultado"))
                    .contains("RESERVADA", "CONCLUIDA", "valor_origem", "posicao_ultima_atualizacao_resultante");
            assertThat(constraints.get("ck_carteira_snapshots_valuation_coerente"))
                    .contains("IS NULL", "IS NOT NULL", "patrimonio_total_brl");
            assertThat(constraints.get("ck_carteira_snapshots_valores_minimos"))
                    .contains("saldo_caixa_brl >=", "total_investido_brl >=", "valor_posicoes_brl IS NULL");

            assertThat(strings(connection, """
                    SELECT constraint_name, delete_rule FROM information_schema.referential_constraints
                    WHERE constraint_schema=? AND constraint_name LIKE 'fk_%'
                      AND constraint_name IN (
                        SELECT constraint_name FROM information_schema.table_constraints
                        WHERE table_schema=? AND table_name IN ('transacoes','posicoes','transacoes_idempotencia'))
                    """, SCHEMA, SCHEMA).values()).allMatch("RESTRICT"::equals);
        }
    }

    private Connection connection() throws SQLException {
        Connection connection = POSTGRES.createConnection("");
        connection.setSchema(SCHEMA);
        return connection;
    }

    private void assertColumns(Connection connection, String table, Map<String, Column> expected) throws SQLException {
        Map<String, Column> actual = new LinkedHashMap<>();
        try (var statement = connection.prepareStatement("""
                SELECT column_name,data_type,numeric_precision,numeric_scale,is_nullable
                FROM information_schema.columns WHERE table_schema=? AND table_name=?
                """)) {
            statement.setString(1, SCHEMA);
            statement.setString(2, table);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String name = result.getString(1);
                    if (expected.containsKey(name)) {
                        actual.put(name, new Column(result.getString(2), integer(result, 3), integer(result, 4), result.getString(5)));
                    }
                }
            }
        }
        assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

    private List<String> indexColumns(Connection connection, String indexName) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (var statement = connection.prepareStatement("""
                SELECT a.attname,
                       CASE WHEN (i.indoption[x.ordinality - 1] & 1) = 1 THEN 'DESC' ELSE 'ASC' END
                FROM pg_index i
                JOIN pg_class idx ON idx.oid=i.indexrelid
                CROSS JOIN LATERAL unnest(i.indkey) WITH ORDINALITY AS x(attnum, ordinality)
                JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=x.attnum
                WHERE idx.relnamespace=?::regnamespace AND idx.relname=?
                ORDER BY x.ordinality
                """)) {
            statement.setString(1, SCHEMA);
            statement.setString(2, indexName);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) columns.add(result.getString(1) + ":" + result.getString(2));
            }
        }
        return columns;
    }

    private Map<String, String> strings(Connection connection, String sql, String... parameters) throws SQLException {
        Map<String, String> values = new LinkedHashMap<>();
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, SCHEMA);
            for (int i = 0; i < parameters.length; i++) statement.setString(i + 1, parameters[i]);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) values.put(result.getString(1), result.getString(2));
            }
        }
        return values;
    }

    private static Integer integer(ResultSet result, int column) throws SQLException {
        int value = result.getInt(column);
        return result.wasNull() ? null : value;
    }

    private static Map.Entry<String, Column> column(String name, String type, Integer precision,
            Integer scale, String nullable) {
        return Map.entry(name, new Column(type, precision, scale, nullable));
    }

    private record Column(String type, Integer precision, Integer scale, String nullable) { }
}
