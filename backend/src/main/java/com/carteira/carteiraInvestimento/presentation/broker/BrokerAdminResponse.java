package com.carteira.carteiraInvestimento.presentation.broker;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.time.Instant;
import java.util.UUID;
public record BrokerAdminResponse(UUID id,String cnpj,String razaoSocial,String nomeFantasia,String cep,String logradouro,
        String bairro,String cidade,String uf,String numero,String complemento,boolean ativo,Instant criadoEm,Instant atualizadoEm){
    static BrokerAdminResponse from(Corretora b){return new BrokerAdminResponse(b.id(),b.cnpj(),b.razaoSocial(),b.nomeFantasia(),b.cep(),b.logradouro(),b.bairro(),b.cidade(),b.uf(),b.numero(),b.complemento(),b.ativo(),b.criadoEm(),b.atualizadoEm());}
}
