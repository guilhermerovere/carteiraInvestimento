package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.*;
import com.carteira.carteiraInvestimento.application.port.*; import com.carteira.carteiraInvestimento.application.service.BrokerUseCase;
import java.sql.*; import java.util.*; import javax.sql.DataSource;
import org.junit.jupiter.api.*; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.context.SpringBootTest; import org.springframework.boot.test.context.TestConfiguration; import org.springframework.context.annotation.*; import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest @Import(BrokerTransactionBoundaryIT.Fakes.class)
class BrokerTransactionBoundaryIT extends PostgreSqlContainerSupport {
    @Autowired BrokerUseCase brokers; @Autowired DataSource dataSource;
    @BeforeEach void clean() throws Exception {try(Connection c=dataSource.getConnection();Statement s=c.createStatement()){s.executeUpdate("DELETE FROM logs_auditoria WHERE tipo_evento LIKE 'CORRETORA_%'");s.executeUpdate("DELETE FROM corretoras");}}
    @Test void allProvidersRunOutsideDatabaseTransactionAndLocalPhaseCommits() throws Exception {var result=brokers.criar("19.131.243/0001-97",null,null,null,UUID.randomUUID());assertThat(result.cnpj()).isEqualTo("19131243000197");try(Connection c=dataSource.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT count(*) FROM corretoras")){r.next();assertThat(r.getLong(1)).isEqualTo(1);}}
    @TestConfiguration(proxyBeanMethods=false) static class Fakes {
        @Bean @Primary ReceitaFederalPort receita(){return c->{assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();return Optional.of(new CadastroCnpj(c,"XP",null,"ATIVA","01310100"));};}
        @Bean @Primary CvmPort cvm(){return c->{assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();return Optional.of(new RegistroCvm(c,true));};}
        @Bean @Primary ViaCepPort cep(){return c->{assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();return new EnderecoPostal("01310-100","Paulista","Bela Vista","São Paulo","SP");};}
    }
}
