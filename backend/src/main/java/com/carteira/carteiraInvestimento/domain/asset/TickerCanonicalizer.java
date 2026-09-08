package com.carteira.carteiraInvestimento.domain.asset;

import java.util.Locale;

public final class TickerCanonicalizer {
	private TickerCanonicalizer() {
	}

	public static String canonicalize(String ticker) {
		if (ticker == null) {
			throw new IllegalArgumentException("ticker must not be null");
		}
		String canonical = ticker.trim().toUpperCase(Locale.ROOT);
		if (canonical.isEmpty()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
		return canonical;
	}

	public static String canonicalizeForLookup(String ticker) {
		String canonical = canonicalize(ticker);
		if (!Mercado.B3.aceitaTicker(canonical) && !Mercado.US.aceitaTicker(canonical)) {
			throw new IllegalArgumentException("ticker format is invalid");
		}
		return canonical;
	}
}
