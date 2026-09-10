package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.EnderecoPostal;
import com.carteira.carteiraInvestimento.application.port.ViaCepPort;
import com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException;
import com.fasterxml.jackson.annotation.JsonProperty;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
public class ViaCepAdapter implements ViaCepPort {
    private final ViaCepClient client;
    public ViaCepAdapter(ViaCepClient client){this.client=client;}
    @Override public EnderecoPostal consultar(String cep){
        try {
            ViaCepProviderResponse r=client.get(cep);
            if(r==null||Boolean.TRUE.equals(r.erro()))throw new BrokerUpstreamException("CEP is unavailable");
            return new EnderecoPostal(r.cep(),r.logradouro(),r.bairro(),r.localidade(),r.uf());
        }catch(FeignException exception){throw new BrokerUpstreamException("CEP provider unavailable",exception);}
        catch(BrokerUpstreamException exception){throw exception;}catch(RuntimeException exception){throw new BrokerUpstreamException("Invalid CEP provider response",exception);}
    }
}
@FeignClient(name="viaCepClient",url="${application.broker-catalog.via-cep.url}")
interface ViaCepClient { @GetMapping("/ws/{cep}/json/") ViaCepProviderResponse get(@PathVariable("cep") String cep); }
record ViaCepProviderResponse(String cep,String logradouro,String bairro,String localidade,String uf,@JsonProperty("erro") Boolean erro) { }
