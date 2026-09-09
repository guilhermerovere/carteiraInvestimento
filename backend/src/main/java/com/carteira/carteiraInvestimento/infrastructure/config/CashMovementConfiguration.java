package com.carteira.carteiraInvestimento.infrastructure.config;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort;
import com.carteira.carteiraInvestimento.application.port.CashLedgerPort;
import com.carteira.carteiraInvestimento.application.port.CashSnapshotPort;
import com.carteira.carteiraInvestimento.application.port.CashWalletPort;
import com.carteira.carteiraInvestimento.application.service.CashMovementApplicationService;
import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CashMovementConfiguration {
	@Bean
	CashMovementUseCase cashMovementUseCase(CashWalletPort wallets, CashLedgerPort ledger,
			CashIdempotencyPort idempotency, CashSnapshotPort snapshots, AuditoriaPort audit, Clock clock,
			@Value("${application.cash-movements.snapshot-zone:America/Sao_Paulo}") String snapshotZone) {
		return new CashMovementApplicationService(wallets, ledger, idempotency, snapshots, audit, clock,
				ZoneId.of(snapshotZone));
	}
}
