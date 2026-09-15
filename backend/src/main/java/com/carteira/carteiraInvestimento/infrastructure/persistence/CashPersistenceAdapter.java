package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.CashIdempotencyPort;
import com.carteira.carteiraInvestimento.application.port.CashLedgerPort;
import com.carteira.carteiraInvestimento.application.port.CashMovementPage;
import com.carteira.carteiraInvestimento.application.port.CashSnapshotPort;
import com.carteira.carteiraInvestimento.application.port.CashWalletPort;
import com.carteira.carteiraInvestimento.domain.wallet.MovimentacaoCaixa;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CashPersistenceAdapter implements CashWalletPort, CashLedgerPort, CashIdempotencyPort, CashSnapshotPort {
	private static final BigDecimal MAX_BRL = new BigDecimal("9999999999999999.99");
	private final JdbcTemplate jdbc;
	private final LocalSnapshotComposer snapshotComposer;

	public CashPersistenceAdapter(JdbcTemplate jdbc, LocalSnapshotComposer snapshotComposer) {
		this.jdbc = jdbc;
		this.snapshotComposer = snapshotComposer;
	}

	@Override
	public Optional<Wallet> findPrimaryByUserId(UUID userId) {
		return jdbc.query("SELECT id, usuario_id, saldo_caixa_brl FROM carteiras WHERE usuario_id = ?", (rs, row) ->
				new Wallet(rs.getObject("id", UUID.class), rs.getObject("usuario_id", UUID.class),
					rs.getBigDecimal("saldo_caixa_brl")), userId).stream().findFirst();
	}

	@Override
	public void lockActiveUser(UUID userId) {
		if (jdbc.query("SELECT id FROM usuarios WHERE id = ? AND ativo = true FOR UPDATE",
				(rs, row) -> rs.getObject(1, UUID.class), userId).isEmpty()) {
			throw new IllegalStateException("active principal unavailable");
		}
	}

	@Override
	public BigDecimal balance(UUID walletId) {
		return jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id = ?", BigDecimal.class, walletId);
	}

	@Override
	public Optional<BigDecimal> credit(UUID walletId, BigDecimal amount) {
		return jdbc.query("""
				UPDATE carteiras
				SET saldo_caixa_brl = saldo_caixa_brl + ?, estado_versao = estado_versao + 1
				WHERE id = ? AND saldo_caixa_brl <= ? - ?
				RETURNING saldo_caixa_brl
				""", (rs, row) -> rs.getBigDecimal(1), amount, walletId, MAX_BRL, amount).stream().findFirst();
	}

	@Override
	public Optional<BigDecimal> debit(UUID walletId, BigDecimal amount) {
		return jdbc.query("""
				UPDATE carteiras
				SET saldo_caixa_brl = saldo_caixa_brl - ?, estado_versao = estado_versao + 1
				WHERE id = ? AND saldo_caixa_brl >= ?
				RETURNING saldo_caixa_brl
				""", (rs, row) -> rs.getBigDecimal(1), amount, walletId, amount).stream().findFirst();
	}

	@Override
	public void save(MovimentacaoCaixa movement) {
		jdbc.update("""
				INSERT INTO movimentacoes_caixa
				(id, carteira_id, usuario_id, tipo, valor_brl, descricao, data_hora)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""", movement.id(), movement.carteiraId(), movement.usuarioId(), movement.tipo().name(),
				movement.valorBrl(), movement.descricao(), Timestamp.from(movement.dataHora()));
	}

	@Override
	public CashMovementPage history(UUID walletId, int page, int size) {
		long total = jdbc.queryForObject("SELECT count(*) FROM movimentacoes_caixa WHERE carteira_id = ?",
				Long.class, walletId);
		List<MovimentacaoCaixa> items = jdbc.query("""
				SELECT id, carteira_id, usuario_id, tipo, valor_brl, descricao, data_hora
				FROM movimentacoes_caixa
				WHERE carteira_id = ?
				ORDER BY data_hora DESC, id DESC
				LIMIT ? OFFSET ?
				""", this::movement, walletId, size, (long) page * size);
		int pages = total == 0 ? 0 : (int) ((total + size - 1) / size);
		return new CashMovementPage(items, page, size, total, pages);
	}

	@Override
	public Reservation reserve(UUID walletId, TipoMovimentacaoCaixa type, String key, String fingerprint,
			Instant createdAt) {
		UUID reservationId = UUID.randomUUID();
		List<UUID> inserted = jdbc.query("""
				INSERT INTO movimentacoes_caixa_idempotencia
				(id, carteira_id, tipo, idempotency_key, fingerprint, criada_em)
				VALUES (?, ?, ?, ?, ?, ?)
				ON CONFLICT (carteira_id, tipo, idempotency_key) DO NOTHING
				RETURNING id
				""", (rs, row) -> rs.getObject(1, UUID.class), reservationId, walletId, type.name(), key, fingerprint,
				Timestamp.from(createdAt));
		if (!inserted.isEmpty()) {
			return new NewReservation(reservationId);
		}
		return jdbc.query("""
				SELECT i.fingerprint, i.saldo_resultante,
				       m.id, m.carteira_id, m.usuario_id, m.tipo, m.valor_brl, m.descricao, m.data_hora
				FROM movimentacoes_caixa_idempotencia i
				LEFT JOIN movimentacoes_caixa m ON m.id = i.movimentacao_id
				WHERE i.carteira_id = ? AND i.tipo = ? AND i.idempotency_key = ?
				""", (rs, row) -> new ExistingReservation(rs.getString("fingerprint"),
				movementNullable(rs), rs.getBigDecimal("saldo_resultante")), walletId, type.name(), key)
				.stream().findFirst().orElseThrow(() -> new IllegalStateException("idempotency reservation unavailable"));
	}

	@Override
	public void complete(UUID reservationId, UUID movementId, BigDecimal resultingBalance) {
		int updated = jdbc.update("""
				UPDATE movimentacoes_caixa_idempotencia
				SET movimentacao_id = ?, saldo_resultante = ?
				WHERE id = ? AND movimentacao_id IS NULL AND saldo_resultante IS NULL
				""", movementId, resultingBalance, reservationId);
		if (updated != 1) {
			throw new IllegalStateException("idempotency result could not be persisted");
		}
	}

	@Override
	public void upsert(UUID walletId, LocalDate referenceDate, BigDecimal resultingBalance) {
		snapshotComposer.composeCash(walletId, referenceDate, resultingBalance);
	}

	private MovimentacaoCaixa movement(ResultSet rs, int row) throws SQLException {
		return new MovimentacaoCaixa(rs.getObject("id", UUID.class), rs.getObject("carteira_id", UUID.class),
				rs.getObject("usuario_id", UUID.class), TipoMovimentacaoCaixa.valueOf(rs.getString("tipo")),
				rs.getBigDecimal("valor_brl"), rs.getString("descricao"), rs.getTimestamp("data_hora").toInstant());
	}

	private MovimentacaoCaixa movementNullable(ResultSet rs) throws SQLException {
		return rs.getObject("id") == null ? null : movement(rs, 0);
	}
}
