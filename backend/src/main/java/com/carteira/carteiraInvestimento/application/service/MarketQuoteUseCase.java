package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import java.util.UUID;

public interface MarketQuoteUseCase {
	Cotacao cotacaoAtual(UUID ativoId);
	Cotacao atualizarCotacao(UUID ativoId);
	HistoricoCotacaoPage historico(UUID ativoId, int page, int size, boolean admin);
}
