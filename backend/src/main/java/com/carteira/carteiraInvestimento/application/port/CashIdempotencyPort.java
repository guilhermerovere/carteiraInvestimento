package com.carteira.carteiraInvestimento.application.port;

import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface CashIdempotencyPort {
	sealed interface Reservation permits NewReservation, ExistingReservation { }

	record NewReservation(UUID id) implements Reservation { }

	record ExistingReservation(String fingerprint, MovimentacaoCaixa movement, BigDecimal resultingBalance)
			implements Reservation { }

	Reservation reserve(UUID walletId, TipoMovimentacaoCaixa type, String key, String fingerprint, Instant createdAt);

	void complete(UUID reservationId, UUID movementId, BigDecimal resultingBalance);
}
