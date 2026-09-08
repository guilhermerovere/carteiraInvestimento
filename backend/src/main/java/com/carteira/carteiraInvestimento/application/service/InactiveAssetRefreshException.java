package com.carteira.carteiraInvestimento.application.service;

public class InactiveAssetRefreshException extends RuntimeException {
	public InactiveAssetRefreshException() { super("inactive asset cannot be refreshed"); }
}
