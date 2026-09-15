package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;

public record AtivoQuery(
		String q,
		TipoAtivo tipo,
		AtivoReadScope scope,
		int page,
		int size,
		AtivoSort sort,
		SortDirection direction) {

	public AtivoQuery {
		q = q == null || q.trim().isEmpty() ? null : q.trim();
		if (page < 0 || size < 1 || size > 100) {
			throw new IllegalArgumentException("invalid pagination");
		}
		if (scope == null || sort == null || direction == null) {
			throw new IllegalArgumentException("query controls must not be null");
		}
	}
}
