package com.carteira.carteiraInvestimento.infrastructure.security;

import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.config.JwtProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {
	private final JwtEncoder encoder;
	private final JwtProperties properties;
	private final Clock clock;

	public JwtAccessTokenIssuer(JwtEncoder encoder, JwtProperties properties, Clock clock) {
		this.encoder = encoder;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	public IssuedAccessToken issue(Usuario usuario) {
		Instant issuedAt = clock.instant();
		Duration lifetime = Duration.ofHours(properties.expirationHours());
		Instant expiresAt = issuedAt.plus(lifetime);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(usuario.id().toString())
				.claim("role", usuario.role().name())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.id(UUID.randomUUID().toString())
				.issuer(properties.issuer())
				.audience(java.util.List.of(properties.audience()))
				.build();
		String token = encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
		return new IssuedAccessToken(token, lifetime.toSeconds());
	}
}
