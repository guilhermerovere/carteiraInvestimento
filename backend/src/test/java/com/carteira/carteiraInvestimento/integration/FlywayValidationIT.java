package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.junit.jupiter.api.Test;

class FlywayValidationIT extends PostgreSqlContainerSupport {

	private static final String POSITIVE_SCHEMA = "flyway_positive_test";
	private static final String NEGATIVE_SCHEMA = "flyway_negative_test";

	@Test
	void validatesTheIdentityMigrationSequenceAgainstPostgreSql() {
		Flyway migrations = Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.schemas(POSITIVE_SCHEMA)
				.defaultSchema(POSITIVE_SCHEMA)
				.cleanDisabled(false)
				.load();
		migrations.clean();
		migrations.migrate();

		assertThat(migrations.validateWithResult().validationSuccessful).isTrue();
		assertThat(migrations.info().current().getVersion().getVersion()).isEqualTo("8");
	}

	@Test
	void rejectsAnIncompatibleMigrationHistory() {
		Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.schemas(NEGATIVE_SCHEMA)
				.defaultSchema(NEGATIVE_SCHEMA)
				.locations("classpath:db/negative/initial")
				.load()
				.migrate();

		Flyway incompatible = Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.schemas(NEGATIVE_SCHEMA)
				.defaultSchema(NEGATIVE_SCHEMA)
				.locations("classpath:db/negative/incompatible")
				.validateOnMigrate(true)
				.load();

		assertThatThrownBy(incompatible::migrate)
				.isInstanceOf(FlywayValidateException.class)
				.hasMessageContaining("checksum");
	}
}

