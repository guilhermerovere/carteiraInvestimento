package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort;
import com.carteira.carteiraInvestimento.application.port.CashLedgerPort;
import com.carteira.carteiraInvestimento.application.port.CashSnapshotPort;
import com.carteira.carteiraInvestimento.application.port.CashWalletPort;
import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CashMovementApplicationServiceTest {
	@Test
	void capturesOneInstantAndUsesConfiguredZoneForMovementAuditAndSnapshot() {
		UUID userId = UUID.randomUUID();
		UUID walletId = UUID.randomUUID();
		Instant instant = Instant.parse("2026-01-02T02:30:00.123456789Z");
		Instant canonicalInstant = Instant.parse("2026-01-02T02:30:00.123456Z");
		Clock clock = mock(Clock.class);
		CashWalletPort wallets = mock(CashWalletPort.class);
		CashLedgerPort ledger = mock(CashLedgerPort.class);
		CashIdempotencyPort idempotency = mock(CashIdempotencyPort.class);
		CashSnapshotPort snapshots = mock(CashSnapshotPort.class);
		AuditoriaPort audit = mock(AuditoriaPort.class);
		UUID reservationId = UUID.randomUUID();
		when(clock.instant()).thenReturn(instant);
		when(wallets.findPrimaryByUserId(userId)).thenReturn(Optional.of(
				new CashWalletPort.Wallet(walletId, userId, BigDecimal.ZERO)));
		when(wallets.credit(eq(walletId), any())).thenReturn(Optional.of(new BigDecimal("10.00")));
		when(idempotency.reserve(eq(walletId), eq(TipoMovimentacaoCaixa.DEPOSITO), eq("key-1"), any(),
				eq(canonicalInstant)))
				.thenReturn(new CashIdempotencyPort.NewReservation(reservationId));
		var service = new CashMovementApplicationService(wallets, ledger, idempotency, snapshots, audit, clock,
				ZoneId.of("America/Sao_Paulo"));

		CashOperationResult result = service.execute(userId, TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("10"), " origem ", "key-1", UUID.randomUUID(), "/deposito");

		assertThat(result.movement().dataHora()).isEqualTo(canonicalInstant);
		assertThat(result.movement().valorBrl()).isEqualTo(new BigDecimal("10.00"));
		assertThat(result.movement().descricao()).isEqualTo("origem");
		verify(clock).instant();
		verify(snapshots).upsert(walletId, java.time.LocalDate.of(2026, 1, 1), new BigDecimal("10.00"));
		verify(audit).record(any(AuditoriaCommand.class));
		verify(idempotency).complete(reservationId, result.movement().id(), new BigDecimal("10.00"));
	}

	@Test
	void replayReturnsOriginalResultWithoutInvokingFinancialEffects() {
		UUID userId = UUID.randomUUID();
		UUID walletId = UUID.randomUUID();
		Clock clock = Clock.fixed(Instant.parse("2026-05-01T12:00:00Z"), ZoneId.of("UTC"));
		CashWalletPort wallets = mock(CashWalletPort.class);
		CashLedgerPort ledger = mock(CashLedgerPort.class);
		CashIdempotencyPort idempotency = mock(CashIdempotencyPort.class);
		CashSnapshotPort snapshots = mock(CashSnapshotPort.class);
		AuditoriaPort audit = mock(AuditoriaPort.class);
		MovimentacaoCaixa original = new MovimentacaoCaixa(UUID.randomUUID(), walletId, userId,
				TipoMovimentacaoCaixa.DEPOSITO, new BigDecimal("100"), null, Instant.parse("2026-04-01T10:00:00Z"));
		when(wallets.findPrimaryByUserId(userId)).thenReturn(Optional.of(
				new CashWalletPort.Wallet(walletId, userId, new BigDecimal("500"))));
		when(idempotency.reserve(eq(walletId), eq(TipoMovimentacaoCaixa.DEPOSITO), eq("replay"), any(), any()))
				.thenAnswer(invocation -> new CashIdempotencyPort.ExistingReservation(invocation.getArgument(3),
						original, new BigDecimal("100.00")));
		var service = new CashMovementApplicationService(wallets, ledger, idempotency, snapshots, audit, clock,
				ZoneId.of("America/Sao_Paulo"));

		CashOperationResult result = service.execute(userId, TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("100.00"), null, "replay", UUID.randomUUID(), "/deposito");

		assertThat(result.movement()).isEqualTo(original);
		assertThat(result.resultingBalance()).isEqualByComparingTo("100.00");
		verify(wallets, org.mockito.Mockito.never()).credit(any(), any());
		verify(ledger, org.mockito.Mockito.never()).save(any());
		verify(snapshots, org.mockito.Mockito.never()).upsert(any(), any(), any());
		verify(audit, org.mockito.Mockito.never()).record(any());
	}

	@Test
	void rejectsDifferentPayloadForACommittedKeyWithoutEffects() {
		UUID userId = UUID.randomUUID();
		UUID walletId = UUID.randomUUID();
		CashWalletPort wallets = mock(CashWalletPort.class);
		CashLedgerPort ledger = mock(CashLedgerPort.class);
		CashIdempotencyPort idempotency = mock(CashIdempotencyPort.class);
		CashSnapshotPort snapshots = mock(CashSnapshotPort.class);
		AuditoriaPort audit = mock(AuditoriaPort.class);
		when(wallets.findPrimaryByUserId(userId)).thenReturn(Optional.of(
				new CashWalletPort.Wallet(walletId, userId, BigDecimal.ZERO)));
		when(idempotency.reserve(eq(walletId), eq(TipoMovimentacaoCaixa.DEPOSITO), eq("conflict"), any(), any()))
				.thenReturn(new CashIdempotencyPort.ExistingReservation("0".repeat(64), null, null));
		var service = new CashMovementApplicationService(wallets, ledger, idempotency, snapshots, audit,
				Clock.systemUTC(), ZoneId.of("America/Sao_Paulo"));

		assertThatThrownBy(() -> service.execute(userId, TipoMovimentacaoCaixa.DEPOSITO, BigDecimal.ONE,
				null, "conflict", UUID.randomUUID(), "/deposito")).isInstanceOf(CashConflictException.class);
		verify(wallets, org.mockito.Mockito.never()).credit(any(), any());
	}

	@Test
	void validatesRawKeyAndPaginationWithoutTrimming() {
		UUID userId = UUID.randomUUID();
		UUID walletId = UUID.randomUUID();
		CashWalletPort wallets = mock(CashWalletPort.class);
		when(wallets.findPrimaryByUserId(userId)).thenReturn(Optional.of(
				new CashWalletPort.Wallet(walletId, userId, BigDecimal.ZERO)));
		var service = new CashMovementApplicationService(wallets, mock(CashLedgerPort.class),
				mock(CashIdempotencyPort.class), mock(CashSnapshotPort.class), mock(AuditoriaPort.class),
				Clock.systemUTC(), ZoneId.of("America/Sao_Paulo"));

		for (String key : new String[] { null, "", " key", "key ", "bad/key", "a".repeat(129) }) {
			assertThatThrownBy(() -> service.execute(userId, TipoMovimentacaoCaixa.DEPOSITO, BigDecimal.ONE,
					null, key, UUID.randomUUID(), "/deposito")).isInstanceOf(IllegalArgumentException.class);
		}
		assertThatThrownBy(() -> service.history(userId, -1, 20)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.history(userId, 0, 0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.history(userId, 0, 101)).isInstanceOf(IllegalArgumentException.class);
	}
}
