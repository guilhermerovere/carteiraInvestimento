package com.carteira.carteiraInvestimento.domain.asset;

public final class NomeAtivoCanonicalizer {
	private NomeAtivoCanonicalizer() {
	}

	public static String canonicalize(String nome) {
		if (nome == null) {
			throw new IllegalArgumentException("nome must not be null");
		}
		String canonical = nome.trim();
		if (canonical.isEmpty() || canonical.length() > 160) {
			throw new IllegalArgumentException("nome must contain between 1 and 160 characters");
		}
		return canonical;
	}
}
