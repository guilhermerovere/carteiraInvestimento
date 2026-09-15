package com.carteira.carteiraInvestimento.integration;
import static org.assertj.core.api.Assertions.*;
import com.carteira.carteiraInvestimento.application.port.*;
import com.carteira.carteiraInvestimento.application.service.BrokerUpstreamException;
import com.carteira.carteiraInvestimento.infrastructure.config.BrokerProviderProperties;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class BrokerProviderAdaptersIT extends PostgreSqlContainerSupport {
    static final MockWebServer SERVER;
    static { try { SERVER=new MockWebServer(); SERVER.start(); System.setProperty("broker.test.url",SERVER.url("/").toString()); } catch(Exception e){throw new ExceptionInInitializerError(e);} }
    @DynamicPropertySource static void providers(DynamicPropertyRegistry registry){String url=SERVER.url("/").toString();registry.add("application.broker-catalog.brasil-api-cnpj.url",()->url);registry.add("application.broker-catalog.brasil-api-cvm.url",()->url);registry.add("application.broker-catalog.via-cep.url",()->url);}
    @Autowired ReceitaFederalPort receita; @Autowired CvmPort cvm; @Autowired ViaCepPort viaCep; @Autowired BrokerProviderProperties properties; @Autowired Environment environment;
    @BeforeEach void drainRequests() throws Exception { while(SERVER.takeRequest(10,TimeUnit.MILLISECONDS)!=null){} }
    @AfterAll static void stop() throws Exception { System.clearProperty("broker.test.url"); SERVER.shutdown(); }
    @Test void decodesTheEstablishedCnpjProviders() throws Exception {
        assertThat(properties.brasilApiCnpj().url()).isEqualTo(SERVER.url("/").toString());assertThat(environment.getProperty("spring.cloud.openfeign.client.config.default.connectTimeout")).isEqualTo("4000");
        SERVER.enqueue(json("{\"cnpj\":\"19131243000197\",\"razao_social\":\"XP\",\"nome_fantasia\":\"XP\",\"descricao_situacao_cadastral\":\"ATIVA\",\"cep\":\"01310100\"}"));
        assertThat(receita.consultar("19131243000197")).get().extracting(CadastroCnpj::razaoSocial).isEqualTo("XP");assertThat(SERVER.takeRequest().getPath()).isEqualTo("/api/cnpj/v1/19131243000197");
        SERVER.enqueue(json("{\"cnpj\":\"19131243000197\",\"status\":\"EM FUNCIONAMENTO NORMAL\"}"));
        assertThat(cvm.consultar("19131243000197")).contains(new RegistroCvm("19131243000197",true));assertThat(SERVER.takeRequest().getPath()).isEqualTo("/api/cvm/corretoras/v1/19131243000197");
        SERVER.enqueue(json("{\"cep\":\"01310-100\",\"logradouro\":\"Avenida Paulista\",\"bairro\":\"Bela Vista\",\"localidade\":\"Sao Paulo\",\"uf\":\"SP\"}"));
        assertThat(viaCep.consultar("01310100").cidade()).isEqualTo("Sao Paulo");assertThat(SERVER.takeRequest().getPath()).isEqualTo("/ws/01310100/json/");
    }
    @Test void mapsCnpj404ToSemanticAbsence(){SERVER.enqueue(new MockResponse().setResponseCode(404));assertThat(receita.consultar("19131243000197")).isEmpty();}
    @Test void mapsCvm404ToSemanticAbsence(){SERVER.enqueue(new MockResponse().setResponseCode(404));assertThat(cvm.consultar("19131243000197")).isEmpty();}
    @Test void mapsProviderFailureToTechnicalFailure(){SERVER.enqueue(new MockResponse().setResponseCode(500));assertThatThrownBy(()->cvm.consultar("19131243000197")).isInstanceOf(BrokerUpstreamException.class);}
    private static MockResponse json(String body){return new MockResponse().setHeader("Content-Type","application/json").setBody(body);}
}
