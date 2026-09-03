package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.service.InitialAdminBootstrapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;

@SpringBootTest
class InitialAdminBootstrapIntegrationTest extends PostgreSqlContainerSupport {
	@Autowired private JdbcTemplate jdbc;
	@Autowired private InitialAdminBootstrapService bootstrap;

	@Test
	void provisionsAnActiveAdminWithoutWalletAndWithAuditOnStartup() {
		var admin = jdbc.queryForMap("select id, role, ativo, senha_hash from usuarios where email = ?", "admin-test@example.test");
		assertThat(admin.get("role")).isEqualTo("ROLE_ADMIN");
		assertThat(admin.get("ativo")).isEqualTo(true);
		assertThat((String) admin.get("senha_hash")).startsWith("$2");
		assertThat(jdbc.queryForObject("select count(*) from carteiras where usuario_id = ?", Integer.class, admin.get("id"))).isZero();
		assertThat(jdbc.queryForObject("select count(*) from logs_auditoria where usuario_id = ?", Integer.class, admin.get("id"))).isPositive();
	}

	@Test
	void aSecondBootstrapPreservesTheSingleAdminRoleAndOriginalHash() {
		String originalHash = jdbc.queryForObject("select senha_hash from usuarios where email = ?", String.class,
				"admin-test@example.test");

		bootstrap.provision("Outro nome", " ADMIN-TEST@example.test ", "Different@2026Password");

		assertThat(jdbc.queryForObject("select count(*) from usuarios where email = ?", Integer.class,
				"admin-test@example.test")).isEqualTo(1);
		assertThat(jdbc.queryForObject("select role from usuarios where email = ?", String.class,
				"admin-test@example.test")).isEqualTo("ROLE_ADMIN");
		assertThat(jdbc.queryForObject("select senha_hash from usuarios where email = ?", String.class,
				"admin-test@example.test")).isEqualTo(originalHash);
		assertThat(jdbc.queryForObject("select count(*) from carteiras", Integer.class)).isZero();
	}

	@Test
	void rejectsAnAdminEmailThatAlreadyBelongsToAUserWithoutPromotingIt() {
		jdbc.update("insert into usuarios (id, nome, email, senha_hash, role, ativo, criado_em, atualizado_em) "
				+ "values (?, ?, ?, ?, 'ROLE_USER', true, now(), now())", UUID.randomUUID(), "Usuario",
				"user-conflict@example.test", "$2a$12$abcdefghijklmnopqrstuvabcdefghijklmnopqrstuvabcdefghijklmn");

		assertThatThrownBy(() -> bootstrap.provision("Administrador", "user-conflict@example.test", "Admin@2026Secure"))
				.isInstanceOf(IllegalStateException.class);
		assertThat(jdbc.queryForObject("select role from usuarios where email = ?", String.class,
				"user-conflict@example.test")).isEqualTo("ROLE_USER");
		assertThat(jdbc.queryForObject("select count(*) from usuarios where email = ?", Integer.class,
				"user-conflict@example.test")).isEqualTo(1);
	}
}
