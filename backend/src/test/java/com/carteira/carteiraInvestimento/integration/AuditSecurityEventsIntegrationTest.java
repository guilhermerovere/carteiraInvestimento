package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer;
import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.PasswordHasher;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException;
import com.carteira.carteiraInvestimento.application.service.LoginService;
import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.LoggerFactory;

@SpringBootTest(properties = {
		"application.security.jwt.secret-key=01234567890123456789012345678901",
		"application.security.jwt.expiration-hours=24",
		"application.security.jwt.issuer=carteira-investimento-backend",
		"application.security.jwt.audience=carteira-investimento-api",
		"application.bootstrap.admin.name=Integration Admin",
		"application.bootstrap.admin.email=admin.integration@example.test",
		"application.bootstrap.admin.password=Valid@123"
})
class AuditSecurityEventsIntegrationTest extends PostgreSqlContainerSupport {

	private static final String[] SENSITIVE_SENTINELS = {
			"PASSWORD_SENTINEL", "HASH_SENTINEL", "JWT_SENTINEL", "Authorization: Bearer AUTHORIZATION_SENTINEL",
			"CREDENTIALS_SENTINEL", "{\"body\":\"BODY_SENTINEL\"}", "999999.99"
	};

	@Autowired private AuditoriaPort auditoria;
	@Autowired private AuditoriaIsoladaPort auditoriaIsolada;
	@Autowired private UsuarioPort usuarios;
	@Autowired private TransactionTemplate transactionTemplate;
	@Autowired private DataSource dataSource;

	@Test
	void persistsEachMinimumSecurityEventWithExpectedHttpAndSystemMetadata() throws Exception {
		Usuario usuario = usuarios.save(Usuario.novoUsuario("Audit User", uniqueEmail(), "hash", Role.ROLE_USER));
		UUID bootstrapCorrelationId = UUID.randomUUID();

		auditoria.record(event(usuario.id(), TipoEvento.CADASTRO_USUARIO, ResultadoAuditoria.SUCESSO,
				SeveridadeAuditoria.INFO, "/api/v1/auth/register"));
		auditoria.record(event(usuario.id(), TipoEvento.LOGIN_SUCESSO, ResultadoAuditoria.SUCESSO,
				SeveridadeAuditoria.INFO, "/api/v1/auth/login"));
		auditoria.record(event(null, TipoEvento.LOGIN_FALHO, ResultadoAuditoria.FALHA,
				SeveridadeAuditoria.AVISO, "/api/v1/auth/login"));
		auditoria.record(event(usuario.id(), TipoEvento.USUARIO_INATIVO, ResultadoAuditoria.NEGADO,
				SeveridadeAuditoria.ALERTA, "/api/v1/auth/login"));
		auditoria.record(event(usuario.id(), TipoEvento.ACESSO_NEGADO, ResultadoAuditoria.NEGADO,
				SeveridadeAuditoria.ALERTA, "/api/v1/protected"));
		auditoria.record(new AuditoriaCommand(usuario.id(), TipoEvento.ADMIN_INICIAL_CRIADO,
				ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO, null, bootstrapCorrelationId));

		assertThat(countEventsFor(usuario.id())).isEqualTo(5);
		assertThat(countEventsFor(null)).isPositive();
		assertThat(systemEventHasGeneratedCorrelation(bootstrapCorrelationId)).isTrue();
	}

	@Test
	void persistsRejectedAuditInItsOwnTransactionWhenTheOriginatingTransactionRollsBack() {
		UUID correlationId = UUID.randomUUID();
		transactionTemplate.executeWithoutResult(status -> {
			auditoriaIsolada.recordIsoladamente(event(null, TipoEvento.LOGIN_FALHO, ResultadoAuditoria.FALHA,
					SeveridadeAuditoria.AVISO, "/api/v1/auth/login", correlationId));
			status.setRollbackOnly();
		});

		assertThat(eventExists(correlationId)).isTrue();
	}

