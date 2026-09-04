package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import com.carteira.carteiraInvestimento.infrastructure.security.PersistedUserJwtAuthenticationConverter;
import com.carteira.carteiraInvestimento.infrastructure.security.SecurityProblemDetailHandler;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import java.time.Instant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
		"application.security.jwt.secret-key=01234567890123456789012345678901",
		"application.security.jwt.expiration-hours=24",
		"application.security.jwt.issuer=carteira-investimento-backend",
		"application.security.jwt.audience=carteira-investimento-api",
		"application.bootstrap.admin.name=Integration Admin",
		"application.bootstrap.admin.email=admin.integration@example.test",
		"application.bootstrap.admin.password=Valid@123"
})
@Import(AuditSecurityEventsIntegrationTest.AccessDeniedProbeConfiguration.class)
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
	@Autowired private MockMvc mockMvc;
	@Autowired private JwtEncoder jwtEncoder;
	@MockitoSpyBean private CorrelationIdFilter correlationIdFilter;

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
		auditoria.record(new AuditoriaCommand(usuario.id(), TipoEvento.ADMIN_INICIAL_CRIADO,
				ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO, null, bootstrapCorrelationId));

		assertThat(countEventsFor(usuario.id())).isEqualTo(4);
		assertThat(countEventsFor(null)).isPositive();
		assertThat(systemEventHasGeneratedCorrelation(bootstrapCorrelationId)).isTrue();
	}

	@Test
	void persistsAccessDeniedFromMethodSecurityAuthorizationFlow() throws Exception {
		Usuario usuario = usuarios.save(Usuario.novoUsuario("Denied User", uniqueEmail(), "hash", Role.ROLE_USER));
		UUID correlationId = UUID.randomUUID();

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/test/security/admin-probe")
				.header(HttpHeaders.AUTHORIZATION, bearer(token(usuario.id(), Role.ROLE_USER)))
				.header("X-Correlation-ID", correlationId))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
						.string("X-Correlation-ID", correlationId.toString()))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("Forbidden"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value(403))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail").value("Access is denied."))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.instance")
						.value("/test/security/admin-probe"));

		assertThat(accessDeniedEvents(usuario.id(), correlationId, "/test/security/admin-probe")).isEqualTo(1);
	}

	@Test
	void persistsAccessDeniedFromSecurityFilterChainAuthorizationFlow() throws Exception {
		Usuario usuario = usuarios.save(Usuario.novoUsuario("Filter Denied User", uniqueEmail(), "hash", Role.ROLE_USER));
		UUID correlationId = UUID.randomUUID();
		clearInvocations(correlationIdFilter);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/test/security/filter-admin-probe")
				.header(HttpHeaders.AUTHORIZATION, bearer(token(usuario.id(), Role.ROLE_USER)))
				.header("X-Correlation-ID", correlationId))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
						.string("X-Correlation-ID", correlationId.toString()))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("Forbidden"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value(403))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail").value("Access is denied."))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.instance")
						.value("/test/security/filter-admin-probe"));

		assertThat(accessDeniedEvents(usuario.id(), correlationId, "/test/security/filter-admin-probe")).isEqualTo(1);
		verify(correlationIdFilter, times(1)).doFilter(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
	}

	@Test
	void returnsCorrelationIdWithoutAuditingUnauthenticatedSecurityRejection() throws Exception {
		UUID correlationId = UUID.randomUUID();

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/test/security/filter-admin-probe")
				.header("X-Correlation-ID", correlationId))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
						.string("X-Correlation-ID", correlationId.toString()))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("Unauthorized"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value(401))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail")
						.value("Authentication is required."));

		assertThat(accessDeniedEventsFor(correlationId)).isZero();
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

	private int accessDeniedEvents(UUID usuarioId, UUID correlationId, String endpoint) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"SELECT usuario_id, endpoint, resultado, severidade, correlation_id FROM logs_auditoria "
						+ "WHERE tipo_evento = 'ACESSO_NEGADO' AND usuario_id = ? AND correlation_id = ?")) {
			statement.setObject(1, usuarioId);
			statement.setObject(2, correlationId);
			try (var result = statement.executeQuery()) {
				int count = 0;
				while (result.next()) {
					count++;
					assertThat(result.getObject("usuario_id", UUID.class)).isEqualTo(usuarioId);
					assertThat(result.getString("endpoint")).isEqualTo(endpoint);
					assertThat(result.getString("resultado")).isEqualTo("NEGADO");
					assertThat(result.getString("severidade")).isEqualTo("ALERTA");
					assertThat(result.getObject("correlation_id", UUID.class)).isEqualTo(correlationId);
				}
				return count;
			}
		}
	}

	private int accessDeniedEventsFor(UUID correlationId) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"SELECT count(*) FROM logs_auditoria WHERE tipo_evento = 'ACESSO_NEGADO' AND correlation_id = ?")) {
			statement.setObject(1, correlationId);
			try (var result = statement.executeQuery()) { result.next(); return result.getInt(1); }
		}
	}

	private String token(UUID subject, Role role) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(subject.toString())
				.claim("role", role.name())
				.issuedAt(now)
				.expiresAt(now.plusSeconds(3600))
				.id(UUID.randomUUID().toString())
				.issuer("carteira-investimento-backend")
				.audience(java.util.List.of("carteira-investimento-api"))
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
	}

	private String bearer(String token) {
		return "Bearer " + token;
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

	@TestConfiguration(proxyBeanMethods = false)
	static class AccessDeniedProbeConfiguration {
		@Bean
		@Order(1)
		SecurityFilterChain filterAccessDeniedSecurityFilterChain(HttpSecurity http,
				PersistedUserJwtAuthenticationConverter persistedUserConverter,
				SecurityProblemDetailHandler securityProblems) throws Exception {
			return http
					.securityMatcher("/test/security/filter-admin-probe")
					.csrf(AbstractHttpConfigurer::disable)
					.formLogin(AbstractHttpConfigurer::disable)
					.httpBasic(AbstractHttpConfigurer::disable)
					.logout(AbstractHttpConfigurer::disable)
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("ADMIN"))
					.oauth2ResourceServer(resourceServer -> resourceServer
							.authenticationEntryPoint(securityProblems)
							.jwt(jwt -> jwt.jwtAuthenticationConverter(persistedUserConverter)))
					.exceptionHandling(exceptions -> exceptions
							.authenticationEntryPoint(securityProblems)
							.accessDeniedHandler(securityProblems))
					.build();
		}

		@Bean
		AccessDeniedProbeController accessDeniedProbeController() {
			return new AccessDeniedProbeController();
		}
	}

	@RestController
	static class AccessDeniedProbeController {
		@GetMapping("/test/security/admin-probe")
		@PreAuthorize("hasRole('ADMIN')")
		String adminOnly() {
			return "admin";
		}

		@GetMapping("/test/security/filter-admin-probe")
		String filterAdminOnly() {
			return "admin";
		}
	}
}
