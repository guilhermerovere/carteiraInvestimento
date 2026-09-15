package com.carteira.carteiraInvestimento.application.service;

public class AuthenticationFailedException extends RuntimeException {
	public AuthenticationFailedException() {
		super("invalid credentials");
	}
}
