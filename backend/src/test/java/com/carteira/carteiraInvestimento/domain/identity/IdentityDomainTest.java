package com.carteira.carteiraInvestimento.domain.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class IdentityDomainTest {

	@Test
	void canonicalizesTrimmedLowercaseEmailAndKeepsOnlySupportedRoles() {
		assertThat(EmailCanonicalizer.canonicalize(" User@Example.TEST ")).isEqualTo("user@example.test");
		assertThat(Role.values()).containsExactly(Role.ROLE_USER, Role.ROLE_ADMIN);
	}

	@Test
	void enforcesThePasswordPolicy() {
		PasswordPolicy.validate("Valid@123");
		assertThatThrownBy(() -> PasswordPolicy.validate("short"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> PasswordPolicy.validate("lowercase@123"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
