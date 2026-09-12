package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.application.port.PortfolioValuationPort;
import com.carteira.carteiraInvestimento.application.service.PrimaryWalletMissingException;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PortfolioValuationPersistenceAdapter implements PortfolioValuationPort {
    private final JdbcTemplate jdbc;
    public PortfolioValuationPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public LocalState readLocalState(UUID userId) {
        WalletRow wallet = jdbc.query("""
                SELECT id,saldo_caixa_brl,estado_versao FROM carteiras WHERE usuario_id=?
                """, (rs, row) -> new WalletRow(rs.getObject("id", UUID.class),
                        rs.getBigDecimal("saldo_caixa_brl"), rs.getLong("estado_versao")), userId)
                .stream().findFirst().orElseThrow(PrimaryWalletMissingException::new);
        List<OpenPosition> positions = jdbc.query("""
                SELECT p.acao_id,a.ticker,a.mercado,a.moeda,a.ativo,p.quantidade,p.preco_medio_brl,
                       p.total_investido_brl
                FROM posicoes p JOIN acoes a ON a.id=p.acao_id
                WHERE p.carteira_id=? AND p.quantidade>0
                ORDER BY a.ticker,a.id
                """, (rs, row) -> new OpenPosition(rs.getObject("acao_id", UUID.class), rs.getString("ticker"),
                        Mercado.valueOf(rs.getString("mercado")), Moeda.valueOf(rs.getString("moeda")),
                        rs.getBoolean("ativo"), rs.getBigDecimal("quantidade"),
                        rs.getBigDecimal("preco_medio_brl"), rs.getBigDecimal("total_investido_brl")), wallet.id());
        BigDecimal realized = jdbc.queryForObject("""
                SELECT coalesce(sum(lucro_realizado_acumulado_brl),0.00)
                FROM posicoes WHERE carteira_id=?
                """, BigDecimal.class, wallet.id());
        return new LocalState(wallet.id(), wallet.balance(), wallet.version(), List.copyOf(positions), realized);
    }

    @Override
    public boolean validateAndOptionallyMaterialize(UUID walletId, long expectedVersion, LocalDate referenceDate,
            Instant valuationInstant, BigDecimal cashBalanceBrl, MaterializedTotals totals, boolean materialize) {
        Long current = jdbc.query("SELECT estado_versao FROM carteiras WHERE id=? FOR UPDATE",
                (rs, row) -> rs.getLong(1), walletId).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("wallet disappeared"));
        if (current != expectedVersion) return false;
        if (materialize) {
            jdbc.update("""
                    INSERT INTO carteira_snapshots(id,carteira_id,data_referencia,saldo_caixa_brl,
                        valor_posicoes_brl,total_investido_brl,patrimonio_total_brl,
                        lucro_nao_realizado_brl,valuation_instant)
                    VALUES (?,?,?,?,?,?,?,?,?)
                    ON CONFLICT (carteira_id,data_referencia) DO UPDATE SET
                        saldo_caixa_brl=EXCLUDED.saldo_caixa_brl,
                        valor_posicoes_brl=EXCLUDED.valor_posicoes_brl,
                        total_investido_brl=EXCLUDED.total_investido_brl,
                        patrimonio_total_brl=EXCLUDED.patrimonio_total_brl,
                        lucro_nao_realizado_brl=EXCLUDED.lucro_nao_realizado_brl,
                        valuation_instant=EXCLUDED.valuation_instant
                    WHERE carteira_snapshots.valuation_instant IS NULL
                       OR EXCLUDED.valuation_instant > carteira_snapshots.valuation_instant
                    """, UUID.randomUUID(), walletId, referenceDate, cashBalanceBrl,
                    totals.positionsValueBrl(), totals.investedBrl(), totals.totalEquityBrl(),
                    totals.unrealizedProfitBrl(), Timestamp.from(valuationInstant));
        }
        return true;
    }

    private record WalletRow(UUID id, BigDecimal balance, long version) { }
}
