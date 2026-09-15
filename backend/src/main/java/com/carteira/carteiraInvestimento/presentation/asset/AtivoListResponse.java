package com.carteira.carteiraInvestimento.presentation.asset;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import java.util.List;

public record AtivoListResponse(List<AtivoResponse> items, int page, int size, long totalElements, int totalPages) {
	public static AtivoListResponse from(AtivoPage result) {
		return new AtivoListResponse(result.items().stream().map(AtivoResponse::from).toList(), result.page(),
				result.size(), result.totalElements(), result.totalPages());
	}
}
