package com.carteira.carteiraInvestimento.domain.identity;

import java.util.Locale;

public final class EmailCanonicalizer {

	private EmailCanonicalizer() {
	}

	public static String canonicalize(String email) {
		if (email == null) {
			throw new IllegalArgumentException("email must not be null");
		}
		String canonical = email.trim().toLowerCase(Locale.ROOT);
		if (canonical.isBlank()) {
			throw new IllegalArgumentException("email must not be blank");
		}
		return canonical;
	}
}
