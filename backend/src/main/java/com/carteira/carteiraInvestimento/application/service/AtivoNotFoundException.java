package com.carteira.carteiraInvestimento.application.service;

public class AtivoNotFoundException extends RuntimeException {
	public AtivoNotFoundException() {
		super("asset not found");
	}
}
