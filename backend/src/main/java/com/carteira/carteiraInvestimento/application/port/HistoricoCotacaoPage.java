package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import java.util.List;

public record HistoricoCotacaoPage(List<Cotacao> items, int page, int size, long totalElements, int totalPages) {
	public HistoricoCotacaoPage {
		items = List.copyOf(items);
	}
}
