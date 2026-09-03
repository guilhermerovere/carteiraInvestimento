package com.carteira.carteiraInvestimento.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

class SecurityProblemDetailHandlerTest {
	private final SecurityProblemDetailHandler handler = new SecurityProblemDetailHandler(new ProblemDetailFactory());

	@Test
	void writesTheSanitizedProblemDetailContractForAuthenticationAndAuthorizationFailures() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/protected");
		MockHttpServletResponse unauthorized = new MockHttpServletResponse();
		MockHttpServletResponse forbidden = new MockHttpServletResponse();

		handler.commence(request, unauthorized, new AuthenticationCredentialsNotFoundException("secret"));
		handler.handle(request, forbidden, new AccessDeniedException("secret"));

		assertThat(unauthorized.getStatus()).isEqualTo(401);
		assertThat(unauthorized.getContentType()).isEqualTo("application/problem+json");
		assertThat(unauthorized.getContentAsString()).contains("Unauthorized").doesNotContain("secret");
		assertThat(forbidden.getStatus()).isEqualTo(403);
		assertThat(forbidden.getContentType()).isEqualTo("application/problem+json");
		assertThat(forbidden.getContentAsString()).contains("Forbidden").doesNotContain("secret");
	}
}
