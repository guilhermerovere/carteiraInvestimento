package com.carteira.carteiraInvestimento.presentation.wallet;

import com.carteira.carteiraInvestimento.application.port.CashMovementPage;
import java.util.List;

public record CashMovementHistoryResponse(List<CashMovementResponse.Movement> items, int page, int size,
		long totalElements, int totalPages) {
	static CashMovementHistoryResponse from(CashMovementPage result) {
		return new CashMovementHistoryResponse(result.items().stream().map(CashMovementResponse.Movement::from).toList(),
				result.page(), result.size(), result.totalElements(), result.totalPages());
	}
}
