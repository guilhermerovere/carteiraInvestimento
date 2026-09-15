package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.CarteiraInvestimentoApplication;
import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CarteiraPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.RegistrationService;
import com.carteira.carteiraInvestimento.infrastructure.persistence.AuditoriaPersistenceAdapter;
import com.carteira.carteiraInvestimento.infrastructure.persistence.IdentityPersistenceAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(classes = { CarteiraInvestimentoApplication.class, RegistrationTransactionIntegrationTest.FaultInjectionConfiguration.class }, properties = {
		"application.security.jwt.secret-key=01234567890123456789012345678901",
		"application.security.jwt.expiration-hours=24",
		"application.security.jwt.issuer=carteira-investimento-backend",
		"application.security.jwt.audience=carteira-investimento-api",
		"application.bootstrap.admin.name=Integration Admin",
		"application.bootstrap.admin.email=admin.registration@example.test",
		"application.bootstrap.admin.password=Valid@123"
})
class RegistrationTransactionIntegrationTest extends PostgreSqlContainerSupport {

	@Autowired private RegistrationService registration;
	@Autowired private FaultSwitches failures;
	@Autowired private DataSource dataSource;

	@BeforeEach
	void resetFailures() {
		failures.wallet = false;
		failures.audit = false;
	}

	@Test
	void rollsBackUserWalletAndAuditWhenWalletPersistenceFails() throws Exception {
		String email = uniqueEmail();
		UUID correlationId = UUID.randomUUID();
		failures.wallet = true;

		assertThatThrownBy(() -> registration.register("Transaction User", email, "Valid@123", correlationId))
				.isInstanceOf(IllegalStateException.class);

		assertNoRegistrationArtifacts(email, correlationId);
	}

	@Test
	void rollsBackUserWalletAndAuditWhenAuditPersistenceFails() throws Exception {
		String email = uniqueEmail();
		UUID correlationId = UUID.randomUUID();
		failures.audit = true;

		assertThatThrownBy(() -> registration.register("Transaction User", email, "Valid@123", correlationId))
				.isInstanceOf(IllegalStateException.class);

		assertNoRegistrationArtifacts(email, correlationId);
	}

	private void assertNoRegistrationArtifacts(String email, UUID correlationId) throws Exception {
		assertThat(count("SELECT count(*) FROM usuarios WHERE email = ?", email)).isZero();
		assertThat(count("SELECT count(*) FROM carteiras c JOIN usuarios u ON u.id = c.usuario_id WHERE u.email = ?", email)).isZero();
		assertThat(count("SELECT count(*) FROM logs_auditoria WHERE correlation_id = ?", correlationId)).isZero();
	}

	private int count(String sql, Object parameter) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setObject(1, parameter);
			try (var result = statement.executeQuery()) { result.next(); return result.getInt(1); }
		}
	}

	private String uniqueEmail() {
		return "registration-" + UUID.randomUUID() + "@example.test";
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class FaultInjectionConfiguration {
		@Bean FaultSwitches faultSwitches() { return new FaultSwitches(); }

		@Bean @Primary
		CarteiraPort failingCarteiraPort(IdentityPersistenceAdapter delegate, FaultSwitches failures) {
			return carteira -> {
				if (failures.wallet) throw new IllegalStateException("wallet persistence failure");
				return delegate.save(carteira);
			};
		}

		@Bean @Primary
		AuditoriaPort failingAuditoriaPort(AuditoriaPersistenceAdapter delegate, FaultSwitches failures) {
			return event -> {
				if (failures.audit) throw new IllegalStateException("audit persistence failure");
				delegate.record(event);
			};
		}
	}

	static class FaultSwitches {
		private boolean wallet;
		private boolean audit;
	}
}
