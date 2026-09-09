package com.carteira.carteiraInvestimento.application.service;

public class CashConflictException extends RuntimeException {
	public CashConflictException(String message) {
		super(message);
	}
}
