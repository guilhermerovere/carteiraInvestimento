package com.carteira.carteiraInvestimento.presentation.auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer.IssuedAccessToken;
import com.carteira.carteiraInvestimento.application.service.AuthenticationFailedException;
import com.carteira.carteiraInvestimento.application.service.LoginService;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import com.carteira.carteiraInvestimento.presentation.error.GlobalExceptionHandler;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LoginControllerTest {
	private LoginService login;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		login = Mockito.mock(LoginService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new LoginController(login))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsOnlyTheBearerAccessTokenContract() throws Exception {
		when(login.login(any(), any(), any())).thenReturn(new IssuedAccessToken("jwt-sentinel", 3600));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.requestAttr(CorrelationIdFilter.ATTRIBUTE, UUID.randomUUID())
				.content("{\"email\":\"user@example.test\",\"senha\":\"Valid@123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("jwt-sentinel"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(3600))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist())
				.andExpect(jsonPath("$.refreshToken").doesNotExist());
	}

	@Test
	void returnsSanitizedUnauthorizedForInvalidCredentials() throws Exception {
		when(login.login(any(), any(), any())).thenThrow(new AuthenticationFailedException());

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.requestAttr(CorrelationIdFilter.ATTRIBUTE, UUID.randomUUID())
				.content("{\"email\":\"user@example.test\",\"senha\":\"wrong\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(content().string(not(containsString("wrong"))));
	}
}
