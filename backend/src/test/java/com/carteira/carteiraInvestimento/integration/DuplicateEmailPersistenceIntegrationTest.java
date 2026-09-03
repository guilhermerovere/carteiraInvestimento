package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.domain.identity.EmailCanonicalizer;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.persistence.IdentityPersistenceAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DuplicateEmailPersistenceIntegrationTest extends PostgreSqlContainerSupport {

	@Autowired
	private IdentityPersistenceAdapter identityPersistenceAdapter;

	@Autowired
	private javax.sql.DataSource dataSource;

	@Test
	void translatesDatabaseUniqueViolationAndPersistsOnlyOneCanonicalIdentity() throws Exception {
		String email = "duplicate-" + UUID.randomUUID() + "@example.test";
		identityPersistenceAdapter.save(Usuario.novoUsuario("First", EmailCanonicalizer.canonicalize(email), "hash", Role.ROLE_USER));

		assertThatThrownBy(() -> identityPersistenceAdapter.save(Usuario.novoUsuario("Second",
				EmailCanonicalizer.canonicalize("  " + email.toUpperCase() + "  "), "hash", Role.ROLE_USER)))
				.isInstanceOf(DuplicateEmailException.class);

		assertThat(countUsersByEmail(EmailCanonicalizer.canonicalize(email))).isEqualTo(1);
	}

	@Test
	void concurrentCanonicalEmailVariationsCreateOnlyOneIdentity() throws Exception {
		String email = "concurrent-" + UUID.randomUUID() + "@example.test";
		CyclicBarrier start = new CyclicBarrier(2);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<Throwable>> outcomes = List.of(
					executor.submit(() -> saveAfterBarrier(start, "First", email)),
					executor.submit(() -> saveAfterBarrier(start, "Second", "  " + email.toUpperCase() + "  ")));

			List<Throwable> failures = outcomes.stream().map(this::resultOf).toList();
			assertThat(failures).filteredOn(java.util.Objects::isNull).hasSize(1);
			assertThat(failures).filteredOn(DuplicateEmailException.class::isInstance).hasSize(1);
			assertThat(countUsersByEmail(EmailCanonicalizer.canonicalize(email))).isEqualTo(1);
		} finally {
			executor.shutdownNow();
		}
	}

	private Throwable saveAfterBarrier(CyclicBarrier start, String name, String email) {
		try {
			start.await();
			identityPersistenceAdapter.save(Usuario.novoUsuario(name, EmailCanonicalizer.canonicalize(email), "hash", Role.ROLE_USER));
			return null;
		} catch (Throwable failure) {
			return failure;
		}
	}

	private Throwable resultOf(Future<Throwable> future) {
		try {
			return future.get();
		} catch (Exception failure) {
			return failure;
		}
	}

	private long countUsersByEmail(String canonicalEmail) throws Exception {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE email = ?")) {
			statement.setString(1, canonicalEmail);
			try (var resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getLong(1);
			}
		}
	}
}
