package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.identity.Usuario;

public interface AccessTokenIssuer {
	IssuedAccessToken issue(Usuario usuario);

	record IssuedAccessToken(String value, long expiresInSeconds) {
	}
}
