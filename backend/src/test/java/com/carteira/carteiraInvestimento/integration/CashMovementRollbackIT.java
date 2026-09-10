package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort;
import com.carteira.carteiraInvestimento.application.port.CashLedgerPort;
import com.carteira.carteiraInvestimento.application.port.CashSnapshotPort;
import com.carteira.carteiraInvestimento.application.service.CashMovementApplicationService;
import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import com.carteira.carteiraInvestimento.infrastructure.persistence.AuditoriaPersistenceAdapter;
import com.carteira.carteiraInvestimento.infrastructure.persistence.CashPersistenceAdapter;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class CashMovementRollbackIT extends PostgreSqlContainerSupport {
	@Autowired private CashMovementUseCase cash;
	@Autowired private CashPersistenceAdapter persistence;
	@Autowired private AuditoriaPersistenceAdapter auditPersistence;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private PlatformTransactionManager transactionManager;

	@BeforeEach
	void cleanCashData() {
		jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia");
		jdbc.update("DELETE FROM carteira_snapshots");
		jdbc.update("DELETE FROM movimentacoes_caixa");
		jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'cash-rollback-%')");
		jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'cash-rollback-%')");
		jdbc.update("DELETE FROM usuarios WHERE email LIKE 'cash-rollback-%'");
	}

	@ParameterizedTest
	@EnumSource(FailureStage.class)
	void everyFailureAfterBalanceMutationRollsBackAllEffectsAndDoesNotConsumeTheKey(FailureStage stage) {
		UUID user = UUID.randomUUID();
		UUID wallet = UUID.randomUUID();
		String key = "rollback-" + stage.name().toLowerCase();
		jdbc.update("INSERT INTO usuarios (id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,?,true)", user,
				"Rollback User", "cash-rollback-" + user + "@example.test", "hash", "ROLE_USER");
		jdbc.update("INSERT INTO carteiras (id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,0)", wallet, user,
				"Carteira Principal");
		CashMovementApplicationService failing = failingService(stage);
		TransactionTemplate transaction = new TransactionTemplate(transactionManager);

		assertThatThrownBy(() -> transaction.executeWithoutResult(ignored -> failing.execute(user,
				TipoMovimentacaoCaixa.DEPOSITO, BigDecimal.TEN, null, key, UUID.randomUUID(), "/deposito")))
				.isInstanceOf(IllegalStateException.class);

		assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?", BigDecimal.class, wallet))
				.isEqualByComparingTo("0.00");
		assertThat(count("movimentacoes_caixa", wallet)).isZero();
		assertThat(count("carteira_snapshots", wallet)).isZero();
		assertThat(count("movimentacoes_caixa_idempotencia", wallet)).isZero();
		assertThat(jdbc.queryForObject("SELECT count(*) FROM logs_auditoria WHERE usuario_id=? AND tipo_evento='DEPOSITO'",
				Long.class, user)).isZero();

		assertThat(cash.execute(user, TipoMovimentacaoCaixa.DEPOSITO, BigDecimal.TEN, null, key,
				UUID.randomUUID(), "/deposito").resultingBalance()).isEqualByComparingTo("10.00");
	}

	@Test
	void contenderWaitingOnAnUncommittedReservationExecutesAfterTheWinnerRollsBack() throws Exception {
		UUID user = UUID.randomUUID();
		UUID wallet = UUID.randomUUID();
		jdbc.update("INSERT INTO usuarios (id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,?,true)", user,
				"Rollback User", "cash-rollback-" + user + "@example.test", "hash", "ROLE_USER");
		jdbc.update("INSERT INTO carteiras (id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,0)", wallet, user,
				"Carteira Principal");
		CountDownLatch winnerReserved = new CountDownLatch(1);
		CountDownLatch releaseWinner = new CountDownLatch(1);
		CountDownLatch contenderEnteredReserve = new CountDownLatch(1);
		CashMovementApplicationService winner = blockingFailureService(winnerReserved, releaseWinner);
		CashMovementApplicationService contender = signalingService(contenderEnteredReserve);
		TransactionTemplate transaction = new TransactionTemplate(transactionManager);

		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			var failed = executor.submit(() -> runInTransaction(transaction, () -> winner.execute(user,
					TipoMovimentacaoCaixa.DEPOSITO, BigDecimal.TEN, null, "rollback-race", UUID.randomUUID(), "/deposito")));
			winnerReserved.await();
			var waiting = executor.submit(() -> runInTransaction(transaction, () -> contender.execute(user,
					TipoMovimentacaoCaixa.DEPOSITO, BigDecimal.TEN, null, "rollback-race", UUID.randomUUID(), "/deposito")));
			contenderEnteredReserve.await();
			assertThatThrownBy(() -> waiting.get(200, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
			releaseWinner.countDown();
			assertThat(failed.get()).isInstanceOf(IllegalStateException.class);
			assertThat(waiting.get()).isInstanceOf(com.carteira.carteiraInvestimento.application.service.CashOperationResult.class);
		}
		assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?", BigDecimal.class, wallet))
				.isEqualByComparingTo("10.00");
		assertThat(count("movimentacoes_caixa", wallet)).isEqualTo(1);
		assertThat(count("movimentacoes_caixa_idempotencia", wallet)).isEqualTo(1);
	}

	private CashMovementApplicationService failingService(FailureStage stage) {
		CashLedgerPort ledger = new CashLedgerPort() {
			@Override public void save(com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa movement) {
				persistence.save(movement);
				if (stage == FailureStage.MOVEMENT) throw failure(stage);
			}
			@Override public com.carteira.carteiraInvestimento.application.port.CashMovementPage history(
					UUID wallet, int page, int size) {
				return persistence.history(wallet, page, size);
			}
		};
		CashSnapshotPort snapshots = (wallet, date, balance) -> {
			persistence.upsert(wallet, date, balance);
			if (stage == FailureStage.SNAPSHOT) throw failure(stage);
		};
		AuditoriaPort audit = event -> {
			auditPersistence.record(event);
			if (stage == FailureStage.AUDIT) throw failure(stage);
		};
		CashIdempotencyPort idempotency = new CashIdempotencyPort() {
			@Override public Reservation reserve(UUID wallet, TipoMovimentacaoCaixa type, String key,
					String fingerprint, java.time.Instant createdAt) {
				return persistence.reserve(wallet, type, key, fingerprint, createdAt);
			}
			@Override public void complete(UUID reservation, UUID movement, BigDecimal balance) {
				persistence.complete(reservation, movement, balance);
				if (stage == FailureStage.RESULT) throw failure(stage);
			}
		};
		return new CashMovementApplicationService(persistence, ledger, idempotency, snapshots, audit,
				Clock.systemUTC(), ZoneId.of("America/Sao_Paulo"));
	}

	private CashMovementApplicationService blockingFailureService(CountDownLatch reserved, CountDownLatch release) {
		CashLedgerPort ledger = new CashLedgerPort() {
			@Override public void save(com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa movement) {
				reserved.countDown();
				try { release.await(); } catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
				throw new IllegalStateException("winner rollback");
			}
			@Override public com.carteira.carteiraInvestimento.application.port.CashMovementPage history(UUID wallet,
					int page, int size) { return persistence.history(wallet, page, size); }
		};
		return new CashMovementApplicationService(persistence, ledger, persistence, persistence, auditPersistence,
				Clock.systemUTC(), ZoneId.of("America/Sao_Paulo"));
	}

	private CashMovementApplicationService signalingService(CountDownLatch entered) {
		CashIdempotencyPort idempotency = new CashIdempotencyPort() {
			@Override public Reservation reserve(UUID wallet, TipoMovimentacaoCaixa type, String key,
					String fingerprint, java.time.Instant createdAt) {
				entered.countDown();
				return persistence.reserve(wallet, type, key, fingerprint, createdAt);
			}
			@Override public void complete(UUID reservation, UUID movement, BigDecimal balance) {
				persistence.complete(reservation, movement, balance);
			}
		};
		return new CashMovementApplicationService(persistence, persistence, idempotency, persistence, auditPersistence,
				Clock.systemUTC(), ZoneId.of("America/Sao_Paulo"));
	}

	private Object runInTransaction(TransactionTemplate transaction, java.util.concurrent.Callable<Object> action) {
		try {
			return transaction.execute(status -> {
				try { return action.call(); }
				catch (Exception exception) { throw new RuntimeException(exception); }
			});
		} catch (RuntimeException exception) {
			return exception.getCause() == null ? exception : exception.getCause();
		}
	}

	private IllegalStateException failure(FailureStage stage) {
		return new IllegalStateException(stage.name().toLowerCase() + " persistence failure");
	}

	private long count(String table, UUID wallet) {
		return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE carteira_id=?", Long.class, wallet);
	}

	private enum FailureStage { MOVEMENT, SNAPSHOT, AUDIT, RESULT }
}
