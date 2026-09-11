package com.carteira.carteiraInvestimento.infrastructure.persistence;

import com.carteira.carteiraInvestimento.domain.investment.InvestmentNumbers;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class LocalSnapshotComposer {
    private final JdbcTemplate jdbc;
    LocalSnapshotComposer(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    void composeInvestment(UUID walletId, LocalDate date, BigDecimal balance) {
        Totals totals=totals(walletId);
        BigDecimal invested=InvestmentNumbers.derivedMoney(totals.invested(),"snapshot invested total");
        BigDecimal value=totals.openCount()==0?InvestmentNumbers.ZERO_2:null;
        BigDecimal profit=totals.openCount()==0?InvestmentNumbers.ZERO_2:null;
        BigDecimal equity=totals.openCount()==0?balance:null;
        upsert(walletId,date,balance,invested,value,profit,equity);
    }

    void composeCash(UUID walletId, LocalDate date, BigDecimal balance) {
        Totals totals=totals(walletId);
        BigDecimal invested=InvestmentNumbers.derivedMoney(totals.invested(),"snapshot invested total");
        KnownValuation current=jdbc.query("""
                SELECT valor_posicoes_brl,lucro_nao_realizado_brl FROM carteira_snapshots
                WHERE carteira_id=? AND data_referencia=?
                """,(rs,n)->new KnownValuation(rs.getBigDecimal(1),rs.getBigDecimal(2)),walletId,date)
                .stream().findFirst().orElse(null);
        BigDecimal value,profit,equity;
        if(totals.openCount()==0){value=InvestmentNumbers.ZERO_2;profit=InvestmentNumbers.ZERO_2;equity=balance;}
        else if(current!=null&&current.value()!=null&&current.profit()!=null){
            value=current.value();profit=current.profit();equity=InvestmentNumbers.derivedMoney(balance.add(value),"snapshot equity");
        } else {value=null;profit=null;equity=null;}
        upsert(walletId,date,balance,invested,value,profit,equity);
    }

    private Totals totals(UUID walletId){
        return jdbc.queryForObject("""
                SELECT count(*) FILTER (WHERE quantidade>0) AS open_count,
                       coalesce(sum(total_investido_brl) FILTER (WHERE quantidade>0),0.00) AS invested
                FROM posicoes WHERE carteira_id=?
                """,(rs,n)->new Totals(rs.getLong(1),rs.getBigDecimal(2)),walletId);
    }

    private void upsert(UUID walletId,LocalDate date,BigDecimal balance,BigDecimal invested,
            BigDecimal value,BigDecimal profit,BigDecimal equity){
        jdbc.update("""
                INSERT INTO carteira_snapshots(id,carteira_id,data_referencia,saldo_caixa_brl,
                    valor_posicoes_brl,total_investido_brl,patrimonio_total_brl,lucro_nao_realizado_brl)
                VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (carteira_id,data_referencia) DO UPDATE SET
                    saldo_caixa_brl=EXCLUDED.saldo_caixa_brl,valor_posicoes_brl=EXCLUDED.valor_posicoes_brl,
                    total_investido_brl=EXCLUDED.total_investido_brl,
                    patrimonio_total_brl=EXCLUDED.patrimonio_total_brl,
                    lucro_nao_realizado_brl=EXCLUDED.lucro_nao_realizado_brl
                """,UUID.randomUUID(),walletId,date,balance,value,invested,equity,profit);
    }
    private record Totals(long openCount,BigDecimal invested){}
    private record KnownValuation(BigDecimal value,BigDecimal profit){}
}
