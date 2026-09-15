package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.CashMovementPage;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.util.UUID;

public interface CashMovementUseCase {
	CashOperationResult execute(UUID userId, TipoMovimentacaoCaixa type, BigDecimal amount, String description,
			String idempotencyKey, UUID correlationId, String endpoint);

	BigDecimal balance(UUID userId);

	CashMovementPage history(UUID userId, int page, int size);
}
