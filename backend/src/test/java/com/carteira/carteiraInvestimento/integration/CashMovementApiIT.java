package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest
class CashMovementApiIT extends PostgreSqlContainerSupport {
	@Autowired private MockMvc mvc;
	@Autowired private JdbcTemplate jdbc;
	@Autowired private JwtEncoder jwtEncoder;

	@BeforeEach
	void cleanCashData() {
		jdbc.update("DELETE FROM movimentacoes_caixa_idempotencia");
		jdbc.update("DELETE FROM carteira_snapshots");
		jdbc.update("DELETE FROM movimentacoes_caixa");
		jdbc.update("DELETE FROM logs_auditoria WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'cash-api-%')");
		jdbc.update("DELETE FROM carteiras WHERE usuario_id IN (SELECT id FROM usuarios WHERE email LIKE 'cash-api-%')");
		jdbc.update("DELETE FROM usuarios WHERE email LIKE 'cash-api-%'");
	}

	@Test
	void allCashEndpointsRejectAnonymousAndAdmin() throws Exception {
		User admin = user("ROLE_ADMIN", false, BigDecimal.ZERO);
		mvc.perform(get("/api/v1/carteira/caixa")).andExpect(status().isUnauthorized())
				.andExpect(header().exists("X-Correlation-ID"));
		mvc.perform(get("/api/v1/carteira/caixa/movimentacoes")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/v1/carteira/caixa/deposito").contentType(MediaType.APPLICATION_JSON)
				.content("{\"valor\":1}")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/v1/carteira/caixa/saque").contentType(MediaType.APPLICATION_JSON)
				.content("{\"valor\":1}")).andExpect(status().isUnauthorized());

		String token = bearer(admin);
		mvc.perform(get("/api/v1/carteira/caixa").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isForbidden()).andExpect(header().exists("X-Correlation-ID"));
		mvc.perform(get("/api/v1/carteira/caixa/movimentacoes").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isForbidden());
		mvc.perform(post("/api/v1/carteira/caixa/deposito").header(HttpHeaders.AUTHORIZATION, token)
				.header("Idempotency-Key", "admin-1").contentType(MediaType.APPLICATION_JSON).content("{\"valor\":1}"))
				.andExpect(status().isForbidden());
		mvc.perform(post("/api/v1/carteira/caixa/saque").header(HttpHeaders.AUTHORIZATION, token)
				.header("Idempotency-Key", "admin-2").contentType(MediaType.APPLICATION_JSON).content("{\"valor\":1}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void postContractsAreStrictValidatedPrivateAndIdempotent() throws Exception {
		User user = user("ROLE_USER", true, BigDecimal.ZERO);
		String token = bearer(user);
		String endpoint = "/api/v1/carteira/caixa/deposito";
		String response = mvc.perform(post(endpoint).header(HttpHeaders.AUTHORIZATION, token)
				.header("Idempotency-Key", "deposit-1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"valor\":100.00,\"descricao\":\"  salario  \"}"))
				.andExpect(status().isCreated()).andExpect(header().exists("X-Correlation-ID"))
				.andExpect(jsonPath("$.movimentacao.tipo").value("DEPOSITO"))
				.andExpect(jsonPath("$.movimentacao.valorBrl").value(100.00))
				.andExpect(jsonPath("$.movimentacao.descricao").value("salario"))
				.andExpect(jsonPath("$.saldoResultante").value(100.00))
				.andExpect(jsonPath("$.movimentacao.carteiraId").doesNotExist())
				.andExpect(jsonPath("$.movimentacao.usuarioId").doesNotExist())
				.andExpect(jsonPath("$.key").doesNotExist()).andReturn().getResponse().getContentAsString();
		String replayWithoutScale = mvc.perform(post(endpoint).header(HttpHeaders.AUTHORIZATION, token)
				.header("Idempotency-Key", "deposit-1")
				.contentType(MediaType.APPLICATION_JSON).content("{\"valor\":100,\"descricao\":\"salario\"}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		String replayWithOneDecimal = mvc.perform(post(endpoint).header(HttpHeaders.AUTHORIZATION, token)
				.header("Idempotency-Key", "deposit-1")
				.contentType(MediaType.APPLICATION_JSON).content("{\"valor\":100.0,\"descricao\":\"salario\"}"))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		assertThat(replayWithoutScale).isEqualTo(response);
		assertThat(replayWithOneDecimal).isEqualTo(response);
		assertThat(jdbc.queryForObject("SELECT estado_versao FROM carteiras WHERE id=?",Long.class,user.walletId()))
				.isEqualTo(1);
		assertThat(response).contains("\"valorBrl\":100.00", "\"saldoResultante\":100.00");

		for (String body : List.of("{\"valor\":0}", "{\"valor\":-1}", "{\"valor\":1.001}",
				"{\"valor\":10000000000000000.00}", "{\"valor\":1,\"descricao\":\"" + "x".repeat(161) + "\"}",
				"{\"valor\":1,\"carteiraId\":\"" + UUID.randomUUID() + "\"}")) {
			mvc.perform(post(endpoint).header(HttpHeaders.AUTHORIZATION, token).header("Idempotency-Key", "bad-" + UUID.randomUUID())
					.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
		}
		mvc.perform(post(endpoint).header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON)
				.content("{\"valor\":1}")).andExpect(status().isBadRequest());
		for (String key : List.of("", " leading", "trailing ", "a".repeat(129), "invalid/key")) {
			mvc.perform(post(endpoint).header(HttpHeaders.AUTHORIZATION, token).header("Idempotency-Key", key)
					.contentType(MediaType.APPLICATION_JSON).content("{\"valor\":1}"))
					.andExpect(status().isBadRequest())
					.andExpect(content().string(not(containsString("invalid/key"))));
		}
	}

	@Test
	void balanceAndHistoryAreOwnedPaginatedAndSanitized() throws Exception {
		User first = user("ROLE_USER", true, new BigDecimal("25.00"));
		User second = user("ROLE_USER", true, new BigDecimal("90.00"));
		mvc.perform(get("/api/v1/carteira/caixa").header(HttpHeaders.AUTHORIZATION, bearer(first)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.saldoCaixaBrl").value(25.00))
				.andExpect(jsonPath("$.carteiraId").doesNotExist());
		mvc.perform(get("/api/v1/carteira/caixa").header(HttpHeaders.AUTHORIZATION, bearer(second)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.saldoCaixaBrl").value(90.00));
		mvc.perform(get("/api/v1/carteira/caixa/movimentacoes").header(HttpHeaders.AUTHORIZATION, bearer(first)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty())
				.andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(0)).andExpect(jsonPath("$.totalPages").value(0));
		for (String query : List.of("?page=-1", "?size=0", "?size=101", "?tipo=SAQUE")) {
			mvc.perform(get("/api/v1/carteira/caixa/movimentacoes" + query)
					.header(HttpHeaders.AUTHORIZATION, bearer(first))).andExpect(status().isBadRequest());
		}
	}

	@Test
	void validUserWithoutWalletReceivesSanitizedInternalError() throws Exception {
		User user = user("ROLE_USER", false, BigDecimal.ZERO);
		UUID correlation = UUID.randomUUID();
		mvc.perform(get("/api/v1/carteira/caixa").header(HttpHeaders.AUTHORIZATION, bearer(user))
				.header("X-Correlation-ID", correlation.toString())).andExpect(status().isInternalServerError())
				.andExpect(header().string("X-Correlation-ID", correlation.toString()))
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
				.andExpect(content().string(not(containsString(user.id().toString()))))
				.andExpect(content().string(not(containsString("carteiras"))));
	}

	@Test
	void cashStrictJsonDoesNotChangeRegistrationJsonBehavior() throws Exception {
		String email = "cash-api-global-" + UUID.randomUUID() + "@example.test";
		mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Global Contract\",\"email\":\"" + email
						+ "\",\"senha\":\"Valid@123\",\"campoExtra\":true}"))
				.andExpect(status().isCreated());
	}

	@Test
	void concurrentHttpCallsExposeTheRequiredCreatedAndConflictStatuses() throws Exception {
		User depositUser = user("ROLE_USER", true, BigDecimal.ZERO);
		String depositToken = bearer(depositUser);
		HttpResults deposits = concurrentHttp(
				() -> postCash("/api/v1/carteira/caixa/deposito", depositToken, "http-same", "100"),
				() -> postCash("/api/v1/carteira/caixa/deposito", depositToken, "http-same", "100.00"));
		assertThat(deposits.first().getResponse().getStatus()).isEqualTo(201);
		assertThat(deposits.second().getResponse().getStatus()).isEqualTo(201);
		assertThat(deposits.first().getResponse().getContentAsString())
				.isEqualTo(deposits.second().getResponse().getContentAsString());
		assertThat(jdbc.queryForObject("SELECT count(*) FROM movimentacoes_caixa WHERE carteira_id=?", Long.class,
				depositUser.walletId())).isEqualTo(1);

		User withdrawalUser = user("ROLE_USER", true, new BigDecimal("100.00"));
		String withdrawalToken = bearer(withdrawalUser);
		HttpResults withdrawals = concurrentHttp(
				() -> postCash("/api/v1/carteira/caixa/saque", withdrawalToken, "http-withdraw-a", "80"),
				() -> postCash("/api/v1/carteira/caixa/saque", withdrawalToken, "http-withdraw-b", "80"));
		assertThat(List.of(withdrawals.first().getResponse().getStatus(), withdrawals.second().getResponse().getStatus()))
				.containsExactlyInAnyOrder(201, 409);
		assertThat(jdbc.queryForObject("SELECT saldo_caixa_brl FROM carteiras WHERE id=?", BigDecimal.class,
				withdrawalUser.walletId())).isEqualByComparingTo("20.00");
	}

	private MvcResult postCash(String endpoint, String token, String key, String amount) throws Exception {
		return mvc.perform(post(endpoint).header(HttpHeaders.AUTHORIZATION, token).header("Idempotency-Key", key)
				.contentType(MediaType.APPLICATION_JSON).content("{\"valor\":" + amount + "}")).andReturn();
	}

	private HttpResults concurrentHttp(HttpCall first, HttpCall second) throws Exception {
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			var a = executor.submit(() -> { ready.countDown(); start.await(); return first.execute(); });
			var b = executor.submit(() -> { ready.countDown(); start.await(); return second.execute(); });
			ready.await(); start.countDown();
			return new HttpResults(a.get(), b.get());
		}
	}

	private User user(String role, boolean wallet, BigDecimal balance) {
		UUID id = UUID.randomUUID();
		UUID walletId = wallet ? UUID.randomUUID() : null;
		jdbc.update("INSERT INTO usuarios (id,nome,email,senha_hash,role,ativo) VALUES (?,?,?,?,?,true)", id,
				"Cash API", "cash-api-" + id + "@example.test", "hash", role);
		if (wallet) {
			jdbc.update("INSERT INTO carteiras (id,usuario_id,nome,saldo_caixa_brl) VALUES (?,?,?,?)", walletId,
					id, "Carteira Principal", balance);
		}
		return new User(id, role, walletId);
	}

	private String bearer(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder().subject(user.id().toString()).claim("role", user.role())
				.issuedAt(now).expiresAt(now.plusSeconds(3600)).id(UUID.randomUUID().toString())
				.issuer("carteira-investimento-backend").audience(List.of("carteira-investimento-api")).build();
		return "Bearer " + jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
	}

	private record User(UUID id, String role, UUID walletId) { }
	private record HttpResults(MvcResult first, MvcResult second) { }
	@FunctionalInterface private interface HttpCall { MvcResult execute() throws Exception; }
}
