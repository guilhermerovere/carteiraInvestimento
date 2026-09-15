package com.carteira.carteiraInvestimento.application.service;

public class QuoteNotFoundException extends RuntimeException {
	public QuoteNotFoundException() { super("quote not found"); }
	public QuoteNotFoundException(Throwable cause) { super("quote not found", cause); }
}
