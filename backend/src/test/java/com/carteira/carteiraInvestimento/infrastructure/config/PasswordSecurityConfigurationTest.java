package com.carteira.carteiraInvestimento.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordSecurityConfigurationTest {
	@Test
	void usesBcryptWithStrengthTwelve() {
		PasswordEncoder encoder = new FoundationSecurityConfiguration().passwordEncoder();
		String hash = encoder.encode("Valid@123");
		assertThat(hash).startsWith("$2").contains("$12$");
		assertThat(encoder.matches("Valid@123", hash)).isTrue();
	}
}
