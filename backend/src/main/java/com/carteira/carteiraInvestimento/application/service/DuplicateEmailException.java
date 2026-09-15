package com.carteira.carteiraInvestimento.application.service;

public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException() {
		super("duplicate email");
	}

	public DuplicateEmailException(Throwable cause) {
		super("duplicate email", cause);
	}
}
