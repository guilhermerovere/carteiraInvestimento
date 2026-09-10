package com.carteira.carteiraInvestimento.presentation.broker;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;
public final class UpdateBrokerRequest {
    private boolean numeroPresent; private String numero; private boolean complementoPresent; private String complemento;
    @JsonSetter("numero") public void setNumero(String value){numeroPresent=true;numero=value;}
    @JsonSetter("complemento") public void setComplemento(String value){complementoPresent=true;complemento=value;}
    @JsonAnySetter public void rejectUnknown(String field,Object value){throw new IllegalArgumentException("unknown field");}
    public boolean numeroPresent(){return numeroPresent;} public String numero(){return numero;}
    public boolean complementoPresent(){return complementoPresent;} public String complemento(){return complemento;}
}
