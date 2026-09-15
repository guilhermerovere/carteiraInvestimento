package com.carteira.carteiraInvestimento.presentation.fx;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.service.CambioUnavailableException;
import com.carteira.carteiraInvestimento.application.service.CambioUseCase;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import com.carteira.carteiraInvestimento.presentation.error.GlobalExceptionHandler;
import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CambioControllerTest {
	private CambioUseCase useCase; private MockMvc mvc;
	private final TestingAuthenticationToken user = new TestingAuthenticationToken("user", "n/a", "ROLE_USER");
	@BeforeEach void setup() {
		useCase=mock(CambioUseCase.class);
		mvc=MockMvcBuilders.standaloneSetup(new CambioController(useCase))
				.setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailFactory(),new AccessDeniedAuditingService(event->{})))
				.addFilter(new CorrelationIdFilter()).build();
	}
	@Test void exposesOnlySpecifiedFieldsAndPreservesDecimalScale() throws Exception {
		var value=new ObservacaoCambio(UUID.randomUUID(),MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5.12000000"),
				CambioProvider.ALPHA_VANTAGE,Instant.parse("2026-09-10T11:59:00Z"),Instant.parse("2026-09-10T12:00:00Z"));
		when(useCase.obterUsdBrl()).thenReturn(value);
		mvc.perform(get("/api/v1/cambio/usd-brl").principal(user)).andExpect(status().isOk())
				.andExpect(header().exists(CorrelationIdFilter.HEADER)).andExpect(jsonPath("$.id").value(value.id().toString()))
				.andExpect(jsonPath("$.moedaOrigem").value("USD")).andExpect(jsonPath("$.moedaDestino").value("BRL"))
				.andExpect(jsonPath("$.provider").value("ALPHA_VANTAGE"))
				.andExpect(content().string(containsString("\"taxa\":5.12000000")))
				.andExpect(jsonPath("$.payload").doesNotExist()).andExpect(jsonPath("$.apiKey").doesNotExist());
	}
	@Test void mapsProviderToSanitized502AndIndependentFailureTo500WithCorrelation() throws Exception {
		when(useCase.obterUsdBrl()).thenThrow(new CambioUnavailableException("secret-api-key",new RuntimeException("payload")));
		mvc.perform(get("/api/v1/cambio/usd-brl").principal(user)).andExpect(status().isBadGateway())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(header().exists(CorrelationIdFilter.HEADER)).andExpect(jsonPath("$.status").value(502))
				.andExpect(content().string(not(containsString("secret-api-key"))));
		reset(useCase); when(useCase.obterUsdBrl()).thenThrow(new IllegalStateException("database-password"));
		mvc.perform(get("/api/v1/cambio/usd-brl").principal(user)).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500)).andExpect(content().string(not(containsString("database-password"))));
	}
}
