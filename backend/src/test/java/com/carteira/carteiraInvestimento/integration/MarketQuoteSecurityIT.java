package com.carteira.carteiraInvestimento.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class MarketQuoteSecurityIT extends PostgreSqlContainerSupport {
	@Autowired MockMvc mvc;
	@Autowired UsuarioPort users;
	@Autowired AtivoUseCase assets;
	@Autowired JwtEncoder jwtEncoder;
	@Autowired DataSource dataSource;
	private UUID assetId;

	@BeforeEach
	void setup() throws Exception {
		clean();
		assetId = assets.criar("PETR4", "Petrobras", TipoAtivo.ACAO, Mercado.B3).id();
	}

	@AfterEach void tearDown() throws Exception { clean(); }

	@Test
	void enforces401403AndAuthorizedProviderConfigurationMapsTo502() throws Exception {
		Usuario user = user(Role.ROLE_USER); Usuario admin = user(Role.ROLE_ADMIN);
		mvc.perform(get("/api/v1/acoes/{id}/cotacao", assetId)).andExpect(status().isUnauthorized())
				.andExpect(header().exists("X-Correlation-ID"));
		UUID deniedCorrelation = UUID.randomUUID();
		mvc.perform(put("/api/v1/acoes/{id}/atualizar-cotacao", assetId)
				.header(HttpHeaders.AUTHORIZATION, bearer(user)).header("X-Correlation-ID", deniedCorrelation))
				.andExpect(status().isForbidden()).andExpect(header().string("X-Correlation-ID", deniedCorrelation.toString()));
		assertThat(auditCount(deniedCorrelation)).isEqualTo(1);
		mvc.perform(get("/api/v1/acoes/{id}/cotacao", assetId).header(HttpHeaders.AUTHORIZATION, bearer(user)))
				.andExpect(status().isBadGateway());
		mvc.perform(put("/api/v1/acoes/{id}/atualizar-cotacao", assetId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isBadGateway());
	}

	@Test
	void lifecycleHistoryAndInvalidUuidFollowMarketContractWithoutOperationalAudit() throws Exception {
		Usuario user = user(Role.ROLE_USER); Usuario admin = user(Role.ROLE_ADMIN);
		long before = totalAudit();
		mvc.perform(get("/api/v1/acoes/{id}/historico", assetId).header(HttpHeaders.AUTHORIZATION, bearer(user)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
		assets.definirLifecycle(assetId, false);
		mvc.perform(get("/api/v1/acoes/{id}/historico", assetId).header(HttpHeaders.AUTHORIZATION, bearer(user)))
				.andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/acoes/{id}/historico", assetId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isOk());
		mvc.perform(get("/api/v1/acoes/{id}/cotacao", assetId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isNotFound());
		mvc.perform(put("/api/v1/acoes/{id}/atualizar-cotacao", assetId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
				.andExpect(status().isConflict());
		mvc.perform(get("/api/v1/acoes/abc/cotacao").header(HttpHeaders.AUTHORIZATION, bearer(user)))
				.andExpect(status().isBadRequest()).andExpect(header().exists("X-Correlation-ID"));
		assertThat(totalAudit()).isEqualTo(before);
	}

	private Usuario user(Role role) {
		return users.save(Usuario.novoUsuario("Quote Test", "quote-" + UUID.randomUUID() + "@example.test", "hash", role));
	}
	private String bearer(Usuario user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder().subject(user.id().toString()).claim("role", user.role().name())
				.issuedAt(now).expiresAt(now.plusSeconds(3600)).id(UUID.randomUUID().toString())
				.issuer("carteira-investimento-backend").audience(List.of("carteira-investimento-api")).build();
		return "Bearer " + jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
	}
	private long auditCount(UUID correlation) throws Exception { return count("SELECT count(*) FROM logs_auditoria WHERE correlation_id='" + correlation + "'"); }
	private long totalAudit() throws Exception { return count("SELECT count(*) FROM logs_auditoria"); }
	private long count(String sql) throws Exception {
		try (Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement(sql); var r=s.executeQuery()) { r.next(); return r.getLong(1); }
	}
	private void execute(String sql) throws Exception { try (Connection c=dataSource.getConnection(); PreparedStatement s=c.prepareStatement(sql)) { s.executeUpdate(); } }
	private void clean() throws Exception { execute("DELETE FROM historico_cotacoes"); execute("DELETE FROM acoes"); }
}
