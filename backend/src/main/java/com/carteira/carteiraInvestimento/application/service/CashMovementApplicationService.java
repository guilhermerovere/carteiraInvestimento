package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort;
import com.carteira.carteiraInvestimento.application.port.CashLedgerPort;
import com.carteira.carteiraInvestimento.application.port.CashMovementPage;
import com.carteira.carteiraInvestimento.application.port.CashSnapshotPort;
import com.carteira.carteiraInvestimento.application.port.CashWalletPort;
import com.carteira.carteiraInvestimento.domain.wallet.CashMovementNormalizer;
import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.transaction.annotation.Transactional;

public class CashMovementApplicationService implements CashMovementUseCase {
	private static final Pattern IDEMPOTENCY_KEY = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$");
	private final CashWalletPort wallets;
	private final CashLedgerPort ledger;
	private final CashIdempotencyPort idempotency;
	private final CashSnapshotPort snapshots;
	private final AuditoriaPort audit;
	private final Clock clock;
	private final ZoneId snapshotZone;

	public CashMovementApplicationService(CashWalletPort wallets, CashLedgerPort ledger,
			CashIdempotencyPort idempotency, CashSnapshotPort snapshots, AuditoriaPort audit, Clock clock,
			ZoneId snapshotZone) {
		this.wallets = wallets;
		this.ledger = ledger;
		this.idempotency = idempotency;
		this.snapshots = snapshots;
		this.audit = audit;
		this.clock = clock;
		this.snapshotZone = snapshotZone;
	}

	@Override
	@Transactional
	public CashOperationResult execute(UUID userId, TipoMovimentacaoCaixa type, BigDecimal rawAmount,
			String rawDescription, String key, UUID correlationId, String endpoint) {
		Objects.requireNonNull(userId, "userId must not be null");
		Objects.requireNonNull(type, "type must not be null");
		var wallet = wallet(userId);
		BigDecimal amount = CashMovementNormalizer.amount(rawAmount);
		String description = CashMovementNormalizer.description(rawDescription);
		validateKey(key);
		Instant operationInstant = clock.instant().truncatedTo(ChronoUnit.MICROS);
		String fingerprint = CashMovementNormalizer.fingerprint(type, amount, description);

		CashIdempotencyPort.Reservation reservation = idempotency.reserve(wallet.id(), type, key, fingerprint,
				operationInstant);
		if (reservation instanceof CashIdempotencyPort.ExistingReservation existing) {
			if (!fingerprint.equals(existing.fingerprint())) {
				throw new CashConflictException("idempotency payload differs");
			}
			if (existing.movement() == null || existing.resultingBalance() == null) {
				throw new IllegalStateException("committed idempotency result is incomplete");
			}
			return new CashOperationResult(existing.movement(), existing.resultingBalance());
		}

		BigDecimal resultingBalance = mutate(wallet.id(), type, amount);
		MovimentacaoCaixa movement = new MovimentacaoCaixa(UUID.randomUUID(), wallet.id(), userId, type, amount,
				description, operationInstant);
		ledger.save(movement);
		snapshots.upsert(wallet.id(), operationInstant.atZone(snapshotZone).toLocalDate(), resultingBalance);
		audit.record(new AuditoriaCommand(userId, TipoEvento.valueOf(type.name()), ResultadoAuditoria.SUCESSO,
				SeveridadeAuditoria.INFO, endpoint, correlationId, operationInstant));
		idempotency.complete(((CashIdempotencyPort.NewReservation) reservation).id(), movement.id(), resultingBalance);
		return new CashOperationResult(movement, resultingBalance);
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal balance(UUID userId) {
		return wallets.balance(wallet(userId).id());
	}

	@Override
	@Transactional(readOnly = true)
	public CashMovementPage history(UUID userId, int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new IllegalArgumentException("invalid pagination");
		}
		return ledger.history(wallet(userId).id(), page, size);
	}

	private CashWalletPort.Wallet wallet(UUID userId) {
		return wallets.findPrimaryByUserId(userId).orElseThrow(PrimaryWalletMissingException::new);
	}

	private BigDecimal mutate(UUID walletId, TipoMovimentacaoCaixa type, BigDecimal amount) {
		return switch (type) {
			case DEPOSITO -> wallets.credit(walletId, amount)
					.orElseThrow(() -> new CashConflictException("resulting balance overflow"));
			case SAQUE -> wallets.debit(walletId, amount)
					.orElseThrow(() -> new CashConflictException("insufficient funds"));
		};
	}

	private void validateKey(String key) {
		if (key == null || !IDEMPOTENCY_KEY.matcher(key).matches()) {
			throw new IllegalArgumentException("invalid idempotency key");
		}
	}
}
