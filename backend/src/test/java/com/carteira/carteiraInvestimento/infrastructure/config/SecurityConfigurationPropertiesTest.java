package com.carteira.carteiraInvestimento.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class SecurityConfigurationPropertiesTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(PropertiesConfiguration.class)
			.withPropertyValues(
					"application.security.jwt.secret-key=01234567890123456789012345678901",
					"application.security.jwt.expiration-hours=24",
					"application.security.jwt.issuer=carteira-investimento-backend",
					"application.security.jwt.audience=carteira-investimento-api",
					"application.bootstrap.admin.name=Administrador do Sistema",
					"application.bootstrap.admin.email=admin@example.test",
					"application.bootstrap.admin.password=Admin@2026Secure");

	@Test
	void startsWithValidJwtAndAdminProperties() {
		contextRunner.run(context -> assertThat(context).hasNotFailed());
	}

	@Test
	void failsForShortJwtSecret() {
		contextRunner.withPropertyValues("application.security.jwt.secret-key=short")
				.run(context -> assertThat(context).hasFailed());
	}

	@Test
	void failsForNonPositiveExpiration() {
		contextRunner.withPropertyValues("application.security.jwt.expiration-hours=0")
				.run(context -> assertThat(context).hasFailed());
	}

	@Test
	void failsWhenAdminConfigurationIsPartialOrInvalid() {
		contextRunner.withPropertyValues("application.bootstrap.admin.password=")
				.run(context -> assertThat(context).hasFailed());
		contextRunner.withPropertyValues("application.bootstrap.admin.email=not-an-email")
				.run(context -> assertThat(context).hasFailed());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties({JwtProperties.class, AdminProperties.class})
	static class PropertiesConfiguration {

		@Bean
		static LocalValidatorFactoryBean configurationPropertiesValidator() {
			return new LocalValidatorFactoryBean();
		}
	}
}
