package com.carteira.carteiraInvestimento.infrastructure.security;

import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class PersistedUserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
	private final UsuarioPort usuarios;

	public PersistedUserJwtAuthenticationConverter(UsuarioPort usuarios) {
		this.usuarios = usuarios;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Usuario usuario = usuarios.findById(subject(jwt)).orElseThrow(this::invalidToken);
		if (!usuario.ativo() || !usuario.role().name().equals(jwt.getClaimAsString("role"))) {
			throw invalidToken();
		}
		return new JwtAuthenticationToken(jwt,
				org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(usuario.role().name()),
				usuario.id().toString());
	}

	private UUID subject(Jwt jwt) {
		try {
			return UUID.fromString(jwt.getSubject());
		}
		catch (IllegalArgumentException | NullPointerException exception) {
			throw invalidToken();
		}
	}

	private InvalidBearerTokenException invalidToken() {
		return new InvalidBearerTokenException("Invalid bearer token");
	}
}
