package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class SnapshotCompatibilityIT extends PostgreSqlContainerSupport {
    @Autowired CashMovementUseCase cash;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach void clean(){
        jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia");jdbc.update("DELETE FROM carteira_snapshots");
        jdbc.update("DELETE FROM movimentacoes_caixa");jdbc.update("DELETE FROM posicoes");
        jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'snapshot-v9-%')");
        jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'snapshot-v9-%')");
        jdbc.update("DELETE FROM usuarios WHERE email LIKE 'snapshot-v9-%'");jdbc.update("DELETE FROM acoes WHERE ticker='SNAP4'");
    }
    @AfterEach void cleanAfter(){clean();}

    @Test void depositAndWithdrawalPreserveKnownValuationAndRecomputeInvestmentAndEquity(){
        Fixture f=fixture();LocalDate today=today();
        snapshot(f,today,new BigDecimal("1000.00"),new BigDecimal("500.00"),new BigDecimal("600.00"),
                new BigDecimal("1500.00"),new BigDecimal("-100.00"));
        cash.execute(f.user(),TipoMovimentacaoCaixa.DEPOSITO,new BigDecimal("100"),null,"deposit",UUID.randomUUID(),"/deposito");
        assertSnapshot(f,today,"1100.00","500.00","600.00","1600.00","-100.00");
        cash.execute(f.user(),TipoMovimentacaoCaixa.SAQUE,new BigDecimal("50"),null,"withdraw",UUID.randomUUID(),"/saque");
        assertSnapshot(f,today,"1050.00","500.00","600.00","1550.00","-100.00");
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM movimentacoes_caixa m JOIN logs_auditoria l
                  ON l.usuario_id=m.usuario_id AND l.tipo_evento=m.tipo AND l.data_hora=m.data_hora
                WHERE m.carteira_id=?
                """,Long.class,f.wallet())).isEqualTo(2);
    }

    @Test void unknownValuationStaysUnknownAndFirstNewDayDoesNotCopyPriorValuation(){
        Fixture f=fixture();LocalDate today=today(),previous=today.minusDays(1);
        snapshot(f,previous,new BigDecimal("900.00"),new BigDecimal("700.00"),new BigDecimal("600.00"),
                new BigDecimal("1600.00"),new BigDecimal("100.00"));
        cash.execute(f.user(),TipoMovimentacaoCaixa.DEPOSITO,new BigDecimal("1"),null,"new-day",UUID.randomUUID(),"/deposito");
        assertSnapshot(f,today,"1001.00",null,"600.00",null,null);
        assertSnapshot(f,previous,"900.00","700.00","600.00","1600.00","100.00");
        cash.execute(f.user(),TipoMovimentacaoCaixa.DEPOSITO,new BigDecimal("1"),null,"same-day",UUID.randomUUID(),"/deposito");
        assertSnapshot(f,today,"1002.00",null,"600.00",null,null);
    }

    private Fixture fixture(){UUID user=UUID.randomUUID(),wallet=UUID.randomUUID(),asset=UUID.randomUUID(),position=UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,'ROLE_USER',true)",user,"Snapshot","snapshot-v9-"+user+"@test","hash");
        jdbc.update("INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,1000)",wallet,user,"Principal");
        jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,'SNAP4','Snapshot Asset','ACAO','B3','BRL',true)",asset);
        jdbc.update("INSERT INTO posicoes VALUES (?,?,?,6,100,600,0,CURRENT_TIMESTAMP)",position,wallet,asset);return new Fixture(user,wallet);}
    private void snapshot(Fixture f,LocalDate date,BigDecimal cash,BigDecimal value,BigDecimal invested,BigDecimal equity,BigDecimal profit){
        jdbc.update("INSERT INTO carteira_snapshots VALUES (?,?,?,?,?,?,?,?)",UUID.randomUUID(),f.wallet(),date,cash,value,invested,equity,profit);}
    private void assertSnapshot(Fixture f,LocalDate date,String cash,String value,String invested,String equity,String profit){
        Map<String,Object> row=jdbc.queryForMap("SELECT * FROM carteira_snapshots WHERE carteira_id=? AND data_referencia=?",f.wallet(),date);
        equal(row.get("saldo_caixa_brl"),cash);equal(row.get("valor_posicoes_brl"),value);equal(row.get("total_investido_brl"),invested);
        equal(row.get("patrimonio_total_brl"),equity);equal(row.get("lucro_nao_realizado_brl"),profit);}
    private void equal(Object actual,String expected){if(expected==null)assertThat(actual).isNull();else assertThat((BigDecimal)actual).isEqualByComparingTo(expected);}
    private LocalDate today(){return LocalDate.now(ZoneId.of("America/Sao_Paulo"));}
    private record Fixture(UUID user,UUID wallet){}
}
