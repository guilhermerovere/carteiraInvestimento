package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AccessTokenIssuer;
import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.AuditoriaIsoladaPort;
import com.carteira.carteiraInvestimento.application.port.PasswordHasher;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.EmailCanonicalizer;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class LoginService {
	private final UsuarioPort usuarios;
	private final PasswordHasher passwordHasher;
	private final AuditoriaPort auditoria;
	private final AuditoriaIsoladaPort auditoriaIsolada;
	private final AccessTokenIssuer tokenIssuer;

	public LoginService(UsuarioPort usuarios, PasswordHasher passwordHasher, AuditoriaPort auditoria,
			AuditoriaIsoladaPort auditoriaIsolada, AccessTokenIssuer tokenIssuer) {
		this.usuarios = usuarios;
		this.passwordHasher = passwordHasher;
		this.auditoria = auditoria;
		this.auditoriaIsolada = auditoriaIsolada;
		this.tokenIssuer = tokenIssuer;
	}

	@Transactional
	public AccessTokenIssuer.IssuedAccessToken login(String email, String password, UUID correlationId) {
		Usuario usuario = usuarios.findByEmail(EmailCanonicalizer.canonicalize(email)).orElse(null);
		if (usuario == null || !passwordHasher.matches(password, usuario.senhaHash())) {
			auditoriaIsolada.recordIsoladamente(new AuditoriaCommand(usuario == null ? null : usuario.id(), TipoEvento.LOGIN_FALHO,
					ResultadoAuditoria.FALHA, SeveridadeAuditoria.AVISO, "/api/v1/auth/login", correlationId));
			throw new AuthenticationFailedException();
		}
		if (!usuario.ativo()) {
			auditoriaIsolada.recordIsoladamente(new AuditoriaCommand(usuario.id(), TipoEvento.USUARIO_INATIVO,
					ResultadoAuditoria.NEGADO, SeveridadeAuditoria.ALERTA, "/api/v1/auth/login", correlationId));
			throw new AuthenticationFailedException();
		}
		auditoria.record(new AuditoriaCommand(usuario.id(), TipoEvento.LOGIN_SUCESSO,
				ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO, "/api/v1/auth/login", correlationId));
		return tokenIssuer.issue(usuario);
	}
}
