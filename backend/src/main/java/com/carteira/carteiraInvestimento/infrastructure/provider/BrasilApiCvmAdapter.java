package com.carteira.carteiraInvestimento.infrastructure.provider;

import com.carteira.carteiraInvestimento.application.port.CvmPort;
import com.carteira.carteiraInvestimento.application.port.RegistroCvm;
import com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException;
import feign.FeignException;
import java.util.Locale;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
public class BrasilApiCvmAdapter implements CvmPort {
    private static final String ACTIVE_STATUS="EM FUNCIONAMENTO NORMAL";
    private final BrasilApiCvmClient client;
    public BrasilApiCvmAdapter(BrasilApiCvmClient client){this.client=client;}
    @Override public Optional<RegistroCvm> consultar(String cnpj){
        try {
            CvmProviderResponse r=client.get(cnpj);
            if(r==null||r.cnpj()==null||r.status()==null||r.status().isBlank())throw new BrokerUpstreamException("Invalid CVM provider response");
            return Optional.of(new RegistroCvm(r.cnpj(),ACTIVE_STATUS.equals(r.status().trim().toUpperCase(Locale.ROOT))));
        } catch(FeignException exception){if(exception.status()==404)return Optional.empty();throw new BrokerUpstreamException("CVM provider unavailable",exception);}
        catch(BrokerUpstreamException exception){throw exception;}catch(RuntimeException exception){throw new BrokerUpstreamException("Invalid CVM provider response",exception);}
    }
}
@FeignClient(name="brasilApiCvmClient",url="${application.broker-catalog.brasil-api-cvm.url}")
interface BrasilApiCvmClient { @GetMapping("/api/cvm/corretoras/v1/{cnpj}") CvmProviderResponse get(@PathVariable("cnpj") String cnpj); }
record CvmProviderResponse(String cnpj,String status) { }
