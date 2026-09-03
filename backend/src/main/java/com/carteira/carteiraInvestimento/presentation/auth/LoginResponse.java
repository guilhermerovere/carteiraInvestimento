package com.carteira.carteiraInvestimento.presentation.auth;

import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer.IssuedAccessToken;

public record LoginResponse(
		String accessToken,
		String tokenType,
		long expiresIn) {

	public static LoginResponse from(IssuedAccessToken token) {
		return new LoginResponse(token.value(), "Bearer", token.expiresInSeconds());
	}
}
