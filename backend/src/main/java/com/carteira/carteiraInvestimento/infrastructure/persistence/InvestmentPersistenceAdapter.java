package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort;
import com.carteira.carteiraInvestimento.application.service.InvestmentConflictException;
import com.carteira.carteiraInvestimento.application.service.InvestmentNotFoundException;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.investment.InvestmentNumbers;
import com.carteira.carteiraInvestimento.domain.investment.Posicao;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.carteira.carteiraInvestimento.domain.investment.Transacao;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvestmentPersistenceAdapter implements InvestmentPersistencePort {
    private final JdbcTemplate jdbc;
    private final LocalSnapshotComposer snapshotComposer;
    public InvestmentPersistenceAdapter(JdbcTemplate jdbc, LocalSnapshotComposer snapshotComposer) {
        this.jdbc = jdbc; this.snapshotComposer = snapshotComposer;
    }

    @Override
    public Optional<Wallet> findPrimaryWallet(UUID userId) {
        return jdbc.query("SELECT id, usuario_id, saldo_caixa_brl FROM carteiras WHERE usuario_id = ?",
                (rs, n) -> wallet(rs), userId).stream().findFirst();
    }

    @Override
    public Reservation reserve(UUID walletId, UUID userId, String key, String fingerprint) {
        UUID id = UUID.randomUUID();
        List<UUID> inserted = jdbc.query("""
                INSERT INTO transacoes_idempotencia
                    (id, carteira_id, usuario_id, idempotency_key, fingerprint, estado)
                VALUES (?, ?, ?, ?, ?, 'RESERVADA')
                ON CONFLICT (carteira_id, idempotency_key) DO NOTHING
                RETURNING id
                """, (rs, n) -> rs.getObject(1, UUID.class), id, walletId, userId, key, fingerprint);
        if (!inserted.isEmpty()) return new NewReservation(id);
        return jdbc.query("""
                SELECT i.fingerprint, i.valor_origem, i.saldo_caixa_brl_resultante,
                       i.ticker_resultante, i.posicao_id, i.posicao_quantidade_resultante,
                       i.posicao_preco_medio_brl_resultante, i.posicao_total_investido_brl_resultante,
                       i.posicao_lucro_realizado_acumulado_brl_resultante,
                       i.posicao_ultima_atualizacao_resultante,
                       t.id, t.carteira_id, t.usuario_id, t.acao_id, t.corretora_id,
                       t.exchange_rate_id, t.tipo, t.quantidade, t.moeda, t.preco_unitario,
                       t.taxas, t.taxa_cambio_brl, t.valor_total_brl, t.resultado_realizado_brl,
                       t.data_negociacao, t.data_registro,
                       a.nome AS asset_nome,a.mercado AS asset_mercado,a.moeda AS asset_moeda,
                       a.ativo AS asset_ativo,a.logo_provider AS asset_logo_provider,
                       a.logo_reference AS asset_logo_reference
                FROM transacoes_idempotencia i
                LEFT JOIN transacoes t ON t.id = i.transacao_id
                LEFT JOIN acoes a ON a.id = t.acao_id
                WHERE i.carteira_id = ? AND i.idempotency_key = ?
                """, (rs, n) -> existing(rs), walletId, key).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("idempotency reservation unavailable"));
    }

    @Override
    public ProtectedAsset lockAsset(UUID id) {
        return jdbc.query("SELECT id,ticker,nome,mercado,moeda,ativo,logo_provider,logo_reference FROM acoes WHERE id=? FOR SHARE",
                (rs, n) -> new ProtectedAsset(rs.getObject("id", UUID.class), rs.getString("ticker"), rs.getString("nome"),
                        Mercado.valueOf(rs.getString("mercado")), Moeda.valueOf(rs.getString("moeda")),
                        rs.getBoolean("ativo"), enumValue(rs.getString("logo_provider"), LogoProvider.class),
                        rs.getString("logo_reference")), id).stream().findFirst().orElseThrow(InvestmentNotFoundException::new);
    }

    @Override
    public ProtectedBroker lockBroker(UUID id) {
        return jdbc.query("SELECT id,ativo FROM corretoras WHERE id=? FOR SHARE",
                (rs, n) -> new ProtectedBroker(rs.getObject("id", UUID.class), rs.getBoolean("ativo")), id)
                .stream().findFirst().orElseThrow(InvestmentNotFoundException::new);
    }

    @Override
    public Optional<LocalExchangeRate> findExchangeRate(UUID id) {
        return jdbc.query("SELECT id,moeda_origem,moeda_destino,taxa,registrado_em FROM historico_cambio WHERE id=?",
                (rs, n) -> new LocalExchangeRate(rs.getObject("id", UUID.class),
                        MoedaCambio.valueOf(rs.getString("moeda_origem").trim()),
                        MoedaCambio.valueOf(rs.getString("moeda_destino").trim()), rs.getBigDecimal("taxa"),
                        rs.getTimestamp("registrado_em").toInstant()), id).stream().findFirst();
    }

    @Override
    public Wallet lockWallet(UUID id) {
        return jdbc.query("SELECT id,usuario_id,saldo_caixa_brl FROM carteiras WHERE id=? FOR UPDATE",
                (rs, n) -> wallet(rs), id).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("wallet disappeared"));
    }

    @Override
    public Optional<Posicao> lockPosition(UUID walletId, UUID assetId) {
        return jdbc.query("SELECT * FROM posicoes WHERE carteira_id=? AND acao_id=? FOR UPDATE",
                (rs, n) -> position(rs), walletId, assetId).stream().findFirst();
    }

    @Override public BigDecimal debit(UUID walletId, BigDecimal amount) {
        return jdbc.query("""
                UPDATE carteiras SET saldo_caixa_brl=saldo_caixa_brl-?,estado_versao=estado_versao+1
                WHERE id=? AND saldo_caixa_brl>=? RETURNING saldo_caixa_brl
                """, (rs, n) -> rs.getBigDecimal(1), amount, walletId, amount).stream().findFirst()
                .orElseThrow(() -> new InvestmentConflictException("insufficient funds"));
    }

    @Override public BigDecimal credit(UUID walletId, BigDecimal amount) {
        return jdbc.query("""
                UPDATE carteiras SET saldo_caixa_brl=saldo_caixa_brl+?,estado_versao=estado_versao+1
                WHERE id=? AND saldo_caixa_brl<=?-? RETURNING saldo_caixa_brl
                """, (rs, n) -> rs.getBigDecimal(1), amount, walletId, InvestmentNumbers.MAX_18_2, amount)
                .stream().findFirst().orElseThrow(() -> new InvestmentConflictException("cash balance overflow"));
    }

    @Override public void insertPosition(Posicao p) {
        jdbc.update("""
                INSERT INTO posicoes (id,carteira_id,acao_id,quantidade,preco_medio_brl,total_investido_brl,
                    lucro_realizado_acumulado_brl,ultima_atualizacao) VALUES (?,?,?,?,?,?,?,?)
                """, p.id(), p.carteiraId(), p.ativoId(), p.quantidade(), p.precoMedioBrl(),
                p.totalInvestidoBrl(), p.lucroRealizadoAcumuladoBrl(), Timestamp.from(p.ultimaAtualizacao()));
    }

    @Override public void updatePosition(Posicao p) {
        int count = jdbc.update("""
                UPDATE posicoes SET quantidade=?,preco_medio_brl=?,total_investido_brl=?,
                    lucro_realizado_acumulado_brl=?,ultima_atualizacao=? WHERE id=?
                """, p.quantidade(), p.precoMedioBrl(), p.totalInvestidoBrl(),
                p.lucroRealizadoAcumuladoBrl(), Timestamp.from(p.ultimaAtualizacao()), p.id());
        if (count != 1) throw new IllegalStateException("position update failed");
    }

    @Override public void insertTransaction(Transacao t) {
        jdbc.update("""
                INSERT INTO transacoes (id,carteira_id,usuario_id,acao_id,corretora_id,exchange_rate_id,tipo,
                    quantidade,moeda,preco_unitario,taxas,taxa_cambio_brl,valor_total_brl,
                    resultado_realizado_brl,data_negociacao,data_registro)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, t.id(), t.carteiraId(), t.usuarioId(), t.ativoId(), t.corretoraId(), t.exchangeRateId(),
                t.tipo().name(), t.quantidade(), t.moeda().name(), t.precoUnitario(), t.taxas(),
                t.taxaCambioBrl(), t.valorTotalBrl(), t.resultadoRealizadoBrl(),
                Timestamp.from(t.dataNegociacao()), Timestamp.from(t.dataRegistro()));
    }

    @Override
    public void composeInvestmentSnapshot(UUID walletId, LocalDate date, BigDecimal balance) {
        snapshotComposer.composeInvestment(walletId, date, balance);
    }

    @Override
    public void complete(UUID id, TransactionView view, BigDecimal source, BigDecimal balance, Posicao p) {
        int count = jdbc.update("""
                UPDATE transacoes_idempotencia SET estado='CONCLUIDA',transacao_id=?,ticker_resultante=?,
                    valor_origem=?,saldo_caixa_brl_resultante=?,posicao_id=?,
                    posicao_quantidade_resultante=?,posicao_preco_medio_brl_resultante=?,
                    posicao_total_investido_brl_resultante=?,
                    posicao_lucro_realizado_acumulado_brl_resultante=?,
                    posicao_ultima_atualizacao_resultante=?,concluida_em=CURRENT_TIMESTAMP
                WHERE id=? AND estado='RESERVADA'
                """, view.transaction().id(), view.ticker(), source, balance, p.id(), p.quantidade(),
                p.precoMedioBrl(), p.totalInvestidoBrl(), p.lucroRealizadoAcumuladoBrl(),
                Timestamp.from(p.ultimaAtualizacao()), id);
        if (count != 1) throw new IllegalStateException("idempotency completion failed");
    }

    @Override public Page<TransactionView> transactions(UUID walletId, int page, int size) {
        long total = jdbc.queryForObject("SELECT count(*) FROM transacoes WHERE carteira_id=?", Long.class, walletId);
        List<TransactionView> items = jdbc.query("""
                SELECT t.*,a.ticker,a.nome AS asset_nome,a.logo_provider AS asset_logo_provider,
                       a.logo_reference AS asset_logo_reference,COALESCE(c.nome_fantasia,c.razao_social) AS broker_nome
                FROM transacoes t JOIN acoes a ON a.id=t.acao_id JOIN corretoras c ON c.id=t.corretora_id
                WHERE t.carteira_id=? ORDER BY t.data_registro DESC,t.id DESC LIMIT ? OFFSET ?
                """, (rs, n) -> transactionView(rs), walletId, size, (long) page * size);
        return page(items, page, size, total);
    }

    @Override public Optional<TransactionView> transaction(UUID id) {
        return jdbc.query("SELECT t.*,a.ticker,a.nome AS asset_nome,a.logo_provider AS asset_logo_provider,a.logo_reference AS asset_logo_reference,COALESCE(c.nome_fantasia,c.razao_social) AS broker_nome FROM transacoes t JOIN acoes a ON a.id=t.acao_id JOIN corretoras c ON c.id=t.corretora_id WHERE t.id=?",
                (rs, n) -> transactionView(rs), id).stream().findFirst();
    }

    @Override public Page<PositionView> openPositions(UUID walletId, int page, int size) {
        long total = jdbc.queryForObject("SELECT count(*) FROM posicoes WHERE carteira_id=? AND quantidade>0",
                Long.class, walletId);
        List<PositionView> items = jdbc.query("""
                SELECT p.*,a.ticker,a.nome,a.mercado,a.moeda,a.ativo,a.logo_provider,a.logo_reference
                FROM posicoes p JOIN acoes a ON a.id=p.acao_id
                WHERE p.carteira_id=? AND p.quantidade>0 ORDER BY a.ticker ASC,a.id ASC LIMIT ? OFFSET ?
                """, (rs, n) -> positionView(rs), walletId, size,
                (long) page * size);
        return page(items, page, size, total);
    }

    @Override public Optional<PositionView> position(UUID walletId, UUID assetId) {
        return jdbc.query("""
                SELECT p.*,a.ticker,a.nome,a.mercado,a.moeda,a.ativo,a.logo_provider,a.logo_reference
                FROM posicoes p JOIN acoes a ON a.id=p.acao_id
                WHERE p.carteira_id=? AND p.acao_id=?
                """, (rs, n) -> positionView(rs), walletId, assetId)
                .stream().findFirst();
    }

    private ExistingReservation existing(ResultSet rs) throws SQLException {
        if (rs.getObject("id") == null) return new ExistingReservation(rs.getString("fingerprint"), null,
                null, null, null);
        Transacao transaction = transaction(rs);
        Posicao snapshot = new Posicao(rs.getObject("posicao_id", UUID.class), transaction.carteiraId(),
                transaction.ativoId(), rs.getBigDecimal("posicao_quantidade_resultante"),
                rs.getBigDecimal("posicao_preco_medio_brl_resultante"),
                rs.getBigDecimal("posicao_total_investido_brl_resultante"),
                rs.getBigDecimal("posicao_lucro_realizado_acumulado_brl_resultante"),
                rs.getTimestamp("posicao_ultima_atualizacao_resultante").toInstant());
        String ticker = rs.getString("ticker_resultante");
        return new ExistingReservation(rs.getString("fingerprint"), new TransactionView(transaction, ticker),
                rs.getBigDecimal("valor_origem"), rs.getBigDecimal("saldo_caixa_brl_resultante"),
                new PositionView(snapshot, ticker, rs.getString("asset_nome"),
                        enumValue(rs.getString("asset_mercado"), Mercado.class),
                        enumValue(rs.getString("asset_moeda"), Moeda.class), rs.getBoolean("asset_ativo"),
                        enumValue(rs.getString("asset_logo_provider"), LogoProvider.class),
                        rs.getString("asset_logo_reference")));
    }

    private TransactionView transactionView(ResultSet rs) throws SQLException {
        return new TransactionView(transaction(rs), rs.getString("ticker"), rs.getString("asset_nome"),
                enumValue(rs.getString("asset_logo_provider"), LogoProvider.class),
                rs.getString("asset_logo_reference"), rs.getString("broker_nome"));
    }

    @Override
    public void lockActiveUser(UUID userId) {
        if (jdbc.query("SELECT id FROM usuarios WHERE id=? AND ativo=true FOR UPDATE",
                (rs, row) -> rs.getObject(1, UUID.class), userId).isEmpty()) {
            throw new IllegalStateException("active principal unavailable");
        }
    }

    private PositionView positionView(ResultSet rs) throws SQLException {
        return new PositionView(position(rs), rs.getString("ticker"), rs.getString("nome"),
                Mercado.valueOf(rs.getString("mercado")), Moeda.valueOf(rs.getString("moeda")),
                rs.getBoolean("ativo"), enumValue(rs.getString("logo_provider"), LogoProvider.class),
                rs.getString("logo_reference"));
    }

    private static <E extends Enum<E>> E enumValue(String value, Class<E> type) {
        return value == null ? null : Enum.valueOf(type, value);
    }

    private Transacao transaction(ResultSet rs) throws SQLException {
        return new Transacao(rs.getObject("id", UUID.class), rs.getObject("carteira_id", UUID.class),
                rs.getObject("usuario_id", UUID.class), rs.getObject("acao_id", UUID.class),
                rs.getObject("corretora_id", UUID.class), rs.getObject("exchange_rate_id", UUID.class),
                TipoTransacao.valueOf(rs.getString("tipo")), rs.getBigDecimal("quantidade"),
                Moeda.valueOf(rs.getString("moeda")), rs.getBigDecimal("preco_unitario"),
                rs.getBigDecimal("taxas"), rs.getBigDecimal("taxa_cambio_brl"),
                rs.getBigDecimal("valor_total_brl"), rs.getBigDecimal("resultado_realizado_brl"),
                rs.getTimestamp("data_negociacao").toInstant(), rs.getTimestamp("data_registro").toInstant());
    }

    private Posicao position(ResultSet rs) throws SQLException {
        return new Posicao(rs.getObject("id", UUID.class), rs.getObject("carteira_id", UUID.class),
                rs.getObject("acao_id", UUID.class), rs.getBigDecimal("quantidade"),
                rs.getBigDecimal("preco_medio_brl"), rs.getBigDecimal("total_investido_brl"),
                rs.getBigDecimal("lucro_realizado_acumulado_brl"),
                rs.getTimestamp("ultima_atualizacao").toInstant());
    }

    private Wallet wallet(ResultSet rs) throws SQLException {
        return new Wallet(rs.getObject("id", UUID.class), rs.getObject("usuario_id", UUID.class),
                rs.getBigDecimal("saldo_caixa_brl"));
    }

    private static <T> Page<T> page(List<T> items, int page, int size, long total) {
        return new Page<>(items, page, size, total, total == 0 ? 0 : (int) ((total + size - 1) / size));
    }
}
