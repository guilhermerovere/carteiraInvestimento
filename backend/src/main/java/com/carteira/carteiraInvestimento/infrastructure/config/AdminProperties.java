package com.carteira.carteiraInvestimento.infrastructure.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.bootstrap.admin")
public record AdminProperties(
		@NotBlank String name,
		@NotBlank @Email String email,
		@NotBlank String password) {
}
