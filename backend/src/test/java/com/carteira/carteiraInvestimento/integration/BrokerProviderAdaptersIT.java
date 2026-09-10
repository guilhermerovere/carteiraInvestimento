package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.*;
import com.carteira.carteiraInvestimento.application.port.*;
import com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import com.carteira.carteiraInvestimento.infrastructure.config.BrokerProviderProperties;
import org.springframework.core.env.Environment;

@SpringBootTest
class BrokerProviderAdaptersIT extends PostgreSqlContainerSupport {
    static final MockWebServer SERVER;
    static { try { SERVER=new MockWebServer(); SERVER.start(); System.setProperty("broker.test.url",SERVER.url("/").toString()); } catch(Exception e){throw new ExceptionInInitializerError(e);} }
    @DynamicPropertySource static void providers(DynamicPropertyRegistry registry){String url=SERVER.url("/").toString();registry.add("application.broker-catalog.brasil-api-cnpj.url",()->url);registry.add("application.broker-catalog.brasil-api-cvm.url",()->url);registry.add("application.broker-catalog.via-cep.url",()->url);}
    @Autowired ReceitaFederalPort receita; @Autowired CvmPort cvm; @Autowired ViaCepPort viaCep; @Autowired BrokerProviderProperties properties; @Autowired Environment environment;
    @AfterAll static void stop() throws Exception {System.clearProperty("broker.test.url");SERVER.shutdown();}
    @Test void decodesAllThreeProviderContractsLocally() throws Exception {
        assertThat(properties.brasilApiCnpj().url()).isEqualTo(SERVER.url("/").toString());assertThat(environment.getProperty("spring.cloud.openfeign.client.config.default.connectTimeout")).isEqualTo("4000");assertThat(environment.getProperty("spring.cloud.openfeign.client.config.default.readTimeout")).isEqualTo("4000");
        SERVER.enqueue(json("{\"cnpj\":\"19131243000197\",\"razao_social\":\"XP\",\"nome_fantasia\":\"XP\",\"descricao_situacao_cadastral\":\"ATIVA\",\"cep\":\"01310100\"}"));
        assertThat(receita.consultar("19131243000197")).get().extracting(CadastroCnpj::razaoSocial).isEqualTo("XP"); assertThat(SERVER.takeRequest().getPath()).isEqualTo("/api/cnpj/v1/19131243000197");
        SERVER.enqueue(json("{\"cnpj\":\"19131243000197\",\"status\":\"EM FUNCIONAMENTO NORMAL\"}"));
        assertThat(cvm.consultar("19131243000197")).get().extracting(RegistroCvm::ativo).isEqualTo(true); assertThat(SERVER.takeRequest().getPath()).isEqualTo("/api/cvm/corretoras/v1/19131243000197");
        SERVER.enqueue(json("{\"cep\":\"01310-100\",\"logradouro\":\"Avenida Paulista\",\"bairro\":\"Bela Vista\",\"localidade\":\"São Paulo\",\"uf\":\"SP\"}"));
        assertThat(viaCep.consultar("01310100").cidade()).isEqualTo("São Paulo"); assertThat(SERVER.takeRequest().getPath()).isEqualTo("/ws/01310100/json/");
    }
    @Test void mapsCnpjAndCvm404ToSemanticAbsence(){SERVER.enqueue(new MockResponse().setResponseCode(404));assertThat(receita.consultar("19131243000197")).isEmpty();SERVER.enqueue(new MockResponse().setResponseCode(404));assertThat(cvm.consultar("19131243000197")).isEmpty();}
    @Test void mapsFiveHundredMalformedAndViaCepErroToTechnicalFailure(){
        SERVER.enqueue(new MockResponse().setResponseCode(500));assertThatThrownBy(()->receita.consultar("19131243000197")).isInstanceOf(BrokerUpstreamException.class);
        SERVER.enqueue(json("not-json"));assertThatThrownBy(()->cvm.consultar("19131243000197")).isInstanceOf(BrokerUpstreamException.class);
        SERVER.enqueue(json("{\"erro\":true}"));assertThatThrownBy(()->viaCep.consultar("01310100")).isInstanceOf(BrokerUpstreamException.class);
    }
    @Test void configuredFourSecondReadTimeoutMapsToTechnicalFailure(){SERVER.enqueue(json("{}").setBodyDelay(5,TimeUnit.SECONDS));long before=System.nanoTime();assertThatThrownBy(()->receita.consultar("19131243000197")).isInstanceOf(BrokerUpstreamException.class);long millis=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-before);assertThat(millis).isBetween(3500L,5500L);}
    private static MockResponse json(String body){return new MockResponse().setHeader("Content-Type","application/json").setBody(body);}
}
