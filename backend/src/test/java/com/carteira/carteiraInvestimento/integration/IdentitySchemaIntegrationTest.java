package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentitySchemaIntegrationTest extends PostgreSqlContainerSupport {

	private static final String SCHEMA = "identity_schema_test";

	@BeforeEach
	void migrateIdentitySchema() {
		Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.schemas(SCHEMA)
				.defaultSchema(SCHEMA)
				.cleanDisabled(false)
				.load()
				.clean();
		Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.schemas(SCHEMA)
				.defaultSchema(SCHEMA)
				.load()
				.migrate();
	}

	@Test
	void rejectsNonCanonicalEmailInvalidRoleAndDuplicateCanonicalEmail() throws Exception {
		insertUser("first@example.test", "ROLE_USER");

		assertThatThrownBy(() -> insertUser(" First@Example.Test ", "ROLE_USER"))
				.hasMessageContaining("ck_usuarios_email_canonico");
		assertThatThrownBy(() -> insertUser("other@example.test", "ROLE_MANAGER"))
				.hasMessageContaining("ck_usuarios_role");
		assertThatThrownBy(() -> insertUser("first@example.test", "ROLE_USER"))
				.hasMessageContaining("ux_usuarios_email_canonico");
	}

	@Test
	void rejectsASecondWalletForTheSameUser() throws Exception {
		UUID userId = insertUser("wallet@example.test", "ROLE_USER");
		insertWallet(userId);

		assertThatThrownBy(() -> insertWallet(userId))
				.hasMessageContaining("uk_carteiras_usuario");
	}

	@Test
	void persistsAnonymousAndSystemAuditEventsWithAppropriateNullFields() throws Exception {
		insertAuditEvent(null, "/api/v1/auth/login");
		insertAuditEvent(null, null);
	}

	private UUID insertUser(String email, String role) throws Exception {
		UUID id = UUID.randomUUID();
		String sql = "INSERT INTO " + SCHEMA
				+ ".usuarios (id, nome, email, senha_hash, role, ativo) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection connection = POSTGRES.createConnection("");
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, id);
			statement.setString(2, "User");
			statement.setString(3, email);
			statement.setString(4, "hash");
			statement.setString(5, role);
			statement.setBoolean(6, true);
			statement.executeUpdate();
		}
		return id;
	}

	private void insertWallet(UUID userId) throws Exception {
		String sql = "INSERT INTO " + SCHEMA
				+ ".carteiras (id, usuario_id, nome, saldo_caixa_brl) VALUES (?, ?, ?, ?)";
		try (Connection connection = POSTGRES.createConnection("");
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, UUID.randomUUID());
			statement.setObject(2, userId);
			statement.setString(3, "Carteira Principal");
			statement.setBigDecimal(4, java.math.BigDecimal.ZERO);
			statement.executeUpdate();
		}
	}

	private void insertAuditEvent(UUID userId, String endpoint) throws Exception {
		String sql = "INSERT INTO " + SCHEMA
				+ ".logs_auditoria (id, usuario_id, tipo_evento, resultado, severidade, endpoint, correlation_id) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (Connection connection = POSTGRES.createConnection("");
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, UUID.randomUUID());
			statement.setObject(2, userId);
			statement.setString(3, "LOGIN_FALHO");
			statement.setString(4, "FALHA");
			statement.setString(5, "AVISO");
			statement.setString(6, endpoint);
			statement.setObject(7, UUID.randomUUID());
			statement.executeUpdate();
		}
	}
}
