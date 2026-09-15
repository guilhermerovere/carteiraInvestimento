package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort;
import com.carteira.carteiraInvestimento.application.service.InvestmentApplicationService;
import com.carteira.carteiraInvestimento.application.service.InvestmentConflictException;
import com.carteira.carteiraInvestimento.application.service.InvestmentTransactionCommand;
import com.carteira.carteiraInvestimento.application.service.InvestmentUseCase;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class InvestmentLifecycleConcurrencyIT extends PostgreSqlContainerSupport {
    private static final ZoneId SNAPSHOT_ZONE=ZoneId.of("America/Sao_Paulo");
    @Autowired InvestmentPersistencePort persistence;
    @Autowired InvestmentUseCase investments;
    @Autowired AuditoriaPort audit;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;

    @BeforeEach @AfterEach
    void clean(){
        jdbc.update("DELETE FROM transacoes_idempotencia");jdbc.update("DELETE FROM transacoes");
        jdbc.update("DELETE FROM carteira_snapshots");jdbc.update("DELETE FROM posicoes");
        jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-life-%')");
        jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-life-%')");
        jdbc.update("DELETE FROM usuarios WHERE email LIKE 'investment-life-%'");
        jdbc.update("DELETE FROM acoes WHERE ticker='LIFX4'");
        jdbc.update("DELETE FROM corretoras WHERE cnpj='77665544000133'");
    }

    @Test
    void assetDeactivationWaitsForBuyCommitAndNextBuyFailsWhileSellRemainsAllowed() throws Exception {
        Fixture f=fixture();
        HeldOperation held=startHeldOperation(f,"lockAsset",TipoTransacao.BUY,"first-buy");
        deactivateAndProveBlocked("acoes",f.asset(),held);
        assertThat(jdbc.queryForObject("SELECT ativo FROM acoes WHERE id=?",Boolean.class,f.asset())).isFalse();
        assertThatThrownBy(()->investments.execute(f.user(),command(f,TipoTransacao.BUY),"next-buy",UUID.randomUUID(),"/transactions"))
                .isInstanceOf(InvestmentConflictException.class);
        assertThat(investments.execute(f.user(),command(f,TipoTransacao.SELL),"allowed-sell",UUID.randomUUID(),"/transactions")
                .transaction().transaction().tipo()).isEqualTo(TipoTransacao.SELL);
    }

    @Test
    void brokerDeactivationWaitsForBuyCommitAndNextFinancialOperationFails() throws Exception {
        Fixture f=fixture();
        HeldOperation held=startHeldOperation(f,"lockBroker",TipoTransacao.BUY,"first-buy");
        deactivateAndProveBlocked("corretoras",f.broker(),held);
        assertThat(jdbc.queryForObject("SELECT ativo FROM corretoras WHERE id=?",Boolean.class,f.broker())).isFalse();
        assertThatThrownBy(()->investments.execute(f.user(),command(f,TipoTransacao.SELL),"next-sell",UUID.randomUUID(),"/transactions"))
                .isInstanceOf(InvestmentConflictException.class);
    }

    private HeldOperation startHeldOperation(Fixture fixture,String lockMethod,TipoTransacao type,String key){
        CountDownLatch acquired=new CountDownLatch(1),release=new CountDownLatch(1);
        AtomicBoolean first=new AtomicBoolean(true);
        InvestmentPersistencePort probe=(InvestmentPersistencePort)Proxy.newProxyInstance(
                InvestmentPersistencePort.class.getClassLoader(),new Class<?>[]{InvestmentPersistencePort.class},
                (proxy,method,args)->{
                    try{
                        Object value=method.invoke(persistence,args);
                        if(method.getName().equals(lockMethod)&&first.compareAndSet(true,false)){
                            acquired.countDown();
                            if(!release.await(10,TimeUnit.SECONDS))throw new AssertionError("lifecycle release not signalled");
                        }
                        return value;
                    }catch(InvocationTargetException exception){throw exception.getCause();}
                });
        InvestmentApplicationService service=new InvestmentApplicationService(probe,audit,Clock.systemUTC(),SNAPSHOT_ZONE);
        var executor=Executors.newVirtualThreadPerTaskExecutor();
        var future=executor.submit(()->new TransactionTemplate(transactionManager).execute(
                status->service.execute(fixture.user(),command(fixture,type),key,UUID.randomUUID(),"/transactions")));
        return new HeldOperation(acquired,release,executor,future);
    }

    private void deactivateAndProveBlocked(String table,UUID id,HeldOperation held)throws Exception{
        assertThat(held.acquired().await(10,TimeUnit.SECONDS)).isTrue();
        try(Connection admin=dataSource.getConnection()){
            admin.setAutoCommit(false);
            int pid;
            try(var statement=admin.createStatement();var result=statement.executeQuery("SELECT pg_backend_pid()")){
                result.next();pid=result.getInt(1);
            }
            CountDownLatch attempted=new CountDownLatch(1);
            var update=held.executor().submit(()->{
                attempted.countDown();
                try(var statement=admin.prepareStatement("UPDATE "+table+" SET ativo=false WHERE id=?")){
                    statement.setObject(1,id);int rows=statement.executeUpdate();admin.commit();return rows;
                }
            });
            assertThat(attempted.await(10,TimeUnit.SECONDS)).isTrue();
            awaitDatabaseLock(pid);
            assertThat(update.isDone()).isFalse();
            held.release().countDown();
            assertThat(held.future().get(10,TimeUnit.SECONDS)).isNotNull();
            assertThat(update.get(10,TimeUnit.SECONDS)).isEqualTo(1);
        }finally{
            held.release().countDown();held.executor().close();
        }
    }

    private void awaitDatabaseLock(int pid){
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
        while(System.nanoTime()<deadline){
            String wait=jdbc.query("SELECT wait_event_type FROM pg_stat_activity WHERE pid=?",rs->rs.next()?rs.getString(1):null,pid);
            if("Lock".equals(wait))return;
            Thread.onSpinWait();
        }
        throw new AssertionError("admin lifecycle update did not wait on PostgreSQL lock");
    }

    private Fixture fixture(){
        UUID user=UUID.randomUUID(),wallet=UUID.randomUUID(),asset=UUID.randomUUID(),broker=UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,'ROLE_USER',true)",
                user,"Lifecycle","investment-life-"+user+"@test","hash");
        jdbc.update("INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,1000)",wallet,user,"Principal");
        jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,?,'Lifecycle','ACAO','B3','BRL',true)",asset,"LIFX4");
        jdbc.update("""
                INSERT INTO corretoras(id,cnpj,razao_social,cep,logradouro,bairro,cidade,uf,ativo,criado_em,atualizado_em)
                VALUES (?,'77665544000133','Lifecycle Broker','01001000','Rua','Centro','Sao Paulo','SP',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
                """,broker);
        return new Fixture(user,wallet,asset,broker);
    }
    private InvestmentTransactionCommand command(Fixture f,TipoTransacao type){
        return new InvestmentTransactionCommand(f.asset(),f.broker(),type,BigDecimal.ONE,new BigDecimal("100"),
                BigDecimal.ZERO,OffsetDateTime.parse("2026-09-11T12:00:00-03:00"),null);
    }
    private record Fixture(UUID user,UUID wallet,UUID asset,UUID broker){}
    private record HeldOperation(CountDownLatch acquired,CountDownLatch release,
            java.util.concurrent.ExecutorService executor,java.util.concurrent.Future<?> future){}
}
