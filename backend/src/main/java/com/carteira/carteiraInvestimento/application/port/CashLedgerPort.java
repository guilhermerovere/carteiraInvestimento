package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import java.util.UUID;

public interface CashLedgerPort {
	void save(MovimentacaoCaixa movement);

	CashMovementPage history(UUID walletId, int page, int size);
}
