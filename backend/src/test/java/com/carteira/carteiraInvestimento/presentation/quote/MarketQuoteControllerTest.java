package com.carteira.carteiraInvestimento.presentation.quote;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.HistoricoCotacaoPage;
import com.carteira.carteiraInvestimento.application.service.InactiveAssetRefreshException;
import com.carteira.carteiraInvestimento.application.service.MarketQuoteUseCase;
import com.carteira.carteiraInvestimento.application.service.QuoteIntegrationException;
import com.carteira.carteiraInvestimento.application.service.QuoteNotFoundException;
import com.carteira.carteiraInvestimento.domain.asset.Moeda;
import com.carteira.carteiraInvestimento.domain.quote.Cotacao;
import com.carteira.carteiraInvestimento.domain.quote.QuoteProvider;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import com.carteira.carteiraInvestimento.presentation.error.GlobalExceptionHandler;
import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MarketQuoteControllerTest {
	private MarketQuoteUseCase useCase;
	private MockMvc mvc;
	private final UUID id = UUID.randomUUID();
	private final TestingAuthenticationToken user = new TestingAuthenticationToken("user", "n/a", "ROLE_USER");
	private final TestingAuthenticationToken admin = new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN");

	@BeforeEach
	void setup() {
		useCase = mock(MarketQuoteUseCase.class);
		mvc = MockMvcBuilders.standaloneSetup(new MarketQuoteController(useCase))
				.setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailFactory(),
						new AccessDeniedAuditingService(event -> {})))
				.addFilter(new CorrelationIdFilter()).build();
	}

	@Test
	void exposesOnlyStableQuoteFieldsForGetAndRefresh() throws Exception {
		Cotacao quote = quote();
		when(useCase.cotacaoAtual(id)).thenReturn(quote);
		when(useCase.atualizarCotacao(id)).thenReturn(quote);
		for (var request : List.of(get("/api/v1/acoes/{id}/cotacao", id).principal(user),
				put("/api/v1/acoes/{id}/atualizar-cotacao", id).principal(admin))) {
			mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(quote.id().toString()))
					.andExpect(jsonPath("$.preco").value(123.4568)).andExpect(jsonPath("$.provider").value("BRAPI"))
					.andExpect(jsonPath("$.payload").doesNotExist()).andExpect(header().exists("X-Correlation-ID"));
		}
	}

	@Test
	void historyDefaultsToOwnEmptyEnvelopeAndValidatesPagination() throws Exception {
		when(useCase.historico(id, 0, 20, false)).thenReturn(new HistoricoCotacaoPage(List.of(), 0, 20, 0, 0));
		mvc.perform(get("/api/v1/acoes/{id}/historico", id).principal(user)).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isEmpty()).andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20)).andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.totalPages").value(0));
		when(useCase.historico(any(), anyInt(), anyInt(), anyBoolean())).thenThrow(new IllegalArgumentException());
		mvc.perform(get("/api/v1/acoes/{id}/historico?page=-1", id).principal(user))
				.andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
	}

	@Test
	void invalidUuidIs400WithProblemDetailAndCorrelationId() throws Exception {
		mvc.perform(get("/api/v1/acoes/abc/cotacao").principal(user)).andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(header().exists("X-Correlation-ID"));
		mvc.perform(get("/api/v1/acoes/abc/historico").principal(user)).andExpect(status().isBadRequest());
		mvc.perform(put("/api/v1/acoes/abc/atualizar-cotacao").principal(admin)).andExpect(status().isBadRequest());
	}

	@Test
	void maps404409And502WithoutInternalDetails() throws Exception {
		doThrow(new QuoteNotFoundException()).when(useCase).cotacaoAtual(id);
		mvc.perform(get("/api/v1/acoes/{id}/cotacao", id).principal(user)).andExpect(status().isNotFound());
		doThrow(new InactiveAssetRefreshException()).when(useCase).atualizarCotacao(id);
		mvc.perform(put("/api/v1/acoes/{id}/atualizar-cotacao", id).principal(admin)).andExpect(status().isConflict());
		doThrow(new QuoteIntegrationException("secret-key-value")).when(useCase).cotacaoAtual(id);
		mvc.perform(get("/api/v1/acoes/{id}/cotacao", id).principal(user)).andExpect(status().isBadGateway())
				.andExpect(content().string(not(containsString("secret-key-value"))));
	}

	private Cotacao quote() {
		return new Cotacao(UUID.randomUUID(), id, new BigDecimal("123.4568"), Moeda.BRL,
				Instant.parse("2026-09-08T18:00:00Z"), QuoteProvider.BRAPI, Instant.parse("2026-09-08T18:00:01Z"));
	}
}
