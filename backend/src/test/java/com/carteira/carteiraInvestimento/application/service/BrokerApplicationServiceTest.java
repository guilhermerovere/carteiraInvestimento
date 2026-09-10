package com.carteira.carteiraInvestimento.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.carteira.carteiraInvestimento.application.port.*;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.time.*; import java.util.*;
import org.junit.jupiter.api.*; import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.mockito.InOrder;

class BrokerApplicationServiceTest {
    CorretoraPort brokers=mock(CorretoraPort.class); ReceitaFederalPort receita=mock(ReceitaFederalPort.class); CvmPort cvm=mock(CvmPort.class);
    ViaCepPort cep=mock(ViaCepPort.class); BrokerLocalTransactionService local=mock(BrokerLocalTransactionService.class); AuditoriaIsoladaPort audit=mock(AuditoriaIsoladaPort.class);
    Clock clock=Clock.fixed(Instant.parse("2026-09-10T10:00:00Z"),ZoneOffset.UTC); BrokerApplicationService service;
    @BeforeEach void setup(){service=new BrokerApplicationService(brokers,receita,cvm,cep,local,audit,clock);}
    @Test void invalidLocalInputCallsNoProvider(){assertThatThrownBy(()->service.criar("123",null,null,null,UUID.randomUUID())).isInstanceOf(IllegalArgumentException.class);verifyNoInteractions(receita,cvm,cep);}
    @Test void callsProvidersInOrderWithoutTransactionAndBuildsOfficialBroker(){
        String c="19131243000197"; when(receita.consultar(c)).thenAnswer(i->{assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();return Optional.of(new CadastroCnpj(c," XP "," XP ","ATIVA","01310-100"));});
        when(cvm.consultar(c)).thenAnswer(i->{assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();return Optional.of(new RegistroCvm(c,true));});
        when(cep.consultar("01310100")).thenAnswer(i->{assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();return new EnderecoPostal("01310-100","Paulista","Bela Vista","São Paulo","SP");});
        when(local.create(any(),isNull(),any())).thenAnswer(i->i.getArgument(0)); UUID correlation=UUID.randomUUID();
        Corretora result=service.criar(c," 42 ",null,null,correlation); assertThat(result.numero()).isEqualTo("42");
        InOrder order=inOrder(receita,cvm,cep,local);order.verify(receita).consultar(c);order.verify(cvm).consultar(c);order.verify(cep).consultar("01310100");order.verify(local).create(any(),isNull(),eq(correlation));
    }
    @Test void regulatoryRejectionIsAuditedAndTechnicalFailureIsNot(){
        String c="19131243000197"; when(receita.consultar(c)).thenReturn(Optional.empty());
        assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerComplianceException.class);verify(audit).recordIsoladamente(any());
        reset(receita,audit);when(receita.consultar(c)).thenThrow(new BrokerUpstreamException("down"));
        assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerUpstreamException.class);verifyNoInteractions(audit);
    }
    @Test void auditFailureTurnsRejectionIntoInternalFailure(){when(receita.consultar(any())).thenReturn(Optional.empty());doThrow(new RuntimeException()).when(audit).recordIsoladamente(any());assertThatThrownBy(()->service.criar("19131243000197",null,null,null,UUID.randomUUID())).isInstanceOf(BrokerAuditException.class);}
    @Test void missingOrOversizedOfficialDataAndInconsistentCepAreTechnical(){
        String c="19131243000197";when(receita.consultar(c)).thenReturn(Optional.of(new CadastroCnpj(c,"x".repeat(256),null,"ATIVA","01310100")));when(cvm.consultar(c)).thenReturn(Optional.of(new RegistroCvm(c,true)));when(cep.consultar("01310100")).thenReturn(new EnderecoPostal("01310100","Rua","Bairro","Cidade","SP"));assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerUpstreamException.class);verifyNoInteractions(local);
        reset(receita,cvm,cep,local);when(receita.consultar(c)).thenReturn(Optional.of(new CadastroCnpj(c,"XP",null,"ATIVA","01310100")));when(cvm.consultar(c)).thenReturn(Optional.of(new RegistroCvm(c,true)));when(cep.consultar("01310100")).thenReturn(new EnderecoPostal("99999999","Rua","Bairro","Cidade","SP"));assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerUpstreamException.class);verifyNoInteractions(local);
    }
    @Test void inactiveRevenueAndMissingOrInactiveCvmAreRegulatory(){
        String c="19131243000197";when(receita.consultar(c)).thenReturn(Optional.of(new CadastroCnpj(c,"XP",null,"BAIXADA","01310100")));assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerComplianceException.class);verifyNoInteractions(cvm,cep,local);
        reset(receita,cvm,cep,local,audit);when(receita.consultar(c)).thenReturn(Optional.of(new CadastroCnpj(c,"XP",null,"ATIVA","01310100")));when(cvm.consultar(c)).thenReturn(Optional.empty());assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerComplianceException.class);verifyNoInteractions(cep,local);
        reset(cvm,audit);when(cvm.consultar(c)).thenReturn(Optional.of(new RegistroCvm(c,false)));assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerComplianceException.class);verifyNoInteractions(cep,local);
    }
    @Test void missingRequiredUpstreamFieldIsTechnical(){String c="19131243000197";when(receita.consultar(c)).thenReturn(Optional.of(new CadastroCnpj(c,"XP",null,"ATIVA","01310100")));when(cvm.consultar(c)).thenReturn(Optional.of(new RegistroCvm(c,true)));when(cep.consultar("01310100")).thenReturn(new EnderecoPostal("01310100",null,"Bairro","Cidade","SP"));assertThatThrownBy(()->service.criar(c,null,null,null,UUID.randomUUID())).isInstanceOf(BrokerUpstreamException.class);verifyNoInteractions(local);}
}
