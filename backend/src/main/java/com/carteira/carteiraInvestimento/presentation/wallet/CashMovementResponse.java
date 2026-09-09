package com.carteira.carteiraInvestimento.presentation.wallet;

import com.carteira.carteiraInvestimento.application.service.CashOperationResult;
import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashMovementResponse(Movement movimentacao, BigDecimal saldoResultante) {
	public record Movement(UUID id, TipoMovimentacaoCaixa tipo, BigDecimal valorBrl, String descricao,
			Instant dataHora) {
		static Movement from(MovimentacaoCaixa movement) {
			return new Movement(movement.id(), movement.tipo(), movement.valorBrl(), movement.descricao(),
					movement.dataHora());
		}
	}

	static CashMovementResponse from(CashOperationResult result) {
		return new CashMovementResponse(Movement.from(result.movement()), result.resultingBalance());
	}
}
