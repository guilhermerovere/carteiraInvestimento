package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import java.util.UUID;

public interface HistoricoCotacaoPort {
	Cotacao save(Cotacao cotacao);
	HistoricoCotacaoPage findByAtivoId(UUID ativoId, int page, int size);
}
