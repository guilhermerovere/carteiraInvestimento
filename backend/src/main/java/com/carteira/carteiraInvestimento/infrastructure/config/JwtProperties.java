package com.carteira.carteiraInvestimento.infrastructure.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.security.jwt")
public record JwtProperties(
		@NotBlank String secretKey,
		@NotNull @Positive Integer expirationHours,
		@NotBlank String issuer,
		@NotBlank String audience) {

	@AssertTrue(message = "JWT_SECRET_KEY must contain at least 32 UTF-8 bytes")
	public boolean hasMinimumSecretLength() {
		return secretKey != null && secretKey.getBytes(StandardCharsets.UTF_8).length >= 32;
	}
}
