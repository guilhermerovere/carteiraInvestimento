package com.carteira.carteiraInvestimento.application.service;

public class FxExpiredException extends InvestmentConflictException {
	public FxExpiredException() { super("expired exchange rate"); }
}
