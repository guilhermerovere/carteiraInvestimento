package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.junit.jupiter.api.Test;

class FlywayValidationIT extends PostgreSqlContainerSupport {

	@Test
	void rejectsAnIncompatibleMigrationHistory() {
		Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.locations("classpath:db/negative/initial")
				.load()
				.migrate();

		Flyway incompatible = Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.locations("classpath:db/negative/incompatible")
				.validateOnMigrate(true)
				.load();

		assertThatThrownBy(incompatible::migrate)
				.isInstanceOf(FlywayValidateException.class)
				.hasMessageContaining("checksum");
	}
}

