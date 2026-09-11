package com.carteira.carteiraInvestimento.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.application.service.CambioUseCase;
import com.carteira.carteiraInvestimento.domain.fx.CambioProvider;
import com.carteira.carteiraInvestimento.domain.fx.MoedaCambio;
import com.carteira.carteiraInvestimento.domain.fx.ObservacaoCambio;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
@Import(CambioSecurityIT.FixedCambio.class)
class CambioSecurityIT extends PostgreSqlContainerSupport {
	@Autowired MockMvc mvc; @Autowired UsuarioPort users; @Autowired JwtEncoder jwtEncoder;
	@Test void anonymousIs401AndUserAndAdminAre200() throws Exception {
		mvc.perform(get("/api/v1/cambio/usd-brl")).andExpect(status().isUnauthorized())
				.andExpect(header().exists(CorrelationIdFilter.HEADER)).andExpect(jsonPath("$.status").value(401));
		for(Role role:List.of(Role.ROLE_USER,Role.ROLE_ADMIN)){
			Usuario user=users.save(Usuario.novoUsuario("FX User","fx-"+UUID.randomUUID()+"@example.test","hash",role));
			mvc.perform(get("/api/v1/cambio/usd-brl").header(HttpHeaders.AUTHORIZATION,bearer(user)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.moedaOrigem").value("USD"))
					.andExpect(jsonPath("$.moedaDestino").value("BRL"));
		}
	}
	private String bearer(Usuario user){Instant now=Instant.now();JwtClaimsSet claims=JwtClaimsSet.builder().subject(user.id().toString()).claim("role",user.role().name()).issuedAt(now).expiresAt(now.plusSeconds(3600)).id(UUID.randomUUID().toString()).issuer("carteira-investimento-backend").audience(List.of("carteira-investimento-api")).build();return "Bearer "+jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();}
	@TestConfiguration(proxyBeanMethods=false) static class FixedCambio {
		@Bean @Primary CambioUseCase fixedCambioUseCase(){return ()->new ObservacaoCambio(UUID.fromString("00000000-0000-0000-0000-000000000008"),MoedaCambio.USD,MoedaCambio.BRL,new BigDecimal("5.12345678"),CambioProvider.ALPHA_VANTAGE,Instant.parse("2026-09-10T11:59:00Z"),Instant.parse("2026-09-10T12:00:00Z"));}
	}
}
