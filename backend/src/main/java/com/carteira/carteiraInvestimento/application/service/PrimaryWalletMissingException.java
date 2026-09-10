package com.carteira.carteiraInvestimento.application.service;

public class PrimaryWalletMissingException extends RuntimeException {
	public PrimaryWalletMissingException() {
		super("primary wallet is unavailable");
	}
}
