package com.carteira.carteiraInvestimento.application.service;

public class AssetValidationUnavailableException extends RuntimeException {
	public AssetValidationUnavailableException(String message) { super(message); }
	public AssetValidationUnavailableException(String message, Throwable cause) { super(message, cause); }
}
