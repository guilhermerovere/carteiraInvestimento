package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import java.util.List;

public record AtivoPage(List<Ativo> items, int page, int size, long totalElements, int totalPages) {
	public AtivoPage {
		items = List.copyOf(items);
	}
}
