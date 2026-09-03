package com.carteira.carteiraInvestimento.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer.IssuedAccessToken;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.config.JwtConfiguration;
import com.carteira.carteiraInvestimento.infrastructure.config.JwtProperties;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class JwtAccessTokenIssuerTest {
	private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	private static final JwtProperties PROPERTIES = new JwtProperties(
			"01234567890123456789012345678901", 24,
			"carteira-investimento-backend", "carteira-investimento-api");

	private final JwtConfiguration configuration = new JwtConfiguration();
	private final JwtEncoder encoder = configuration.jwtEncoder(PROPERTIES);
	private final JwtDecoder decoder = configuration.jwtDecoder(PROPERTIES);
	private final JwtAccessTokenIssuer issuer = new JwtAccessTokenIssuer(encoder, PROPERTIES,
			Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void issuesHs256TokenWithAllRequiredClaims() throws Exception {
		Usuario usuario = usuario();

		IssuedAccessToken issued = issuer.issue(usuario);

		assertThat(issued.expiresInSeconds()).isEqualTo(24 * 60 * 60);
		assertThat(SignedJWT.parse(issued.value()).getHeader().getAlgorithm().getName()).isEqualTo("HS256");
		var jwt = decoder.decode(issued.value());
		assertThat(jwt.getSubject()).isEqualTo(usuario.id().toString());
		assertThat(jwt.getClaimAsString("role")).isEqualTo(Role.ROLE_USER.name());
		assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
		assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plusSeconds(24 * 60 * 60));
		assertThat(jwt.getId()).isNotBlank();
		assertThat(jwt.getClaimAsString("iss")).isEqualTo(PROPERTIES.issuer());
		assertThat(jwt.getAudience()).containsExactly(PROPERTIES.audience());
	}

	@Test
	void rejectsTokensWithInvalidSignatureIssuerAudienceMalformedOrExpiredClaims() {
		JwtProperties anotherSecret = new JwtProperties("abcdefghijklmnopqrstuvwxyz123456", 24,
			PROPERTIES.issuer(), PROPERTIES.audience());
		JwtEncoder wrongSecretEncoder = configuration.jwtEncoder(anotherSecret);
		assertThatThrownBy(() -> decoder.decode(token(wrongSecretEncoder, PROPERTIES.issuer(), PROPERTIES.audience(), NOW.plusSeconds(60))))
				.isInstanceOf(BadJwtException.class);
		assertThatThrownBy(() -> decoder.decode(token(encoder, "another-issuer", PROPERTIES.audience(), NOW.plusSeconds(60))))
				.isInstanceOf(BadJwtException.class);
		assertThatThrownBy(() -> decoder.decode(token(encoder, PROPERTIES.issuer(), "another-audience", NOW.plusSeconds(60))))
				.isInstanceOf(BadJwtException.class);
		assertThatThrownBy(() -> decoder.decode("not-a-jwt")).isInstanceOf(BadJwtException.class);
		assertThatThrownBy(() -> decoder.decode(token(encoder, PROPERTIES.issuer(), PROPERTIES.audience(), Instant.EPOCH)))
				.isInstanceOf(BadJwtException.class);
	}

	private String token(JwtEncoder source, String issuerValue, String audience, Instant expiresAt) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(UUID.randomUUID().toString())
				.claim("role", Role.ROLE_USER.name())
				.issuedAt(expiresAt.isBefore(NOW) ? expiresAt.minusSeconds(60) : NOW)
				.expiresAt(expiresAt)
				.id(UUID.randomUUID().toString())
				.issuer(issuerValue)
				.audience(List.of(audience))
				.build();
		return source.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
	}

	private Usuario usuario() {
		return new Usuario(UUID.randomUUID(), "Usuario", "usuario@example.test", "hash", Role.ROLE_USER,
				true, NOW, NOW);
	}
}
