package com.carteira.carteiraInvestimento.infrastructure.security;

import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.service.AuditoriaCommand;
import com.carteira.carteiraInvestimento.application.service.ResultadoAuditoria;
import com.carteira.carteiraInvestimento.application.service.SeveridadeAuditoria;
import com.carteira.carteiraInvestimento.application.service.TipoEvento;
import com.carteira.carteiraInvestimento.infrastructure.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AccessDeniedAuditingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessDeniedAuditingService.class);

	private final AuditoriaIsoladaPort auditoria;

	public AccessDeniedAuditingService(AuditoriaIsoladaPort auditoria) {
		this.auditoria = auditoria;
	}

	public void record(HttpServletRequest request) {
		try {
			var authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication == null || !authentication.isAuthenticated()
					|| authentication instanceof AnonymousAuthenticationToken) {
				return;
			}
			UUID usuarioId = UUID.fromString(authentication.getName());
			auditoria.recordIsoladamente(new AuditoriaCommand(usuarioId, TipoEvento.ACESSO_NEGADO,
					ResultadoAuditoria.NEGADO, SeveridadeAuditoria.ALERTA, request.getRequestURI(),
					CorrelationIdFilter.correlationId(request)));
		} catch (RuntimeException auditFailure) {
			LOGGER.warn("Unable to persist access-denied audit event");
		}
	}
}
