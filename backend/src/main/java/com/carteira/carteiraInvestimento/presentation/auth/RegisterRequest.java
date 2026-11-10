package com.carteira.carteiraInvestimento.presentation.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank @Size(max = 255) String nome,
		@NotBlank @Email String email,
		@NotBlank String senha) {

	public RegisterRequest {
		if (nome != null) {
			nome = nome.trim();
		}
	}
}
