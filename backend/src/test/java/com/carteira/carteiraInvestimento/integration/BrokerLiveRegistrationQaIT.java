package com.carteira.carteiraInvestimento.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** Opt-in network QA: mvnw.cmd -DliveBrokerQa=true -Dit.test=BrokerLiveRegistrationQaIT verify. */
@Tag("live")
@EnabledIfSystemProperty(named = "liveBrokerQa", matches = "true")
@AutoConfigureMockMvc
@SpringBootTest
class BrokerLiveRegistrationQaIT extends PostgreSqlContainerSupport {
    static {
        System.setProperty("broker.test.cnpj.url", "https://brasilapi.com.br");
        System.setProperty("broker.test.cvm.url", "https://brasilapi.com.br");
        System.setProperty("broker.test.cep.url", "https://viacep.com.br");
    }
    @Autowired MockMvc mvc;
    @Autowired DataSource dataSource;

    @DynamicPropertySource
    static void publicProviders(DynamicPropertyRegistry registry) {
        registry.add("application.broker-catalog.brasil-api-cnpj.url", () -> "https://brasilapi.com.br");
        registry.add("application.broker-catalog.brasil-api-cvm.url", () -> "https://brasilapi.com.br");
        registry.add("application.broker-catalog.via-cep.url", () -> "https://viacep.com.br");
    }

    @BeforeEach
    void clean() throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM logs_auditoria WHERE tipo_evento LIKE 'CORRETORA_%'");
            statement.executeUpdate("DELETE FROM corretoras");
        }
    }

    @Test
    void registersLiveProvidersAndSeparatesDuplicateFromRegulatoryAbsence() throws Exception {
        create("02.332.886/0001-04").andExpect(status().isCreated());
        create("74.014.747/0001-35").andExpect(status().isCreated());
        create("43.815.158/0001-22").andExpect(status().isCreated());
        create("02.332.886/0001-04").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BROKER_ALREADY_REGISTERED"));
        create("12.345.678/0001-95").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Este CNPJ não corresponde a uma corretora válida."));
    }

    private org.springframework.test.web.servlet.ResultActions create(String cnpj) throws Exception {
        return mvc.perform(post("/api/v1/corretoras").with(user("live-admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"cnpj\":\"" + cnpj + "\"}"));
    }
}
