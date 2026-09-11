package com.carteira.carteiraInvestimento.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

abstract class PostgreSqlContainerSupport {

	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.15-alpine3.24")
			.withDatabaseName("carteira_test")
			.withUsername("carteira_test")
			.withPassword("carteira_test");

	static {
		POSTGRES.start();
	}

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
		registry.add("application.security.jwt.secret-key", () -> "01234567890123456789012345678901");
		registry.add("application.bootstrap.admin.name", () -> "Administrador de Teste");
		registry.add("application.bootstrap.admin.email", () -> "admin-test@example.test");
		registry.add("application.bootstrap.admin.password", () -> "Admin@2026Secure");
		registry.add("application.market-quotes.brapi.url", () -> "http://127.0.0.1:1");
		registry.add("application.market-quotes.brapi.credential", () -> "");
		registry.add("application.market-quotes.alpha-vantage.url", () -> "http://127.0.0.1:1");
		registry.add("application.market-quotes.alpha-vantage.credential", () -> "");
		registry.add("application.market-quotes.twelve-data.url", () -> "http://127.0.0.1:1");
		registry.add("application.market-quotes.twelve-data.credential", () -> "");
		registry.add("application.broker-catalog.brasil-api-cnpj.url", () -> System.getProperty("broker.test.url", "http://127.0.0.1:1"));
		registry.add("application.broker-catalog.brasil-api-cvm.url", () -> System.getProperty("broker.test.url", "http://127.0.0.1:1"));
		registry.add("application.broker-catalog.via-cep.url", () -> System.getProperty("broker.test.url", "http://127.0.0.1:1"));
	}
}
