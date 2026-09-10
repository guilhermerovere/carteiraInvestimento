package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CadastroCnpj;
import com.carteira.carteiraInvestimento.application.port.ReceitaFederalPort;
import com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException;
import com.fasterxml.jackson.annotation.JsonProperty;
import feign.FeignException;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
public class BrasilApiCnpjAdapter implements ReceitaFederalPort {
    private final BrasilApiCnpjClient client;
    public BrasilApiCnpjAdapter(BrasilApiCnpjClient client){this.client=client;}
    @Override public Optional<CadastroCnpj> consultar(String cnpj){
        try {
            CnpjProviderResponse r=client.get(cnpj);
            if(r==null)throw new BrokerUpstreamException("Invalid CNPJ provider response");
            return Optional.of(new CadastroCnpj(r.cnpj(),r.razaoSocial(),r.nomeFantasia(),r.situacao(),r.cep()));
        } catch(FeignException exception){ if(exception.status()==404)return Optional.empty(); throw new BrokerUpstreamException("CNPJ provider unavailable",exception); }
        catch(BrokerUpstreamException exception){throw exception;} catch(RuntimeException exception){throw new BrokerUpstreamException("Invalid CNPJ provider response",exception);}
    }
}
@FeignClient(name="brasilApiCnpjClient",url="${application.broker-catalog.brasil-api-cnpj.url}")
interface BrasilApiCnpjClient { @GetMapping("/api/cnpj/v1/{cnpj}") CnpjProviderResponse get(@PathVariable("cnpj") String cnpj); }
record CnpjProviderResponse(String cnpj,@JsonProperty("razao_social") String razaoSocial,
        @JsonProperty("nome_fantasia") String nomeFantasia,@JsonProperty("descricao_situacao_cadastral") String situacao,String cep) { }
