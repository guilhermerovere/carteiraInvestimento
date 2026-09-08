package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.application.service.AtivoUseCase;
import com.carteira.carteiraInvestimento.domain.asset.Mercado;
import com.carteira.carteiraInvestimento.domain.asset.TipoAtivo;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class AssetCatalogSecurityIT extends PostgreSqlContainerSupport {
	@Autowired private MockMvc mvc;
	@Autowired private UsuarioPort usuarios;
	@Autowired private AtivoUseCase ativos;
	@Autowired private JwtEncoder jwtEncoder;
	@Autowired private DataSource dataSource;

	@BeforeEach
	void cleanCatalog() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			try (PreparedStatement history = connection.prepareStatement("DELETE FROM historico_cotacoes")) { history.executeUpdate(); }
			try (PreparedStatement statement = connection.prepareStatement("DELETE FROM acoes")) { statement.executeUpdate(); }
		}
	}

	@Test
	void anonymousIsUnauthorizedAndUserCanReadButCannotMutateWithAuditedForbidden() throws Exception {
		Usuario user = user(Role.ROLE_USER);
		String bearer = bearer(user);
		UUID correlationId = UUID.randomUUID();
		mvc.perform(get("/api/v1/acoes")).andExpect(status().isUnauthorized()).andExpect(header().exists("X-Correlation-ID"));
		mvc.perform(get("/api/v1/acoes").header(HttpHeaders.AUTHORIZATION, bearer)).andExpect(status().isOk());
		mvc.perform(post("/api/v1/acoes").header(HttpHeaders.AUTHORIZATION, bearer)
				.header("X-Correlation-ID", correlationId).contentType(MediaType.APPLICATION_JSON)
				.content("{\"ticker\":\"PETR4\",\"nome\":\"Petrobras\",\"tipo\":\"ACAO\",\"mercado\":\"B3\"}"))
				.andExpect(status().isForbidden()).andExpect(header().string("X-Correlation-ID", correlationId.toString()));
		assertThat(auditCount(user.id(), correlationId)).isEqualTo(1);
		UUID patchCorrelationId = UUID.randomUUID();
		mvc.perform(patch("/api/v1/acoes/{id}", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, bearer)
				.header("X-Correlation-ID", patchCorrelationId).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Negado\"}"))
				.andExpect(status().isForbidden());
		assertThat(auditCount(user.id(), patchCorrelationId)).isEqualTo(1);
	}

	@Test
	void userNeverSeesInactiveWhileAdminReadsAndMutatesCompleteCatalog() throws Exception {
		Usuario user = user(Role.ROLE_USER);
		Usuario admin = user(Role.ROLE_ADMIN);
		var active = ativos.criar("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3);
		var inactive = ativos.criar("AAPL", "Apple", TipoAtivo.ACAO, Mercado.US);
		ativos.definirLifecycle(inactive.id(), false);

		mvc.perform(get("/api/v1/acoes?ativo=false").header(HttpHeaders.AUTHORIZATION, bearer(user)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.items[0].ticker").value("PETR4"));
		mvc.perform(get("/api/v1/acoes/ticker/AAPL").header(HttpHeaders.AUTHORIZATION, bearer(user)))
				.andExpect(status().isNotFound()).andExpect(header().exists("X-Correlation-ID"));
		mvc.perform(get("/api/v1/acoes").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
		mvc.perform(get("/api/v1/acoes").param("q", " pPl ").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.items[0].ticker").value("AAPL"));
		mvc.perform(get("/api/v1/acoes").param("q", "   ").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
		mvc.perform(get("/api/v1/acoes?ativo=false").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.items[0].ticker").value("AAPL"));
		mvc.perform(patch("/api/v1/acoes/{id}", active.id()).header(HttpHeaders.AUTHORIZATION, bearer(admin))
				.contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Petrobras PN\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Petrobras PN"));
	}

	private Usuario user(Role role) {
		return usuarios.save(Usuario.novoUsuario("Catalog Test", "catalog-" + UUID.randomUUID() + "@example.test", "hash", role));
	}

	private String bearer(Usuario user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder().subject(user.id().toString()).claim("role", user.role().name())
				.issuedAt(now).expiresAt(now.plusSeconds(3600)).id(UUID.randomUUID().toString())
				.issuer("carteira-investimento-backend").audience(List.of("carteira-investimento-api")).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
		return "Bearer " + token;
	}

	private long auditCount(UUID userId, UUID correlationId) throws Exception {
		try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
				"SELECT count(*) FROM logs_auditoria WHERE usuario_id=? AND correlation_id=? AND tipo_evento='ACESSO_NEGADO'")) {
			statement.setObject(1, userId);
			statement.setObject(2, correlationId);
			try (var result = statement.executeQuery()) {
				result.next();
				return result.getLong(1);
			}
		}
	}
}
