package com.carteira.carteiraInvestimento.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import com.carteira.carteiraInvestimento.presentation.error.ProblemDetailFactory;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityProblemDetailHandlerTest {
	private final AtomicReference<AuditoriaCommand> recorded = new AtomicReference<>();
	private final SecurityProblemDetailHandler handler = new SecurityProblemDetailHandler(new ProblemDetailFactory(),
			new AccessDeniedAuditingService(recorded::set));

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

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

	@Test
	void recordsAuthenticatedAccessDeniedWithRequestMetadata() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();
		MockHttpServletRequest request = request(correlationId);
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuarioId.toString(), null, "ROLE_USER"));

		handler.handle(request, new MockHttpServletResponse(), new AccessDeniedException("secret"));

		assertThat(recorded.get()).isEqualTo(new AuditoriaCommand(usuarioId, TipoEvento.ACESSO_NEGADO,
				ResultadoAuditoria.NEGADO, SeveridadeAuditoria.ALERTA, "/protected", correlationId));
	}

	@Test
	void keepsTheSanitizedForbiddenResponseWhenAuditPersistenceFails() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		MockHttpServletRequest request = request(UUID.randomUUID());
		MockHttpServletResponse response = new MockHttpServletResponse();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuarioId.toString(), null, "ROLE_USER"));
		SecurityProblemDetailHandler failingHandler = new SecurityProblemDetailHandler(new ProblemDetailFactory(),
				new AccessDeniedAuditingService(event -> { throw new IllegalStateException("database PASSWORD_SENTINEL"); }));

		failingHandler.handle(request, response, new AccessDeniedException("secret"));

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentType()).isEqualTo("application/problem+json");
		assertThat(response.getContentAsString()).contains("Forbidden", "Access is denied.")
				.doesNotContain("secret", "PASSWORD_SENTINEL", "database");
	}

	private MockHttpServletRequest request(UUID correlationId) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/protected");
		request.setAttribute(CorrelationIdFilter.ATTRIBUTE, correlationId);
		return request;
	}
}
