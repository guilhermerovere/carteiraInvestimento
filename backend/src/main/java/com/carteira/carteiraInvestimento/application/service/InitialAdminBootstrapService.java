package com.carteira.carteiraInvestimento.application.service;

import com.carteira.carteiraInvestimento.application.port.AuditoriaPort;
import com.carteira.carteiraInvestimento.application.port.PasswordHasher;
import com.carteira.carteiraInvestimento.application.port.UsuarioPort;
import com.carteira.carteiraInvestimento.domain.identity.EmailCanonicalizer;
import com.carteira.carteiraInvestimento.domain.identity.PasswordPolicy;
import com.carteira.carteiraInvestimento.domain.identity.Role;
import com.carteira.carteiraInvestimento.domain.identity.Usuario;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class InitialAdminBootstrapService {
	private final UsuarioPort usuarios;
	private final PasswordHasher passwordHasher;
	private final AuditoriaPort auditoria;

	public InitialAdminBootstrapService(UsuarioPort usuarios, PasswordHasher passwordHasher, AuditoriaPort auditoria) {
		this.usuarios = usuarios;
		this.passwordHasher = passwordHasher;
		this.auditoria = auditoria;
	}

	@Transactional
	public Usuario provision(String name, String email, String password) {
		String canonical = EmailCanonicalizer.canonicalize(email);
		PasswordPolicy.validate(password);
		Usuario existing = usuarios.findByEmail(canonical).orElse(null);
		if (existing != null) {
			if (existing.role() != Role.ROLE_ADMIN) {
				throw new IllegalStateException("configured admin email belongs to a user");
			}
			return existing;
		}
		Usuario admin = Usuario.novoUsuario(name, canonical, passwordHasher.hash(password), Role.ROLE_ADMIN);
		usuarios.save(admin);
		auditoria.record(new AuditoriaCommand(admin.id(), TipoEvento.ADMIN_INICIAL_CRIADO,
				ResultadoAuditoria.SUCESSO, SeveridadeAuditoria.INFO, null, UUID.randomUUID()));
		return admin;
	}
}
