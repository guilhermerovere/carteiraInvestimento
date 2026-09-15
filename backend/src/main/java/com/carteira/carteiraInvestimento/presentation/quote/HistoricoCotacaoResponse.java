package com.carteira.carteiraInvestimento.presentation.quote;

import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage;
import java.util.List;

public record HistoricoCotacaoResponse(List<CotacaoResponse> items, int page, int size,
		long totalElements, int totalPages) {
	public static HistoricoCotacaoResponse from(HistoricoCotacaoPage page) {
		return new HistoricoCotacaoResponse(page.items().stream().map(CotacaoResponse::from).toList(),
				page.page(), page.size(), page.totalElements(), page.totalPages());
	}
}
