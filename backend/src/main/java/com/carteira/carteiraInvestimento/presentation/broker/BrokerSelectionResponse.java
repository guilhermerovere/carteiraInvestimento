package com.carteira.carteiraInvestimento.presentation.broker;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.util.UUID;
public record BrokerSelectionResponse(UUID id,String cnpj,String razaoSocial,String nomeFantasia,boolean ativo){
    static BrokerSelectionResponse from(Corretora b){return new BrokerSelectionResponse(b.id(),b.cnpj(),b.razaoSocial(),b.nomeFantasia(),b.ativo());}
}
