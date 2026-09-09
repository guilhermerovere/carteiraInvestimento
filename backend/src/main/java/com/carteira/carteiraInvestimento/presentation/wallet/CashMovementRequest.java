package com.carteira.carteiraInvestimento.presentation.wallet;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public final class CashMovementRequest {
	private final BigDecimal valor;
	private final String descricao;

	@JsonCreator
	public CashMovementRequest(@JsonProperty("valor") BigDecimal valor,
			@JsonProperty("descricao") String descricao) {
		this.valor = valor;
		this.descricao = descricao;
	}

	public BigDecimal valor() { return valor; }
	public String descricao() { return descricao; }

	@JsonAnySetter
	public void rejectUnknown(String name, Object value) {
		throw new IllegalArgumentException("unknown cash movement field");
	}
}
