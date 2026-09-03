package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration(proxyBeanMethods = false)
public class JwtConfiguration {

	@Bean
	public Clock jwtClock() {
		return Clock.systemUTC();
	}

	@Bean
	public JwtEncoder jwtEncoder(JwtProperties properties) {
		return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey(properties)));
	}

	@Bean
	public JwtDecoder jwtDecoder(JwtProperties properties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(properties))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.issuer()),
				audienceValidator(properties.audience()),
				requiredClaimsValidator()));
		return decoder;
	}

	private static SecretKeySpec secretKey(JwtProperties properties) {
		return new SecretKeySpec(properties.secretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	private static OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
		return jwt -> jwt.getAudience().contains(audience)
				? OAuth2TokenValidatorResult.success()
				: invalidToken("JWT audience is invalid");
	}

	private static OAuth2TokenValidator<Jwt> requiredClaimsValidator() {
		return jwt -> {
			if (!isUuid(jwt.getSubject())) {
				return invalidToken("JWT subject is invalid");
			}
			if (!isRole(jwt.getClaimAsString("role"))) {
				return invalidToken("JWT role is invalid");
			}
			if (jwt.getIssuedAt() == null || jwt.getExpiresAt() == null
					|| jwt.getId() == null || jwt.getId().isBlank()) {
				return invalidToken("JWT required claims are missing");
			}
			return OAuth2TokenValidatorResult.success();
		};
	}

	private static boolean isUuid(String value) {
		try {
			java.util.UUID.fromString(value);
			return true;
		}
		catch (IllegalArgumentException | NullPointerException exception) {
			return false;
		}
	}

	private static boolean isRole(String value) {
		try {
			Role.valueOf(value);
			return true;
		}
		catch (IllegalArgumentException | NullPointerException exception) {
			return false;
		}
	}

	private static OAuth2TokenValidatorResult invalidToken(String description) {
		return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", description, null));
	}
}
