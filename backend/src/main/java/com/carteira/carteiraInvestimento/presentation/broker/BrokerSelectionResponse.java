package com.carteira.carteiraInvestimento.presentation.broker;
import com.carteira.carteiraInvestimento.domain.broker.Corretora;
import java.util.UUID;
import com.carteira.carteiraInvestimento.domain.shared.LogoProvider;
public record BrokerSelectionResponse(UUID id,String cnpj,String razaoSocial,String nomeFantasia,boolean ativo,LogoProvider logoProvider,String logoReference){
    static BrokerSelectionResponse from(Corretora b){return new BrokerSelectionResponse(b.id(),b.cnpj(),b.razaoSocial(),b.nomeFantasia(),b.ativo(),b.logoProvider(),b.logoReference());}
}
