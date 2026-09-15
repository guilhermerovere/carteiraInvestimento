package com.carteira.carteiraInvestimento.application.port;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CashWalletPort {
	record Wallet(UUID id, UUID usuarioId, BigDecimal balance) { }

	Optional<Wallet> findPrimaryByUserId(UUID userId);

	void lockActiveUser(UUID userId);

	BigDecimal balance(UUID walletId);

	Optional<BigDecimal> credit(UUID walletId, BigDecimal amount);

	Optional<BigDecimal> debit(UUID walletId, BigDecimal amount);
}
