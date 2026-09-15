package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import java.util.List;

public record CashMovementPage(List<MovimentacaoCaixa> items, int page, int size, long totalElements,
		int totalPages) {
	public CashMovementPage {
		items = List.copyOf(items);
	}
}
