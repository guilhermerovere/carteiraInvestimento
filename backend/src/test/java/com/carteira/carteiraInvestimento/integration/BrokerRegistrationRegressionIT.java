package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.*;

import com.carteira.carteiraInvestimento.application.port.CorretoraPort;
import com.carteira.carteiraInvestimento.application.port.CorretoraReadScope;
import com.carteira.carteiraInvestimento.application.service.BrokerApplicationService;
import com.carteira.carteiraInvestimento.application.service.BrokerComplianceException;
import com.carteira.carteiraInvestimento.application.service.DuplicateBrokerException;
import java.util.Map;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Exercises the real persistence path; only upstream HTTP is deterministic. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class BrokerRegistrationRegressionIT extends PostgreSqlContainerSupport {
    private static final MockWebServer PROVIDERS = startProviders();
    private static final Map<String, String> BROKERS = Map.of(
            "02332886000104", "XP INVESTIMENTOS CCTVM S.A.",
            "74014747000135", "AGORA CTVM S.A.",
            "43815158000122", "BTG PACTUAL CTVM S/A");

    @Autowired BrokerApplicationService service;
    @Autowired CorretoraPort repository;

    @DynamicPropertySource
    static void providerProperties(DynamicPropertyRegistry registry) {
        String url = PROVIDERS.url("/").toString();
        registry.add("application.broker-catalog.brasil-api-cnpj.url", () -> url);
        registry.add("application.broker-catalog.brasil-api-cvm.url", () -> url);
        registry.add("application.broker-catalog.via-cep.url", () -> url);
    }

    @BeforeEach
    void clean() throws Exception {
        try (var connection = POSTGRES.createConnection("" ); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM logs_auditoria WHERE tipo_evento LIKE 'CORRETORA_%'");
            statement.executeUpdate("DELETE FROM corretoras");
        }
    }

    @Test
    void emptyDatabaseRegistersEachRecognizedBrokerByExactCnpjOnly() {
        BROKERS.keySet().forEach(cnpj -> service.criar(mask(cnpj), null, null, null, UUID.randomUUID()));

        assertThat(repository.list(0, 20, CorretoraReadScope.ALL).items())
                .extracting(broker -> broker.cnpj())
                .containsExactlyInAnyOrderElementsOf(BROKERS.keySet());
        assertThatThrownBy(() -> service.criar("02.332.886/0001-04", null, null, null, UUID.randomUUID()))
                .isInstanceOf(DuplicateBrokerException.class);
        assertThatThrownBy(() -> service.criar("12.345.678/0001-95", null, null, null, UUID.randomUUID()))
                .isInstanceOf(BrokerComplianceException.class);
    }

    @AfterAll
    static void stopProviders() throws Exception {
        System.clearProperty("broker.test.url");
        PROVIDERS.shutdown();
    }

    private static MockWebServer startProviders() {
        try {
            MockWebServer server = new MockWebServer();
            server.setDispatcher(new okhttp3.mockwebserver.Dispatcher() {
                @Override public MockResponse dispatch(RecordedRequest request) {
                    String path = request.getPath();
                    String cnpj = path.substring(path.lastIndexOf('/') + 1);
                    if (path.startsWith("/api/cnpj/")) return json("{\"cnpj\":\"" + cnpj + "\",\"razao_social\":\"" + BROKERS.getOrDefault(cnpj, "EMPRESA NAO CORRETORA") + "\",\"descricao_situacao_cadastral\":\"ATIVA\",\"cep\":\"01310100\"}");
                    if (path.startsWith("/api/cvm/")) return BROKERS.containsKey(cnpj)
                            ? json("{\"cnpj\":\"" + cnpj + "\",\"status\":\"EM FUNCIONAMENTO NORMAL\"}")
                            : new MockResponse().setResponseCode(404);
                    if (path.startsWith("/ws/")) return json("{\"cep\":\"01310-100\",\"logradouro\":\"Avenida Paulista\",\"bairro\":\"Bela Vista\",\"localidade\":\"Sao Paulo\",\"uf\":\"SP\"}");
                    return new MockResponse().setResponseCode(404);
                }
            });
            server.start();
            System.setProperty("broker.test.url", server.url("/").toString());
            return server;
        } catch (Exception exception) { throw new ExceptionInInitializerError(exception); }
    }

    private static MockResponse json(String body) { return new MockResponse().setHeader("Content-Type", "application/json").setBody(body); }
    private static String mask(String cnpj) { return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12); }
}
