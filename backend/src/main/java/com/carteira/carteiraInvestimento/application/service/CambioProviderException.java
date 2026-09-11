package com.carteira.carteiraInvestimento.application.service;

public class CambioProviderException extends RuntimeException {
	private final boolean fallbackEligible;

	public CambioProviderException(boolean fallbackEligible, String message) {
		super(message);
		this.fallbackEligible = fallbackEligible;
	}

	public CambioProviderException(boolean fallbackEligible, String message, Throwable cause) {
		super(message, cause);
		this.fallbackEligible = fallbackEligible;
	}

	public boolean fallbackEligible() { return fallbackEligible; }
}
