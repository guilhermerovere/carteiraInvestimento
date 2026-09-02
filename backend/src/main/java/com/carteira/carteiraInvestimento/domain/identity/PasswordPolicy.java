package com.carteira.carteiraInvestimento.domain.identity;

public final class PasswordPolicy {

	private PasswordPolicy() {
	}

	public static void validate(String password) {
		if (password == null || password.length() < 8
				|| !password.matches(".*[A-Z].*")
				|| !password.matches(".*[a-z].*")
				|| !password.matches(".*\\d.*")
				|| !password.matches(".*[^A-Za-z0-9].*")) {
			throw new IllegalArgumentException("password does not meet the required policy");
		}
	}
}
