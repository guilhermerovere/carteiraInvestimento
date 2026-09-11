package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.application.service.CashMovementUseCase;
import com.carteira.carteiraInvestimento.application.service.InvestmentConflictException;
import com.carteira.carteiraInvestimento.application.service.InvestmentOperationResult;
import com.carteira.carteiraInvestimento.application.service.InvestmentTransactionCommand;
import com.carteira.carteiraInvestimento.application.service.InvestmentUseCase;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.carteira.carteiraInvestimento.domain.wallet.TipoMovimentacaoCaixa;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class InvestmentConcurrencyIT extends PostgreSqlContainerSupport {
    @Autowired InvestmentUseCase investments;
    @Autowired CashMovementUseCase cash;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;

    @BeforeEach void clean(){
        jdbc.update("DELETE FROM transacoes_idempotencia");jdbc.update("DELETE FROM transacoes");
        jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia");jdbc.update("DELETE FROM carteira_snapshots");
        jdbc.update("DELETE FROM movimentacoes_caixa");jdbc.update("DELETE FROM posicoes");
        jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-race-%')");
        jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-race-%')");
        jdbc.update("DELETE FROM usuarios WHERE email LIKE 'investment-race-%'");
        jdbc.update("DELETE FROM acoes WHERE ticker IN ('RACE4','LOCK4','RBSA4','RCAA4','RCAB4','RCAC4','RCAD4','RDAA4','RDBB4','RDCC4')");
        jdbc.update("DELETE FROM corretoras WHERE cnpj='88776655000144'");
    }
    @AfterEach void cleanAfter(){clean();}

    @Test void sameKeyConcurrentSamePayloadCommitsOneEffectAndDivergentPayloadOneConflict() throws Exception {
        Fixture f=fixture("1000.00"); UUID asset=asset("RACE4"), broker=broker();
        var command=command(asset,broker,TipoTransacao.BUY,"1","100");
        Outcomes same=concurrent(()->execute(f,command,"same"),()->execute(f,command,"same"));
        assertThat(same.values()).allMatch(InvestmentOperationResult.class::isInstance);
        assertThat(same.first()).isEqualTo(same.second());
        assertThat(count("transacoes",f.wallet())).isEqualTo(1);
        assertThat(balance(f)).isEqualByComparingTo("900.00");

        Outcomes divergent=concurrent(()->execute(f,command(asset,broker,TipoTransacao.BUY,"1","10"),"different"),
                ()->execute(f,command(asset,broker,TipoTransacao.BUY,"1","20"),"different"));
        assertThat(divergent.values().stream().filter(InvestmentOperationResult.class::isInstance).count()).isEqualTo(1);
        assertThat(divergent.values().stream().filter(InvestmentConflictException.class::isInstance).count()).isEqualTo(1);
        assertThat(count("transacoes",f.wallet())).isEqualTo(2);
    }

    @Test void distinctBuysSameAssetSerializeWithoutLostUpdates() throws Exception {
        Fixture f=fixture("1000.00");UUID asset=asset("RACE4"),broker=broker();
        Outcomes out=concurrent(()->execute(f,command(asset,broker,TipoTransacao.BUY,"1","100"),"a"),
                ()->execute(f,command(asset,broker,TipoTransacao.BUY,"1","100"),"b"));
        assertThat(out.values()).allMatch(InvestmentOperationResult.class::isInstance);
        assertThat(balance(f)).isEqualByComparingTo("800.00");
        assertThat(jdbc.queryForObject("SELECT quantidade FROM posicoes WHERE carteira_id=? AND acao_id=?",
                BigDecimal.class,f.wallet(),asset)).isEqualByComparingTo("2.00000000");
        assertThat(jdbc.queryForObject("SELECT total_investido_brl FROM posicoes WHERE carteira_id=? AND acao_id=?",
                BigDecimal.class,f.wallet(),asset)).isEqualByComparingTo("200.00");
        assertSnapshot(f, "800.00", "200.00", null);
    }

    @Test void concurrentBuyAndSellSerializeWithoutLostUpdatesAndLeaveAConsistentSnapshot() throws Exception {
        Fixture f=fixture("1000.00");UUID asset=asset("RBSA4"),broker=broker();
        execute(f,command(asset,broker,TipoTransacao.BUY,"2","100"),"seed");
        Outcomes out=concurrent(()->execute(f,command(asset,broker,TipoTransacao.BUY,"1","50"),"buy"),
                ()->execute(f,command(asset,broker,TipoTransacao.SELL,"1","120"),"sell"));
        assertThat(out.values()).allMatch(InvestmentOperationResult.class::isInstance);
        assertThat(balance(f)).isEqualByComparingTo("870.00");
        assertThat(count("transacoes",f.wallet())).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT quantidade FROM posicoes WHERE carteira_id=?",BigDecimal.class,f.wallet()))
                .isEqualByComparingTo("2.00000000");
        assertThat(jdbc.queryForObject("SELECT total_investido_brl FROM posicoes WHERE carteira_id=?",BigDecimal.class,f.wallet()))
                .isIn(new BigDecimal("150.00"),new BigDecimal("166.67"));
        assertSnapshot(f,"870.00",jdbc.queryForObject(
                "SELECT total_investido_brl FROM posicoes WHERE carteira_id=?",BigDecimal.class,f.wallet()),null);
    }

    @Test void concurrentSellsCannotOversell() throws Exception {
        Fixture f=fixture("1000.00");UUID asset=asset("RACE4"),broker=broker();
        execute(f,command(asset,broker,TipoTransacao.BUY,"1","100"),"seed");
        var sell=command(asset,broker,TipoTransacao.SELL,"1","110");
        Outcomes out=concurrent(()->execute(f,sell,"sell-a"),()->execute(f,sell,"sell-b"));
        assertThat(out.values().stream().filter(InvestmentOperationResult.class::isInstance).count()).isEqualTo(1);
        assertThat(out.values().stream().filter(InvestmentConflictException.class::isInstance).count()).isEqualTo(1);
        assertThat(balance(f)).isEqualByComparingTo("1010.00");
        assertThat(count("transacoes",f.wallet())).isEqualTo(2);
        assertSnapshot(f,"1010.00","0.00","0.00");
    }

    @Test void cashWithdrawalAndBuyShareWalletLockAndNeverMakeBalanceNegative() throws Exception {
        Fixture f=fixture("1000.00");UUID asset=asset("RACE4"),broker=broker();
        Outcomes out=concurrent(()->execute(f,command(asset,broker,TipoTransacao.BUY,"1","100"),"buy"),
                ()->cash.execute(f.user(),TipoMovimentacaoCaixa.SAQUE,new BigDecimal("950"),null,"withdraw",
                        UUID.randomUUID(),"/saque"));
        assertThat(out.values().stream().filter(x->x instanceof InvestmentOperationResult).count()
                +out.values().stream().filter(x->x instanceof com.carteira.carteiraInvestimento.application.service.CashOperationResult).count())
                .isGreaterThanOrEqualTo(1);
        assertThat(balance(f)).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM carteira_snapshots WHERE carteira_id=?",Long.class,f.wallet()))
                .isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(CashInvestmentRace.class)
    void depositAndWithdrawalShareTheWalletLockWithBuyAndSell(CashInvestmentRace race) throws Exception {
        Fixture f=fixture("1000.00");UUID asset=asset(new String[]{"RCAA4","RCAB4","RCAC4","RCAD4"}[race.ordinal()]),broker=broker();
        boolean sell=race.investmentType==TipoTransacao.SELL;
        if(sell) execute(f,command(asset,broker,TipoTransacao.BUY,"1","100"),"seed");
        LocalDate yesterday=LocalDate.now(ZoneId.of("America/Sao_Paulo")).minusDays(1);
        jdbc.update("""
                INSERT INTO carteira_snapshots(id,carteira_id,data_referencia,saldo_caixa_brl,valor_posicoes_brl,
                  total_investido_brl,patrimonio_total_brl,lucro_nao_realizado_brl)
                VALUES (?,?,?,777.00,333.00,222.00,1110.00,111.00)
                """,UUID.randomUUID(),f.wallet(),yesterday);
        Outcomes out=concurrent(
                ()->execute(f,command(asset,broker,race.investmentType,"1",sell?"120":"100"),"investment"),
                ()->cash.execute(f.user(),race.cashType,new BigDecimal("200"),null,"cash",
                        UUID.randomUUID(),"/cash"));
        assertThat(out.values()).allMatch(value->value instanceof InvestmentOperationResult
                || value instanceof com.carteira.carteiraInvestimento.application.service.CashOperationResult);
        assertThat(balance(f)).isEqualByComparingTo(race.expectedBalance);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM carteira_snapshots WHERE carteira_id=?",Long.class,f.wallet()))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT patrimonio_total_brl FROM carteira_snapshots WHERE carteira_id=? AND data_referencia=?",
                BigDecimal.class,f.wallet(),yesterday)).isEqualByComparingTo("1110.00");
        assertSnapshot(f,race.expectedBalance,sell?"0.00":"100.00",sell?"0.00":null);
    }

    @Test void differentAssetsSerializePerWalletWhileDifferentWalletsHaveNoGlobalFinancialLock() throws Exception {
        Fixture same=fixture("1000.00");UUID firstAsset=asset("RDAA4"),secondAsset=asset("RDBB4"),broker=broker();
        Outcomes sameWallet=concurrent(
                ()->execute(same,command(firstAsset,broker,TipoTransacao.BUY,"1","100"),"first"),
                ()->execute(same,command(secondAsset,broker,TipoTransacao.BUY,"1","100"),"second"));
        assertThat(sameWallet.values()).allMatch(InvestmentOperationResult.class::isInstance);
        assertThat(count("transacoes",same.wallet())).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM posicoes WHERE carteira_id=?",Long.class,same.wallet())).isEqualTo(2);
        assertSnapshot(same,"800.00","200.00",null);

        Fixture blocked=fixture("1000.00"),independent=fixture("1000.00");UUID sharedAsset=asset("RDCC4");
        try(Connection holder=dataSource.getConnection();var executor=Executors.newVirtualThreadPerTaskExecutor()){
            holder.setAutoCommit(false);
            try(var statement=holder.prepareStatement("SELECT id FROM carteiras WHERE id=? FOR UPDATE")){
                statement.setObject(1,blocked.wallet());statement.executeQuery();
            }
            var blockedFuture=executor.submit(()->execute(blocked,
                    command(sharedAsset,broker,TipoTransacao.BUY,"1","100"),"blocked-wallet"));
            var independentFuture=executor.submit(()->execute(independent,
                    command(sharedAsset,broker,TipoTransacao.BUY,"1","100"),"independent-wallet"));
            assertThat(independentFuture.get(10,TimeUnit.SECONDS)).isInstanceOf(InvestmentOperationResult.class);
            assertThat(blockedFuture.isDone()).isFalse();
            holder.commit();
            assertThat(blockedFuture.get(10,TimeUnit.SECONDS)).isInstanceOf(InvestmentOperationResult.class);
        }
        assertThat(balance(blocked)).isEqualByComparingTo("900.00");
        assertThat(balance(independent)).isEqualByComparingTo("900.00");
    }

    private InvestmentOperationResult execute(Fixture f,InvestmentTransactionCommand c,String key){
        return investments.execute(f.user(),c,key,UUID.randomUUID(),"/transactions");
    }
    private InvestmentTransactionCommand command(UUID asset,UUID broker,TipoTransacao type,String quantity,String price){
        return new InvestmentTransactionCommand(asset,broker,type,new BigDecimal(quantity),new BigDecimal(price),
                BigDecimal.ZERO,OffsetDateTime.parse("2026-09-11T12:00:00-03:00"),null);
    }
    private Outcomes concurrent(Action a,Action b)throws Exception{
        CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var x=executor.submit(()->outcome(a,ready,start));var y=executor.submit(()->outcome(b,ready,start));
            ready.await();start.countDown();return new Outcomes(x.get(),y.get());
        }
    }
    private Object outcome(Action a,CountDownLatch ready,CountDownLatch start){try{ready.countDown();start.await();return a.run();}catch(Exception e){return e;}}
    private Fixture fixture(String balance){UUID u=UUID.randomUUID(),w=UUID.randomUUID();jdbc.update("INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?, 'ROLE_USER',true)",u,"Race","investment-race-"+u+"@test","hash");jdbc.update("INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,?)",w,u,"Principal",new BigDecimal(balance));return new Fixture(u,w);}
    private UUID asset(String ticker){UUID id=UUID.randomUUID();jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,?,'ACAO','B3','BRL',true)",id,ticker,"Race Asset");return id;}
    private UUID broker(){UUID id=UUID.randomUUID();jdbc.update("INSERT INTO corretoras(id,cnpj,razao_social,cep,logradouro,bairro,cidade,uf,ativo,criado_em,atualizado_em) VALUES (?,'88776655000144','Broker','01001000','Rua','Centro','Sao Paulo','SP',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id);return id;}
    private BigDecimal balance(Fixture f){return jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?",BigDecimal.class,f.wallet());}
    private long count(String table,UUID wallet){return jdbc.queryForObject("SELECT count(*) FROM "+table+" WHERE carteira_id=?",Long.class,wallet);}
    private record Fixture(UUID user,UUID wallet){}
    private record Outcomes(Object first,Object second){List<Object> values(){return List.of(first,second);}}
    private void assertSnapshot(Fixture f,Object cashValue,Object investedValue,Object positionsValue){
        LocalDate today=LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM carteira_snapshots WHERE carteira_id=? AND data_referencia=?",
                Long.class,f.wallet(),today)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteira_snapshots WHERE carteira_id=? AND data_referencia=?",
                BigDecimal.class,f.wallet(),today)).isEqualByComparingTo(cashValue.toString());
        assertThat(jdbc.queryForObject("SELECT total_investido_brl FROM carteira_snapshots WHERE carteira_id=? AND data_referencia=?",
                BigDecimal.class,f.wallet(),today)).isEqualByComparingTo(investedValue.toString());
        BigDecimal positions=jdbc.queryForObject("SELECT valor_posicoes_brl FROM carteira_snapshots WHERE carteira_id=? AND data_referencia=?",
                BigDecimal.class,f.wallet(),today);
        if(positionsValue==null) assertThat(positions).isNull(); else assertThat(positions).isEqualByComparingTo(positionsValue.toString());
    }
    private enum CashInvestmentRace {
        DEPOSIT_BUY(TipoMovimentacaoCaixa.DEPOSITO,TipoTransacao.BUY,"1100.00"),
        DEPOSIT_SELL(TipoMovimentacaoCaixa.DEPOSITO,TipoTransacao.SELL,"1220.00"),
        WITHDRAW_BUY(TipoMovimentacaoCaixa.SAQUE,TipoTransacao.BUY,"700.00"),
        WITHDRAW_SELL(TipoMovimentacaoCaixa.SAQUE,TipoTransacao.SELL,"820.00");
        final TipoMovimentacaoCaixa cashType;final TipoTransacao investmentType;final String expectedBalance;
        CashInvestmentRace(TipoMovimentacaoCaixa cashType,TipoTransacao investmentType,String expectedBalance){
            this.cashType=cashType;this.investmentType=investmentType;this.expectedBalance=expectedBalance;
        }
    }
    @FunctionalInterface interface Action{Object run()throws Exception;}
}
