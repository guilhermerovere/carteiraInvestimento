package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;

public interface HistoricoCambioPort {
	ObservacaoCambio save(ObservacaoCambio observacao);
}
