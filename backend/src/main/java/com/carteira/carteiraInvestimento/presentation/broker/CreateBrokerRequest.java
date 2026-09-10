package com.carteira.carteiraInvestimento.presentation.broker;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
public final class CreateBrokerRequest {
    @NotBlank private final String cnpj; private final String numero; private final String complemento;
    @JsonCreator public CreateBrokerRequest(@JsonProperty("cnpj") String cnpj,@JsonProperty("numero") String numero,@JsonProperty("complemento") String complemento){this.cnpj=cnpj;this.numero=numero;this.complemento=complemento;}
    @JsonAnySetter public void rejectUnknown(String field,Object value){throw new IllegalArgumentException("unknown field");}
    public String cnpj(){return cnpj;} public String numero(){return numero;} public String complemento(){return complemento;}
}
