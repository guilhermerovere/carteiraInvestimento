package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootTest
class ApplicationFoundationIT extends PostgreSqlContainerSupport {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Flyway flyway;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private UserDetailsService userDetailsService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private MockMvc mockMvc;

	@BeforeEach
	void setUpMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.apply(springSecurity())
				.build();
	}

	@Test
	void startsWithPostgreSqlFlywayAndHibernateValidation() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
			assertThat(connection.getMetaData().getURL()).startsWith("jdbc:postgresql:");
		}
		assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
	}

	@Test
	void exposesOnlySanitizedHealthDetailsWithoutCredentials() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());
	}

	@Test
	void exposesOpenApiAndSwaggerWithoutCredentials() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Carteira Investimento API"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.paths['/api/v1/carteira/transacoes'].post").exists())
				.andExpect(jsonPath("$.paths['/api/v1/carteira/transacoes'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/carteira/transacoes'].put").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/v1/carteira/transacoes'].patch").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/v1/carteira/transacoes'].delete").doesNotExist())
				.andExpect(jsonPath("$.paths['/api/v1/carteira/transacoes/{id}'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/carteira/posicoes'].get").exists())
				.andExpect(jsonPath("$.paths['/api/v1/carteira/posicoes/{ativoId}'].get").exists());

		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void documentsTheExactInvestmentOpenApiContractWithoutValuationOrMutationRoutes() throws Exception {
		String json = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		JsonNode api = objectMapper.readTree(json);
		Set<String> investmentPaths = new HashSet<>();
		api.path("paths").fieldNames().forEachRemaining(path -> {
			if (path.startsWith("/api/v1/carteira/transacoes") || path.startsWith("/api/v1/carteira/posicoes"))
				investmentPaths.add(path);
		});
		assertThat(investmentPaths).containsExactlyInAnyOrder(
				"/api/v1/carteira/transacoes", "/api/v1/carteira/transacoes/{id}",
				"/api/v1/carteira/posicoes", "/api/v1/carteira/posicoes/{ativoId}");
		assertThat(operationNames(api, investmentPaths)).containsExactlyInAnyOrder(
				"POST /api/v1/carteira/transacoes", "GET /api/v1/carteira/transacoes",
				"GET /api/v1/carteira/transacoes/{id}", "GET /api/v1/carteira/posicoes",
				"GET /api/v1/carteira/posicoes/{ativoId}");

		JsonNode post = api.at("/paths/~1api~1v1~1carteira~1transacoes/post");
		assertThat(post.path("security").toString()).contains("bearerAuth");
		assertThat(post.path("parameters").toString()).contains("Idempotency-Key", "required\":true");
		assertThat(fieldNames(schema(api, "InvestmentTransactionRequest"))).containsExactlyInAnyOrder(
				"ativoId", "corretoraId", "tipo", "quantidade", "precoUnitario", "taxas",
				"dataNegociacao", "exchangeRateId");
		assertThat(fieldNames(schema(api, "InvestmentOperationResponse"))).containsExactlyInAnyOrder(
				"transacao", "valorOrigem", "saldoCaixaBrl", "posicao");
		assertThat(fieldNames(schema(api, "TransactionResponse"))).containsExactlyInAnyOrder(
				"id", "ativoId", "ticker", "corretoraId", "exchangeRateId", "tipo", "quantidade",
				"moeda", "precoUnitario", "taxas", "taxaCambioBrl", "valorTotalBrl",
				"resultadoRealizadoBrl", "dataNegociacao", "dataRegistro");
		assertThat(fieldNames(schema(api, "PositionResponse"))).containsExactlyInAnyOrder(
				"id", "ativoId", "ticker", "quantidade", "precoMedioBrl", "totalInvestidoBrl",
				"lucroRealizadoAcumuladoBrl", "ultimaAtualizacao");
		assertThat(fieldNames(schema(api, "TransactionPageResponse"))).containsExactlyInAnyOrder(
				"items", "page", "size", "totalElements", "totalPages");
		assertThat(fieldNames(schema(api, "PositionPageResponse"))).containsExactlyInAnyOrder(
				"items", "page", "size", "totalElements", "totalPages");

		assertThat(fieldNames(post.path("responses")))
				.contains("201", "400", "401", "403", "404", "409", "500")
				.doesNotContain("422", "502");
		String investmentDocumentation = investmentPaths.stream()
				.map(path -> api.path("paths").path(path).toString()).reduce("", String::concat).toLowerCase();
		assertThat(investmentDocumentation).contains("role_user", "b3", "us", "fx", "8 casas",
				"page", "size", "ownership", "zerada", "valuation");
		assertThat(schema(api, "TransactionResponse").toString().toLowerCase()).doesNotContain("valuation");
		assertThat(schema(api, "PositionResponse").toString().toLowerCase()).doesNotContain("valuation");
	}

	@Test
	void usesRepositoryBackedAuthenticationWithoutAGeneratedDefaultUser() {
		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("user"))
				.isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
	}

	private static Set<String> operationNames(JsonNode api, Set<String> paths) {
		Set<String> operations = new HashSet<>();
		for (String path : paths) {
			Iterator<String> methods = api.path("paths").path(path).fieldNames();
			while (methods.hasNext()) {
				String method = methods.next();
				if (Set.of("get", "post", "put", "patch", "delete").contains(method))
					operations.add(method.toUpperCase() + " " + path);
			}
		}
		return operations;
	}

	private static JsonNode schema(JsonNode api, String name) {
		return api.path("components").path("schemas").path(name).path("properties");
	}

	private static Set<String> fieldNames(JsonNode properties) {
		Set<String> fields = new HashSet<>();
		properties.fieldNames().forEachRemaining(fields::add);
		return fields;
	}
}

