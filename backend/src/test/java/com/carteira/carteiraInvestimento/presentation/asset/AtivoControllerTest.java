package com.carteira.carteiraInvestimento.presentation.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.AtivoPage;
import com.carteira.carteiraInvestimento.application.port.AtivoQuery;
import com.carteira.carteiraInvestimento.application.port.AtivoReadScope;
import com.carteira.carteiraInvestimento.application.port.AtivoSort;
import com.carteira.carteiraInvestimento.application.port.SortDirection;
import com.carteira.carteiraInvestimento.application.service.AtivoNotFoundException;
import com.carteira.carteiraInvestimento.application.service.AtivoUseCase;
import com.carteira.carteiraInvestimento.application.service.DuplicateTickerException;
import com.carteira.carteiraInvestimento.domain.asset.Ativo;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.infrastructure.security.AccessDeniedAuditingService;
import com.carteira.carteiraInvestimento.presentation.error.GlobalExceptionHandler;
import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AtivoControllerTest {
	private AtivoUseCase useCase;
	private MockMvc mvc;
	private TestingAuthenticationToken admin;
	private TestingAuthenticationToken user;

	@BeforeEach
	void setUp() {
		useCase = Mockito.mock(AtivoUseCase.class);
		mvc = MockMvcBuilders.standaloneSetup(new AtivoController(useCase))
				.setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailFactory(),
						new AccessDeniedAuditingService(event -> { })))
				.build();
		admin = new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN");
		user = new TestingAuthenticationToken("user", "n/a", "ROLE_USER");
	}

	@Test
	void createsAndReturnsOnlyTheStableResponseFields() throws Exception {
		Ativo ativo = asset("PETR4", "Petrobras");
		when(useCase.criar(any(), any(), any(), any())).thenReturn(ativo);
		mvc.perform(post("/api/v1/acoes").principal(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ticker\":\" petr4 \",\"nome\":\"Petrobras\",\"tipo\":\"ACAO\",\"mercado\":\"B3\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(ativo.id().toString()))
				.andExpect(jsonPath("$.ticker").value("PETR4")).andExpect(jsonPath("$.moeda").value("BRL"))
				.andExpect(jsonPath("$.ativo").value(true)).andExpect(jsonPath("$.senha").doesNotExist());
	}

	@Test
	void listsInOwnEnvelopeWithDefaultsAndUserActiveOnlyScope() throws Exception {
		when(useCase.listar(any())).thenReturn(new AtivoPage(List.of(asset("PETR4", "Petrobras")), 0, 20, 1, 1));
		mvc.perform(get("/api/v1/acoes?ativo=false").principal(user))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items[0].ticker").value("PETR4"))
				.andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.totalPages").value(1));
		ArgumentCaptor<AtivoQuery> capture = ArgumentCaptor.forClass(AtivoQuery.class);
		verify(useCase).listar(capture.capture());
		assertThat(capture.getValue()).isEqualTo(new AtivoQuery(null, null, AtivoReadScope.ACTIVE_ONLY, 0, 20,
				AtivoSort.TICKER, SortDirection.ASC));
	}

	@Test
	void adminCanApplyInactiveFilterAndQIsTrimmed() throws Exception {
		when(useCase.listar(any())).thenReturn(new AtivoPage(List.of(), 0, 20, 0, 0));
		mvc.perform(get("/api/v1/acoes?ativo=false&tipo=ACAO&sort=nome&direction=desc").param("q", " PeTr ")
				.principal(admin))
				.andExpect(status().isOk());
		ArgumentCaptor<AtivoQuery> capture = ArgumentCaptor.forClass(AtivoQuery.class);
		verify(useCase).listar(capture.capture());
		assertThat(capture.getValue().scope()).isEqualTo(AtivoReadScope.INACTIVE_ONLY);
		assertThat(capture.getValue().q()).isEqualTo("PeTr");
	}

	@Test
	void getsByTickerAndUpdatesNameAndLifecycleIdempotently() throws Exception {
		Ativo ativo = asset("AAPL", "Apple");
		Ativo inactive = ativo.definirAtivo(false);
		when(useCase.consultarPorTicker("aapl", AtivoReadScope.ALL)).thenReturn(ativo);
		when(useCase.atualizarNome(ativo.id(), "Apple Inc.")).thenReturn(ativo.renomear("Apple Inc."));
		when(useCase.definirLifecycle(ativo.id(), false)).thenReturn(inactive);
		mvc.perform(get("/api/v1/acoes/ticker/aapl").principal(admin)).andExpect(status().isOk())
				.andExpect(jsonPath("$.ticker").value("AAPL"));
		mvc.perform(patch("/api/v1/acoes/{id}", ativo.id()).principal(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Apple Inc.\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Apple Inc."));
		for (int i = 0; i < 2; i++) {
			mvc.perform(patch("/api/v1/acoes/{id}/ativo", ativo.id()).principal(admin)
					.contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
					.andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
		}
	}

	@Test
	void rejectsExtraCurrencyAndIdentityFieldsLocally() throws Exception {
		mvc.perform(post("/api/v1/acoes").principal(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ticker\":\"PETR4\",\"nome\":\"Petrobras\",\"tipo\":\"ACAO\",\"mercado\":\"B3\",\"moeda\":\"BRL\"}"))
				.andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
		mvc.perform(patch("/api/v1/acoes/{id}", UUID.randomUUID()).principal(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Novo\",\"ticker\":\"VALE3\"}"))
				.andExpect(status().isBadRequest());
		mvc.perform(patch("/api/v1/acoes/{id}/ativo", UUID.randomUUID()).principal(admin)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsInvalidPaginationSortDirectionEnumsAndUnknownQueryParameters() throws Exception {
		for (String query : List.of("page=-1", "size=0", "size=101", "sort=id", "direction=up", "tipo=CRIPTO", "extra=x")) {
			mvc.perform(get("/api/v1/acoes?" + query).principal(admin)).andExpect(status().isBadRequest())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
		}
	}

	@Test
	void mapsNotFoundAndDuplicateToSanitizedProblemDetails() throws Exception {
		when(useCase.consultarPorTicker(any(), any())).thenThrow(new AtivoNotFoundException());
		mvc.perform(get("/api/v1/acoes/ticker/ZZZZ").principal(user)).andExpect(status().isNotFound())
				.andExpect(content().string(not(containsString("stack"))));
		when(useCase.criar(any(), any(), any(), any())).thenThrow(new DuplicateTickerException());
		mvc.perform(post("/api/v1/acoes").principal(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ticker\":\"AAPL\",\"nome\":\"Apple\",\"tipo\":\"ACAO\",\"mercado\":\"US\"}"))
				.andExpect(status().isConflict()).andExpect(content().string(not(containsString("uk_acoes_ticker"))));
	}

	@Test
	void userCanOnlySubmitTickerAndMarket() throws Exception {
		Ativo ativo = asset("PETR4", "Petrobras");
		when(useCase.registrar(" petr4 ", Mercado.B3)).thenReturn(new com.carteira.carteiraInvestimento.application.service.AtivoRegistrationResult(ativo, "PETR4", true));
		mvc.perform(post("/api/v1/acoes").principal(user).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ticker\":\" petr4 \",\"mercado\":\"B3\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.ticker").value("PETR4"));
		for (String body : List.of(
				"{\"ticker\":\"PETR4\",\"mercado\":\"B3\",\"nome\":\"fake\"}",
				"{\"ticker\":\"PETR4\",\"mercado\":\"B3\",\"logoUrl\":\"https://evil.test\"}")) {
			mvc.perform(post("/api/v1/acoes").principal(user).contentType(MediaType.APPLICATION_JSON).content(body))
					.andExpect(status().isBadRequest());
		}
	}

	@Test
	void userAndAdminUseTheSameProviderBackedRegistrationPath() throws Exception {
		Ativo ativo = asset("PETR4", "Petroleo Brasileiro SA Pfd");
		when(useCase.registrar("PETR4", Mercado.B3)).thenReturn(
				new com.carteira.carteiraInvestimento.application.service.AtivoRegistrationResult(ativo, "PETR4", true));
		for (TestingAuthenticationToken principal : List.of(user, admin)) {
			mvc.perform(post("/api/v1/acoes").principal(principal).contentType(MediaType.APPLICATION_JSON)
					.content("{\"ticker\":\"PETR4\",\"mercado\":\"B3\"}"))
					.andExpect(status().isCreated()).andExpect(jsonPath("$.nome").value("Petroleo Brasileiro SA Pfd"));
		}
		verify(useCase, Mockito.times(2)).registrar("PETR4", Mercado.B3);
	}

	@Test
	void userCanUseB3DiscoveryBeforeRegistration() throws Exception {
		when(useCase.descobrir("PETR", Mercado.B3)).thenReturn(List.of("PETR4", "PETR4F"));
		mvc.perform(get("/api/v1/acoes/discovery?q=PETR&mercado=B3").principal(user))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].ticker").value("PETR4"))
				.andExpect(jsonPath("$[1].ticker").value("PETR4F"));
	}

	private Ativo asset(String ticker, String nome) {
		Mercado mercado = ticker.matches(".*\\d.*") ? Mercado.B3 : Mercado.US;
		return Ativo.novo(ticker, nome, TipoAtivo.ACAO, mercado);
	}
}