	@Test
	void neverPersistsOrLogsCredentialPayloadOrFinancialSentinels() throws Exception {
		UUID correlationId = UUID.randomUUID();
		Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		ListAppender<ILoggingEvent> capturedLogs = new ListAppender<>();
		rootLogger.addAppender(capturedLogs);
		capturedLogs.start();
		try {
			LoginService login = new LoginService(usuarios, noOpPasswordHasher(), auditoria, auditoriaIsolada,
					user -> { throw new AssertionError("a failed login must not issue a token"); });
			assertThatThrownBy(() -> login.login("unknown@example.test", "PASSWORD_SENTINEL", correlationId))
					.isInstanceOf(AuthenticationFailedException.class);

			assertDoesNotContainSensitiveValues(persistedAuditMetadata(correlationId));
			assertDoesNotContainSensitiveValues(capturedLogs.list.stream()
					.map(ILoggingEvent::getFormattedMessage).reduce("", (left, right) -> left + right));
		} finally {
			rootLogger.detachAppender(capturedLogs);
			capturedLogs.stop();
		}
	}

	private AuditoriaCommand event(UUID usuarioId, TipoEvento tipo, ResultadoAuditoria resultado,
			SeveridadeAuditoria severidade, String endpoint) {
		return event(usuarioId, tipo, resultado, severidade, endpoint, UUID.randomUUID());
	}

	private AuditoriaCommand event(UUID usuarioId, TipoEvento tipo, ResultadoAuditoria resultado,
			SeveridadeAuditoria severidade, String endpoint, UUID correlationId) {
		return new AuditoriaCommand(usuarioId, tipo, resultado, severidade, endpoint, correlationId);
	}

	private int countEventsFor(UUID usuarioId) throws Exception {
		String sql = usuarioId == null
				? "SELECT count(*) FROM logs_auditoria WHERE usuario_id IS NULL"
				: "SELECT count(*) FROM logs_auditoria WHERE usuario_id = ?";
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			if (usuarioId != null) statement.setObject(1, usuarioId);
			try (var result = statement.executeQuery()) { result.next(); return result.getInt(1); }
		}
	}

	private boolean systemEventHasGeneratedCorrelation(UUID correlationId) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"SELECT endpoint, correlation_id FROM logs_auditoria WHERE tipo_evento = 'ADMIN_INICIAL_CRIADO' AND correlation_id = ?")) {
			statement.setObject(1, correlationId);
			try (var result = statement.executeQuery()) {
				return result.next() && result.getString("endpoint") == null
						&& correlationId.equals(result.getObject("correlation_id", UUID.class));
			}
		}
	}

	private boolean eventExists(UUID correlationId) {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"SELECT 1 FROM logs_auditoria WHERE correlation_id = ?")) {
			statement.setObject(1, correlationId);
			try (var result = statement.executeQuery()) { return result.next(); }
		} catch (Exception exception) {
			throw new AssertionError(exception);
		}
	}

	private String persistedAuditMetadata(UUID correlationId) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"SELECT id::text, usuario_id::text, tipo_evento, resultado, severidade, endpoint, correlation_id::text, data_hora::text "
						+ "FROM logs_auditoria WHERE correlation_id = ?")) {
			statement.setObject(1, correlationId);
			try (var result = statement.executeQuery()) {
				assertThat(result.next()).isTrue();
				return String.join("|", String.valueOf(result.getString(1)), String.valueOf(result.getString(2)),
						String.valueOf(result.getString(3)), String.valueOf(result.getString(4)),
						String.valueOf(result.getString(5)), String.valueOf(result.getString(6)),
						String.valueOf(result.getString(7)), String.valueOf(result.getString(8)));
			}
		}
	}

	private PasswordHasher noOpPasswordHasher() {
		return new PasswordHasher() {
			@Override public String hash(String password) { return "HASH_SENTINEL"; }
			@Override public boolean matches(String password, String hash) { return false; }
		};
	}

	private void assertDoesNotContainSensitiveValues(String output) {
		assertThat(output).doesNotContain(SENSITIVE_SENTINELS);
	}

	private String uniqueEmail() {
		return "audit-" + UUID.randomUUID() + "@example.test";
	}
}
