package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.InvestmentPersistencePort;
import com.carteira.carteiraInvestimento.application.service.InvestmentApplicationService;
import com.carteira.carteiraInvestimento.application.service.InvestmentTransactionCommand;
import com.carteira.carteiraInvestimento.application.service.InvestmentUseCase;
import com.carteira.carteiraInvestimento.domain.investment.TipoTransacao;
import com.carteira.carteiraInvestimento.infrastructure.persistence.AuditoriaPersistenceAdapter;
import com.carteira.carteiraInvestimento.infrastructure.persistence.InvestmentPersistenceAdapter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class InvestmentRollbackIT extends PostgreSqlContainerSupport {
    @Autowired InvestmentUseCase realUseCase;
    @Autowired InvestmentPersistenceAdapter realPersistence;
    @Autowired AuditoriaPersistenceAdapter realAudit;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @BeforeEach void clean(){
        jdbc.update("DELETE FROM transacoes_idempotencia");jdbc.update("DELETE FROM transacoes");
        jdbc.update("DELETE FROM carteira_snapshots");jdbc.update("DELETE FROM posicoes");
        jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-rollback-%')");
        jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'investment-rollback-%')");
        jdbc.update("DELETE FROM usuarios WHERE email LIKE 'investment-rollback-%'");
        jdbc.update("DELETE FROM acoes WHERE ticker='ROLL4'");jdbc.update("DELETE FROM corretoras WHERE cnpj='77665544000133'");
    }
    @AfterEach void cleanAfter(){clean();}

    @ParameterizedTest @EnumSource(Stage.class)
    void failuresAfterDebitRollbackEntireUnitAndReleaseKey(Stage stage){
        Fixture f=fixture();String key="rollback-"+stage.name().toLowerCase();
        InvestmentPersistencePort persistence=stage==Stage.AUDIT?realPersistence:failingPersistence(stage);
        AuditoriaPort audit=stage==Stage.AUDIT?command->{realAudit.record(command);throw new IllegalStateException("audit failure");}:realAudit;
        var failing=new InvestmentApplicationService(persistence,audit,Clock.systemUTC(),ZoneId.of("America/Sao_Paulo"));
        var tx=new TransactionTemplate(transactions);
        assertThatThrownBy(()->tx.executeWithoutResult(ignored->failing.execute(f.user(),command(f),key,UUID.randomUUID(),"/transactions")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?",BigDecimal.class,f.wallet()))
                .isEqualByComparingTo("1000.00");
        for(String table:new String[]{"transacoes","posicoes","carteira_snapshots","transacoes_idempotencia"})
            assertThat(jdbc.queryForObject("SELECT count(*) FROM "+table+" WHERE carteira_id=?",Long.class,f.wallet())).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM logs_auditoria WHERE usuario_id=? AND tipo_evento='COMPRA'",Long.class,f.user())).isZero();
        assertThat(realUseCase.execute(f.user(),command(f),key,UUID.randomUUID(),"/transactions").resultingCashBalance())
                .isEqualByComparingTo("900.00");
    }

    private InvestmentPersistencePort failingPersistence(Stage stage){
        String target=switch(stage){case POSITION->"insertPosition";case TRANSACTION->"insertTransaction";
            case SNAPSHOT->"composeInvestmentSnapshot";case COMPLETE->"complete";case AUDIT->"none";};
        return (InvestmentPersistencePort)Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{InvestmentPersistencePort.class},(proxy,method,args)->{
                    try{Object result=method.invoke(realPersistence,args);if(method.getName().equals(target))throw new IllegalStateException(target+" failure");return result;}
                    catch(InvocationTargetException e){throw e.getCause();}
                });
    }
    private Fixture fixture(){UUID user=UUID.randomUUID(),wallet=UUID.randomUUID(),asset=UUID.randomUUID(),broker=UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios(id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,'ROLE_USER',true)",user,"Rollback","investment-rollback-"+user+"@test","hash");
        jdbc.update("INSERT INTO carteiras(id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,1000)",wallet,user,"Principal");
        jdbc.update("INSERT INTO acoes(id,ticker,nome,tipo,mercado,moeda,ativo) VALUES (?,'ROLL4','Rollback Asset','ACAO','B3','BRL',true)",asset);
        jdbc.update("INSERT INTO corretoras(id,cnpj,razao_social,cep,logradouro,bairro,cidade,uf,ativo,criado_em,atualizado_em) VALUES (?,'77665544000133','Broker','01001000','Rua','Centro','Sao Paulo','SP',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",broker);
        return new Fixture(user,wallet,asset,broker);}
    private InvestmentTransactionCommand command(Fixture f){return new InvestmentTransactionCommand(f.asset(),f.broker(),TipoTransacao.BUY,BigDecimal.ONE,new BigDecimal("100"),BigDecimal.ZERO,OffsetDateTime.parse("2026-09-11T12:00:00-03:00"),null);}
    enum Stage{POSITION,TRANSACTION,SNAPSHOT,AUDIT,COMPLETE}
    record Fixture(UUID user,UUID wallet,UUID asset,UUID broker){}
}
