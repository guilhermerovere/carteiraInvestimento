package com.carteira.carteiraInvestimento.application.service;

public class QuoteIntegrationException extends RuntimeException {
	public QuoteIntegrationException(String message) { super(message); }
	public QuoteIntegrationException(String message, Throwable cause) { super(message, cause); }
}
