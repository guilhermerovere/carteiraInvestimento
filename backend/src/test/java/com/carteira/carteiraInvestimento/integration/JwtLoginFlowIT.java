package com.carteira.carteiraInvestimento.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest
class JwtLoginFlowIT extends PostgreSqlContainerSupport {

	@Autowired private MockMvc mvc;
	@Autowired private UsuarioPort usuarios;
	@Autowired private PasswordEncoder passwords;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void persistedBcryptUserLogsInAndUsesReturnedTokenToReadCurrentPrincipal() throws Exception {
		String password = "Valid@123";
		String passwordHash = passwords.encode(password);
		String email = "jwt-login-flow-" + UUID.randomUUID() + "@example.test";
		Usuario usuario = usuarios.save(Usuario.novoUsuario("JWT Login Flow", email, passwordHash, Role.ROLE_USER));

		MvcResult login = mvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"senha\":\"" + password + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").isNumber())
				.andExpect(jsonPath("$.expiresIn").value(org.hamcrest.Matchers.greaterThan(0)))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist())
				.andExpect(jsonPath("$.senha_hash").doesNotExist())
				.andExpect(content().string(not(containsString(password))))
				.andExpect(content().string(not(containsString(passwordHash))))
				.andReturn();

		JsonNode body = objectMapper.readTree(login.getResponse().getContentAsByteArray());
		String token = body.required("accessToken").asText();

		mvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(usuario.id().toString()))
				.andExpect(jsonPath("$.nome").value(usuario.nome()))
				.andExpect(jsonPath("$.email").value(usuario.email()))
				.andExpect(jsonPath("$.role").value("ROLE_USER"))
				.andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist())
				.andExpect(jsonPath("$.senha_hash").doesNotExist())
				.andExpect(content().string(not(containsString(password))))
				.andExpect(content().string(not(containsString(passwordHash))));
	}
}
