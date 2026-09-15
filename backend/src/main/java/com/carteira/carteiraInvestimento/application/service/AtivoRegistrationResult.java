package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.domain.asset.Ativo;

public record AtivoRegistrationResult(Ativo ativo, String requestedTicker, boolean created) {
	public boolean renamed() { return !requestedTicker.equals(ativo.ticker()); }
}
