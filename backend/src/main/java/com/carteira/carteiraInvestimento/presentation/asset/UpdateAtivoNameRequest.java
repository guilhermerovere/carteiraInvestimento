package com.carteira.carteiraInvestimento.presentation.asset;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UpdateAtivoNameRequest {
	@NotBlank
	@Size(max = 160)
	private final String nome;

	@JsonCreator
	public UpdateAtivoNameRequest(@JsonProperty("nome") String nome) {
		this.nome = nome;
	}

	@JsonAnySetter
	public void rejectUnknown(String field, Object value) {
		throw new IllegalArgumentException("unknown field");
	}

	public String nome() { return nome; }
}
