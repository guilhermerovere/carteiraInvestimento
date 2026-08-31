package com.carteira.carteiraInvestimento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class MissingDatabaseConfigurationTest {

	@Test
	void missingRequiredDatabaseUrlFailsInsteadOfUsingAnEmbeddedDatabase() {
		SpringApplication application = new SpringApplication(CarteiraInvestimentoApplication.class);
		ConfigurableApplicationContext[] context = new ConfigurableApplicationContext[1];

		Throwable failure = catchThrowable(() -> context[0] = application.run(
				"--spring.main.web-application-type=none",
				"--spring.main.banner-mode=off",
				"--spring.datasource.url=${REQUIRED_TEST_DB_URL}"));

		if (context[0] != null) {
			context[0].close();
		}
		assertThat(failure)
				.isNotNull()
				.hasRootCauseMessage("Driver org.postgresql.Driver claims to not accept jdbcUrl, ${REQUIRED_TEST_DB_URL}");
		assertThatThrownBy(() -> Class.forName("org.h2.Driver"))
				.isInstanceOf(ClassNotFoundException.class);
	}
}
