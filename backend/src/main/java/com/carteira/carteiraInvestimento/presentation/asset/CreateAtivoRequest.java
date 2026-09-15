package com.carteira.carteiraInvestimento.presentation.asset;

import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@io.swagger.v3.oas.annotations.media.Schema(name = "AdminAssetCreateRequest", description = "ROLE_ADMIN: contrato administrativo existente.")
public final class CreateAtivoRequest {
	@NotBlank
	private final String ticker;
	@NotBlank
	@Size(max = 160)
	private final String nome;
	@NotNull
	private final TipoAtivo tipo;
	@NotNull
	private final Mercado mercado;

	@JsonCreator
	public CreateAtivoRequest(@JsonProperty("ticker") String ticker, @JsonProperty("nome") String nome,
			@JsonProperty("tipo") TipoAtivo tipo, @JsonProperty("mercado") Mercado mercado) {
		this.ticker = ticker;
		this.nome = nome;
		this.tipo = tipo;
		this.mercado = mercado;
	}

	@JsonAnySetter
	public void rejectUnknown(String field, Object value) {
		throw new IllegalArgumentException("unknown field");
	}

	public String ticker() { return ticker; }
	public String nome() { return nome; }
	public TipoAtivo tipo() { return tipo; }
	public Mercado mercado() { return mercado; }
}
