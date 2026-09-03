package com.carteira.carteiraInvestimento.presentation.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.service.DuplicateEmailException;
import com.carteira.carteiraInvestimento.application.service.RegistrationService;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import com.carteira.carteiraInvestimento.presentation.error.GlobalExceptionHandler;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RegistrationControllerTest {

	private RegistrationService registration;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		registration = Mockito.mock(RegistrationService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new RegistrationController(registration))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void createsAUserResponseWithoutPasswordHashOrToken() throws Exception {
		UUID id = UUID.randomUUID();
		when(registration.register(any(), any(), any(), any())).thenReturn(new Usuario(id, "Ana", "ana@example.test", "hash",
				Role.ROLE_USER, true, Instant.now(), Instant.now()));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.requestAttr(CorrelationIdFilter.ATTRIBUTE, UUID.randomUUID())
				.content("{\"nome\":\"Ana\",\"email\":\"ana@example.test\",\"senha\":\"Valid@123\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(id.toString()))
			.andExpect(jsonPath("$.nome").value("Ana"))
			.andExpect(jsonPath("$.email").value("ana@example.test"))
			.andExpect(jsonPath("$.role").value("ROLE_USER"))
			.andExpect(jsonPath("$.ativo").value(true))
			.andExpect(jsonPath("$.senha").doesNotExist())
			.andExpect(jsonPath("$.senhaHash").doesNotExist())
			.andExpect(jsonPath("$.accessToken").doesNotExist());
	}

	@Test
	void rejectsInvalidRegistrationInputWithBadRequest() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.requestAttr(CorrelationIdFilter.ATTRIBUTE, UUID.randomUUID())
				.content("{\"nome\":\"Ana\",\"email\":\"invalid\",\"senha\":\"weak\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(content().string(not(containsString("weak"))));
	}

	@Test
	void rejectsDuplicateCanonicalEmailWithConflict() throws Exception {
		when(registration.register(any(), any(), any(), any())).thenThrow(new DuplicateEmailException());

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.requestAttr(CorrelationIdFilter.ATTRIBUTE, UUID.randomUUID())
				.content("{\"nome\":\"Ana\",\"email\":\"ana@example.test\",\"senha\":\"Valid@123\"}"))
			.andExpect(status().isConflict())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
	}
}
