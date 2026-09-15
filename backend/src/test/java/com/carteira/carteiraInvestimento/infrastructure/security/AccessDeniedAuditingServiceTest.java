package com.carteira.carteiraInvestimento.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class AccessDeniedAuditingServiceTest {

	private final AtomicReference<AuditoriaCommand> recorded = new AtomicReference<>();
	private final AccessDeniedAuditingService service = new AccessDeniedAuditingService(recorded::set);

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void recordsAuthenticatedPrincipalWithRequestMetadata() {
		UUID usuarioId = UUID.randomUUID();
		UUID correlationId = UUID.randomUUID();
		MockHttpServletRequest request = request(correlationId);
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuarioId.toString(), null, "ROLE_USER"));

		service.record(request);

		assertThat(recorded.get()).isEqualTo(new AuditoriaCommand(usuarioId, TipoEvento.ACESSO_NEGADO,
				ResultadoAuditoria.NEGADO, SeveridadeAuditoria.ALERTA, "/protected", correlationId));
	}

	@Test
	void ignoresMissingAndAnonymousPrincipals() {
		service.record(request(UUID.randomUUID()));
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("key", "anonymous",
				AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

		service.record(request(UUID.randomUUID()));

		assertThat(recorded.get()).isNull();
	}

	@Test
	void containsAuditFailure() {
		UUID usuarioId = UUID.randomUUID();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuarioId.toString(), null, "ROLE_USER"));
		AccessDeniedAuditingService failingService = new AccessDeniedAuditingService(
				event -> { throw new IllegalStateException("database PASSWORD_SENTINEL"); });

		assertThatCode(() -> failingService.record(request(UUID.randomUUID()))).doesNotThrowAnyException();
	}

	private MockHttpServletRequest request(UUID correlationId) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/protected");
		request.setAttribute(CorrelationIdFilter.ATTRIBUTE, correlationId);
		return request;
	}
}
