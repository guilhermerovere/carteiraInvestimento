package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.*;
import com.carteira.carteiraInvestimento.application.port.CorretoraPort;
import com.carteira.carteiraInvestimento.application.port.CorretoraReadScope;
import com.carteira.carteiraInvestimento.application.service.*;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.sql.*; import java.time.*; import java.util.*; import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*; import org.postgresql.util.PSQLException; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BrokerCatalogPersistenceIT extends PostgreSqlContainerSupport {
    @Autowired DataSource dataSource; @Autowired BrokerLocalTransactionService local; @Autowired CorretoraPort port;
    Clock clock=Clock.fixed(Instant.parse("2026-09-10T10:00:00Z"),ZoneOffset.UTC);
    @BeforeEach void clean() throws Exception {execute("DELETE FROM logs_auditoria WHERE tipo_evento LIKE 'CORRETORA_%'");execute("DELETE FROM corretoras");}
    @Test void v7CreatesConstraintsAndHibernateValidates() throws Exception {assertThat(count("SELECT count(*) FROM flyway_schema_history WHERE version='7' AND success")).isEqualTo(1);assertThat(count("SELECT count(*) FROM pg_constraint WHERE conname='uk_corretoras_cnpj'")).isEqualTo(1);}
    @Test void databaseEnforcesCanonicalChecksAndLimits() throws Exception {
        assertConstraint("1913124300019X","01310100","SP","Empresa","ck_corretoras_cnpj_canonico");
        assertConstraint("19131243000197","01310X00","SP","Empresa","ck_corretoras_cep_canonico");
        assertConstraint("19131243000197","01310100","sp","Empresa","ck_corretoras_uf_canonica");
        assertConstraint("19131243000197","01310100","SP","x".repeat(256),null);
    }
    @Test void concurrentCreationProducesOneRowOneConflictAndOneAudit() throws Exception {
        CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);try(var executor=Executors.newFixedThreadPool(2)){
            var a=executor.submit(()->create(ready,start));var b=executor.submit(()->create(ready,start));ready.await();start.countDown();List<Object> results=List.of(a.get(),b.get());
            assertThat(results.stream().filter(x->x instanceof DuplicateBrokerException)).hasSize(1);assertThat(results.stream().filter("ok"::equals)).hasSize(1);
        }
        assertThat(count("SELECT count(*) FROM corretoras")).isEqualTo(1);assertThat(count("SELECT count(*) FROM logs_auditoria WHERE tipo_evento='CORRETORA_CRIADA'")).isEqualTo(1);
    }
    @Test void lifecycleNoOpDoesNotUpdateTimestampOrAudit() throws Exception {Corretora saved=local.create(broker(),null,UUID.randomUUID());Corretora same=local.lifecycle(saved.id(),true,null,UUID.randomUUID());assertThat(same.atualizadoEm()).isEqualTo(saved.atualizadoEm());assertThat(count("SELECT count(*) FROM logs_auditoria WHERE tipo_evento='CORRETORA_ATIVADA'")).isZero();}
    @Test void realEditsAndLifecycleAuditOnceWhileNormalizedNoOpsDoNotWrite() throws Exception {Corretora saved=local.create(broker(),null,UUID.randomUUID());Corretora edited=local.edit(saved.id(),new BrokerPatch(true," 99 ",true," Sala 2 "),null,UUID.randomUUID());assertThat(edited.numero()).isEqualTo("99");assertThat(edited.atualizadoEm()).isAfter(saved.atualizadoEm());Corretora persistedEdit=port.findById(saved.id(),CorretoraReadScope.ALL).orElseThrow();Corretora noOp=local.edit(saved.id(),new BrokerPatch(true,"99",true,"Sala 2"),null,UUID.randomUUID());assertThat(noOp.atualizadoEm()).isEqualTo(persistedEdit.atualizadoEm());assertThat(count("SELECT count(*) FROM logs_auditoria WHERE tipo_evento='CORRETORA_EDITADA'")).isEqualTo(1);Corretora inactive=local.lifecycle(saved.id(),false,null,UUID.randomUUID());Corretora persistedLifecycle=port.findById(saved.id(),CorretoraReadScope.ALL).orElseThrow();Corretora lifecycleNoOp=local.lifecycle(saved.id(),false,null,UUID.randomUUID());assertThat(lifecycleNoOp.atualizadoEm()).isEqualTo(persistedLifecycle.atualizadoEm());assertThat(count("SELECT count(*) FROM logs_auditoria WHERE tipo_evento='CORRETORA_DESATIVADA'")).isEqualTo(1);}
    @Test void auditFailureRollsBackRealMutation() throws Exception {
        Corretora saved=local.create(broker(),null,UUID.randomUUID());
        execute("CREATE OR REPLACE FUNCTION reject_broker_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN IF NEW.tipo_evento='CORRETORA_EDITADA' THEN RAISE EXCEPTION 'audit rejected'; END IF; RETURN NEW; END $$");
        execute("CREATE TRIGGER reject_broker_audit_trigger BEFORE INSERT ON logs_auditoria FOR EACH ROW EXECUTE FUNCTION reject_broker_audit()");
        try { assertThatThrownBy(()->local.edit(saved.id(),new BrokerPatch(true,"99",false,null),null,UUID.randomUUID())).isInstanceOf(RuntimeException.class); assertThat(text("SELECT numero FROM corretoras WHERE id='"+saved.id()+"'")).isNull(); }
        finally { execute("DROP TRIGGER IF EXISTS reject_broker_audit_trigger ON logs_auditoria");execute("DROP FUNCTION IF EXISTS reject_broker_audit()"); }
    }
    private Object create(CountDownLatch ready,CountDownLatch start){try{ready.countDown();start.await();local.create(broker(),null,UUID.randomUUID());return "ok";}catch(DuplicateBrokerException e){return e;}catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}}
    private Corretora broker(){return Corretora.nova("19131243000197","XP Investimentos",null,"01310100","Avenida Paulista","Bela Vista","São Paulo","SP",null,null,clock);}
    private void execute(String sql)throws Exception{try(Connection c=dataSource.getConnection();Statement s=c.createStatement()){s.executeUpdate(sql);}}
    private long count(String sql)throws Exception{try(Connection c=dataSource.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getLong(1);}}
    private String text(String sql)throws Exception{try(Connection c=dataSource.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getString(1);}}
    private void assertConstraint(String cnpj,String cep,String uf,String razao,String named){assertThatThrownBy(()->{try(Connection c=dataSource.getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO corretoras(id,cnpj,razao_social,cep,logradouro,bairro,cidade,uf,ativo,criado_em,atualizado_em) VALUES (?,?,?,?,?,?,?,?,true,now(),now())")){s.setObject(1,UUID.randomUUID());s.setString(2,cnpj);s.setString(3,razao);s.setString(4,cep);s.setString(5,"Rua");s.setString(6,"Bairro");s.setString(7,"Cidade");s.setString(8,uf);s.executeUpdate();}}).isInstanceOf(PSQLException.class).satisfies(e->{if(named!=null)assertThat(((PSQLException)e).getServerErrorMessage().getConstraint()).isEqualTo(named);});}
}
