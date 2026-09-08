package com.carteira.carteiraInvestimento.presentation.asset;

import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import java.time.Instant;
import java.util.UUID;

public record AtivoResponse(UUID id, String ticker, String nome, TipoAtivo tipo, Mercado mercado, Moeda moeda,
		boolean ativo, Instant criadoEm, Instant atualizadoEm) {
	public static AtivoResponse from(Ativo ativo) {
		return new AtivoResponse(ativo.id(), ativo.ticker(), ativo.nome(), ativo.tipo(), ativo.mercado(), ativo.moeda(),
				ativo.ativo(), ativo.criadoEm(), ativo.atualizadoEm());
	}
}
