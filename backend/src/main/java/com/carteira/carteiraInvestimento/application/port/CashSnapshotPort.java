package com.carteira.carteiraInvestimento.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CashSnapshotPort {
	void upsert(UUID walletId, LocalDate referenceDate, BigDecimal resultingBalance);
}
