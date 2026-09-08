package com.carteira.carteiraInvestimento.presentation.asset;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public final class UpdateAtivoLifecycleRequest {
	@NotNull
	private final Boolean ativo;

	@JsonCreator
	public UpdateAtivoLifecycleRequest(@JsonProperty("ativo") Boolean ativo) {
		this.ativo = ativo;
	}

	@JsonAnySetter
	public void rejectUnknown(String field, Object value) {
		throw new IllegalArgumentException("unknown field");
	}

	public Boolean ativo() { return ativo; }
}
