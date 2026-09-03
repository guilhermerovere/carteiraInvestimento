package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.application.service.RegistrationService;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"application.security.jwt.secret-key=01234567890123456789012345678901",
		"application.security.jwt.expiration-hours=24",
		"application.security.jwt.issuer=carteira-investimento-backend",
		"application.security.jwt.audience=carteira-investimento-api",
		"application.bootstrap.admin.name=Integration Admin",
		"application.bootstrap.admin.email=admin.concurrency@example.test",
		"application.bootstrap.admin.password=Valid@123"
})
class RegistrationConcurrencyIntegrationTest extends PostgreSqlContainerSupport {

	@Autowired private RegistrationService registration;
	@Autowired private DataSource dataSource;

	@Test
	void concurrentRegistrationCreatesOneUserAndOneZeroBalancePrimaryWallet() throws Exception {
		String email = "concurrent-" + UUID.randomUUID() + "@example.test";
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
			Future<Boolean> first = executor.submit(() -> registerConcurrently(email, ready, start));
			Future<Boolean> second = executor.submit(() -> registerConcurrently(email, ready, start));
			ready.await();
			start.countDown();

			assertThat(first.get()).isNotEqualTo(second.get());
		}

		assertThat(count("SELECT count(*) FROM usuarios WHERE email = ?", email)).isEqualTo(1);
		assertThat(count("SELECT count(*) FROM carteiras c JOIN usuarios u ON u.id = c.usuario_id WHERE u.email = ?", email))
				.isEqualTo(1);
		assertThat(walletBalance(email)).isEqualByComparingTo(BigDecimal.ZERO);
	}

	private boolean registerConcurrently(String email, CountDownLatch ready, CountDownLatch start) throws Exception {
		ready.countDown();
		start.await();
		try {
			registration.register("Concurrent User", email, "Valid@123", UUID.randomUUID());
			return true;
		} catch (DuplicateEmailException exception) {
			return false;
		}
	}

	private int count(String sql, String email) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, email);
			try (var result = statement.executeQuery()) { result.next(); return result.getInt(1); }
		}
	}

	private BigDecimal walletBalance(String email) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"SELECT c.saldo_caixa_brl FROM carteiras c JOIN usuarios u ON u.id = c.usuario_id WHERE u.email = ?")) {
			statement.setString(1, email);
			try (var result = statement.executeQuery()) { assertThat(result.next()).isTrue(); return result.getBigDecimal(1); }
		}
	}
}
