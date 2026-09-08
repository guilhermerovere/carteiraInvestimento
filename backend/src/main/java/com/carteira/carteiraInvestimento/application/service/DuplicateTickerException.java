package com.carteira.carteiraInvestimento.application.service;

public class DuplicateTickerException extends RuntimeException {
	public DuplicateTickerException() {
		super("ticker already exists");
	}

	public DuplicateTickerException(Throwable cause) {
		super("ticker already exists", cause);
	}
}
