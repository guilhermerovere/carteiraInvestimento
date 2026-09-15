package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.service.CashConflictException;
import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import com.carteira.carteiraInvestimento.application.service.CashOperationResult;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class CashMovementConcurrencyIT extends PostgreSqlContainerSupport {
	@Autowired private CashMovementUseCase cash;
	@Autowired private JdbcTemplate jdbc;

	@BeforeEach
	void cleanCashData() {
		jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia");
		jdbc.update("DELETE FROM carteira_snapshots");
		jdbc.update("DELETE FROM movimentacoes_caixa");
		jdbc.update("DELETE FROM logs_auditoria WHERE tipo_evento IN ('DEPOSITO','SAQUE')");
		jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'cash-it-%')");
		jdbc.update("DELETE FROM usuarios WHERE email LIKE 'cash-it-%'");
	}

	@Test
	void identicalConcurrentRequestsCommitOneEffectAndReturnTheSameOriginalResult() throws Exception {
		Fixture fixture = fixture(BigDecimal.ZERO);
		var outcomes = concurrent(
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO, new BigDecimal("100"),
						" salary ", "same-key", UUID.randomUUID(), "/deposito"),
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO, new BigDecimal("100.00"),
						"salary", "same-key", UUID.randomUUID(), "/deposito"));

		CashOperationResult first = (CashOperationResult) outcomes.first();
		CashOperationResult second = (CashOperationResult) outcomes.second();
		assertThat(first).isEqualTo(second);
		assertThat(balance(fixture.walletId())).isEqualByComparingTo("100.00");
		assertArtifacts(fixture.walletId(), 1, 1, 1, 1);
	}

	@Test
	void divergentConcurrentPayloadsApplyOnlyTheWinner() throws Exception {
		Fixture fixture = fixture(BigDecimal.ZERO);
		var outcomes = concurrent(
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO, new BigDecimal("10"), null,
						"divergent", UUID.randomUUID(), "/deposito"),
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO, new BigDecimal("20"), null,
						"divergent", UUID.randomUUID(), "/deposito"));

		assertThat(java.util.List.of(outcomes.first(), outcomes.second()).stream()
				.filter(CashOperationResult.class::isInstance).count()).isEqualTo(1);
		assertThat(java.util.List.of(outcomes.first(), outcomes.second()).stream()
				.filter(CashConflictException.class::isInstance).count()).isEqualTo(1);
		assertThat(balance(fixture.walletId())).isIn(new BigDecimal("10.00"), new BigDecimal("20.00"));
		assertArtifacts(fixture.walletId(), 1, 1, 1, 1);
	}

	@Test
	void concurrentWithdrawalsFromOneHundredAllowOnlyOneDebit() throws Exception {
		Fixture fixture = fixture(new BigDecimal("100.00"));
		var outcomes = concurrent(
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.SAQUE, new BigDecimal("80"), null,
						"withdraw-a", UUID.randomUUID(), "/saque"),
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.SAQUE, new BigDecimal("80"), null,
						"withdraw-b", UUID.randomUUID(), "/saque"));

		assertThat(java.util.List.of(outcomes.first(), outcomes.second()).stream()
				.filter(CashOperationResult.class::isInstance).count()).isEqualTo(1);
		assertThat(java.util.List.of(outcomes.first(), outcomes.second()).stream()
				.filter(CashConflictException.class::isInstance).count()).isEqualTo(1);
		assertThat(balance(fixture.walletId())).isEqualByComparingTo("20.00");
		assertArtifacts(fixture.walletId(), 1, 1, 1, 1);
	}

	@Test
	void overflowAndInsufficientFundsRollbackReservationAndPermitRetry() {
		Fixture full = fixture(new BigDecimal("9999999999999999.99"));
		assertThatThrownBy(() -> cash.execute(full.userId(), TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("0.01"), null, "overflow", UUID.randomUUID(), "/deposito"))
				.isInstanceOf(CashConflictException.class);
		assertArtifacts(full.walletId(), 0, 0, 0, 0);

		Fixture empty = fixture(BigDecimal.ZERO);
		assertThatThrownBy(() -> cash.execute(empty.userId(), TipoMovimentacaoCaixa.SAQUE,
				BigDecimal.ONE, null, "retry-key", UUID.randomUUID(), "/saque"))
				.isInstanceOf(CashConflictException.class);
		jdbc.update("UPDATE carteiras SET saldo_caixa_brl=10 WHERE id=?", empty.walletId());
		CashOperationResult retry = cash.execute(empty.userId(), TipoMovimentacaoCaixa.SAQUE,
				BigDecimal.ONE, null, "retry-key", UUID.randomUUID(), "/saque");
		assertThat(retry.resultingBalance()).isEqualByComparingTo("9.00");
		assertArtifacts(empty.walletId(), 1, 1, 1, 1);
	}

	@Test
	void replayPreservesOriginalBalanceAfterLaterOperationsAndHistoryOrder() {
		Fixture fixture = fixture(BigDecimal.ZERO);
		CashOperationResult original = cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("100"), null, "original", UUID.randomUUID(), "/deposito");
		CashOperationResult later = cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("400"), null, "later", UUID.randomUUID(), "/deposito");
		CashOperationResult replay = cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO,
				new BigDecimal("100.00"), "  ", "original", UUID.randomUUID(), "/deposito");

		assertThat(replay).isEqualTo(original);
		assertThat(replay.movement().dataHora()).isEqualTo(original.movement().dataHora());
		assertThat(replay.movement().valorBrl()).isEqualTo(new BigDecimal("100.00"));
		assertThat(replay.resultingBalance()).isEqualByComparingTo("100.00");
		assertThat(balance(fixture.walletId())).isEqualByComparingTo("500.00");
		var history = cash.history(fixture.userId(), 0, 20).items();
		assertThat(history).extracting("id")
				.containsExactly(later.movement().id(), original.movement().id());
		assertThat(history.get(1).dataHora()).isEqualTo(original.movement().dataHora());
		assertArtifacts(fixture.walletId(), 2, 1, 2, 2);
	}

	@Test
	void concurrentDistinctDepositsUpsertOneSnapshotAndPreservePreviousDates() throws Exception {
		Fixture fixture = fixture(BigDecimal.ZERO);
		LocalDate previousDate = LocalDate.now(ZoneId.of("America/Sao_Paulo")).minusDays(1);
		UUID previousId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO carteira_snapshots(id,carteira_id,data_referencia,saldo_caixa_brl,
                    valor_posicoes_brl,total_investido_brl,patrimonio_total_brl,lucro_nao_realizado_brl)
                VALUES (?,?,?,?,?,?,?,?)
                """, previousId, fixture.walletId(),
				previousDate, new BigDecimal("7.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("7.00"),
				BigDecimal.ZERO);
		var outcomes = concurrent(
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO, new BigDecimal("30"), null,
						"snapshot-a", UUID.randomUUID(), "/deposito"),
				() -> cash.execute(fixture.userId(), TipoMovimentacaoCaixa.DEPOSITO, new BigDecimal("70"), null,
						"snapshot-b", UUID.randomUUID(), "/deposito"));

		assertThat(outcomes.first()).isInstanceOf(CashOperationResult.class);
		assertThat(outcomes.second()).isInstanceOf(CashOperationResult.class);
		assertThat(balance(fixture.walletId())).isEqualByComparingTo("100.00");
		assertThat(count("SELECT count(*) FROM carteira_snapshots WHERE carteira_id=?", fixture.walletId())).isEqualTo(2);
		assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteira_snapshots WHERE id=?", BigDecimal.class,
				previousId)).isEqualByComparingTo("7.00");
		assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteira_snapshots WHERE carteira_id=? AND data_referencia<>?",
				BigDecimal.class, fixture.walletId(), previousDate)).isEqualByComparingTo("100.00");
	}

	private Outcomes concurrent(ThrowingSupplier first, ThrowingSupplier second) throws Exception {
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			Future<Object> a = executor.submit(() -> outcome(first, ready, start));
			Future<Object> b = executor.submit(() -> outcome(second, ready, start));
			ready.await(); start.countDown();
			return new Outcomes(a.get(), b.get());
		}
	}

	private Object outcome(ThrowingSupplier action, CountDownLatch ready, CountDownLatch start) {
		try { ready.countDown(); start.await(); return action.get(); }
		catch (Exception exception) { return exception; }
	}

	private Fixture fixture(BigDecimal balance) {
		UUID user = UUID.randomUUID();
		UUID wallet = UUID.randomUUID();
		jdbc.update("INSERT INTO usuarios (id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,?,true)", user,
				"Cash User", "cash-it-" + user + "@example.test", "hash", "ROLE_USER");
		jdbc.update("INSERT INTO carteiras (id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,?)", wallet, user,
				"Carteira Principal", balance);
		return new Fixture(user, wallet);
	}

	private BigDecimal balance(UUID wallet) {
		return jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?", BigDecimal.class, wallet);
	}

	private void assertArtifacts(UUID wallet, int movements, int snapshots, int audits, int reservations) {
		assertThat(count("SELECT count(*) FROM movimentacoes_caixa WHERE carteira_id=?", wallet)).isEqualTo(movements);
		assertThat(count("SELECT count(*) FROM carteira_snapshots WHERE carteira_id=?", wallet)).isEqualTo(snapshots);
		assertThat(count("SELECT count(*) FROM logs_auditoria l JOIN carteiras c ON c.usuario_id=l.usuario_id WHERE c.id=? AND l.tipo_evento IN ('DEPOSITO','SAQUE')", wallet)).isEqualTo(audits);
		assertThat(count("SELECT count(*) FROM movimentacoes_caixa_idempotencia WHERE carteira_id=?", wallet)).isEqualTo(reservations);
	}

	private long count(String sql, UUID id) { return jdbc.queryForObject(sql, Long.class, id); }
	private record Fixture(UUID userId, UUID walletId) { }
	private record Outcomes(Object first, Object second) { }
	@FunctionalInterface private interface ThrowingSupplier { Object get() throws Exception; }
}
